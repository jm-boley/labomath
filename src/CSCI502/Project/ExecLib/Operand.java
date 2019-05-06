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
    private final boolean m_isRef;          // Is a reference value
    
    public Operand(int immInteger)
    {
        m_dataType = DataType.Imm_Int4;
        m_refTargetVal = immInteger;
        m_isRef = false;
    }
    
    public Operand(boolean immBoolean)
    {
        m_dataType = DataType.Imm_Bool;
        m_refTargetVal = immBoolean;
        m_isRef = false;
    }
    
    public Operand(String strLiteral)
    {
        m_dataType = DataType.Imm_Str;
        m_refTargetVal = strLiteral;
        m_isRef = false;
    }
    
    public Operand(Register reg)
    {
        m_dataType = DataType.Register;
        m_refTargetVal = reg;
        m_isRef = false;
    }
    
    public Operand(DataType type, int symbolOffset)
    {
        m_dataType = type;
        m_refTargetVal = symbolOffset;
        m_isRef = true;
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
        return m_refTargetVal;
    }
    
    /**
     * Returns whether or not operand is a reference type
     * @return True or false
     */
    public boolean isReference()
    {
        return m_isRef;
    }
}
