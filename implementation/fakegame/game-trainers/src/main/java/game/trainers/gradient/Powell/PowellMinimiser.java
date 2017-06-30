package game.trainers.gradient.Powell;

/**
 * <dl>
 * <dt>Description:
 * <dd>Minimises a function objfunc of n variables.
 * <dl>
 *
 * @author Kathleen Curran $Id: PowellMinimiser.java,v 1.3 2005/07/25 15:40:17
 *         ucacpco Exp $
 */
public abstract class PowellMinimiser {
    /**
     * Minimises a function.
     *
     * @param ITMAX
     * maximum allowed number of iterations in <code> powell </code>
     * @param TOL
     * tolerance passed to <code> brent </code> in
     * <code> linmin </code>
     * @param GOLD
     * default ratio by which successive intervals are magnified in
     * <code> mnbrak </code>
     * @param GLIMIT
     * maximum magnification allowed for a parabolic-fit step in
     * <code> mnbrak </code>
     * @param TINY
     * used to prevent any possible division by zero in
     * <code> mnbrak </code>
     * @param BRENTITMAX
     * maximum allowed number of iterations in <code>brent</code>
     * @param CGOLD
     * golden ratio in <code>brent</code>
     * @param ZEPS
     * a small number that protects against trying to achieve
     * fractional accuracy for a minimum that happens to be exactly
     * zero in <code> brent </code>
     * @param xi
     * an initial matrix, whose columns contain the initial set of
     * directions
     * @param iter
     * number of iterations taken
     * @param fret
     * returned function value at p
     * @param pt
     * @param ptt
     * @param xit
     * @param pcom
     * global variable communicating with f1dim
     * @param xicom
     * global variable communicating with f1dim
     * @param nrfunc
     * function
     * @param xt
     * @param ax
     * initial point for <code> mnbrak </code> and returned new point
     * that brackets a minimum of the function in
     * <code> mnbrak </code>
     * @param bx
     * initial point for <code> mnbrak </code> and returned new point
     * that brackets a minimum of the function in
     * <code> mnbrak </code>
     * @param cx
     * new point that brackets a minimum of the function in
     * <code> mnbrak </code>
     * @param fa
     * returned function values at the 3 points that bracket a
     * minimum in <code> mnbrak </code>
     * @param fb
     * returned function values at the 3 points that bracket a
     * minimum in <code> mnbrak </code>
     * @param fc
     * returned function values at the 3 points that bracket a
     * minimum in <code> mnbrak </code>
     * @param f
     * function in <code> brent </code>
     */

    //powell variables
    protected static int ITMAX = 200;

    //linmin variables
    private static double BRENTTOL = 2.0E-4;

    protected double xx;

    protected double fx;

    private double bx;

    private double[] pcom;

    private double[] xicom;

    private double[] xit;

    private int ncom;

    //mnbrak variables
    private static double GOLD = 1.618034;

    private static double GLIMIT = 100.0;

    private static double TINY = 1.0E-20;

    //brent variables
    private static double BRENTITMAX = 100;

    private static double CGOLD = 0.3819660;

    private static double ZEPS = 1.0E-10;

    //powell variables
    private double[][] xi;

    private double[] pt;

    private int iter;

    private double fret;

    //brent + linmin variable
    private double xmin;

    //mnbrak variables
    private double ax;

    // protected double bx;
    private double cx;

    private double fa;

    private double fb;

    private double fc;

    //brent variables
    private double f;

    /**
     * Default constructor that sets the initial direction set to the identity.
     *
     * @param d
     */
    protected PowellMinimiser(int d) {
        xi = new double[d + 1][d + 1];
        for (int i = 1; i <= d; i++) {
            for (int j = 1; j <= d; j++) {
                xi[i][j] = (i == j) ? 1.0 : 0.0;
            }
        }
    }

    /**
     * Computes the objective function at the point specified by array params.
     * Note that params should be indexed from 1. This function is implemented
     * to obtain a PowellMinimiser for a particular function.
     *
     * @param params
     */
    protected abstract double fObj(double[] params);

    /**
     * Returns the values of the parameters at the minimum point after
     * minimisation has been performed. Note that the array returned should be
     * indexed from 1.
     */
    public double[] getMinParams() {
        return pt;
    }

