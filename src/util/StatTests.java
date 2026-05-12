package util;
import java.util.Arrays;


public class StatTests
{

    private static final int NUM_RUNS = 30;
    private static final double ALPHA = 0.05;


    //////////{EITHER TTEST OR WILCOXON}\\\\\\\\\\\\\

    public static void run(double[] arith, double[] dt, String type)
    {
        validateInputs(arith, dt, type);

        if (type.equalsIgnoreCase("ttest"))
        {
            runTTest(arith, dt);
        } 
        else 
        {
            runWilcoxon(arith, dt);
        }

    }



    /////////////___________paired t-test_________________\\\\\\\\\\\\\

    private static void runTTest(double[] arith, double[] dt)
    {
        double[] d = differences(arith, dt);
        double mean = mean(d);
        double std = stddev(d, mean);

        double tStat, pValue;

        if (std == 0.0)
        {
            tStat  = 0.0;
            pValue = 1.0;
        } 
        else 
        {
            tStat  = mean / (std / Math.sqrt(NUM_RUNS));
            pValue = twoTailedPValueT(tStat, NUM_RUNS - 1);
        }


        System.out.println("+========Paired T-Test ==========+");
        System.out.printf("t-statistic : %.4f%n", tStat);
        System.out.printf("p-value     : %.4f%n", pValue);
        printConclusion(pValue);
    }




    
    /////////________________wicoxon signed-rank test_________________\\\\\\\\\\\
    

    private static void runWilcoxon(double[] arith, double[] dt)
    {

        double[] d = differences(arith, dt);

        // take out zero differences
        int nonZero = 0;
        for (double v : d) if (v != 0.0) nonZero++;

        if (nonZero == 0)
        {
            System.out.println("~====== Wilcoxon Signed-Rank Test ========~");
            System.out.printf("W-statistic : 0.0000%n");
            System.out.printf("p-value     : 1.0000%n");
            printConclusion(1.0);
            return;
        }

        // theb need to build array of |d_i|, sign  for non-zero differences
        double[] absDiffs = new double[nonZero];
        int[] signs = new int[nonZero];
        int idx = 0;

        for (double v : d)
        {
            if (v != 0.0) 
            {
                absDiffs[idx] = Math.abs(v);
                signs[idx] = v > 0 ? 1 : -1;
                idx++;
            }
        }


        // *rank the absolute diffs and average ranks for ties

        double[] ranks = rank(absDiffs);
        double wPlus  = 0.0, wMinus = 0.0;

        for (int i = 0; i < nonZero; i++) 
        {
            if (signs[i] > 0)
            {
                 wPlus  += ranks[i];
            }
            else
            { 
                 wMinus += ranks[i];
            }
        
        }

        double wStat  = Math.min(wPlus, wMinus);

        double pValue = wilcoxonPValue(wStat, nonZero);

        System.out.println("+======= Wilcoxon Signed-Rank Test ========+");
        System.out.printf("W-statistic : %.4f%n", wStat);
        System.out.printf("p-value     : %.4f%n", pValue);
        printConclusion(pValue);
    }




    ///////////////____________Helpers_______________\\\\\\\\\\\\\\\\\\\

    private static double[] differences(double[] a, double[] b)
    {
        double[] d = new double[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) d[i] = a[i] - b[i];
        return d;
    }


    private static double mean(double[] d)
    {
        double sum = 0;
        for (double v : d) sum += v;
        return sum / d.length;
    }


    private static double stddev(double[] d, double mean)
    {
        double sum = 0;
        for (double v : d) sum += (v - mean) * (v - mean);
        return Math.sqrt(sum / (d.length - 1));   // sample std dev
    }




    private static double[] rank(double[] absDiffs)
    {
        int n = absDiffs.length;

        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Double.compare(absDiffs[a], absDiffs[b]));  //sorted by absDiffs


        double[] ranks = new double[n];
        int i = 0;

        while (i < n)
        {
            int j = i;
            //find end of tie group
            while (j < n && absDiffs[idx[j]] == absDiffs[idx[i]]) j++;
            double avgRank = (i + 1 + j) / 2.0;   // average of 1-based ranks
            for (int k = i; k < j; k++) ranks[idx[k]] = avgRank;
            i = j;
        }

