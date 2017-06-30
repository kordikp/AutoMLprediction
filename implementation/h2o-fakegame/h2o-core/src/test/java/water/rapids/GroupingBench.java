package water.rapids;

import java.util.Arrays;
import java.util.Random;
import hex.CreateFrame;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import water.*;
import water.fvec.*;
import water.util.ArrayUtils;


class MySample extends MRTask<MySample> {
    final int K;
    public MySample(int K) {
        this.K = K;
    }
    @Override public void map( Chunk chk ) {
        Random randomGenerator = water.util.RandomUtils.getRNG(chk.cidx());
        for (int i=0; i<chk.len(); i++) {
            chk.set(i, randomGenerator.nextInt(K));
        }
    }
}

class MySeq extends MRTask<MySeq> {
    // Much faster than :
    // for (int i=0; i<vec.length(); i++) vec.set(i, i % 100);
    final int K;
    public MySeq(int K) {
        this.K = K;
    }
    @Override public void map( Chunk chk ) {
        for (int i=0; i<chk.len(); i++) {
            chk.set(i, (chk.start() + i) % K);
        }
    }
}


class MyCountRange extends MRTask<MyCountRange> {
    final long _max, _min;
    long _counts[][];
    int _nChunks;
    // int ans;
    MyCountRange(long max, long min, int nChunks) {
        System.out.println("Constructor for MyCountRange");
        _max = max; _min = min; _counts = new long[nChunks][]; _nChunks = nChunks;
    }
    // setupLocal(); // to do.  No because the memory gets copied across. Constructor doesn't need to run on other nodes.
    @Override public void map( Chunk chk ) {
        long tmp[] = _counts[chk.cidx()] = new long[(int)(_max-_min+1)];
        //long tmp[] = new long[(int)(_max-_min+1)];   // does non-sharing help?   If so, assign afterwards after the loop?
        int rows = chk._len;
        //double dummyCounter=1;
        for (int r=0; r<rows; r++) tmp[(int)(chk.at8(r)-_min)]++;
        // for (int w=0; w<10; w++)
        // for (long r=0; r<((long)rows)*25; r++) dummyCounter *= 3.14;  //Math.sin(3.14);  //(int)(chk.at8(r)-_min);
        // ans = (int)Math.round(dummyCounter);  // use it to stop it being dropped by optimizer
    }
    @Override public void reduce(MyCountRange g) {
        if (g._counts != _counts) {
            // assign the counts from the other one
            System.out.println("This should just print once since 2 nodes");
            for (int c=0; c<_nChunks; c++) {
                if (g._counts[c] != null) {
                    assert _counts[c] == null;
                    _counts[c] = g._counts[c];
                    //for (int i=0; i<_max-_min+1; i++) {
                    //
                    //}
                } else {
                    assert _counts[c] != null;
                }
            }
            // throw H2O.unimpl();
        } else {
            //System.out.println("Reduce of two chunks on the same node");
        }
    }
}


class MyCountRangeNoSpline extends MRTask<MyCountRangeNoSpline> {
    final long _max, _min;
    long _counts[];
    MyCountRangeNoSpline(long max, long min, int nChunks) {
        System.out.println("Constructor for MyCountRange");
        _max = max; _min = min;
    }
    @Override public void map( Chunk chk ) {
        _counts = new long[(int)(_max-_min+1)];
        int rows = chk._len;
        for (int r=0; r<rows; r++) _counts[(int)(chk.at8(r)-_min)]++;
    }
    @Override public void reduce(MyCountRangeNoSpline g) {
        ArrayUtils.add(_counts, g._counts);
    }
}

/*
 * When order is one single array. But Java memalloc fail on 1e9 items.
 */
