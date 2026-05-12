package gp.decisiontree;
import gp.engine.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class DTFunctionSet
{


    //// node labels in function set
    public static final String[] FUNCTION_LABELS = {"AND", "OR", "NOT", "IF", "<=", ">"};

    ////// arities respectively 
    public static final int[] FUNCTION_ARITIES = {2,     2,    1,     3,    2,     2};

    //// vals drawn from [0, max]
    private static final int CONST_MAX = 5;

    /// f0 - 8
    public static final int NUM_FEATURES = 9;




    ////////////***FACTORY
  
    public static Node randomFunction(Random rand_generation)
    {
        int idx = rand_generation.nextInt(FUNCTION_LABELS.length);

        return new Node(Node.NodeType.FUNCTION, FUNCTION_LABELS[idx], FUNCTION_ARITIES[idx]);
    }



    ////*** to be used by point mutation to find replacement candidates
    public static List<String> functionsOfArity(int arity)
    {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < FUNCTION_LABELS.length; i++)
        {
            if (FUNCTION_ARITIES[i] == arity) result.add(FUNCTION_LABELS[i]);
        }

        return result;
    }

  
  

    public static Node randomTerminal(Random rand_generation)
    {
        
        int totalTerminals = NUM_FEATURES + (CONST_MAX + 1);

        int choice = rand_generation.nextInt(totalTerminals);

        if (choice < NUM_FEATURES)
        {
            //// feat terminal
            return new Node(Node.NodeType.TERMINAL, "f" + choice, 0);
        } 
        else
        {
            //// constant
            int constVal = choice - NUM_FEATURES;   

            return new Node(Node.NodeType.TERMINAL, String.valueOf(constVal), 0);
        }
    }



    
     ///// exclude the label
    public static Node randomTerminalExcluding(String excludeLabel, Random rand_generation) 
    {
        return randomTerminal(rand_generation);
    }





    ///////****** Evaluation



    public static double evaluate(String lbl, double[] children)
    {
        switch (lbl)
        {
            case "AND": return (children[0] != 0.0 && children[1] != 0.0) ? 1.0 : 0.0;
            case "OR":  return (children[0] != 0.0 || children[1] != 0.0) ? 1.0 : 0.0;
            case "NOT": return (children[0] == 0.0) ? 1.0 : 0.0;
            case "IF":  return (children[0] != 0.0) ? children[1] : children[2];
            case "<=":  return (children[0] <= children[1]) ? 1.0 : 0.0;
            case ">":   return (children[0] >  children[1]) ? 1.0 : 0.0;

            default:
                throw new IllegalStateException("[DTFunctionSet]  unrecognised function lbl '" + lbl + "'");
        }
    }

    
    public static double evaluateTerminal(String lbl, double[] featVec)
    {
        if (lbl.startsWith("f"))
        {
            int idx = Integer.parseInt(lbl.substring(1));

            if (idx < 0 || idx >= featVec.length) 
            {
                throw new IllegalArgumentException(
                    "Feature index " + idx + " out of range for vector of length " + featVec.length);
            }

            return featVec[idx];
        }
   
        return Double.parseDouble(lbl);
    }
}
