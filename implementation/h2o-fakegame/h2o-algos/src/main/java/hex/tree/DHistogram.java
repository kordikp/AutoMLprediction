package hex.tree;

import com.google.common.util.concurrent.AtomicDouble;
import sun.misc.Unsafe;
import water.*;
import water.fvec.Frame;
import water.fvec.Vec;
import water.nbhm.UtilUnsafe;
import water.util.*;

import java.util.Arrays;
import java.util.Random;

/** A Histogram, computed in parallel over a Vec.
 *
 *  <p>A {@code DHistogram} bins every value added to it, and computes a the
 *  vec min and max (for use in the next split), and response mean and variance
 *  for each bin.  {@code DHistogram}s are initialized with a min, max and
 *  number-of- elements to be added (all of which are generally available from
 *  a Vec).  Bins run from min to max in uniform sizes.  If the {@code
 *  DHistogram} can determine that fewer bins are needed (e.g. boolean columns
 *  run from 0 to 1, but only ever take on 2 values, so only 2 bins are
 *  needed), then fewer bins are used.
 *  
 *  <p>{@code DHistogram} are shared per-node, and atomically updated.  There's
 *  an {@code add} call to help cross-node reductions.  The data is stored in
 *  primitive arrays, so it can be sent over the wire.
 *  
 *  <p>If we are successively splitting rows (e.g. in a decision tree), then a
 *  fresh {@code DHistogram} for each split will dynamically re-bin the data.
 *  Each successive split will logarithmically divide the data.  At the first
 *  split, outliers will end up in their own bins - but perhaps some central
 *  bins may be very full.  At the next split(s) - if they happen at all -
 *  the full bins will get split, and again until (with a log number of splits)
 *  each bin holds roughly the same amount of data.  This 'UniformAdaptive' binning
 *  resolves a lot of problems with picking the proper bin count or limits -
 *  generally a few more tree levels will equal any fancy but fixed-size binning strategy.
 *
 *  <p>Support for histogram split points based on quantiles (or random points) is
 *  available as well, via {@code _histoType}.
 *
*/
public final class DHistogram extends Iced {
  public final transient String _name; // Column name (for debugging)
  public final double _minSplitImprovement;
  public final byte  _isInt;    // 0: float col, 1: int col, 2: categorical & int col
  public char  _nbin;     // Bin count (excluding NA bucket)
  public double _step;     // Linear interpolation step per bin
  public final double _min, _maxEx; // Conservative Min/Max over whole collection.  _maxEx is Exclusive.
  public double _w[];           // weighted count of observations per bin, shared, atomically incremented
  private double _wY[], _wYY[]; // weighted response per bin and weighted squared response per bin, shared, atomically incremented
  private AtomicDouble _wNA, _wYNA, _wYYNA; // same for missing observations

  // Atomically updated double min/max
  protected    double  _min2, _maxIn; // Min/Max, shared, atomically updated.  _maxIn is Inclusive.
  private static final Unsafe _unsafe = UtilUnsafe.getUnsafe();
  static private final long _min2Offset;
  static private final long _max2Offset;
  static {
    try {
      _min2Offset = _unsafe.objectFieldOffset(DHistogram.class.getDeclaredField("_min2"));
      _max2Offset = _unsafe.objectFieldOffset(DHistogram.class.getDeclaredField("_maxIn"));
    } catch( Exception e ) {
      throw H2O.fail();
    }
  }

  public SharedTreeModel.SharedTreeParameters.HistogramType _histoType; //whether ot use random split points
  public transient double _splitPts[]; // split points between _min and _maxEx (either random or based on quantiles)
  public final long _seed;
  public transient boolean _hasQuantiles;
  public Key _globalQuantilesKey; //key under which original top-level quantiles are stored;

  // split direction for missing values
  public enum NASplitDir {
    //never saw NAs in training
    None,     //initial state - should not be present in a trained model

