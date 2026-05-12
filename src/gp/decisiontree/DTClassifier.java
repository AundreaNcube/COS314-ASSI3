package gp.decisiontree;
import gp.engine.Node;
import gp.engine.Tree;


 /*
  Classification rule:
  tree output != 0.0 class 1 - recurrence-events
  tree output == 0.0 class 0 - no-recurrence-events
 */

public class DTClassifier
{

    private final Tree tree;


    public DTClassifier(Tree tree)
    {
        if (tree == null) throw new IllegalArgumentException("tree cannot be null.");
        this.tree = tree;
    }


    public Tree getTree() { return tree;}


    
    ///////////{ classify a single instance }\\\\\\\\\\\\\
    public int classify(double[] featVec)
    {
        double output = evaluateNode(tree.getRoot(), featVec);
        return output != 0.0 ? 1 : 0;
    }

    

    ////////{ classify all instances in a dataset }\\\\\\\\\\\
    public int[] classifyAll(java.util.List<int[]> data)
    {
        int[] predictions = new int[data.size()];

        for (int i = 0; i < data.size(); i++) 
        {
            double[] fv = util.DataLoader.toFeatureVector(data.get(i));
            predictions[i] = classify(fv);
        }

        return predictions;
    }



    //////// { tree evaluation } \\\\\\\\\\\ 
    private double evaluateNode(Node node, double[] fv)
    {
        if (node.getType() == Node.NodeType.TERMINAL)
        {
            return DTFunctionSet.evaluateTerminal(node.getLabel(), fv);
        }

        ////eval all children first
        double[] childVals = new double[node.getArity()];
        for (int i = 0; i < node.getArity(); i++)
        {
            childVals[i] = evaluateNode(node.getChild(i), fv);
        }

        return DTFunctionSet.evaluate(node.getLabel(), childVals);
    }
}