    /**
     * Performs the actual minimisation given a starting point and convergence
     * threshold. The value of the function at the minimum point is returned.
     * Note that array p should be indexed from 1.
     *
     * @param p
     * @param ftol
     */
    public double minimise(double[] p, double ftol) throws PowellMinimiserException {
        powell(p, p.length - 1, ftol);
        return fret;
    }

    /**
     * Implementation of Powells method.
     *
     * @param p
     * @param n
     * @param ftol
     */
    private void powell(double[] p, int n, double ftol) throws PowellMinimiserException {
        //System.out.println("Starting powell");
        /*
         * Minimisation of a function <code> func </code> of n variables. Input
         * consists of an initial starting point p[1..n]; an initial matrix
         * xi[1..n][1..n], whose columns contain the initial set of directions
         * (usually the n unit vectors); and ftol; the fractional tolerance in
         * the function value such that failure to decrease by more than this
         * amount on one iteration signals doneness. On output, p is set to the
         * best point found, xi is the then current direction set, fret is the
         * returned function value at p, and iter is the number of iterations
         * taken. The method linmin is used.
         * 
         * @param p initial starting point @param n number of variables in the
         * function @param ftol the fractional tolerance in the function value
         * such that failure to decrease by more than this amount on one
         * iteration signals doneness @param ibig @param del @param fp @param
         * fptt @param t
         */

        int ibig;
        double del, fp, fptt, t;
        pt = new double[n + 1];
        double[] ptt = new double[n + 1];
        xit = new double[n + 1];
        fptt = 0.0;
        fret = (fObj(p));
        //save the initial point
        for (int j = 1; j <= n; j++)
            pt[j] = p[j];
        for (iter = 1; ; ++iter) {
            //	    System.out.println("Iter: " + iter);
            //	    System.out.println();
            //	    System.out.println();
            //	    System.out.println();
            //	    System.out.println();

            fp = fret;
            ibig = 0;

            //will be the biggest function decrease
            del = 0.0;

            //In each iteration loop over all directions in the set
            for (int i = 1; i <= n; i++) {
                //Copy the direction
                for (int j = 1; j <= n; j++)
                    xit[j] = xi[j][i];
                fptt = fret;

                //Minimise along it.
                linmin(p, xit, n);

                //		    	for(int k=0;k<xit.length;k++)
                //			{
                //				System.out.print("xit after 1st linmin call: " + xit[k]);
                //			    System.out.println();
                //			 }

                //		    System.out.println("Fret: " + fret);
                //and record it if it is the largest decrease so far.

                if (Math.abs(fptt - fret) > del) {
                    del = Math.abs(fptt - fret);
                    ibig = i;
                }
            }
            //Termination criteria

            if (2.0 * Math.abs(fp - fret) <= (ftol * (Math.abs(fp) + Math.abs(fret)) + TINY)) {
                return;
            }

            if (iter == ITMAX) {
                throw new PowellMinimiserException("Powell exceeding maximum iterations.");

            }

            /**
             * Construct the extrapolated point and the average direction moved.
             * Save the old starting point.
             */

            for (int j = 1; j <= n; j++) {
                ptt[j] = 2.0 * p[j] - pt[j];
                xit[j] = p[j] - pt[j]; // toto zde chybelo!
                pt[j] = p[j];
            }

            fptt = fObj(ptt);
            if (fptt < fp) {
                t = 2.0 * (fp - 2.0 * fret + fptt)
                        * ((fp - fret - del) * (fp - fret - del))
                        //* ((fp - fptt) * (fp - fptt)); // toto neni spravne prepsano
                        - del * ((fp - fptt) * (fp - fptt)); // spravne dle "Numerical Recipes in C"
                if (t < 0.0) {

                    /*
                     * Move to the minimum of the new direction. Save the new
                     * direction.
                     */
                    linmin(p, xit, n);

                    //CHECK xit
                    //			for(int i=0;i<xit.length;i++)
                    //			{
                    //				System.out.print("xit: " + xit[i]);
                    //			    System.out.println();
                    //			}

                    for (int j = 1; j <= n; j++) {
                        xi[j][ibig] = xi[j][n];
                        xi[j][n] = xit[j];
                    }
                }

            }

        }
    }

