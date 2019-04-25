package CSCI502.Project.ExecLib;

import java.util.HashMap;
import java.util.Map;

/**
 * Symbol table 
 * @author Joshua Boley
 */
public class SymbolTable
{
    /**
     * Encapsulates symbol parameters (data type, symbol name, and storage
     * location offset).
     */
    class Entry
    {
        private final DataType m_type;        // Type of stored data
        private final String   m_name;        // Symbol name
        private final int      m_byteOffset;  // (Byte) offset into storage
        
        Entry(String name, DataType type, int offset)
        {
            m_type = type;
            m_name = name;
            m_byteOffset = offset;
        }
        
        DataType getType()   { return m_type; }
        String   getName()   { return m_name; }
        int      getOffset() { return m_byteOffset; }
    }
    
    private static int nextId;
    private static final SymbolTable symbolTable;
    
    static {
        nextId = 0;
        symbolTable = new SymbolTable();
    }
    
    public static int registerSymbol(String name, DataType type)
    {
        return symbolTable.createSymbolEntry(name, type);
    }
    
    public static SymbolParams getSymbolParams(int symbolId) throws IllegalArgumentException
    {
        Entry symbolEntry;
        if ((symbolEntry = symbolTable.getSymbolEntry(symbolId)) == null)
            throw new IllegalArgumentException();
        return new SymbolParams(
            symbolEntry.getType(), symbolEntry.getOffset()
        );
    }
    
    private final Map<Integer, Entry> m_catalog;
    public SymbolTable()
    {
        m_catalog = new HashMap<>();
    }
    
    private int createSymbolEntry(String name, DataType type)
    {
        int offset = VariableStorage.allocate(type);
        m_catalog.put(nextId, new Entry(name, type, offset));
        return nextId++;
    }
    
    private Entry getSymbolEntry(int symbolId)
    {
        return m_catalog.get(symbolId);
    }
}