    // saw NAs in training
    NAvsREST, //split off non-NA (left) vs NA (right)
    NALeft,   //NA goes left
    NARight,  //NA goes right

    // never NAs in training, but have a way to deal with them in scoring
    Left,     //test time NA should go left
    Right,    //test time NA should go right
  }

  static class HistoQuantiles extends Keyed<HistoQuantiles> {
    public HistoQuantiles(Key<HistoQuantiles> key, double[] splitPts) {
      super(key);
      this.splitPts = splitPts;
    }
    double[/*nbins*/] splitPts;
  }


  public static int[] activeColumns(DHistogram[] hist) {
    int[] cols = new int[hist.length];
    int len=0;
    for( int i=0; i<hist.length; i++ ) {
      if (hist[i]==null) continue;
      assert hist[i]._min < hist[i]._maxEx && hist[i].nbins() > 1 : "broken histo range "+ hist[i];
      cols[len++] = i;        // Gather active column
    }
//    cols = Arrays.copyOfRange(cols, len, hist.length);
    return cols;
  }

  public void setMin( double min ) {
    long imin = Double.doubleToRawLongBits(min);
    double old = _min2;
    while( min < old && !_unsafe.compareAndSwapLong(this, _min2Offset, Double.doubleToRawLongBits(old), imin ) )
      old = _min2;
  }
  // Find Inclusive _max2
  public void setMaxIn( double max ) {
    long imax = Double.doubleToRawLongBits(max);
    double old = _maxIn;
    while( max > old && !_unsafe.compareAndSwapLong(this, _max2Offset, Double.doubleToRawLongBits(old), imax ) )
      old = _maxIn;
  }

  public DHistogram(String name, final int nbins, int nbins_cats, byte isInt, double min, double maxEx,
                    double minSplitImprovement, SharedTreeModel.SharedTreeParameters.HistogramType histogramType, long seed, Key globalQuantilesKey) {
    assert nbins > 1;
    assert nbins_cats > 1;
    assert maxEx > min : "Caller ensures "+maxEx+">"+min+", since if max==min== the column "+name+" is all constants";
    _isInt = isInt;
    _name = name;
    _min=min;
    _maxEx=maxEx;               // Set Exclusive max
    _min2 = Double.MAX_VALUE;   // Set min/max to outer bounds
    _maxIn= -Double.MAX_VALUE;
    _minSplitImprovement = minSplitImprovement;
    _histoType = histogramType;
    _seed = seed;
    while (_histoType == SharedTreeModel.SharedTreeParameters.HistogramType.RoundRobin) {
      SharedTreeModel.SharedTreeParameters.HistogramType[] h = SharedTreeModel.SharedTreeParameters.HistogramType.values();
      _histoType = h[(int)Math.abs(seed++ % h.length)];
    }
    if (_histoType== SharedTreeModel.SharedTreeParameters.HistogramType.AUTO)
      _histoType= SharedTreeModel.SharedTreeParameters.HistogramType.UniformAdaptive;
    assert(_histoType!= SharedTreeModel.SharedTreeParameters.HistogramType.RoundRobin);
    _globalQuantilesKey = globalQuantilesKey;
    // See if we can show there are fewer unique elements than nbins.
    // Common for e.g. boolean columns, or near leaves.
    int xbins = isInt == 2 ? nbins_cats : nbins;
    if (isInt > 0 && maxEx - min <= xbins) {
      assert ((long) min) == min : "Overflow for integer/categorical histogram: minimum value cannot be cast to long without loss: (long)" + min + " != " + min + "!";                // No overflow
      xbins = (char) ((long) maxEx - (long) min);  // Shrink bins
      _step = 1.0f;                           // Fixed stepsize
    } else {
      _step = xbins / (maxEx - min);              // Step size for linear interpolation, using mul instead of div
      assert _step > 0 && !Double.isInfinite(_step) : "Histogram step size for column '" + name + "' is invalid: " + _step + ".";
    }
    _nbin = (char) xbins;
    assert(_nbin>0);
    assert(_w ==null);
    assert(_wY ==null);
    assert(_wYY ==null);
//    Log.info("Histogram: " + this);
    // Do not allocate the big arrays here; wait for scoreCols to pick which cols will be used.
  }

