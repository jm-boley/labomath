package CSCI502.Project.Runtime.Machine;

import CSCI502.Project.ExecLib.DataType;
import CSCI502.Project.ExecLib.Instruction;
import CSCI502.Project.ExecLib.Operand;
import CSCI502.Project.ExecLib.SymbolParams;
import CSCI502.Project.ExecLib.SymbolTable;
import CSCI502.Project.ExecLib.VariableStorage;
import java.util.ArrayList;
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
                    if (dst.getType() != DataType.Register)
                        throw new UnsupportedOperationException("MOV: Only moves to register or reference stored in a register is supported");
                    switch (src.getType()) {
                        case Register:
                        {
                            Register srcRegId = (Register) src.getEnclosed(),
                                     dstRegId = (Register) dst.getEnclosed();
                            VRegisterStore srcReg = m_registers.get(srcRegId.id()),
                                           dstReg = m_registers.get(dstRegId.id());
                            // If destination register contains a symbol reference then move value in src register to storage
                            if (dstReg.valIsRef()) {
                                SymbolParams symParams;
                                try {
                                    symParams = SymbolTable.getVariableParams((String) dstReg.getValue());
                                }
                                catch (IllegalArgumentException ex) {
                                    throw new RuntimeException("MOV: Unknown variable reference");
                                }
                                if (srcReg.getType() != symParams.getType())
                                    throw new UnsupportedOperationException("MOV: Type mismatch, conversion not supported");

                                VariableStorage.assign(srcReg.getValue(), srcReg.getType(), symParams.getOffset());
                            }
                            // Otherwise assign contents of src register to dst register
                            else
                                dstReg.set(srcReg.getValue(), srcReg.getType(), srcReg.valIsRef());
                        }
                        case Imm_Int4:
                        {
                            Register dstRegId = (Register) dst.getEnclosed();
                            VRegisterStore dstReg = m_registers.get(dstRegId.id());
                            dstReg.set(src.getEnclosed(), DataType.Int4, false);
                            break;
                        }
                        case Imm_Str:
                        {
                            Register dstRegId = (Register) dst.getEnclosed();
                            VRegisterStore dstReg = m_registers.get(dstRegId.id());
                            dstReg.set(src.getEnclosed(), DataType.Imm_Str, false);
                            break;
                        }
                        default:
                            SymbolParams symParams;
                            try {
                                symParams = SymbolTable.getVariableParams((String) dst.getEnclosed());
                            }
                            catch (IllegalArgumentException ex) {
                                throw new RuntimeException("MOV: Unknown variable reference");
                            }
                            Register dstRegId = (Register) dst.getEnclosed();
                            VRegisterStore dstReg = m_registers.get(dstRegId.id());
                            dstReg.set(
                                VariableStorage.retrieve(
                                    symParams.getType(),
                                    symParams.getOffset()
                                ),
                                symParams.getType(), false
                            );
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
                    
                    if (dstReg.valIsRef() || srcReg.valIsRef())
                        throw new UnsupportedOperationException(
                            "ADD: Invalid symbol reference; dst=" + dstReg.getValue() +
                            ", src=" + srcReg.getValue()
                        );
                    
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
                    dstReg.set(result, dstReg.getType(), false);
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
                    
                    if (dstReg.valIsRef() || srcReg.valIsRef())
                        throw new UnsupportedOperationException(
                            "SUBTRACT: Invalid symbol reference; dst=" + dstReg.getValue() +
                            ", src=" + srcReg.getValue()
                        );
                    
                    if (dstReg.getType() != srcReg.getType())
                        throw new UnsupportedOperationException(
                            "SUBTRACT: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                            " to " + dstReg.getType().toString()
                        );
                    
                    // Perform addition op and set result to dst operand
                    Object result;
                    switch (dstReg.getType()) {
                        case Int4:
                            result = (int) dstReg.getValue() - (int) srcReg.getValue();
                            break;
                        default:
                            throw new UnsupportedOperationException("SUBTRACT: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(result, dstReg.getType(), false);
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
                    
                    if (dstReg.valIsRef() || srcReg.valIsRef())
                        throw new UnsupportedOperationException(
                            "MULTIPLY: Invalid symbol reference; dst=" + dstReg.getValue() +
                            ", src=" + srcReg.getValue()
                        );
                    
                    if (dstReg.getType() != srcReg.getType())
                        throw new UnsupportedOperationException(
                            "MULTIPLY: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                            " to " + dstReg.getType().toString()
                        );
                    
                    // Perform addition op and set result to dst operand
                    Object result;
                    switch (dstReg.getType()) {
                        case Int4:
                            result = (int) dstReg.getValue() * (int) srcReg.getValue();
                            break;
                        default:
                            throw new UnsupportedOperationException("MULTIPLY: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(result, dstReg.getType(), false);
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
                             modRegId = Register.R3;
                    VRegisterStore dstReg = m_registers.get(dstRegId.id()),
                                   srcReg = m_registers.get(srcRegId.id()),
                                   modReg = m_registers.get(modRegId.id());
                    
                    if (dstReg.valIsRef() || srcReg.valIsRef())
                        throw new UnsupportedOperationException(
                            "DIVIDE: Invalid symbol reference; dst=" + dstReg.getValue() +
                            ", src=" + srcReg.getValue()
                        );
                    
                    if (dstReg.getType() != srcReg.getType())
                        throw new UnsupportedOperationException(
                            "DIVIDE: Type mismatch, unable to convert from " + srcReg.getType().toString() +
                            " to " + dstReg.getType().toString()
                        );
                    
                    // Perform addition op and set result to dst operand
                    Object multResult,
                           modResult;
                    switch (dstReg.getType()) {
                        case Int4:
                            multResult = (int) dstReg.getValue() / (int) srcReg.getValue();
                            modResult = (int) dstReg.getValue() % (int) srcReg.getValue();
                            break;
                        default:
                            throw new UnsupportedOperationException("DIVIDE: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(multResult, dstReg.getType(), false);
                    modReg.set(modResult, dstReg.getType(), false);
                    break;
                }
                case PUSH:
                {
                    Operand src = operands.get(0);
                    Register srcRegId = (Register) src.getEnclosed();
                    VRegisterStore srcReg = m_registers.get(srcRegId.id());
                    m_vstack.push(new VRegisterStore(srcReg.getValue(), srcReg.getType()));
                }
                case POP:
                {
                    Operand dst = operands.get(0);
                    Register dstRegId = (Register) dst.getEnclosed();
                    VRegisterStore dstReg = m_registers.get(dstRegId.id());
                    VRegisterStore temp = m_vstack.pop();
                    dstReg.set(temp.getValue(), temp.getType(), temp.valIsRef());
                }
                case PRINT:
                {
                    Operand src = operands.get(0);
                    sendToConsole((Register) src.getEnclosed());
                }
            }
        }
        
        // Print Register.R1 contents to console
        sendToConsole(Register.R1);
    }
    
    private void sendToConsole(Register src)
    {
        VRegisterStore srcReg = m_registers.get(src.id());
        // If accumulator contains a symbol reference then dereference and overwrite with stored value
        if (srcReg.valIsRef()) {
            SymbolParams symParams = SymbolTable.getVariableParams((String) srcReg.getValue());
            srcReg.set(
                VariableStorage.retrieve(symParams.getType(), symParams.getOffset()),
                symParams.getType(),
                false
            );
        }
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
