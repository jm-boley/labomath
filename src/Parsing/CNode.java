package Parsing;

import Runtime.API.InstructionBuilder;
import Runtime.API.DataType;
import Lexical.Token;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Instruction (code) generation tree node.
 * @author Joshua Boley
 */
public class CNode
{
    /**
     * Returns a node's i-th child subtree
     * @param node  Parent node
     * @param i     Child index
     * @return      Child subtree
     */
    public static CNode getChild (CNode node, int i)
    {
        CNode child = node.getFirstChild();
        for (int j = 0; j < i; ++j)
            child = child.getSibling();
        return child;
    }

    /**
     * Adds a subtree to a node
     * @param root       Root node that will contain new subtree
     * @param childToAdd Root node of subtree to add (child of new root)
     */
    @SuppressWarnings("empty-statement")
    public static void addChild (CNode root, CNode childToAdd)
    {
        if (numChildren (root) == 0) {
            root.setChild (childToAdd);
            return;
        }
        CNode child = root.getFirstChild();
        for (; child.getSibling() != null; child = child.getSibling());
        child.setSibling(childToAdd);
    }
    
    /**
     * Gets the number of subtrees reached from a node
     * @param node  Root node
     * @return      # of subtrees
     */
    public static int numChildren (CNode node)
    {
        if (node.getFirstChild() == null)
            return 0;

        int i = 1;
        CNode child = node.getFirstChild ();
        while ((child = child.getSibling()) != null)
            ++i;
        return i;
    }
    
    /**
     * Prints the instruction (code) tree beginning at root
     * @param root Root node
     */
    public static void printCodeTree(CNode root)
    {
        List<List<CNode>> levels = new ArrayList<>();
        List<CNode> rootl = new ArrayList<>();
        rootl.add(root);
        levels.add(rootl);

        // Restructure code tree for easy step-through by level
        boolean lowerLevelNodes;
        List<CNode> level = levels.get(0);
        do {
            lowerLevelNodes = false;
            List<CNode> lower = new ArrayList<>();
            for (CNode node : level) {
                if (node.getFirstChild() != null) {
                    lowerLevelNodes = true;
                    CNode child = node.getFirstChild();
                    do {
                        lower.add(child);
                    }
                    while ((child = child.getSibling()) != null);
                }
            }
            if (lowerLevelNodes) {
                levels.add(lower);
                level = levels.get(levels.size() - 1);
            }
        }
        while (lowerLevelNodes);

        // Print levels
        for (List<CNode> prntLevel : levels) {
            List<List<String>> levelOut = new ArrayList<>();

            // Build level output
            for (CNode node : prntLevel) {
                List<String> block = new ArrayList<>(2);
                StringBuilder ostrLvl = new StringBuilder();

                if (node.getParent() != null)
                    block.set(0,
                            Integer.toString(node.getParent().getId())
                    );
                else
                    block.set(0, "root");
                ostrLvl
                    .append(node.getId()).append(": ")
                    .append(node.getToken().getValue());
                block.set(1, ostrLvl.toString());
                levelOut.add(block);
            }

            // Print formatted level output
            StringBuilder ostrLine1 = new StringBuilder(),
                          ostrLine2 = new StringBuilder();
            for (List<String> nodeOut : levelOut) {
                int width = Math.max (
                        nodeOut.get(0).length(),
                        nodeOut.get(1).length()
                );
                ostrLine1
                    .append(String.format("%" + width + "s", nodeOut.get(0)))
                    .append("  ");
                ostrLine2
                    .append(String.format("%" + width + "s", nodeOut.get(1)))
                    .append("  ");
            }
            System.out.println(ostrLine1);
            System.out.println(ostrLine2);
            System.out.println("");
        }
    }
    
    private static int nextId = 0;                  // Next available node ID
    
    private final BiFunction<CNode, InstructionBuilder, Integer>
                  m_injected;                       // Injected code
    private final DataType m_valType;               // Node value type (implied by instruction operands)
    private final Token m_token;                    // Encapsulated token
    private final int m_id;                         // Node ID
    private       String m_capture;                 // Custom capture label
    private       CNode m_parent, m_rsib, m_child;  // Parent, sibling, child node references

    CNode(Token token, BiFunction<CNode, InstructionBuilder, Integer> executedCode)
    {
        m_injected = executedCode;
        m_valType = DataType.Empty;
        m_token = token;
        m_id = nextId++;
        m_capture = null;
        m_parent = null;
        m_rsib = null;
        m_child = null;
    }
    
    CNode(Token token, BiFunction<CNode, InstructionBuilder, Integer> executedCode, DataType type)
    {
        m_injected = executedCode;
        m_valType = type;
        m_token = token;
        m_id = nextId++;
        m_capture = null;
        m_parent = null;
        m_rsib = null;
        m_child = null;
    }

    public void setParent(CNode parent)     { m_parent = parent; }
    public void setChild(CNode child)       { m_child = child; }
    public void setSibling(CNode sibling)   { m_rsib = sibling; }
    public void setCapture(String capture)  { m_capture = capture; }

    public DataType getValType()    { return m_valType; }
    public CNode    getParent()     { return m_parent; }
    public CNode    getFirstChild() { return m_child; }
    public CNode    getSibling()    { return m_rsib; }
    public Token    getToken()      { return m_token; }
    public String   getCapture()    { return m_capture; }
    public int      getId()         { return m_id; }

    public int execInstrGen (InstructionBuilder builder)
            { return m_injected.apply(this, builder); }
}
