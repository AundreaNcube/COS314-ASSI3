package util;

public class Metrics
{

     // NOTE:
     // positive class = 1 (recurrence-events) AND negative class = 0 (no-recurrence-events)



    ///////////{ compute accuracy and f-measure from parallel prediction/label arrays }\\\\\\\\\\\\\\\\
    //212 or 353 
     /*                     Predicted
                    +----------+----------+
                    | Positive | Negative |
                ---------+----------+----------+
           Positive |    TP    |    FN    |
         Actua   ---------+----------+----------+
           Negative |    FP    |    TN    |
                  +----------+----------+
     */
     
    public static double[] compute(int[] predicted, int[] actual)
    {
        if (predicted == null || actual == null || predicted.length != actual.length)
        {
            int pLen = predicted == null ? -1 : predicted.length;
            int aLen = actual == null ? -1 : actual.length;

            throw new IllegalArgumentException( "predicted.length=" + pLen + " != actual.length=" + aLen);
        }

        if (predicted.length == 0) 
        {
            throw new IllegalArgumentException("arrays must not be empty.");
        }


        int tp = 0, fp = 0, fn = 0, tn = 0;
        for (int i = 0; i < predicted.length; i++)
        {
            int p = predicted[i];
            int a = actual[i];
            if (p == 1 && a == 1) tp++;
            else if (p == 1 && a == 0) fp++;
            else if (p == 0 && a == 1) fn++;
            else tn++;
        }


      
        // (: 
        double accuracy  = (double)(tp + tn) / predicted.length;

        double precision = (tp + fp) == 0 ? 0.0 : (double) tp / (tp + fp);

        double recall = (tp + fn) == 0 ? 0.0 : (double) tp / (tp + fn);

        double f1 = (precision + recall) == 0.0 ? 0.0 : (2.0 * precision * recall) / (precision + recall);

        return new double[]{accuracy, f1};
    }


    ///////{ return accuracy and f1 measure }\\\\\\\\\\\\
    public static double accuracy(int[] predicted, int[] actual)
    {
        return compute(predicted, actual)[0];
    }

    public static double f1(int[] predicted, int[] actual) 
    {
        return compute(predicted, actual)[1];
    }
    
}
