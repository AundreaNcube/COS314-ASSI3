public class Node {
    public enum NodeType { FUNCTION, TERMINAL }
    public Node(NodeType type, String label, int arity) { ... }
    public NodeType getType() { ... }
    public String getLabel() { ... }
    public int getArity() { ... }
    public Node getChild(int i) { ... }
    public void setChild(int i, Node child) { ... }
}