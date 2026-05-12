package gp.decisiontree;
import gp.engine.Tree;
import util.DataLoader;
import util.Metrics;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;




public class DTTester
{

    public static void main(String[] args)
    {

        Scanner sc = new Scanner(System.in);
        System.out.println("++============ Decision Tree GP Tester =============+");

        String modelPath = promptString(sc, "Enter path to saved model file : ");
        String testPath = promptString(sc, "Enter path to test data CSV: ");

        
        /// load model
        Tree tree;
        try
        {
            String sExpr = new String(Files.readAllBytes(Paths.get(modelPath))).trim();
            tree = Tree.fromSExpression(sExpr);
        } 
        catch (IOException e)
        {
            System.err.println("ERROR: Cannot read model file '" + modelPath + "': " + e.getMessage());
            System.exit(1);
            return;
        } 
        catch (Exception e)
        {
            System.err.println("ERROR: Cannot parse model file '" + modelPath + "': " + e.getMessage());
            System.exit(1);
            return;
        }


        /// load test data 
        List<int[]> testData;
        try
        {
            testData = DataLoader.load(testPath);
        } 
        catch (Exception e) 
        {
            System.err.println("ERROR: Cannot load test data '" + testPath + "': " + e.getMessage());
            System.exit(1);
            return;
        }
        System.out.println("Loaded " + testData.size() + " test instances.");

        

        //// classify 
        long startTime = System.currentTimeMillis();

        DTClassifier clf = new DTClassifier(tree);
        int[] predictions = clf.classifyAll(testData);

        long elapsed = System.currentTimeMillis() - startTime;


        //// true labels
        int[] trueLabels = new int[testData.size()];
        for (int i = 0; i < testData.size(); i++) {
            trueLabels[i] = DataLoader.getLabel(testData.get(i));
        }


        ////// metrics
        double[] metrics = Metrics.compute(predictions, trueLabels);

        System.out.printf("Test Accuracy : %.2f%%%n", metrics[0] * 100);
        System.out.printf("F-measure     : %.4f%n",   metrics[1]);
        System.out.printf("Runtime       : %d ms%n",  elapsed);

        sc.close();
    }


    private static String promptString(Scanner sc, String msg)
    {
        System.out.print(msg);
        return sc.next();
    }
}
