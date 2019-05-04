/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSCI502.Project.ExecLib;

import CSCI502.Project.Runtime.Machine.Register;

/**
 *
 * @author Joshua Boley
 */
public class Operand
{
    public static Operand Empty = new Operand(0);
    
    private final DataType m_dataType;      // Enclosed data type
    private final Object m_refTargetVal;    // Symbol table target or literal value
    
    public Operand(int immInteger)
    {
        m_dataType = DataType.Imm_Int4;
        m_refTargetVal = immInteger;
    }
    
    public Operand(Register reg)
    {
        m_dataType = DataType.Register;
        m_refTargetVal = reg;
    }
    
    public Operand(DataType type, String symbolNameOrStrLiteral)
    {
        m_dataType = type;
        // Get symbol table reference
        m_refTargetVal = 0;
    }
    
    /**
     * Returns the operand's data type
     * @return Data type
     */
    public DataType getType()
    {
        return m_dataType;
    }
    
    /**
     * Returns the enclosed value (either retrieved from symbol table
     * or the encapsulated literal value).
     * @return 
     */
    public Object getEnclosed()
    {
        if (m_dataType == DataType.Imm_Int4 || m_dataType == DataType.Imm_Str)
            return m_refTargetVal;
        return null;
    }
}