        return ranks;
    }



     ////////::::: Two-tailed p-value for t-distribution using a numerical approximation
    private static double twoTailedPValueT(double t, int df)
    {
        double x = df / (df + t * t);
        double p = incompleteBeta(x, df / 2.0, 0.5);
        return Math.min(1.0, p);   //regularised incomplete beta
    }


    ///basically used for the t-distribution CDF.
    private static double incompleteBeta(double x, double a, double b)
    {
        if (x < 0 || x > 1) return Double.NaN;
        if (x == 0) return 0;
        if (x == 1) return 1;

        // Use symmetry relation when x > (a+1)/(a+b+2)

        if (x > (a + 1) / (a + b + 2))
        {
            return 1.0 - incompleteBeta(1 - x, b, a);
        }

        double lbeta = lgamma(a + b) - lgamma(a) - lgamma(b);
        double front = Math.exp(lbeta + a * Math.log(x) + b * Math.log(1 - x)) / a;

    
        double cf = continuedFraction(x, a, b); //lentz continuatrion
        return front * cf;
    }



    private static double continuedFraction(double x, double a, double b)
    {
        final int MAX_ITER = 200;
        final double EPS = 1e-10;


        double c = 1.0, d = 1.0 - (a + b) * x / (a + 1);

        if (Math.abs(d) < 1e-30) d = 1e-30;
        d = 1.0 / d;
        double h = d;


        for (int m = 1; m <= MAX_ITER; m++)
        {
            ///// even 
            double aa = m * (b - m) * x / ((a + 2 * m - 1) * (a + 2 * m));

            d = 1.0 + aa * d;
            if (Math.abs(d) < 1e-30) d = 1e-30;

            c = 1.0 + aa / c;
            if (Math.abs(c) < 1e-30) c = 1e-30;

            d = 1.0 / d;
            h *= d * c;

            ////// odd 
            aa = -(a + m) * (a + b + m) * x / ((a + 2 * m) * (a + 2 * m + 1));

            d = 1.0 + aa * d;
            if (Math.abs(d) < 1e-30) d = 1e-30;

            c = 1.0 + aa / c;
            if (Math.abs(c) < 1e-30) c = 1e-30;

            d = 1.0 / d;
            double delta = d * c;
            h *= delta;

            if (Math.abs(delta - 1.0) < EPS) break;
        }
        return h;
    }


    /////// consider : Stirling-series log-gamma approximation (accurate for a > 0)

    private static double lgamma(double a)
    {
        ///***** lanczos approximation (g=7, n=9)
        double[] c =
        {
            0.99999999999980993,  676.5203681218851,   -1259.1392167224028,
            771.32342877765313,  -176.61502916214059,   12.507343278686905,
           -0.13857109526572012,   9.9843695780195716e-6, 1.5056327351493116e-7
        };

        if (a < 0.5) return Math.log(Math.PI / Math.sin(Math.PI * a)) - lgamma(1 - a);
        a -= 1;

        double x = c[0];

        for (int i = 1; i < 9; i++) x += c[i] / (a + i);

        double t = a + 7.5;


        return 0.5 * Math.log(2 * Math.PI) + (a + 0.5) * Math.log(t) - t + Math.log(x);
    }




    /// same as ass 2 approcimating to normal approximation

    private static double wilcoxonPValue(double w, int n)
    {
        double mean = n * (n + 1) / 4.0;
        double var  = n * (n + 1) * (2 * n + 1) / 24.0;
        double z    = (w - mean) / Math.sqrt(var);


        // Two-tailed p-value from standard normal
        return 2.0 * normalCDF(-Math.abs(z));
    }


    private static double normalCDF(double z)
    {
        return 0.5 * (1.0 + erf(z / Math.sqrt(2)));
    }


    /** Error function approximation */
    private static double erf(double x)
    {
        double t = 1.0 / (1.0 + 0.3275911 * Math.abs(x));

        double poly = t * (0.254829592 + t * (-0.284496736 + t * (1.421413741
                   + t * (-1.453152027 + t * 1.061405429))));

        double result = 1.0 - poly * Math.exp(-x * x);
        return x >= 0 ? result : -result;
    }


    private static void printConclusion(double pValue)
    {
        if (pValue < ALPHA)
        {
            System.out.printf("Conclusion: Statistically significant difference (p < %.2f)%n", ALPHA);
        } 
        else
        {
            System.out.printf("Conclusion: No statistically significant difference (p >= %.2f)%n", ALPHA);
        }
    }



    private static void validateInputs(double[] arith, double[] dt, String type)
    {
        if (arith == null || arith.length != NUM_RUNS)
        {
            throw new IllegalArgumentException("arith array must have length " + NUM_RUNS + " but got " + (arith == null ? "null" : arith.length));
        }

        if (dt == null || dt.length != NUM_RUNS)
        {
            throw new IllegalArgumentException( "dt array must have length " + NUM_RUNS + " but got " + (dt == null ? "null" : dt.length));
        }

        if (!type.equalsIgnoreCase("ttest") && !type.equalsIgnoreCase("wilcoxon"))
        {
            throw new IllegalArgumentException("unknown test type '" + type + "'. only valid options: 'ttest', 'wilcoxon'.");
        }
    }
}
