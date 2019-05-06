package CSCI502.Project.Runtime.Machine;

import CSCI502.Project.ExecLib.DataType;
import CSCI502.Project.ExecLib.Instruction;
import CSCI502.Project.ExecLib.Operand;
import CSCI502.Project.ExecLib.VariableStorage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.JTextArea;

/**
 * Virtual machine on which instructions are executed
 * @author Joshua Boley
 */
public class VirtualMachine
{
    private final Map<Integer, VRegisterStore> m_registers;
    private final Stack<VRegisterStore> m_vstack;
    private List<Instruction> m_instructionCache;
    private JTextArea m_console;
    
    public VirtualMachine()
    {
        m_registers = new HashMap<>();
        m_vstack = new Stack<>();
        m_console = null;
        
        m_registers.put(Register.R1.id(), new VRegisterStore());
        m_registers.put(Register.R2.id(), new VRegisterStore());
        m_registers.put(Register.R3.id(), new VRegisterStore());
        m_registers.put(Register.R4.id(), new VRegisterStore());
        m_registers.put(Register.R5.id(), new VRegisterStore());
        m_registers.put(Register.R6.id(), new VRegisterStore());
        m_registers.put(Register.R7.id(), new VRegisterStore());
        m_registers.put(Register.R8.id(), new VRegisterStore());
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
        for (Instruction instr : m_instructionCache) {
            Instruction.Opcode opcode = instr.getCode();
            List<Operand> operands = instr.getOperands();
            
            switch (opcode) {
                case MOVE:
                {
                    Operand dst = operands.get(0),
                            src = operands.get(1);
                    if (dst.getType() == DataType.Imm_Int4 || dst.getType() == DataType.Imm_Str)
                        throw new UnsupportedOperationException("MOV: Only moves to register or reference stored in a register supported");
                    switch (dst.getType()) {
                        case Register:
                            switch (src.getType()) {
                                case Register:
                                {
                                    Register srcRegId = (Register) src.getEnclosed(),
                                             dstRegId = (Register) dst.getEnclosed();
                                    VRegisterStore srcReg = m_registers.get(srcRegId.id()),
                                                   dstReg = m_registers.get(dstRegId.id());
                                    dstReg.set(srcReg.getValue(), srcReg.getType());
                                    break;
                                }
                                case Imm_Int4:
                                {
                                    Register dstRegId = (Register) dst.getEnclosed();
                                    VRegisterStore dstReg = m_registers.get(dstRegId.id());
                                    dstReg.set(src.getEnclosed(), DataType.Int4);
                                    break;
                                }
                                case Imm_Str:
                                {
                                    Register dstRegId = (Register) dst.getEnclosed();
                                    VRegisterStore dstReg = m_registers.get(dstRegId.id());
                                    dstReg.set(src.getEnclosed(), DataType.Imm_Str);
                                    break;
                                }
                                default:
                                {
                                    if (!src.isReference())
                                        throw new UnsupportedOperationException("MOV: Attempting to move to register from unrecognized location type");
                                    Register dstRegId = (Register) dst.getEnclosed();
                                    VRegisterStore dstReg = m_registers.get(dstRegId.id());
                                    dstReg.set(
                                        VariableStorage.retrieve(
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
                            if (!dst.isReference())
                                throw new RuntimeException("MOV: Attempted to move to an unknown location type");
                            switch (src.getType()) {
                                case Register:
                                {
                                    Register srcRegId = (Register) src.getEnclosed();
                                    VRegisterStore srcReg = m_registers.get(srcRegId.id());
                                    // Perform type check
                                    if (srcReg.getType() != dst.getType())
                                        throw new UnsupportedOperationException("MOV: Type mismatch, conversion not supported");
                                    VariableStorage.assign(srcReg.getValue(), dst.getType(), (int) dst.getEnclosed());
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
                    
                    if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                        throw new UnsupportedOperationException(
                            "ADD: An operand does not name a register; dst=" + dst.getType().toString() +
                            ", src=" + src.getType().toString()
                        );
                    
                    Register dstRegId = (Register) dst.getEnclosed(),
                             srcRegId = (Register) src.getEnclosed();
                    VRegisterStore dstReg = m_registers.get(dstRegId.id()),
                                   srcReg = m_registers.get(srcRegId.id());
                    
                    if (dstReg.getType() != srcReg.getType())
                        throw new UnsupportedOperationException(
                            "ADD: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                            " to " + dstReg.getType().toString()
                        );
                    
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
                    
                    if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                        throw new UnsupportedOperationException(
                            "SUBTRACT: An operand does not name a register; dst=" + dst.getType().toString() +
                            ", src=" + src.getType().toString()
                        );
                    
                    Register dstRegId = (Register) dst.getEnclosed(),
                             srcRegId = (Register) src.getEnclosed();
                    VRegisterStore dstReg = m_registers.get(dstRegId.id()),
                                   srcReg = m_registers.get(srcRegId.id());
                    
                    if (dstReg.getType() != srcReg.getType())
                        throw new UnsupportedOperationException(
                            "SUBTRACT: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                            " to " + dstReg.getType().toString()
                        );
                    
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
                    
                    if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                        throw new UnsupportedOperationException(
                            "MULTIPLY: An operand does not name a register; dst=" + dst.getType().toString() +
                            ", src=" + src.getType().toString()
                        );
                    
                    Register dstRegId = (Register) dst.getEnclosed(),
                             srcRegId = (Register) src.getEnclosed();
                    VRegisterStore dstReg = m_registers.get(dstRegId.id()),
                                   srcReg = m_registers.get(srcRegId.id());
                    
                    if (dstReg.getType() != srcReg.getType())
                        throw new UnsupportedOperationException(
                            "MULTIPLY: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                            " to " + dstReg.getType().toString()
                        );
                    
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
                    
                    if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                        throw new UnsupportedOperationException(
                            "DIVIDE: An operand does not name a register; dst=" + dst.getType().toString() +
                            ", src=" + src.getType().toString()
                        );
                    
                    Register dstRegId = (Register) dst.getEnclosed(),
                             srcRegId = (Register) src.getEnclosed(),
                             modRegId = Register.R4;
                    VRegisterStore dstReg = m_registers.get(dstRegId.id()),
                                   srcReg = m_registers.get(srcRegId.id()),
                                   modReg = m_registers.get(modRegId.id());
                    
                    if (dstReg.getType() != srcReg.getType())
                        throw new UnsupportedOperationException(
                            "DIVIDE: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                            " to " + dstReg.getType().toString()
                        );
                    
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
                    if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                        throw new UnsupportedOperationException(
                            "EXP: An operand does not name a register; dst=" + dst.getType().toString() +
                            ", src=" + src.getType().toString()
                        );
                    VRegisterStore dstReg = m_registers.get(
                                        ((Register) dst.getEnclosed()).id()
                                    ),
                                   srcReg = m_registers.get(
                                        ((Register) src.getEnclosed()).id()
                                    );
                    
                    if (dstReg.getType() != srcReg.getType())
                        throw new UnsupportedOperationException(
                            "EXP: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                            " to " + dstReg.getType().toString()
                        );

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
                    VRegisterStore opReg = m_registers.get(
                        ((Register) op.getEnclosed()).id()
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
                    VRegisterStore srcReg = m_registers.get(srcRegId.id());
                    m_vstack.push(new VRegisterStore(srcReg.getValue(), srcReg.getType()));
                    break;
                }
                case POP:
                {
                    Operand dst = operands.get(0);
                    Register dstRegId = (Register) dst.getEnclosed();
                    VRegisterStore dstReg = m_registers.get(dstRegId.id());
                    VRegisterStore temp = m_vstack.pop();
                    dstReg.set(temp.getValue(), temp.getType());
                    break;
                }
                case PRINT:
                {
                    Operand src = operands.get(0);
                    sendToConsole((Register) src.getEnclosed());
                    break;
                }
            }
        }
    }
    
    public void getAccumulatorValue(StringBuilder sb)
    {
        VRegisterStore accumulator = m_registers.get(0);
        switch (accumulator.getType()) {
            case Int4:
                sb.append((int) accumulator.getValue());
            default:;
        }
    }
    
    private void sendToConsole(Register src)
    {
        VRegisterStore srcReg = m_registers.get(src.id());
        // If accumulator contains a symbol reference then dereference and overwrite with stored value
//        if (srcReg.valIsRef()) {
//            SymbolParams symParams = SymbolTable.getVariableParams((String) srcReg.getValue());
//            srcReg.set(
//                VariableStorage.retrieve(symParams.getType(), symParams.getOffset()),
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
