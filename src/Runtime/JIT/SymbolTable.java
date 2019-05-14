package Runtime.JIT;

import Runtime.API.DataType;
import Runtime.Machine.StaticVariableStorage;
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
    
    private static final SymbolTable symbolTable;       // Symbol table
    static {
        symbolTable = new SymbolTable();
    }

    /**
     * Registers a new variable and allocates new storage.
     * @param name  Symbol's name
     * @param type  Symbol's data type
     */
    public static void registerVariable(String name, DataType type)
    {
        symbolTable.createSymbolEntry(name, type);
    }
    
    /**
     * Returns the parameters (data type, offset into the variable store) associated
     * with the identified variable.
     * @param name  Variable's identifier
     * @return Variable's symbol parameters
     * @throws IllegalArgumentException
     */
    public static SymbolParams getVariableParams(String name) throws IllegalArgumentException
    {
        Entry symbolEntry;
        if ((symbolEntry = symbolTable.getSymbolEntry(name)) == null)
            throw new IllegalArgumentException();
        return new SymbolParams(
            symbolEntry.getType(), symbolEntry.getOffset()
        );
    }
    
    public static boolean isRegistered(String name)
    {
        return symbolTable.hasSymbolDefinition(name);
    }
    
    private final Map<String, Entry> m_catalog;
    public SymbolTable()
    {
        m_catalog = new HashMap<>();
    }
    
    private void createSymbolEntry(String name, DataType type)
    {
        int offset = StaticVariableStorage.allocate(type);
        m_catalog.put(name, new Entry(name, type, offset));
    }
    
    private Entry getSymbolEntry(String name)
    {
        return m_catalog.get(name);
    }
    
    private boolean hasSymbolDefinition(String name)
    {
        return m_catalog.containsKey(name);
    }
}
