package game.trainers;

import game.trainers.gradient.Newton.Uncmin_f77;
import game.trainers.gradient.Newton.Uncmin_methods;
import game.utils.GlobalRandom;
import game.utils.MyRandom;
import configuration.game.trainers.QuasiNewtonConfig;

/**
 * <p>Title: </p>
 * <p/>
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class QuasiNewtonTrainer extends Trainer implements Uncmin_methods {
    private transient double[] na; // new coefficients
    private transient double[] besta;
    private transient double[] f;
    private transient double[] g;
    private transient double[][] aa;
    private transient double[] udiag;
    private transient int[] msg;
    private transient double[] typsiz;
    private transient double[] dlt;
    private transient double[] fscale;
    private transient double[] stepmx;
    private transient int[] ndigit;
    private transient int[] method;
    private transient int[] iexp;
    private transient int[] itnlim;
    private transient int[] iagflg;
    private transient int[] iahflg;
    private transient double[] gradtl;
    private transient double[] steptl;
    private transient int[] info;

    private transient double[] xn;
    private transient double[] gn;
    private transient double[][] hn;

    double lastError = -1;
    double firstError = -1;
    int rec;
    int draw;
    boolean forceHessian;

    public QuasiNewtonTrainer() {
        dlt = new double[2];
        fscale = new double[2];
        stepmx = new double[2];
        ndigit = new int[2];
        method = new int[2];
        iexp = new int[2];
        itnlim = new int[2];
        iagflg = new int[2];
        iahflg = new int[2];
        gradtl = new double[2];
        steptl = new double[2];
        msg = new int[2];

        // SET TOLERANCES
        double epsm = 1.12e-16;
        dlt[1] = -1.0;
        gradtl[1] = Math.pow(epsm, 1.0 / 3.0);
        steptl[1] = Math.sqrt(epsm);
        stepmx[1] = 0.0;

        // SET FLAGS
        method[1] = 2; // 1=line search, 2=double dogleg, 3=More-Hebdon
        fscale[1] = 1.0;
        msg[1] = 0;
        ndigit[1] = -1;
        itnlim[1] = 150; //iteration limit
    }

    public void init(GradientTrainable uni, Object cfg) {
        super.init(uni, cfg);
        QuasiNewtonConfig cf = (QuasiNewtonConfig) cfg;

        draw = cf.getDraw();
        rec = cf.getRec();
        forceHessian = cf.isForceAnalyticHessian();
    }

    public void setCoef(int coef) {
        super.setCoef(coef);

        xn = new double[coefficients];
        gn = new double[coefficients];
        //hn = new double[coefficients][coefficients];  //allocate memory for util fields
        hn = new double[0][0];

        na = new double[coef + 1]; // new coefficients
        besta = new double[coef + 1];
        f = new double[coef + 1];
        g = new double[coef + 1];
        aa = new double[coef + 1][coef + 1];
        udiag = new double[coef + 1];
        info = new int[coef + 1];
        typsiz = new double[coef + 1];
        MyRandom rnd = GlobalRandom.getInstance();
        for (int i = 0; i < coef + 1; i++)
            na[i] = rnd.getSmallDouble();// generate new random coefficients

        // SET TYPICAL SIZE OF X AND MINIMIZATION FUNCTION
        for (int i = 1; i <= coef; i++) {
            typsiz[i] = 1.0;
        }

        double[] x = new double[coef + 1];
        double[] g = new double[coef + 1];
        //TODO do better testing for hessian !
        //double[][] h = new double[coef + 1][coef + 1];
        double[][] h = new double[0][0];

        if (!unit.gradient(x, g)) {
            iagflg[1] = 0; //disable gradient - nothing changed
        } else {
            iagflg[1] = 1; //gradient supplied, enable it
        }

        if (forceHessian && unit.hessian(x, h)) {
            iexp[1] = 0;
            iahflg[1] = 1; //hessian matrix IS supplied and analytic hessian FORCED by option
            System.out.println("analytic hessian enabled");
        } else {
            iexp[1] = 1;
            iahflg[1] = 0; //hessian matrix NOT supplied
        }
    }

    /**
     * starts the teaching process
     */
    public void teach() {
        //optimization.Uncmin_f77.optif0_f77(coefficients, na, this, besta, f, g,
        //                                   info, aa, udiag);
        if (startingPoint != null)
            System.arraycopy(startingPoint, 0, na, 1, coefficients);

        Uncmin_f77.optif9_f77(coefficients, na, this, typsiz, fscale, method, iexp, msg, ndigit, itnlim, iagflg, iahflg, dlt, gradtl, stepmx, steptl, besta, f, g, info, aa, udiag);

/*
    public static void optif9_f77(int n, double x[], Uncmin_methods minclass,
                                double typsiz[], double fscale[], int method[],
                                int iexp[], int msg[], int ndigit[], int itnlim[],
                                int iagflg[], int iahflg[], double dlt[],
                                double gradtl[], double stepmx[], double steptl[],
                                double xpls[], double fpls[], double gpls[],
                                int itrmcd[], double a[][], double udiag[]) {



Here is a copy of the optif9 FORTRAN documentation:

     SUBROUTINE OPTIF9(NR,N,X,FCN,D1FCN,D2FCN,TYPSIZ,FSCALE,
    +     METHOD,IEXP,MSG,NDIGIT,ITNLIM,IAGFLG,IAHFLG,IPR,
    +     DLT,GRADTL,STEPMX,STEPTL,
    +     XPLS,FPLS,GPLS,ITRMCD,A,WRK)
c
     implicit double precision (a-h,o-z)
c
C
C PURPOSE
C -------
C PROVIDE COMPLETE INTERFACE TO MINIMIZATION PACKAGE.
C USER HAS FULL CONTROL OVER OPTIONS.
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C X(N)         --> ON ENTRY: ESTIMATE TO A ROOT OF FCN
C FCN          --> NAME OF SUBROUTINE TO EVALUATE OPTIMIZATION FUNCTION
C                  MUST BE DECLARED EXTERNAL IN CALLING ROUTINE
C                            FCN: R(N) --> R(1)
C D1FCN        --> (OPTIONAL) NAME OF SUBROUTINE TO EVALUATE GRADIENT
C                  OF FCN.  MUST BE DECLARED EXTERNAL IN CALLING ROUTINE
C D2FCN        --> (OPTIONAL) NAME OF SUBROUTINE TO EVALUATE HESSIAN OF
C                  OF FCN.  MUST BE DECLARED EXTERNAL IN CALLING ROUTINE
C TYPSIZ(N)    --> TYPICAL SIZE FOR EACH COMPONENT OF X
C FSCALE       --> ESTIMATE OF SCALE OF OBJECTIVE FUNCTION
C METHOD       --> ALGORITHM TO USE TO SOLVE MINIMIZATION PROBLEM
C                    =1 LINE SEARCH
C                    =2 DOUBLE DOGLEG
C                    =3 MORE-HEBDON
C IEXP         --> =1 IF OPTIMIZATION FUNCTION FCN IS EXPENSIVE TO
C                  EVALUATE, =0 OTHERWISE.  IF SET THEN HESSIAN WILL
C                  BE EVALUATED BY SECANT UPDATE INSTEAD OF
C                  ANALYTICALLY OR BY FINITE DIFFERENCES
C MSG         <--> ON INPUT:  (.GT.0) MESSAGE TO INHIBIT CERTAIN
C                    AUTOMATIC CHECKS
C                  ON OUTPUT: (.LT.0) ERROR CODE; =0 NO ERROR
C NDIGIT       --> NUMBER OF GOOD DIGITS IN OPTIMIZATION FUNCTION FCN
C ITNLIM       --> MAXIMUM NUMBER OF ALLOWABLE ITERATIONS
C IAGFLG       --> =1 IF ANALYTIC GRADIENT SUPPLIED
C IAHFLG       --> =1 IF ANALYTIC HESSIAN SUPPLIED
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C DLT          --> TRUST REGION RADIUS
C GRADTL       --> TOLERANCE AT WHICH GRADIENT CONSIDERED CLOSE
C                  ENOUGH TO ZERO TO TERMINATE ALGORITHM
C STEPMX       --> MAXIMUM ALLOWABLE STEP SIZE
C STEPTL       --> RELATIVE STEP SIZE AT WHICH SUCCESSIVE ITERATES
C                  CONSIDERED CLOSE ENOUGH TO TERMINATE ALGORITHM
C XPLS(N)     <--> ON EXIT:  XPLS IS LOCAL MINIMUM
C FPLS        <--> ON EXIT:  FUNCTION VALUE AT SOLUTION, XPLS
C GPLS(N)     <--> ON EXIT:  GRADIENT AT SOLUTION XPLS
C ITRMCD      <--  TERMINATION CODE
C A(N,N)       --> WORKSPACE FOR HESSIAN (OR ESTIMATE)
C                  AND ITS CHOLESKY DECOMPOSITION
C WRK(N,8)     --> WORKSPACE
C

*/

//System.out.println(Integer.toString(cnt)+" "+Integer.toString(cntg));
    }

    /**
     * returns the name of the algorithm used for weights(coeffs.) estimation
     */
    public String getMethodName() {
        return "Quasi Newton Method";
    }

    public double f_to_minimize(double[] x) {
        System.arraycopy(x, 1, xn, 0, coefficients);
        return getAndRecordError(xn, rec, draw, true);
    }

    /**
     * no config class bbb
     */
    public Class getConfigClass() {
        return QuasiNewtonConfig.class;
    }

    public void gradient(double[] x, double[] g) {
        for (int i = 0; i < coefficients; i++) {
            xn[i] = x[i + 1];
            gn[i] = g[i + 1];
        } // correct indexes (starting from zero)
        unit.gradient(xn, gn);
        for (int i = 0; i < coefficients; i++) {
            x[i + 1] = xn[i];
            g[i + 1] = gn[i];
        } // putting back indexes (starting from 1) - fortran notation


        //       System.out.println("asking for gradient");
        //    double[][] hn = new double[coefficients][coefficients];
        //    unit.hessian(x, hn);

        //TODO REMOVE


    }

    public void hessian(double[] x, double[][] h) {
        System.out.println("asking for hessian");
        for (int i = 0; i < coefficients; i++) {
            xn[i] = x[i + 1];
            System.arraycopy(h[i + 1], 1, hn[i], 0, coefficients);
        } // correct indexes (starting from zero)

        unit.hessian(xn, hn);
        for (int i = 0; i < coefficients; i++) {
            x[i + 1] = xn[i];
            System.arraycopy(hn[i], 0, h[i + 1], 1, coefficients);
        } // putting back indexes (starting from 1) - fortran notation

    }

    public boolean allowedByDefault() {
        return true;
    }

    /**
     * added for multiprocessor support
     * by jakub spirk spirk.jakub@gmail.com
     * 05. 2008
     */
    public boolean isExecutableInParallelMode() {
        return true;
    }
}
