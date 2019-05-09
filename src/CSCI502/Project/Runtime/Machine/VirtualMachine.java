package CSCI502.Project.Runtime.Machine;

import CSCI502.Project.Runtime.Interface.DataType;
import CSCI502.Project.Runtime.Interface.Instruction;
import CSCI502.Project.Runtime.Interface.Operand;
import java.util.EnumMap;
import java.util.List;
import javax.swing.JTextArea;

/**
 * Virtual machine on which instructions are executed
 * @author Joshua Boley
 */
public class VirtualMachine
{
    private static final boolean debug = true;
    
    private final EnumMap<Register, RegisterContent> m_registers;
    private final VStack m_vstack;
    private List<Instruction> m_instructionCache;
    private JTextArea m_console;
    
    public VirtualMachine()
    {
        m_registers = new EnumMap<>(Register.class);
        m_console = null;
        
        // Initialize registers
        for (Register reg : Register.values())
            m_registers.put(reg, new RegisterContent());
        
        // Initialize instruction, stack and base pointers
        m_registers.get(Register.IP).set(0, DataType.Imm_Int4);

        // Initialize virtual stack (base and stack pointer initialization handled
        // in VStack constructor
        m_vstack = new VStack(m_registers.get(Register.BP), m_registers.get(Register.SP));
    }
    
    public void initializeIO(JTextArea console)
    {
        m_console = console;
    }
    
    public void load(List<Instruction> instructions)
    {
        m_instructionCache = instructions;
    }
    