  // Interpolate d to find bin#
  public int bin( double col_data ) {
    assert( !Double.isNaN(col_data) ); //NAs go to a separate bucket
    if (Double.isInfinite(col_data)) // Put infinity to most left/right bin
      if (col_data<0) return 0;
      else return _w.length-1;
    assert _min <= col_data && col_data < _maxEx : "Coldata " + col_data + " out of range " + this;
    // When the model is exposed to new test data, we could have data that is
    // out of range of any bin - however this binning call only happens during
    // model-building.
    int idx1;
    double pos = _hasQuantiles ? col_data : ((col_data - _min) * _step);
    if (_splitPts != null) {
      idx1 = Arrays.binarySearch(_splitPts, pos);
      if (idx1 < 0) idx1 = -idx1 - 2;
    } else {
      idx1 = (int) pos;
    }
    if (idx1 == _w.length) idx1--; // Roundoff error allows idx1 to hit upper bound, so truncate
    assert 0 <= idx1 && idx1 < _w.length : idx1 + " " + _w.length;
    return idx1;
  }
  public double binAt( int b ) {
    if (_hasQuantiles) return _splitPts[b];
    return _min + (_splitPts == null ? b : _splitPts[b]) / _step;
  }

  public int nbins() { return _nbin; }
  public double bins(int b) { return _w[b]; }

  // Big allocation of arrays
  public void init() {
    assert _w == null;
    if (_histoType==SharedTreeModel.SharedTreeParameters.HistogramType.Random) {
      // every node makes the same split points
      Random rng = RandomUtils.getRNG((Double.doubleToRawLongBits(((_step+0.324)*_min+8.3425)+89.342*_maxEx) + 0xDECAF*_nbin + 0xC0FFEE*_isInt + _seed));
      assert(_nbin>1);
      _splitPts = new double[_nbin];
      _splitPts[0] = 0;
      _splitPts[_nbin - 1] = _nbin-1;
      for (int i = 1; i < _nbin-1; ++i)
         _splitPts[i] = rng.nextFloat() * (_nbin-1);
      Arrays.sort(_splitPts);
    }
    else if (_histoType== SharedTreeModel.SharedTreeParameters.HistogramType.QuantilesGlobal) {
      assert (_splitPts == null);
      if (_globalQuantilesKey != null) {
        HistoQuantiles hq = DKV.getGet(_globalQuantilesKey);
        if (hq != null) {
          _splitPts = ((HistoQuantiles) DKV.getGet(_globalQuantilesKey)).splitPts;
          if (_splitPts!=null) {
//            Log.info("Obtaining global splitPoints: " + Arrays.toString(_splitPts));
            _splitPts = ArrayUtils.limitToRange(_splitPts, _min, _maxEx);
            if (_splitPts.length > 1 && _splitPts.length < _nbin)
              _splitPts = ArrayUtils.padUniformly(_splitPts, _nbin);
            if (_splitPts.length <= 1) {
              _splitPts = null; //abort, fall back to uniform binning
              _histoType = SharedTreeModel.SharedTreeParameters.HistogramType.UniformAdaptive;
            }
            else {
              _hasQuantiles=true;
              _nbin = (char)_splitPts.length;
//              Log.info("Refined splitPoints: " + Arrays.toString(_splitPts));
            }
          }
        }
      }
    }
    else assert(_histoType== SharedTreeModel.SharedTreeParameters.HistogramType.UniformAdaptive);
    //otherwise AUTO/UniformAdaptive
    assert(_nbin>0);
    _w = MemoryManager.malloc8d(_nbin);
    _wY = MemoryManager.malloc8d(_nbin);
    _wYY = MemoryManager.malloc8d(_nbin);
    _wNA = new AtomicDouble();
    _wYNA = new AtomicDouble();
    _wYYNA = new AtomicDouble();
  }

