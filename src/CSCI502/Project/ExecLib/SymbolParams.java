/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSCI502.Project.ExecLib;

/**
 * Symbol parameters returned from the symbol table. Immutable type.
 * @author Joshua Boley
 */
public class SymbolParams
{
    private final DataType m_type;
    private final int m_offset;
    
    SymbolParams(DataType type, int offset)
    {
        m_type = type;
        m_offset = offset;
    }
    
    public DataType getType()
    {
        return m_type;
    }
    
    public int getOffset()
    {
        return m_offset;
    }
}