    public void execute()
    {
        int instrAddr;
        boolean jumped = false;
        while ((instrAddr = (int) m_registers.get(Register.IP).getValue()) < m_instructionCache.size()) {
            Instruction instr = m_instructionCache.get(instrAddr);
            Instruction.Opcode opcode = instr.getCode();
            List<Operand> operands = instr.getOperands();
            
            switch (opcode) {
                case MOVE:
                {
                    Operand dst = operands.get(0),
                            src = operands.get(1);
                    if (debug) {
                        if (dst.getType() == DataType.Imm_Int4 || dst.getType() == DataType.Imm_Str)
                            throw new UnsupportedOperationException("MOV: Only moves to register or reference stored in a register supported");
                    }
                    switch (dst.getType()) {
                        case Register:
                            switch (src.getType()) {
                                case Register:
                                {
                                    Register srcRegId = (Register) src.getEnclosed(),
                                             dstRegId = (Register) dst.getEnclosed();
                                    RegisterContent srcReg = m_registers.get(srcRegId);
                                    RegisterContent dstReg = m_registers.get(dstRegId);
                                    dstReg.set(srcReg.getValue(), srcReg.getType());
                                    break;
                                }
                                case Imm_Int4:
                                {
                                    Register dstRegId = (Register) dst.getEnclosed();
                                    RegisterContent dstReg = m_registers.get(dstRegId);
                                    dstReg.set(src.getEnclosed(), DataType.Int4);
                                    break;
                                }
                                case Imm_Str:
                                {
                                    Register dstRegId = (Register) dst.getEnclosed();
                                    RegisterContent dstReg = m_registers.get(dstRegId);
                                    dstReg.set(src.getEnclosed(), DataType.Imm_Str);
                                    break;
                                }
                                default:
                                {
                                    if (debug) {
                                        if (!src.isReference())
                                            throw new UnsupportedOperationException("MOV: Attempting to move to register from unrecognized location type");
                                    }
                                    Register dstRegId = (Register) dst.getEnclosed();
                                    RegisterContent dstReg = m_registers.get(dstRegId);
                                    dstReg.set(
                                        StaticVariableStorage.retrieve(
                                            src.getType(),
                                            (int) src.getEnclosed()
                                        ),
                                        src.getType()
                                    );
                                }
                            }
                            break;
                        default:
                            // Moving to variable storage
                            if (debug) {
                                if (!dst.isReference())
                                    throw new RuntimeException("MOV: Attempted to move to an unknown location type");
                            }
                            switch (src.getType()) {
                                case Register:
                                {
                                    Register srcRegId = (Register) src.getEnclosed();
                                    RegisterContent srcReg = m_registers.get(srcRegId);
                                    // Perform type check
                                    if (debug) {
                                        if (srcReg.getType() != dst.getType())
                                            throw new UnsupportedOperationException("MOV: Type mismatch, conversion not supported");
                                    }
                                    StaticVariableStorage.assign(srcReg.getValue(), dst.getType(), (int) dst.getEnclosed());
                                    break;
                                }
                                case Imm_Int4:
                                case Imm_Str:
                                    throw new UnsupportedOperationException("MOV: Moving a literal type directly to variable storage not supported");
                                default:
                                    throw new UnsupportedOperationException("MOV: Direct memory-to-memory moves not supported");
                            }
                    }
                    break;
                }
                case ADD:
                {
                    // Get register references and perform type check
                    Operand dst = operands.get(0),
                            src = operands.get(1);

                    if (debug) {
                        if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                            throw new UnsupportedOperationException(
                                "ADD: An operand does not name a register; dst=" + dst.getType().toString() +
                                ", src=" + src.getType().toString()
                            );
                    }
                    
                    Register dstRegId = (Register) dst.getEnclosed(),
                             srcRegId = (Register) src.getEnclosed();
                    RegisterContent dstReg = m_registers.get(dstRegId),
                                   srcReg = m_registers.get(srcRegId);

                    if (debug) {
                        if (dstReg.getType() != srcReg.getType())
                            throw new UnsupportedOperationException(
                                "ADD: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                                " to " + dstReg.getType().toString()
                            );
                    }
                    
                    // Perform addition op and set result to dst operand
                    Object result;
                    switch (dstReg.getType()) {
                        case Int4:
                            result = (int) dstReg.getValue() + (int) srcReg.getValue();
                            break;
                        default:
                            throw new UnsupportedOperationException("ADD: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(result, dstReg.getType());
                    break;
                }
                case SUBTRACT:
                {
                    // Get register references and perform type check
                    Operand dst = operands.get(0),
                            src = operands.get(1);
                    
                    if (debug) {
                        if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                            throw new UnsupportedOperationException(
                                "SUBTRACT: An operand does not name a register; dst=" + dst.getType().toString() +
                                ", src=" + src.getType().toString()
                            );
                    }
                    
                    Register dstRegId = (Register) dst.getEnclosed(),
                             srcRegId = (Register) src.getEnclosed();
                    RegisterContent dstReg = m_registers.get(dstRegId),
                                   srcReg = m_registers.get(srcRegId);
                    
                    if (debug) {
                        if (dstReg.getType() != srcReg.getType())
                            throw new UnsupportedOperationException(
                                "SUBTRACT: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                                " to " + dstReg.getType().toString()
                            );
                    }
                    
                    // Perform addition op and set result to dst register
                    Object result;
                    switch (dstReg.getType()) {
                        case Int4:
                            result = (int) dstReg.getValue() - (int) srcReg.getValue();
                            break;
                        default:
                            throw new UnsupportedOperationException("SUBTRACT: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(result, dstReg.getType());
                    break;
                }
                case MULTIPLY:
                {
                    // Get register references and perform type check
                    Operand dst = operands.get(0),
                            src = operands.get(1);

                    if (debug) {
                        if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                            throw new UnsupportedOperationException(
                                "MULTIPLY: An operand does not name a register; dst=" + dst.getType().toString() +
                                ", src=" + src.getType().toString()
                            );
                    }
                    
                    Register dstRegId = (Register) dst.getEnclosed(),
                             srcRegId = (Register) src.getEnclosed();
                    RegisterContent dstReg = m_registers.get(dstRegId),
                                   srcReg = m_registers.get(srcRegId);
                    
                    if (debug) {
                        if (dstReg.getType() != srcReg.getType())
                            throw new UnsupportedOperationException(
                                "MULTIPLY: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                                " to " + dstReg.getType().toString()
                            );
                    }
                    
                    // Perform multiplication op and set result to dst register
                    Object result;
                    switch (dstReg.getType()) {
                        case Int4:
                            result = (int) dstReg.getValue() * (int) srcReg.getValue();
                            break;
                        default:
                            throw new UnsupportedOperationException("MULTIPLY: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(result, dstReg.getType());
                    break;
                }
                case DIVIDE:
                {
                    // Get register references and perform type check
                    Operand dst = operands.get(0),
                            src = operands.get(1);

                    if (debug) {
                        if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                            throw new UnsupportedOperationException(
                                "DIVIDE: An operand does not name a register; dst=" + dst.getType().toString() +
                                ", src=" + src.getType().toString()
                            );
                    }
                    
                    Register dstRegId = (Register) dst.getEnclosed(),
                             srcRegId = (Register) src.getEnclosed(),
                             modRegId = Register.R4;
                    RegisterContent dstReg = m_registers.get(dstRegId),
                                   srcReg = m_registers.get(srcRegId),
                                   modReg = m_registers.get(modRegId);

                    if (debug) {
                        if (dstReg.getType() != srcReg.getType())
                            throw new UnsupportedOperationException(
                                "DIVIDE: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                                " to " + dstReg.getType().toString()
                            );
                    }
                    
                    // Perform div/mod op and set results to dst, R4 (mod) register
                    Object divResult,
                           modResult;
                    switch (dstReg.getType()) {
                        case Int4:
                            divResult = (int) dstReg.getValue() / (int) srcReg.getValue();
                            modResult = (int) dstReg.getValue() % (int) srcReg.getValue();
                            break;
                        default:
                            throw new UnsupportedOperationException("DIVIDE: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(divResult, dstReg.getType());
                    modReg.set(modResult, dstReg.getType());
                    break;
                }
                case EXP:
                {
                    Operand dst = operands.get(0),
                            src = operands.get(1);
                    
                    if (debug) {
                        if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                            throw new UnsupportedOperationException(
                                "EXP: An operand does not name a register; dst=" + dst.getType().toString() +
                                ", src=" + src.getType().toString()
                            );
                    }
                    
                    RegisterContent dstReg = m_registers.get(
                                        ((Register) dst.getEnclosed()) 
                                    ),
                                   srcReg = m_registers.get(
                                        ((Register) src.getEnclosed()) 
                                    );
                    
                    if (debug) {
                        if (dstReg.getType() != srcReg.getType())
                            throw new UnsupportedOperationException(
                                "EXP: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                                " to " + dstReg.getType().toString()
                            );
                    }

                    Object expResult;
                    switch (dstReg.getType()) {
                        case Int4:
                            expResult = (int) Math.pow(
                                (int) dstReg.getValue(),
                                (int) srcReg.getValue()
                            );
                            break;
                        default:
                            throw new UnsupportedOperationException("EXP: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(expResult, dstReg.getType());
                    break;
                }
                case NEGATION:
                {
                    Operand op = operands.get(0);
                    RegisterContent opReg = m_registers.get(
                        ((Register) op.getEnclosed()) 
                    );
                    Object negResult;
                    switch (opReg.getType()) {
                        case Int4:
                            negResult = -((int) opReg.getValue());
                            break;
                        default:
                            throw new UnsupportedOperationException("NEGATION: Unsupported type " + op.getType().toString());
                    }
                    opReg.set(negResult, opReg.getType());
                }
                case PUSH:
                {
                    Operand src = operands.get(0);
                    Register srcRegId = (Register) src.getEnclosed();
                    RegisterContent srcReg = m_registers.get(srcRegId);
                    m_vstack.push(new RegisterContent(srcReg.getValue(), srcReg.getType()));
                    break;
                }
                case POP:
                {
                    Operand dst = operands.get(0);
                    Register dstRegId = (Register) dst.getEnclosed();
                    RegisterContent dstReg = m_registers.get(dstRegId );
                    RegisterContent temp = (RegisterContent) m_vstack.pop();
                    dstReg.set(temp.getValue(), temp.getType());
                    break;
                }
                case PRINT:
                {
                    Operand src = operands.get(0);
                    sendToConsole((Register) src.getEnclosed());
                    break;
                }
                case CLEAR:
                {
                    // Clears console, also sets accumulator to 0
                    m_console.setText("");
                    RegisterContent accum = m_registers.get(Register.R1);
                    accum.set("", DataType.Imm_Str);
                    break;
                }
            }
            
            // Increment instruction pointer if a jump was not executed
            if (!jumped)
                m_registers
                    .get(Register.IP)
                    .set(++instrAddr);
        }
    }
    
    public void getAccumulatorValue(StringBuilder sb)
    {
        RegisterContent accumulator = m_registers.get(Register.R1);
        switch (accumulator.getType()) {
            case Int4:
                sb.append((int) accumulator.getValue());
            default:;
        }
    }
    
    private void sendToConsole(Register src)
    {
        RegisterContent srcReg = m_registers.get(src );
        // If accumulator contains a symbol reference then dereference and overwrite with stored value
//        if (srcReg.valIsRef()) {
//            SymbolParams symParams = SymbolTable.getVariableParams((String) srcReg.getValue());
//            srcReg.set(
//                StaticVariableStorage.retrieve(symParams.getType(), symParams.getOffset()),
//                symParams.getType(),
//                false
//            );
//        }
        switch (srcReg.getType()) {
            case Int4:
                m_console.append(Integer.toString((int) srcReg.getValue()));
                break;
            case Imm_Str:
                m_console.append((String) srcReg.getValue());
                break;
            default:
                throw new UnsupportedOperationException("PRINT: Not supported for type " + srcReg.getType().toString());
        }
    }
}
