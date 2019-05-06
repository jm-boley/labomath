package CSCI502.Project.Runtime.Machine;

import CSCI502.Project.ExecLib.DataType;

/**
 * Virtual register storage structure. Contains both the stored value
 * and its type.
 * @author Joshua Boley
 */
public class VRegisterStore
{
    private Object m_value;
    private DataType m_type;
    
    VRegisterStore()
    {
        m_value = null;
        m_type = DataType.Empty;
    }
    
    VRegisterStore(Object value, DataType type)
    {
        m_value = value;
        m_type = type;
    }
    
    void set(Object value, DataType type)
    {
        m_value = value;
        m_type = type;
    }
    
    Object getValue()
    {
        return m_value;
    }
    
    DataType getType()
    {
        return m_type;
    }
}