/*
class WriteOrder extends MRTask<WriteOrder> {
    final long _counts[][];
    final long _order[];
    final long _min;
    final long _max;
    WriteOrder(long[][] counts, long[] order, long min, long max) { _counts = counts; _order = order; _min = min; _max=max; }
    @Override public void map( Chunk chk ) {
        long myCounts[] = _counts[chk._cidx];
        for (int r=0; r<chk._len; r++) _order[ (int)(myCounts[(int)(chk.at8(r)-_min)]++) ] = r+chk._start;
    }
}
*/
class WriteOrder extends MRTask<WriteOrder> {
    final long _counts[][];
    final int _order[][];
    final long _min;
    final long _max;
    WriteOrder(long[][] counts, int[][] order, long min, long max) { _counts = counts; _order = order; _min = min; _max = max; }
    @Override public void map( Chunk chk ) {
        long nanos[] = new long[5];
        Vec vec = chk.vec();
        int range = (int)(_max-_min+1);
        long[] espc = vec.espc();

        long myCounts[] = _counts[chk.cidx()];

        // Test thread local counts. Keep in cache and never push to RAM (don't need to be shared)
        // long myCounts[] = new long[(int)(_max-_min+1)];
        // for (int i=0; i<_max-_min+1; i++) myCounts[i] = _counts[chk._cidx][i];

        int myTargetChunks[] = new int[range];
        for (int i=0; i<_max-_min+1; i++) myTargetChunks[i] = vec.elem2ChunkIdx(myCounts[i]);  // elem2ChunkIdx is a binary search due to chunks not being equal size. Try to avoid.
        for (int r=0; r<chk._len; r++) {
            //long t0 = System.nanoTime();
            int group = (int)(chk.at8(r)-_min);
            //nanos[0] += System.nanoTime()-t0; t0=System.nanoTime();
            long target = myCounts[group]++;
            //nanos[1] += System.nanoTime()-t0; t0=System.nanoTime();
            int targetChunk = myTargetChunks[group];
            //nanos[2] += System.nanoTime()-t0; t0=System.nanoTime();
            if ( target == espc[targetChunk+1] ) { myTargetChunks[group]++; targetChunk++; }  // crossed chunk boundary
            //nanos[3] += System.nanoTime()-t0; t0=System.nanoTime();
            //_order[targetChunk][(int)(target - espc[targetChunk])] = r+(int)chk._start;
            //nanos[4] += System.nanoTime()-t0;
        }
        //System.out.print("Chunk "+chk._cidx+": "); for (int i=0; i<5; i++) System.out.print(Math.round(nanos[i]/1e6)+" "); System.out.print("\n");  // print ms
    }
}


public class GroupingBench extends TestUtil {
    @BeforeClass public static void setup() { stall_till_cloudsize(2); }

