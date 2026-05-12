package gp.decisiontree;
import gp.engine.Population;
import gp.engine.Tree;
import util.DataLoader;
import util.Metrics;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;



public class DTTrainer
{

    private static final int POP_SIZE = 200;
    private static final int MAX_GENS = 100;
    private static final int NUM_RUNS = 30;
    private static final String MODEL_FILE = "best_dt_model.txt";

    public static void main(String[] args)
    {

        Scanner sc = new Scanner(System.in);
        System.out.println("+++============= Decision Tree GP Trainer ==============+++");

        long sedd = promptLong  (sc, "Enter seed:  ");
        String trainPath = promptString(sc, "Enter training data file path:  ");
        double pc = promptDouble(sc, "Enter crossover probability:  ");
        double pm = promptDouble(sc, "Enter mutation probability:  ");
        int ts = promptInt(sc, "Enter tournament size:  ");
        int maxInitDepth = promptInt(sc, "Enter max initial tree depth:  ");
        int maxOffDepth  = promptInt(sc, "Enter max offspring depth:  ");
        int maxMutDepth = promptInt(sc, "Enter max mutation offspring depth:  ");


        /////{ load training data }\\\\\\\
        List<int[]> trainData;
        try
        {
            trainData = DataLoader.load(trainPath);
        } 
        catch (Exception e) 
        {
            System.err.println("ERROR loading training data: " + e.getMessage());
            System.exit(1);
            return;
        }

        System.out.println("Loaded " + trainData.size() + " training instances.\n");



        ///// true labels array reused***
        int[] trueLabels = new int[trainData.size()];

        for (int i = 0; i < trainData.size(); i++)
        {
            trueLabels[i] = DataLoader.getLabel(trainData.get(i));
        }


        
        Tree bestOverallTree = null;
        double bestOverallFitness = -1.0;
        int bestRunIndex = 0;
        long bestRunTime = 0;

        double[] runBestAccuracies = new double[NUM_RUNS];

        for (int run = 0; run < NUM_RUNS; run++)
        {
            long seed = sedd + run;
            System.out.println("--- Run " + (run + 1) + "/" + NUM_RUNS + "  (seed=" + seed + ") ---");

            long startTime = System.currentTimeMillis();

            
            ////(1) init population
            Population pop = new Population(POP_SIZE, maxInitDepth, seed,DTFunctionSet.FUNCTION_LABELS, 
            DTFunctionSet.FUNCTION_ARITIES, buildTerminalLabels(),"DT");

            Tree bestInRun = null;
            double bestFitnessInRun = -1.0;

            for (int gen = 1; gen <= MAX_GENS; gen++)
            {
                ////(2) eval fitness for every individual
                Tree[] individuals = pop.getIndividuals();
                double[] fitnesses = new double[POP_SIZE];
                double sumFitness = 0.0;

                for (int i = 0; i < POP_SIZE; i++)
                {
                    DTClassifier clf = new DTClassifier(individuals[i]);
                    int[] preds = clf.classifyAll(trainData);

                    fitnesses[i] = Metrics.accuracy(preds, trueLabels);
                    sumFitness += fitnesses[i];

                    if (fitnesses[i] > bestFitnessInRun)
                    {
                        bestFitnessInRun = fitnesses[i];
                        bestInRun = individuals[i].deepCopy();
                    }

                }

                double avgFitness  = sumFitness / POP_SIZE;
                double bestFitness = bestFitnessInRun;

                System.out.printf("Gen %3d | Best=%.4f | Avg=%.4f | %s%n",gen, bestFitness, avgFitness,
                    bestInRun != null ? bestInRun.toSExpression() : "null");

                ////////(3) evolve next generation i.e) elitism + selection + crossover + mutation
                pop.evolve(fitnesses,pc, pm,ts,
                    maxOffDepth,maxMutDepth, bestInRun,
                    DTFunctionSet.FUNCTION_LABELS, DTFunctionSet.FUNCTION_ARITIES,
                    buildTerminalLabels()
                );
            }

            long elapsed = System.currentTimeMillis() - startTime;
            runBestAccuracies[run] = bestFitnessInRun;

            ///// compute f-measure for curr run's best tree
            DTClassifier bestClf = new DTClassifier(bestInRun);
            int[] bestPreds = bestClf.classifyAll(trainData);
            double[] metrics = Metrics.compute(bestPreds, trueLabels);

            System.out.printf("Run %2d complete | TrainAcc=%.2f%% | F1=%.4f | Time=%dms%n%n", run + 1, metrics[0] * 100, metrics[1], elapsed);

            if (bestFitnessInRun > bestOverallFitness)
            {
                bestOverallFitness = bestFitnessInRun;
                bestOverallTree = bestInRun;
                bestRunIndex = run;
                bestRunTime = elapsed;
            }
        }


        ////////{:::::}\\\\\\\\

        System.out.println("~~===== Best Run: " + (bestRunIndex + 1) + "  (seed=" + (seed + bestRunIndex) + ") =====~~");

        DTClassifier finalClf = new DTClassifier(bestOverallTree);
        int[] finalPreds = finalClf.classifyAll(trainData);
        double[] finalMetrics = Metrics.compute(finalPreds, trueLabels);

        System.out.printf("Training Accuracy : %.2f%%%n", finalMetrics[0] * 100);
        System.out.printf("F-measure         : %.4f%n",   finalMetrics[1]);
        System.out.printf("Runtime           : %d ms%n",  bestRunTime);




        //// save best model
        try (PrintWriter pw = new PrintWriter(new FileWriter(MODEL_FILE)))
        {
            pw.println(bestOverallTree.toSExpression());
            System.out.println("Best model saved to: " + MODEL_FILE);
        } 
        catch (IOException e) 
        {
            System.err.println("ERROR saving model: " + e.getMessage());
        }

        sc.close();


    }



    ////////_________ Helpers _____________\\\\\\\\

    
    private static String[] buildTerminalLabels()
    {
        /// 9 feat terminals + 6 consts 
        String[] terminals = new String[9 + 6];
        for (int i = 0; i < 9; i++) terminals[i] = "f" + i;
        for (int i = 0; i < 6; i++) terminals[9 + i] = String.valueOf(i);
        return terminals;
    }

    private static long promptLong(Scanner sc, String msg)
    {
        System.out.print(msg);
        return sc.nextLong();
    }

    private static double promptDouble(Scanner sc, String msg)
    {
        System.out.print(msg);
        return sc.nextDouble();
    }

    private static int promptInt(Scanner sc, String msg)
    {
        System.out.print(msg);
        return sc.nextInt();
    }

    private static String promptString(Scanner sc, String msg)
    {
        System.out.print(msg);
        return sc.next();
    }
}
