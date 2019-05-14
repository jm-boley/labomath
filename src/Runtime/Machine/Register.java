package Runtime.Machine;

import Runtime.API.DataType;

/**
 * Virtual register storage structure. Contains both the stored value
 * and its type.
 * @author Joshua Boley
 */
public class Register
{
    private Object m_value;
    private DataType m_type;
    
    Register()
    {
        m_value = null;
        m_type = DataType.Empty;
    }
    
    Register(Object value, DataType type)
    {
        m_value = value;
        m_type = type;
    }
    
    void set(Object value, DataType type)
    {
        m_value = value;
        m_type = type;
    }
    
    void set(Object value)
    {
        m_value = value;
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