  // Add one row to a bin found via simple linear interpolation.
  // Compute bin min/max.
  // Compute response mean & variance.
  void incr( double col_data, double y, double w ) {
    assert !Double.isNaN(col_data);
    assert Double.isInfinite(col_data) || (_min <= col_data && col_data < _maxEx) : "col_data "+col_data+" out of range "+this;
    int b = bin(col_data);      // Compute bin# via linear interpolation
    water.util.AtomicUtils.DoubleArray.add(_w,b,w); // Bump count in bin
    // Track actual lower/upper bound per-bin
    if (!Double.isInfinite(col_data)) {
      setMin(col_data);
      setMaxIn(col_data);
    }
    if( y != 0 && w != 0) incr0(b,y,w);
  }

  // Merge two equal histograms together.  Done in a F/J reduce, so no
  // synchronization needed.
  public void add( DHistogram dsh ) {
    assert _isInt == dsh._isInt && _nbin == dsh._nbin && _step == dsh._step &&
      _min == dsh._min && _maxEx == dsh._maxEx;
    assert (_w == null && dsh._w == null) || (_w != null && dsh._w != null);
    if( _w == null ) return;
    ArrayUtils.add(_w,dsh._w);
    if( _min2 > dsh._min2  ) _min2 = dsh._min2;
    if( _maxIn < dsh._maxIn) _maxIn = dsh._maxIn;
    add0(dsh);
    _wNA.addAndGet(dsh._wNA.get());
    _wYNA.addAndGet(dsh._wYNA.get());
    _wYYNA.addAndGet(dsh._wYYNA.get());
  }

  // Inclusive min & max
  public double find_min  () { return _min2 ; }
  public double find_maxIn() { return _maxIn; }
  // Exclusive max
  public double find_maxEx() { return find_maxEx(_maxIn,_isInt); }
  public static double find_maxEx(double maxIn, int isInt ) {
    double ulp = Math.ulp(maxIn);
    if( isInt > 0 && 1 > ulp ) ulp = 1;
    double res = maxIn+ulp;
    return Double.isInfinite(res) ? maxIn : res;
  }

  // The initial histogram bins are setup from the Vec rollups.
  public static DHistogram[] initialHist(Frame fr, int ncols, int nbins, DHistogram hs[], long seed, SharedTreeModel.SharedTreeParameters parms, Key[] globalQuantilesKey) {
    Vec vecs[] = fr.vecs();
    for( int c=0; c<ncols; c++ ) {
      Vec v = vecs[c];
      final double minIn = Math.max(v.min(),-Double.MAX_VALUE); // inclusive vector min
      final double maxIn = Math.min(v.max(), Double.MAX_VALUE); // inclusive vector max
      final double maxEx = find_maxEx(maxIn,v.isInt()?1:0);     // smallest exclusive max
      final long vlen = v.length();
      hs[c] = v.naCnt()==vlen || v.min()==v.max() ?
          null : make(fr._names[c],nbins, (byte)(v.isCategorical() ? 2 : (v.isInt()?1:0)), minIn, maxEx, seed, parms, globalQuantilesKey[c]);
      assert (hs[c] == null || vlen > 0);
    }
    return hs;
  }


  public static DHistogram make(String name, final int nbins, byte isInt, double min, double maxEx, long seed, SharedTreeModel.SharedTreeParameters parms, Key globalQuantilesKey) {
    return new DHistogram(name,nbins, parms._nbins_cats, isInt, min, maxEx, parms._min_split_improvement, parms._histogram_type, seed, globalQuantilesKey);
  }

