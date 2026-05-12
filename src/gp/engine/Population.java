public class Population {
    public Population(int size, int maxDepth, long seed, String[] funcLabels, int[] funcArities,String[] termLabels, String type) { ... }
    public Tree[] getIndividuals() { ... }
    public void evolve(double[] fitnesses, double pc,
                       double pm, int tournamentSize,
                       int maxOffDepth, int maxMutDepth,
                       Tree eliteIndividual,
                       String[] funcLabels, int[] funcArities,
                       String[] termLabels) { ... }
}