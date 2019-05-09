package CSCI502.Project.Parsing;

import CSCI502.Project.Runtime.Interface.DataType;
import CSCI502.Project.Runtime.Interface.Instruction;
import CSCI502.Project.Runtime.Interface.Operand;
import CSCI502.Project.Runtime.Interface.SymbolTable;
import CSCI502.Project.Runtime.Machine.Register;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Joshua Boley
 */
public class InstructionBuilder
{
	private final List<List<Instruction>> m_codeSegments;
	private int m_activeSegment;    // Active instruction segment

	public InstructionBuilder()
        {
            m_codeSegments = new ArrayList<>();
            m_codeSegments.add(new ArrayList<>());
            m_activeSegment = 0;
        }

        /**
         * Returns the currently active instruction segment in the code segment chain
         * @return Active code segment
         */
	public List<Instruction> getActiveCodeSegment()
        {
            return m_codeSegments.get(m_activeSegment);
        }
        
        /**
         * Returns the ID of the currently active instruction segment in the code
         * segment chain
         * @return 
         */
        public int getActiveCodeSegmentId()
        {
            return m_activeSegment;
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
            SymbolTable.registerVariable(name, datatype);
        }
        
	public int createStrLiteral(String value)
        {
            return 0;
        }
        
        public List<Instruction> commit()
        {
            List<Instruction> program = new ArrayList<>();
            m_codeSegments.forEach((codeSegment) -> {
                program.addAll(codeSegment);
            });
            return program;
        }

        public InstructionBuilder MOV(Operand dst, Operand src)
        {
            if (dst.getType() == DataType.Imm_Int4 || dst.getType() == DataType.Imm_Str)
                throw new RuntimeException("");
            List<Operand> operands = new ArrayList<>();
            operands.add(dst);
            operands.add(src);
            Instruction instr = new Instruction(Instruction.Opcode.MOVE, operands);
            m_codeSegments
                .get(m_activeSegment)
                .add(instr);
            return this;
        }
        
	public InstructionBuilder ADD(Register op1, Register op2)
        {
            List<Operand> operands = new ArrayList<>();
            operands.add(new Operand(op1));
            operands.add(new Operand(op2));
            Instruction instr = new Instruction(Instruction.Opcode.ADD, operands);
            m_codeSegments
                .get(m_activeSegment)
                .add(instr);
            return this;
        }

	public InstructionBuilder SUB(Register op1, Register op2)
        {
            List<Operand> operands = new ArrayList<>();
            operands.add(new Operand(op1));
            operands.add(new Operand(op2));
            Instruction instr = new Instruction(Instruction.Opcode.SUBTRACT, operands);
            m_codeSegments
                .get(m_activeSegment)
                .add(instr);
            return this;
        }

	public InstructionBuilder MUL(Register op1, Register op2)
        {
            List<Operand> operands = new ArrayList<>();
            operands.add(new Operand(op1));
            operands.add(new Operand(op2));
            Instruction instr = new Instruction(Instruction.Opcode.MULTIPLY, operands);
            m_codeSegments
                .get(m_activeSegment)
                .add(instr);
            return this;
        }
        
	public InstructionBuilder DIV(Register op1, Register op2)
        {
            List<Operand> operands = new ArrayList<>();
            operands.add(new Operand(op1));
            operands.add(new Operand(op2));
            Instruction instr = new Instruction(Instruction.Opcode.DIVIDE, operands);
            m_codeSegments
                .get(m_activeSegment)
                .add(instr);
            return this;
        }
        
        public InstructionBuilder EXP(Register dst, Register src)
        {
            List<Operand> operands = new ArrayList<>();
            operands.add(new Operand(dst));
            operands.add(new Operand(src));
            m_codeSegments
                .get(m_activeSegment)
                .add(
                    new Instruction(Instruction.Opcode.EXP, operands)
                );
            return this;
        }

	public InstructionBuilder NEG(Register dst)
        {
            List<Operand> operands = new ArrayList<>();
            operands.add(new Operand(dst));
            m_codeSegments
                .get(m_activeSegment)
                .add(
                    new Instruction(Instruction.Opcode.NEGATION, operands)
                );
            return this;
        }

	public InstructionBuilder PUSH(Register src)
        {
            List<Operand> operands = new ArrayList<>();
            operands.add(new Operand(src));
            Instruction instr = new Instruction(Instruction.Opcode.PUSH, operands);
            m_codeSegments
                .get(m_activeSegment)
                .add(instr);
            return this;
        }
        
	public InstructionBuilder POP(Register dst)
        {
            List<Operand> operands = new ArrayList<>();
            operands.add(new Operand(dst));
            Instruction instr = new Instruction(Instruction.Opcode.POP, operands);
            m_codeSegments
                .get(m_activeSegment)
                .add(instr);
            return this;
        }

//	InstructionBuilder CALL(Register64Low indirect);

	public InstructionBuilder PRINT(Operand src)
        {
            List<Operand> operands = new ArrayList<>();
            operands.add(src);
            Instruction instr = new Instruction(Instruction.Opcode.PRINT, operands);
            m_codeSegments
                .get(m_activeSegment)
                .add(instr);
            return this;
        }
        
        public InstructionBuilder CLEAR()
        {
            m_codeSegments
                .get(m_activeSegment)
                .add(new Instruction(Instruction.Opcode.CLEAR, null));
            return this;
        }
        
	public InstructionBuilder READ(Operand dst)
        {
            return this;
        }
}