    /**
     * Given an n-dimensional point p[1..n] and an n-dimensional direction
     * xi[1..n] moves and resets p to the function objfunc(p) takes on a minimum
     * along the direction xi from p, and replaces xi by the actual vector
     * displacement that p was moved. Also returns as fret the value of objfunc
     * at the returned loaction p. This is actually all accomplished by calling
     * the methods <code> mnbrak</code> and <code> brent</code>.
     *
     * @param p
     * @param xit
     * @param n
     */

    private void linmin(double[] p, double[] xit, int n) throws PowellMinimiserException {
        //	    System.out.println("Starting linmin");
        //	    for(int i=1;i<p.length;i++)
        //	    {
        //		    System.out.print("p values: " + p[i]);
        //		    System.out.println();
        //	    }
        //	    for(int i=1;i<xi.length;i++)
        //	    {
        //		for(int j=1; j< xi.length; j++)
        //		    System.out.print(xi[i][j] +" ");
        //		System.out.println();
        //	    }
        //linmin global initialisations
        pcom = new double[n + 1];
        xicom = new double[n + 1];
        ncom = n;
        for (int j = 1; j <= n; j++) {
            pcom[j] = p[j];
            xicom[j] = xit[j];
        }
        //Initial guess for brackets
        ax = 0.0;
        bx = 1.0;
        mnbrak();
        fret = brent();
        //Construct the array results to return.
        for (int j = 1; j <= n; j++) {
            xit[j] *= xmin;
            p[j] += xit[j];
        }
        //		for(int i=0;i<p.length;i++)
        //		{
        //			System.out.print(p[i] + " ");
        //			System.out.println();
        //		}
        //		for(int i=1;i<xi.length;i++)
        //		{
        //		       for(int j=1; j< xi.length; j++)
        //		       System.out.print(xi[i][j]+ " ");
        //		       System.out.println();
        //		}
    }

    /*
     * Given a function f and given a bracketing triplet of abscissas ax, bx and
     * cx (such that bx is between ax and cx) and f(bx) is less than both f(ax)
     * and f(cx), this method isolates the minimum to a fractional precision of
     * about tol using Brent's method. The abscissa of the minimum is returned
     * as xmin, and the minimum function value is returned as brent, the
     * returned function value.
     */
    private double brent() throws PowellMinimiserException

    {
        //	    System.out.println("Starting brent");

        int iter;
        double a, b, etemp, fv, fw, fx, p, q, r, tol1, tol2, v, w, x, xm;
        //This will be the distance moved on the step before last
        double e = 0.0;
        double d = 0.0;
        double u;
        double fu;

        /**
         * a and b must be in ascending order but input abscissas need not be
         */
        a = (ax < cx ? ax : cx);
        b = (ax > cx ? ax : cx);

        //Initialisations
        x = w = v = bx;
        fw = fv = fx = f1dim(x);

        //Main program loop
        for (iter = 1; iter <= BRENTITMAX; iter++) {
            xm = 0.5 * (a + b);
            tol2 = 2.0 * (tol1 = BRENTTOL * Math.abs(x) + ZEPS);

            //Test for done here
            if (Math.abs(x - xm) <= (tol2 - 0.5 * (b - a))) {
                xmin = x;
                return fx;
            }

            //Construct a trial parabolic fit.
            if (Math.abs(e) > tol1) {
                r = (x - w) * (fx - fv);
                q = (x - v) * (fx - fw);
                p = (x - v) * q - (x - w) * r;
                q = (float) 2.0 * (q - r);
                if (q > 0.0) p = -p;
                q = Math.abs(q);
                etemp = e;
                e = d;

                /**
                 * Following conditions determine the acceptibility of the
                 * paraboli fit. We take the golden section step into the larger
                 * of the two segments.
                 */
                if (Math.abs(p) >= Math.abs(0.5 * q * etemp) || p <= q * (a - x)
                        || p >= q * (b - x))
                    d = CGOLD * (e = (x >= xm ? a - x : b - x));
                else {
                    //Take the parabolic step.
                    d = p / q;
                    u = x + d;
                    if (u - a < tol2 || b - u < tol2) d = sign(tol1, xm - x);
                }
            } else {
                d = CGOLD * (e = (x >= xm ? a - x : b - x));
            }
            u = (Math.abs(d) >= tol1 ? x + d : x + sign(tol1, d));
            //This is the one function evaluation per iteration.
            fu = f1dim(u);

            //Now decide what to do with function evaluation.
            if (fu <= fx) {
                if (u >= x)
                    a = x;
                else
                    b = x;

                //Housekeeping
                v = w;
                w = x;
                x = u;
                fv = fw;
                fw = fx;
                fx = fu;

            } else {
                if (u < x)
                    a = u;
                else
                    b = u;
                if (fu <= fw || w == x) {
                    v = w;
                    w = u;
                    fv = fw;
                    fw = fu;
                } else if (fu <= fv || v == x || v == w) {
                    v = u;
                    fv = fu;
                }
            }
        }
        //Back for another iteration
        if (iter >= BRENTITMAX) {
            throw new PowellMinimiserException("Too many iterations in brent");
        }
        //Never get here
        xmin = x;
        return fx;
    }

