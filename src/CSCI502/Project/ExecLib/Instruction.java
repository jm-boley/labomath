package CSCI502.Project.ExecLib;

import java.util.List;

/**
 * Virtual machine instruction opcode and operands structure.
 * @author Joshua Boley
 */
public class Instruction
{
    public enum Opcode
    {
        MOVE,
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        PUSH,
        POP,
        PRINT;
    }
    
    private final Opcode m_opcode;
    private final List<Operand> m_operands;
    
    public Instruction(Opcode opcode, List<Operand> operands)
    {
        m_opcode = opcode;
        m_operands = operands;
    }
    
    public Opcode getCode()
    {
        return m_opcode;
    }
    
    public List<Operand> getOperands()
    {
        return m_operands;
    }
}
