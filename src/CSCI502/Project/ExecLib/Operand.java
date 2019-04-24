/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSCI502.Project.ExecLib;

/**
 *
 * @author Joshua Boley
 */
class Operand
{
    private final DataType m_dataType;      // Enclosed data type
    private final Object m_refTargetVal;    // Symbol table target or literal value
    
    public Operand(int immInteger)
    {
        m_dataType = DataType.Imm_Int4;
        m_refTargetVal = immInteger;
    }
    
    public Operand(String immString)
    {
        m_dataType = DataType.Imm_Str;
        m_refTargetVal = immString;
    }
    
    public Operand(DataType type, String symbolName)
    {
        m_dataType = type;
        // Get symbol table reference
        m_refTargetVal = 0;
    }
    
    /**
     * Returns the operand's data type
     * @return Data type
     */
    DataType getType()
    {
        return m_dataType;
    }
    
    /**
     * Returns the enclosed value (either retrieved from symbol table
     * or the encapsulated literal value).
     * @return 
     */
    Object getEnclosed()
    {
        if (m_dataType == DataType.Imm_Int4 || m_dataType == DataType.Imm_Str)
            return m_refTargetVal;
        return null;
    }
}