    @Ignore @Test public void runGroupingBench() {
        // Simplified version of tests in runit_quantile_1_golden.R. There we test probs=seq(0,1,by=0.01)
        //Vec vec = Vec.makeCon(1.1, 1000000000);
        //Vec vec = Vec.makeRepSeq(10,10);
        Vec vec = Vec.makeZero((long)1e9);
        //System.out.println("Chunks: " + vec.nChunks());
        //System.out.println("Vec length: " + vec.length());
        //System.out.println("Populating vector... ");

        //new MySeq((int)100).doAll(vec);
        //new MySample((int)10).doAll(vec);
        new MySample((int)10).doAll(vec);
        vec.max(); // to cache rollups,  so timing below excludes it

        System.out.println("\nFirst 30 of vec ...");
        System.out.println("There are "+vec.nChunks()+" chunks");
        for (int i=0; i<vec.nChunks(); i++) {
            System.out.println("Chunk"+i+"is on"+vec.chunkKey(i).home_node());
        }

        CreateFrame cf = new CreateFrame();
        cf.rows = 100;
        cf.cols = 10;
        cf.categorical_fraction = 0.1;
        cf.integer_fraction = 1 - cf.categorical_fraction;
        cf.binary_fraction = 0;
        cf.factors = 4;
        cf.response_factors = 2;
        cf.positive_response = false;
        cf.has_response = true;
        cf.seed = 1234;
        Frame frame = cf.execImpl().get();
        System.out.print( frame.toString(0,14) );

        for (int i=0; i<30; i++) System.out.print((int)vec.at(i) + " ");  System.out.println("\n");
        // Vec vec = vec(5 , 8 ,  9 , 12 , 13 , 16 , 18 , 23 , 27 , 28 , 30 , 31 , 33 , 34 , 43,  45,  48, 161);
        // makeSeq;

        // Take out memory alloc before the loop to avoid GC costs, before vtune profiling
        // Now broken up into arrays of same shape as vec.chunks. Really cannot have one array of 1e9 items in Java.
        // nanos = System.nanoTime();
        long heapsize=Runtime.getRuntime().totalMemory();
        System.out.println("heapsize is::"+heapsize);

        //long o[] = new long[(int)vec.length()];

        int o[][] = new int[vec.nChunks()][];    // [(int)vec.length()];
        for (int c=0; c<o.length; c++)
            o[c] = new int[vec.chunkForChunkIdx(c)._len];

        for (int timeRep=0; timeRep<3; timeRep++) {   // TO DO: caliper java project

            // TO DO:  search for utils.Timer,  prettyPrint

            long nanos = System.nanoTime();
            long ans2[][] = new MyCountRange((long) vec.max(), (long) vec.min(), vec.nChunks()).doAll(vec)._counts;
            long nanos1 = System.nanoTime() - nanos;
            System.out.println("Counts per chunk (first 5 chunks) ...");
            for (int c = 0; c < 5; c++) System.out.println(Arrays.toString(ans2[c]));



            /*
            nanos = System.nanoTime();
            // cumulate across chunks
            int nBuckets = (int)((long) vec.max() - (long) vec.min() + 1);
            long rollSum = 0;
            for (int b = 0; b < nBuckets; b++) {
                for (int c = 0; c < vec.nChunks(); c++) {
                    long tmp = ans2[c][b];
                    ans2[c][b] = rollSum;
                    rollSum += tmp;
                }
            }
            long nanos2 = System.nanoTime() - nanos;
            //System.out.println("\nCounts after cumulate ...");
            //for (int c = 0; c < vec.nChunks(); c++) System.out.println(Arrays.toString(ans2[c]));

            nanos = System.nanoTime();
            new WriteOrder(ans2, o, (long) vec.min(), (long) vec.max()).doAll(vec);
            long nanos3 = System.nanoTime() - nanos;

            //System.out.println("\nCounts after WriteOrder ...");
            //for (int c = 0; c < vec.nChunks(); c++) System.out.println(Arrays.toString(ans2[c]));

            System.out.println("\nFirst 10 of order ...");
            //for (int i=0; i<10; i++) System.out.print(o[i] + " ");
            for (int i=0; i<10; i++) System.out.print(o[0][i] + " ");

            System.out.println("\nLast 10 of order ...");
            //for (int i=9; i>=0; i--) System.out.print(o[(int)(vec.length()-i-1)] + " "); System.out.print("\n");
            int c = vec.nChunks()-1;
            long cstart = vec._espc[c];
            for (int i=9; i>=0; i--) System.out.print(o[c][(int)(vec.length()-i-1-cstart)] + " "); System.out.print("\n");

            System.out.println("\nFirst 40 of vec ...");
            for (int i=0; i<40; i++) System.out.print((int)vec.at(i) + " ");
            System.out.println("\nLast 40 of vec ...");
            for (int i=39; i>=0; i--) System.out.print((int)vec.at((int)vec.length()-i-1) + " ");  System.out.print("\n");
            */
            System.out.println("\nInitial count: " + nanos1 / 1e9);
            //System.out.println("Cumulate across chunks: " + nanos2 / 1e9);
            //System.out.println("Write to order[]: " + nanos3 / 1e9);
            //System.out.println("Total time: " + (nanos1+nanos2+nanos3) / 1e9);
            System.out.println("");

        }
        // Next: input int, then large groups, small groups

        vec.remove();
        frame.delete();
    }
}


