package CSCI502.Project.Runtime.Interface;

import CSCI502.Project.Runtime.Machine.Opcodes;
import java.util.List;

/**
 * Virtual machine instruction opcode and operands structure.
 * @author Joshua Boley
 */
public class Instruction
{
    
    private final Opcodes m_opcode;
    private final List<Operand> m_operands;
    
    public Instruction(Opcodes opcode, List<Operand> operands)
    {
        m_opcode = opcode;
        m_operands = operands;
    }
    
    public Opcodes getCode()
    {
        return m_opcode;
    }
    
    public List<Operand> getOperands()
    {
        return m_operands;
    }
}
