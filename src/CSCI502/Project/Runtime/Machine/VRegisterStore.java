package CSCI502.Project.Runtime.Machine;

import CSCI502.Project.ExecLib.DataType;

/**
 *
 * @author Joshua Boley
 */
public class VRegisterStore
{
    private Object m_value;
    private DataType m_type;
    private boolean m_containsSymbolRef;
    
    VRegisterStore()
    {
        m_value = null;
        m_type = DataType.Empty;
        m_containsSymbolRef = false;
    }
    
    VRegisterStore(Object value, DataType type)
    {
        m_value = value;
        m_type = type;
        m_containsSymbolRef = false;
    }
    
    void set(Object value, DataType type, boolean isSymbol)
    {
        m_value = value;
        m_type = type;
        m_containsSymbolRef = isSymbol;
    }
    
    Object getValue()
    {
        return m_value;
    }
    
    DataType getType()
    {
        return m_type;
    }
    
    boolean valIsRef()
    {
        return m_containsSymbolRef;
    }
}