    private double f1dim(double x) {
        //System.out.println("Starting F1dim");

        double[] xt = new double[ncom + 1];
        for (int j = 1; j <= ncom; j++)
            xt[j] = pcom[j] + x * xicom[j];
        f = fObj(xt);
        return f;
    }

    /**
     * Given a function objfunc and given distinct initial points ax and bx,
     * this method searches in the downhill direction (defined by the function
     * as evaluated at the initial points) and returns new points ax, bx, cx
     * that bracket a minimum of the function. Also returned are the function
     * values at the three points, fa, fb and fc.
     *
     * @param
     * @param
     */

    private void mnbrak() {
        //	    System.out.println("Starting mnbrak");
        double r, q, dum, temp;
        double u;
        double fu = 0;
        double ulim;
        fa = f1dim(ax);
        fb = f1dim(bx);

        //Switches roles of a and b
        //to go downhill in the direction of a and b
        if (fb > fa) {
            dum = ax;
            ax = bx;
            bx = dum;
            dum = fb;
            fb = fa;
            fa = dum;
        }
        //First guess for c
        cx = bx + GOLD * (bx - ax);
        //Keep returning here until we bracket
        while (fb > fc) {
            //Compute u by parabolic extrapolation from a,b,c.
            //TINY is used to prevent any possible division by zero.
            r = (bx - ax) * (fb - fc);
            q = (bx - cx) * (fb - fa);
            u = (bx) - ((bx - cx) * q - (bx - ax) * r)
                    / (2.0 * sign(Math.max(Math.abs(q - r), TINY), q - r));
            ulim = bx + GLIMIT * (cx - bx);

            //Test various possibilities

            //Parabolic u is between b and c
            if ((bx - u) * (u - cx) > 0.0) {
                fu = f1dim(u);

                //Got a minimum between b and c.
                if (fu < fc) {
                    ax = bx;
                    bx = u;
                    fa = fb;
                    fb = fu;
                    return;
                }
                //Parabolic fit was no use.
                //Use default magnification.
                u = cx + GOLD * (cx - bx);
                fu = f1dim(u);
            }
            //Parabolic fit is between c and its allowed limit
            else if ((cx - u) * (u - ulim) > 0.0) {
                fu = f1dim(u);
                if (fu < fc) {
                    bx = cx;
                    cx = u;
                    u = cx + GOLD * (cx - bx);
                    fb = fc;
                    fc = fu;
                    fu = f1dim(u);
                }

                //Limit parabolic u to maximum allowed value.
                else if ((u - ulim) * (ulim - cx) >= 0.0) {
                    u = ulim;
                    fu = f1dim(u);
                }
                //Reject parabolic u, use default magnification
                else {
                    u = cx + GOLD * (cx - bx);
                    fu = f1dim(u);
                }
            }
            //Eliminate oldest point and continue
            ax = bx;
            bx = cx;
            cx = u;
            fa = fb;
            fb = fc;
            fc = fu;
        }
    }

    //magnitude of a times sign of b

    private double sign(double a, double b) {
        //System.out.println("Starting sign");
        return ((b) >= 0.0 ? Math.abs(a) : -Math.abs(a));
    }

}