  // Pretty-print a histogram
  @Override public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(_name).append(":").append(_min).append("-").append(_maxEx).append(" step=" + (1 / _step) + " nbins=" + nbins() + " isInt=" + _isInt);
    if( _w != null ) {
      for(int b = 0; b< _w.length; b++ ) {
        sb.append(String.format("\ncnt=%f, [%f - %f], mean/var=", _w[b],_min+b/_step,_min+(b+1)/_step));
        sb.append(String.format("%6.2f/%6.2f,", mean(b), var(b)));
      }
      sb.append('\n');
    }
    return sb.toString();
  }
  double mean(int b) {
    double n = _w[b];
    return n>0 ? _wY[b]/n : 0;
  }

  /**
   * compute the sample variance within a given bin
   * @param b bin id
   * @return sample variance (>= 0)
   */
  public double var (int b) {
    double n = _w[b];
    if( n<=1 ) return 0;
    return Math.max(0, (_wYY[b] - _wY[b]* _wY[b]/n)/(n-1)); //not strictly consistent with what is done elsewhere (use n instead of n-1 to get there)
  }

  // Add one row to a bin found via simple linear interpolation.
  // Compute response mean & variance.
  // Done racily instead F/J map calls, so atomic
  public void incr0( int b, double y, double w ) {
    AtomicUtils.DoubleArray.add(_wY,b,(float)(w*y)); //See 'HistogramTest' JUnit for float-casting rationalization
    AtomicUtils.DoubleArray.add(_wYY,b,(float)(w*y*y));
  }
  // Same, except square done by caller
  public void incr1( int b, double y, double yy) {
    AtomicUtils.DoubleArray.add(_wY,b,(float)y); //See 'HistogramTest' JUnit for float-casting rationalization
    AtomicUtils.DoubleArray.add(_wYY,b,(float)yy);
  }

  // Merge two equal histograms together.
  // Done in a F/J reduce, so no synchronization needed.
  public void add0( DHistogram dsh ) {
    ArrayUtils.add(_wY,dsh._wY);
    ArrayUtils.add(_wYY,dsh._wYY);
  }

  public DTree.Split findBestSplitPoint(int col, double min_rows) {
    final int nbins = nbins();
    assert nbins > 1;

    // Histogram arrays used for splitting, these are either the original bins
    // (for an ordered predictor), or sorted by the mean response (for an
    // unordered predictor, i.e. categorical predictor).
    double[]   w =   _w;
    double[]  wY =  _wY;
    double[] wYY = _wYY;
    int idxs[] = null;          // and a reverse index mapping

    // For categorical (unordered) predictors, sort the bins by average
    // prediction then look for an optimal split.  Currently limited to categoricals
    // where we're one-per-bin.
    if( _isInt == 2 && _step == 1.0f ) {
      // Sort the index by average response
      idxs = MemoryManager.malloc4(nbins+1); // Reverse index
      for( int i=0; i<nbins+1; i++ ) idxs[i] = i;
      final double[] avgs = MemoryManager.malloc8d(nbins+1);
      for( int i=0; i<nbins; i++ ) avgs[i] = _w[i]==0 ? 0 : _wY[i]/ _w[i]; // Average response
      avgs[nbins] = Double.MAX_VALUE;
      ArrayUtils.sort(idxs, avgs);
      // Fill with sorted data.  Makes a copy, so the original data remains in
      // its original order.
        w = MemoryManager.malloc8d(nbins);
       wY = MemoryManager.malloc8d(nbins);
      wYY = MemoryManager.malloc8d(nbins);
      for( int i=0; i<nbins; i++ ) {
          w[i] =   _w[idxs[i]];
         wY[i] =  _wY[idxs[i]];
        wYY[i] = _wYY[idxs[i]];
      }
    }

    // Compute mean/var for cumulative bins from 0 to nbins inclusive.
    double   wlo[] = MemoryManager.malloc8d(nbins+1);
    double  wYlo[] = MemoryManager.malloc8d(nbins+1);
    double wYYlo[] = MemoryManager.malloc8d(nbins+1);
    for( int b=1; b<=nbins; b++ ) {
      double n0 =   wlo[b-1], n1 =   w[b-1];
      if( n0==0 && n1==0 )
        continue;
      double m0 =  wYlo[b-1], m1 =  wY[b-1];
      double s0 = wYYlo[b-1], s1 = wYY[b-1];
        wlo[b] = n0+n1;
       wYlo[b] = m0+m1;
      wYYlo[b] = s0+s1;
    }
    double wNA = _wNA.doubleValue();
    double tot = wlo[nbins] + wNA; //total number of (weighted) rows
    // Is any split possible with at least min_obs?
    if( tot < 2*min_rows )
      return null;
    // If we see zero variance, we must have a constant response in this
    // column.  Normally this situation is cut out before we even try to split,
    // but we might have NA's in THIS column...
    double wYNA = _wYNA.doubleValue();
    double wYYNA = _wYYNA.doubleValue();
    double var = (wYYlo[nbins]+wYYNA)*tot - (wYlo[nbins]+wYNA)*(wYlo[nbins]+wYNA);
    if( ((float)var) == 0f )
      return null;

    // Compute mean/var for cumulative bins from nbins to 0 inclusive.
    double   whi[] = MemoryManager.malloc8d(nbins+1);
    double  wYhi[] = MemoryManager.malloc8d(nbins+1);
    double wYYhi[] = MemoryManager.malloc8d(nbins+1);
    for( int b=nbins-1; b>=0; b-- ) {
      double n0 =   whi[b+1], n1 =   w[b];
      if( n0==0 && n1==0 )
        continue;
      double m0 =  wYhi[b+1], m1 =  wY[b];
      double s0 = wYYhi[b+1], s1 = wYY[b];
        whi[b] = n0+n1;
       wYhi[b] = m0+m1;
      wYYhi[b] = s0+s1;
      assert MathUtils.compare(wlo[b]+ whi[b]+wNA,tot,1e-5,1e-5);
    }

    double best_seL=Double.MAX_VALUE;   // squared error for left side of the best split (so far)
    double best_seR=Double.MAX_VALUE;   // squared error for right side of the best split (so far)
    NASplitDir nasplit = NASplitDir.None;

    // squared error of all non-NAs
    double seNonNA = wYYhi[0] - wYhi[0]* wYhi[0]/ whi[0]; // Squared Error with no split
    if (seNonNA < 0) seNonNA = 0;
    double seBefore = seNonNA;

    // if there are any NAs, then try to split them from the non-NAs
    if (wNA>=min_rows) {
      double seAll = (wYYhi[0] + wYYNA) - (wYhi[0] + wYNA) * (wYhi[0] + wYNA) / (whi[0] + wNA);
      double seNA = wYYNA - wYNA * wYNA / wNA;
      if (seNA < 0) seNA = 0;
      best_seL = seNonNA;
      best_seR = seNA;
      nasplit = NASplitDir.NAvsREST;
      seBefore = seAll;
    }

    // Now roll the split-point across the bins.  There are 2 ways to do this:
    // split left/right based on being less than some value, or being equal/
    // not-equal to some value.  Equal/not-equal makes sense for categoricals
    // but both splits could work for any integral datatype.  Do the less-than
    // splits first.
    int best=0;                         // The no-split
    byte equal=0;                       // Ranged check
    for( int b=1; b<=nbins-1; b++ ) {
      if( w[b] == 0 ) continue; // Ignore empty splits
      if( wlo[b]+wNA < min_rows ) continue;
      if( whi[b]+wNA < min_rows ) break; // w1 shrinks at the higher bin#s, so if it fails once it fails always
      // We're making an unbiased estimator, so that MSE==Var.
      // Then Squared Error = MSE*N = Var*N
      //                    = (wYY/N - wY^2)*N
      //                    = wYY - N*wY^2
      //                    = wYY - N*(wY/N)(wY/N)
      //                    = wYY - wY^2/N

      // no NAs
      if (wNA==0) {
        double selo = wYYlo[b] - wYlo[b] * wYlo[b] / wlo[b];
        double sehi = wYYhi[b] - wYhi[b] * wYhi[b] / whi[b];
        if (selo < 0) selo = 0;    // Roundoff error; sometimes goes negative
        if (sehi < 0) sehi = 0;    // Roundoff error; sometimes goes negative
        if ((selo + sehi < best_seL + best_seR) || // Strictly less error?
                // Or tied MSE, then pick split towards middle bins
                (selo + sehi == best_seL + best_seR &&
                        Math.abs(b - (nbins >> 1)) < Math.abs(best - (nbins >> 1)))) {
          best_seL = selo;
          best_seR = sehi;
          best = b;
        }
      } else {
        // option 1: split the numeric feature and throw NAs to the left
        {
          double selo = wYYlo[b] + wYYNA - (wYlo[b] + wYNA) * (wYlo[b] + wYNA) / (wlo[b] + wNA);
          double sehi = wYYhi[b] - wYhi[b] * wYhi[b] / whi[b];
          if (selo < 0) selo = 0;    // Roundoff error; sometimes goes negative
          if (sehi < 0) sehi = 0;    // Roundoff error; sometimes goes negative
          if ((selo + sehi < best_seL + best_seR) || // Strictly less error?
                  // Or tied SE, then pick split towards middle bins
                  (selo + sehi == best_seL + best_seR &&
                          Math.abs(b - (nbins >> 1)) < Math.abs(best - (nbins >> 1)))) {
            if( (wlo[b] + wNA) >= min_rows && whi[b] >= min_rows) {
              best_seL = selo;
              best_seR = sehi;
              best = b;
              nasplit = NASplitDir.NALeft;
            }
          }
        }

        // option 2: split the numeric feature and throw NAs to the right
        {
          double selo = wYYlo[b] - wYlo[b] * wYlo[b] / wlo[b];
          double sehi = wYYhi[b]+wYYNA - (wYhi[b]+wYNA) * (wYhi[b]+wYNA) / (whi[b]+wNA);
          if (selo < 0) selo = 0;    // Roundoff error; sometimes goes negative
          if (sehi < 0) sehi = 0;    // Roundoff error; sometimes goes negative
          if ((selo + sehi < best_seL + best_seR) || // Strictly less error?
                  // Or tied SE, then pick split towards middle bins
                  (selo + sehi == best_seL + best_seR &&
                          Math.abs(b - (nbins >> 1)) < Math.abs(best - (nbins >> 1)))) {
            if( wlo[b] >= min_rows && (whi[b] + wNA) >= min_rows ) {
              best_seL = selo;
              best_seR = sehi;
              best = b;
              nasplit = NASplitDir.NARight;
            }
          }
        }
      }
    }

    // For categorical (unordered) predictors, we sorted the bins by average
    // prediction then found the optimal split on sorted bins
    IcedBitSet bs = null;       // In case we need an arbitrary bitset
    if( idxs != null ) {        // We sorted bins; need to build a bitset
      int min=Integer.MAX_VALUE;// Compute lower bound and span for bitset
      int max=Integer.MIN_VALUE;
      for( int i=best; i<nbins; i++ ) {
        min=Math.min(min,idxs[i]);
        max=Math.max(max,idxs[i]);
      }
      bs = new IcedBitSet(max-min+1,min); // Bitset with just enough span to cover the interesting bits
      for( int i=best; i<nbins; i++ ) bs.set(idxs[i]); // Reverse the index then set bits
      equal = (byte)(bs.max() <= 32 ? 2 : 3); // Flag for bitset split; also check max size
    }

    if( best==0 && nasplit==NASplitDir.None) {
//      Log.info("Not splitting: no optimal split point found:\n" + this);
      return null;
    }

    //if( se <= best_seL+best_se1) return null; // Ultimately roundoff error loses, and no split actually helped
    if (!(best_seL+ best_seR < seBefore * (1- _minSplitImprovement))) {
//      Log.info("Not splitting: not enough relative improvement: " + (1-(best_seL + best_seR) / seBefore) + "\n" + this);
      return null;
    }

    double nLeft = wlo[best];
    double nRight = whi[best];
    double predLeft = wYlo[best];
    double predRight = wYhi[best];

    if (nasplit==NASplitDir.NAvsREST) {
      assert(best == 0);
      nLeft = whi[0]; //all non-NAs
      predLeft = wYhi[0];
      nRight = wNA;
      predRight = wYNA;
    }
    else if (nasplit==NASplitDir.NALeft) {
      nLeft +=wNA;
      predLeft +=wYNA;
    }
    else if (nasplit==NASplitDir.NARight) {
      nRight +=wNA;
      predRight +=wYNA;
    }

    if( MathUtils.equalsWithinOneSmallUlp((float)(predLeft / nLeft),(float)(predRight / nRight)) ) {
//      Log.info("Not splitting: Predictions for left/right are the same:\n" + this);
      return null;
    }

    if (nLeft < min_rows || nRight < min_rows) {
//      Log.info("Not splitting: split would violate min_rows limit:\n" + this);
      return null;
    }

    // if still undecided (e.g., if there are no NAs in training), pick a good default direction for NAs in test time
    if (nasplit == NASplitDir.None) {
      nasplit = nLeft > nRight ? NASplitDir.Left : NASplitDir.Right;
    }
    return new DTree.Split(col,best,nasplit,bs,equal,seBefore,best_seL, best_seR, nLeft, nRight, predLeft / nLeft, predRight / nRight);
  }

  public void updateSharedHistosAndReset(double[] w, double[] wY, double[] wYY, double[] ws, double[] cs, double[] ys, int [] rows, int hi, int lo) {
    double minmax[] = new double[]{_min2,_maxIn};
    // Gather all the data for this set of rows, for 1 column and 1 split/NID
    // Gather min/max, wY and sum-squares.
    for(int r = lo; r< hi; ++r) {
      int k = rows[r];
      double weight = ws[k];
      if (weight == 0) continue;
      double col_data = cs[k];
      if( col_data < minmax[0] ) minmax[0] = col_data;
      if( col_data > minmax[1] ) minmax[1] = col_data;
      double y = ys[k];
      assert(!Double.isNaN(y));
      double wy = weight * y;
      double wyy = wy * y;
      if (Double.isNaN(col_data)) {
        //separate bucket for NA - atomically added to the shared histo
        _wNA.addAndGet(weight);
        _wYNA.addAndGet(wy);
        _wYYNA.addAndGet(wyy);
      } else {
        // increment local pre-thread histograms
        int b = bin(col_data);
        w[b] += weight;
        wY[b] += wy;
        wYY[b] += wyy;
      }
    }

    // Atomically update histograms
    setMin(minmax[0]);       // Track actual lower/upper bound per-bin
    setMaxIn(minmax[1]);

    final int len = _w.length;
    for( int b=0; b<len; b++ ) { // Bump counts in weighted counts and reset the temp arrays to 0
      if( w[b] != 0 ) { AtomicUtils.DoubleArray.add(_w,b,w[b]); w[b]=0; }
    }
    for( int b=0; b<len; b++ ) { // Bump counts in weighted response and response^2 and reset the temp arrays to 0
      if( wY[b] != 0 || wYY[b] != 0 ) { incr1(b,wY[b],wYY[b]); wY[b]=wYY[b]=0; }
    }
  }

}
