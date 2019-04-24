package CSCI502.Project.ExecLib;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Joshua Boley
 */
public class InstructionBuilder
{
	private final List<List<Integer>> m_codeSegments;
	private int m_writeIdx,         // Current instruction index
                    m_activeSegment;    // Active instruction segment

	public InstructionBuilder()
        {
            m_codeSegments = new ArrayList<>();
            m_writeIdx = 0;
            m_activeSegment = 0;
        }

        /**
         * Returns the top instruction segment from the stack
         * @return Top level code segment
         */
	public List<Integer> getActiveCodeSegment()
        {
            return m_codeSegments.get(m_activeSegment);
        }
        
        /**
         * 
         * @param idx 
         */
	public void setActiveCodeSegment(int idx)
        {
            if (idx > 0)
                m_activeSegment = idx;
            else
                throw new IllegalArgumentException("");
        }
        
        /**
         * @return
         */
	public int createCodeSegment()
        {
            m_codeSegments.add(new ArrayList<>());
            return m_codeSegments.size() - 1;
        }

	public void createSymbol(String name, DataType datatype)
        {
            
        }
        
	public int createStrLiteral(String value)
        {
            return 0;
        }

	InstructionBuilder ADD(Operand dst, Operand src)
        {
            return this;
        }

	InstructionBuilder SUB(Operand dst, Operand src)
        {
            return this;
        }

	InstructionBuilder IMUL(Operand dst, Operand src)
        {
            return this;
        }
        
	InstructionBuilder IDIV(Operand dst, Operand src)
        {
            return this;
        }

	InstructionBuilder NEG(Operand dst)
        {
            return this;
        }

	InstructionBuilder PUSH(Operand src)
        {
            return this;
        }
        
	InstructionBuilder POP(Operand dst)
        {
            return this;
        }

//	InstructionBuilder CALL(Register64Low indirect);

	InstructionBuilder PRINT(Operand src)
        {
            return this;
        }
        
	InstructionBuilder READ(Operand dst)
        {
            return this;
        }
}
