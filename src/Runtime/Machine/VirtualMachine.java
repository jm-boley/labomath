package Runtime.Machine;

import Runtime.API.DataType;
import Runtime.API.Instruction;
import Runtime.API.Operand;
import java.util.EnumMap;
import java.util.List;
import javax.swing.JTextArea;

/**
 * Virtual machine on which instructions are executed
 * @author Joshua Boley
 */
public class VirtualMachine
{
    enum Flag {
        ZF,     // Zero flag
        SF,     // Sign flag
        OF      // Overflow flag
    }

    private static final boolean DEBUG = true;

    private EnumMap<Flag, Boolean> m_eflags;
    private final EnumMap<RegId, Register> m_registers;
    private final VHdwStack m_vstack;
    private List<Instruction> m_instructionCache;
    private JTextArea m_console;
    
    public VirtualMachine()
    {
        m_eflags = new EnumMap<>(Flag.class);
        m_eflags.put(Flag.ZF, false);
        m_eflags.put(Flag.SF, false);
        m_eflags.put(Flag.OF, false);
        m_registers = new EnumMap<>(RegId.class);
        m_console = null;
        
        // Initialize registers
        for (RegId reg : RegId.values())
            m_registers.put(reg, new Register());
        
        // Initialize instruction, stack and base pointers
        m_registers.get(RegId.IP).set(0, DataType.Imm_Int4);

        // Initialize virtual stack (base and stack pointer initialization handled
        // in VHdwStack constructor
        m_vstack = new VHdwStack(m_registers.get(RegId.BP), m_registers.get(RegId.SP));
    }
    
    public void initializeIO(JTextArea console)
    {
        m_console = console;
    }
    
    public void load(List<Instruction> instructions)
    {
        m_instructionCache = instructions;
        m_registers
            .get(RegId.IP)
            .set(0);
    }
    
    public void execute()
    {
        int instrAddr;
        boolean jumped = false;
        while ((instrAddr = (int) m_registers.get(RegId.IP).getValue()) < m_instructionCache.size()) {
            Instruction instr = m_instructionCache.get(instrAddr);
            Opcodes opcode = instr.getCode();
            List<Operand> operands = instr.getOperands();
            
            switch (opcode) {
                // Move data between virtual registers and/or memory
                case MOV:
                {
                    Operand dst = operands.get(0),
                            src = operands.get(1);
                    if (DEBUG) {
                        if (dst.getType() == DataType.Imm_Int4 || dst.getType() == DataType.Imm_Str)
                            throw new UnsupportedOperationException("MOV: Only moves to register or reference stored in a register supported");
                    }
                    switch (dst.getType()) {
                        case Register:
                            switch (src.getType()) {
                                case Register:
                                {
                                    RegId srcRegId = (RegId) src.getEnclosed();
                                    RegId dstRegId = (RegId) dst.getEnclosed();
                                    Register srcReg = m_registers.get(srcRegId);
                                    Register dstReg = m_registers.get(dstRegId);
                                    dstReg.set(srcReg.getValue(), srcReg.getType());
                                    break;
                                }
                                case Imm_Int4:
                                {
                                    RegId dstRegId = (RegId) dst.getEnclosed();
                                    Register dstReg = m_registers.get(dstRegId);
                                    dstReg.set(src.getEnclosed(), DataType.Int4);
                                    break;
                                }
                                case Imm_Str:
                                {
                                    RegId dstRegId = (RegId) dst.getEnclosed();
                                    Register dstReg = m_registers.get(dstRegId);
                                    dstReg.set(src.getEnclosed(), DataType.Imm_Str);
                                    break;
                                }
                                default:
                                {
                                    if (DEBUG) {
                                        if (!src.isReference())
                                            throw new UnsupportedOperationException("MOV: Attempting to move to register from unrecognized location type");
                                    }
                                    RegId dstRegId = (RegId) dst.getEnclosed();
                                    Register dstReg = m_registers.get(dstRegId);
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
                            if (DEBUG) {
                                if (!dst.isReference())
                                    throw new RuntimeException("MOV: Attempted to move to an unknown location type");
                            }
                            switch (src.getType()) {
                                case Register:
                                {
                                    RegId srcRegId = (RegId) src.getEnclosed();
                                    Register srcReg = m_registers.get(srcRegId);
                                    // Perform type check
                                    if (DEBUG) {
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
                // Addition operation
                case ADD:
                {
                    // Get register references and perform type check
                    Operand dst = operands.get(0),
                            src = operands.get(1);

                    if (DEBUG) {
                        if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                            throw new UnsupportedOperationException(
                                "ADD: An operand does not name a register; dst=" + dst.getType().toString() +
                                ", src=" + src.getType().toString()
                            );
                    }
                    
                    RegId dstRegId = (RegId) dst.getEnclosed(),
                          srcRegId = (RegId) src.getEnclosed();
                    Register dstReg = m_registers.get(dstRegId);
                    Register srcReg = m_registers.get(srcRegId);

                    if (DEBUG) {
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
                        {
                            int res;
                            try {
                                res = Math.addExact((int) dstReg.getValue(), (int) srcReg.getValue());
                                m_eflags.put(Flag.OF, false);
                            }
                            catch (ArithmeticException ex) {
                                m_eflags.put(Flag.OF, true);
                                res = (int) dstReg.getValue() + (int) srcReg.getValue();
                            }
                            m_eflags.put(Flag.ZF, res == 0);
                            m_eflags.put(Flag.SF, res < 0);
                            result = res;
                            break;
                        }
                        default:
                            throw new UnsupportedOperationException("ADD: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(result);
                    break;
                }
                // Subtraction operation
                case SUB:
                {
                    // Get register references and perform type check
                    Operand dst = operands.get(0),
                            src = operands.get(1);
                    
                    if (DEBUG) {
                        if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                            throw new UnsupportedOperationException(
                                "SUBTRACT: An operand does not name a register; dst=" + dst.getType().toString() +
                                ", src=" + src.getType().toString()
                            );
                    }
                    
                    RegId dstRegId = (RegId) dst.getEnclosed(),
                          srcRegId = (RegId) src.getEnclosed();
                    Register dstReg = m_registers.get(dstRegId);
                    Register srcReg = m_registers.get(srcRegId);
                    
                    if (DEBUG) {
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
                            int res;
                            try {
                                res = Math.subtractExact((int) dstReg.getValue(), (int) srcReg.getValue());
                                m_eflags.put(Flag.OF, false);
                            }
                            catch (ArithmeticException ex) {
                                m_eflags.put(Flag.OF, true);
                                res = (int) dstReg.getValue() - (int) srcReg.getValue();
                            }
                            m_eflags.put(Flag.ZF, res == 0);
                            m_eflags.put(Flag.SF, res < 0);
                            result = res;
                            break;
                        default:
                            throw new UnsupportedOperationException("SUBTRACT: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(result);
                    break;
                }
                // Multiplication operation
                case MULT:
                {
                    // Get register references and perform type check
                    Operand dst = operands.get(0),
                            src = operands.get(1);

                    if (DEBUG) {
                        if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                            throw new UnsupportedOperationException(
                                "MULTIPLY: An operand does not name a register; dst=" + dst.getType().toString() +
                                ", src=" + src.getType().toString()
                            );
                    }
                    
                    RegId dstRegId = (RegId) dst.getEnclosed(),
                          srcRegId = (RegId) src.getEnclosed();
                    Register dstReg = m_registers.get(dstRegId),
                             srcReg = m_registers.get(srcRegId);
                    
                    if (DEBUG) {
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
                            int res;
                            try {
                                res = Math.multiplyExact((int) dstReg.getValue(), (int) srcReg.getValue());
                                m_eflags.put(Flag.OF, false);
                            }
                            catch (ArithmeticException ex) {
                                m_eflags.put(Flag.OF, true);
                                res = (int) dstReg.getValue() * (int) srcReg.getValue();
                            }
                            m_eflags.put(Flag.ZF, res == 0);
                            m_eflags.put(Flag.SF, res < 0);
                            result = res;
                            break;
                        default:
                            throw new UnsupportedOperationException("MULTIPLY: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(result);
                    break;
                }
                // Division operation
                case DIV:
                {
                    // Get register references and perform type check
                    Operand dst = operands.get(0),
                            src = operands.get(1);

                    if (DEBUG) {
                        if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                            throw new UnsupportedOperationException(
                                "DIVIDE: An operand does not name a register; dst=" + dst.getType().toString() +
                                ", src=" + src.getType().toString()
                            );
                    }
                    
                    RegId dstRegId = (RegId) dst.getEnclosed(),
                          srcRegId = (RegId) src.getEnclosed(),
                          modRegId = RegId.R4;
                    Register dstReg = m_registers.get(dstRegId),
                             srcReg = m_registers.get(srcRegId),
                             modReg = m_registers.get(modRegId);

                    if (DEBUG) {
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
                            int res = (int) dstReg.getValue() / (int) srcReg.getValue();
                            modResult = (int) dstReg.getValue() % (int) srcReg.getValue();
                            m_eflags.put(Flag.OF, false);
                            m_eflags.put(Flag.ZF, res == 0);
                            m_eflags.put(Flag.SF, res < 0);
                            divResult = res;
                            break;
                        default:
                            throw new UnsupportedOperationException("DIVIDE: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(divResult);
                    modReg.set(modResult);
                    break;
                }
                // Exponentiation operation (x^y)
                case EXP:
                {
                    Operand dst = operands.get(0),
                            src = operands.get(1);
                    
                    if (DEBUG) {
                        if (dst.getType() != DataType.Register || src.getType() != DataType.Register)
                            throw new UnsupportedOperationException(
                                "EXP: An operand does not name a register; dst=" + dst.getType().toString() +
                                ", src=" + src.getType().toString()
                            );
                    }
                    
                    Register dstReg = m_registers.get(
                            ((RegId) dst.getEnclosed())
                    );
                    Register srcReg = m_registers.get(
                            ((RegId) src.getEnclosed())
                    );
                    
                    if (DEBUG) {
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
                    dstReg.set(expResult);
                    break;
                }
                // Negation operation
                case NEG:
                {
                    Operand op = operands.get(0);
                    Register opReg = m_registers.get(
                        ((RegId) op.getEnclosed()) 
                    );
                    Object negResult;
                    switch (opReg.getType()) {
                        case Int4:
                            negResult = -((int) opReg.getValue());
                            break;
                        default:
                            throw new UnsupportedOperationException("NEGATION: Unsupported type " + op.getType().toString());
                    }
                    opReg.set(negResult);
                }
                // Arithmetic right-shift
                case SAR:
                {
                    Operand dst = operands.get(0),
                            mutator = operands.get(1);
                    Register dstReg = m_registers.get(
                        ((RegId) dst.getEnclosed())
                    );
                    byte shiftVal = (byte) mutator.getEnclosed();
                    Object shiftResult;
                    switch (dstReg.getType()) {
                        case Int4:
                            shiftResult = (int) dstReg.getValue() >> shiftVal;
                            break;
                        default:
                            throw new UnsupportedOperationException("SAR: Unsupproted type " + dst.getType().toString());
                    }
                    dstReg.set(shiftResult);
                }
                // Arithmetic left-shift
                case SAL:
                {
                    Operand dst = operands.get(0),
                            mutator = operands.get(1);
                    Register dstReg = m_registers.get(
                        ((RegId) dst.getEnclosed())
                    );
                    byte shiftVal = (byte) mutator.getEnclosed();
                    Object shiftResult;
                    switch (dstReg.getType()) {
                        case Int4:
                            shiftResult = (int) dstReg.getValue() << shiftVal;
                            break;
                        default:
                            throw new UnsupportedOperationException("SAL: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(shiftResult);
                }
                // Logical (bitwise) right-shift
                case SLR:
                {
                    Operand dst = operands.get(0),
                            mutator = operands.get(1);
                    Register dstReg = m_registers.get(
                        ((RegId) dst.getEnclosed())
                    );
                    byte shiftVal = (byte) mutator.getEnclosed();
                    Object shiftResult;
                    switch (dstReg.getType()) {
                        case Int4:
                            shiftResult = (int) dstReg.getValue() >>> shiftVal;
                            break;
                        default:
                            throw new UnsupportedOperationException("SLR: Unsupported type " + dst.getType());
                    }
                    dstReg.set(shiftResult);
                }
                // Logical (bitwise) left-shift
                case SLL:
                {
                    Operand dst = operands.get(0),
                            mutator = operands.get(1);
                    Register dstReg = m_registers.get(
                        ((RegId) dst.getEnclosed())
                    );
                    byte shiftVal = (byte) mutator.getEnclosed();
                    Object shiftResult;
                    switch (dstReg.getType()) {
                        case Int4:
                            shiftResult = (int) dstReg.getValue() << shiftVal;
                            break;
                        default:
                            throw new UnsupportedOperationException("SAL: Unsupported type " + dst.getType().toString());
                    }
                    dstReg.set(shiftResult);
                }
                // Push to virtual hardware stack
                case PUSH:
                {
                    Operand src = operands.get(0);
                    RegId srcRegId = (RegId) src.getEnclosed();
                    Register srcReg = m_registers.get(srcRegId);
                    m_vstack.push(new Register(srcReg.getValue(), srcReg.getType()));
                    break;
                }
                // Pop from virtual hardware stack
                case POP:
                {
                    Operand dst = operands.get(0);
                    RegId dstRegId = (RegId) dst.getEnclosed();
                    Register dstReg = m_registers.get(dstRegId );
                    Register temp = (Register) m_vstack.pop();
                    dstReg.set(temp.getValue(), temp.getType());
                    break;
                }
                // Arithmetic comparison
                case CMP:
                {
                    Register
                            lhReg = m_registers.get(
                                (RegId) operands.get(0).getEnclosed()
                            ),
                            rhReg = m_registers.get(
                                (RegId) operands.get(1).getEnclosed()
                            );
                    
                    if (DEBUG) {
                        if (lhReg.getType() != rhReg.getType())
                            throw new UnsupportedOperationException(
                                "CMP: Unsupported comparison between types: op1=" +
                                lhReg.getType() + ", op2=" + rhReg.getType()
                            );
                    }
                    
                    switch (lhReg.getType()) {
                        case Int4:
                            int res;
                            try {
                                res = Math.subtractExact((int) lhReg.getValue(), (int) rhReg.getValue());
                                m_eflags.put(Flag.OF, false);
                            }
                            catch (ArithmeticException ex) {
                                m_eflags.put(Flag.OF, true);
                                res = (int) lhReg.getValue() - (int) rhReg.getValue();
                            }
                            m_eflags.put(Flag.ZF, res == 0);
                            m_eflags.put(Flag.SF, res < 0);
                            break;
                        default:
                            throw new UnsupportedOperationException(
                                "CMP: Unsupported type " + lhReg.getType()
                            );
                    }
                }
                // Logical comparison
                case TEST:
                {
                    Register lhReg = m_registers.get(
                        (RegId) operands.get(0).getEnclosed()
                    );
                    if (DEBUG) {
                        if (lhReg.getType() != DataType.Int4)
                            throw new UnsupportedOperationException(
                                "TEST: Unsupported left-hand operand type " +
                                lhReg.getType()
                            );
                    }
                    switch (operands.get(1).getType()) {
                        case Register:
                        {
                            Register rhReg = m_registers.get(
                                (RegId) operands.get(1).getEnclosed()
                            );
                            switch (rhReg.getType()) {
                                case Int4:
                                    int tmp = (int) lhReg.getValue() & (int) rhReg.getValue();
                                    m_eflags.put(Flag.ZF, tmp == 0);
                                    m_eflags.put(Flag.SF, tmp < 0);
                                    break;
                                default:
                                    throw new UnsupportedOperationException(
                                        "TEST: Unsupported right-hand operand type " +
                                        rhReg.getType()
                                    );
                            }
                            break;
                        }
                        case Imm_Int4:
                            int tmp = (int) lhReg.getValue() & (int) operands.get(1).getEnclosed();
                            m_eflags.put(Flag.ZF, tmp == 0);
                            m_eflags.put(Flag.SF, tmp < 0);
                            break;
                        default:
                            throw new UnsupportedOperationException(
                                "TEST: Unsupported right-hand operand type " +
                                operands.get(1).getType()
                            );
                    }
                    m_eflags.put(Flag.OF, false);
                }
                // Logical OR
                case OR:
                {
                    Register lhReg = m_registers.get(
                        (RegId) operands.get(0).getEnclosed()
                    );
                    if (DEBUG) {
                        if (lhReg.getType() != DataType.Int4)
                            throw new UnsupportedOperationException(
                                "TEST: Unsupported left-hand operand type " +
                                lhReg.getType()
                            );
                    }
                    switch (operands.get(1).getType()) {
                        case Register:
                        {
                            Register rhReg = m_registers.get(
                                (RegId) operands.get(1).getEnclosed()
                            );
                            switch (rhReg.getType()) {
                                case Int4:
                                    int tmp = (int) lhReg.getValue() | (int) rhReg.getValue();
                                    m_eflags.put(Flag.ZF, tmp == 0);
                                    m_eflags.put(Flag.SF, tmp < 0);
                                    lhReg.set(tmp);
                                    break;
                                default:
                                    throw new UnsupportedOperationException(
                                        "TEST: Unsupported right-hand operand type " +
                                        rhReg.getType()
                                    );
                            }
                            break;
                        }
                        case Imm_Int4:
                            int tmp = (int) lhReg.getValue() | (int) operands.get(1).getEnclosed();
                            m_eflags.put(Flag.ZF, tmp == 0);
                            m_eflags.put(Flag.SF, tmp < 0);
                            lhReg.set(tmp);
                            break;
                        default:
                            throw new UnsupportedOperationException(
                                "TEST: Unsupported right-hand operand type " +
                                operands.get(1).getType()
                            );
                    }
                    m_eflags.put(Flag.OF, false);
                }
                // Logical XOR
                case XOR:
                {
                    Register lhReg = m_registers.get(
                        (RegId) operands.get(0).getEnclosed()
                    );
                    if (DEBUG) {
                        if (lhReg.getType() != DataType.Int4)
                            throw new UnsupportedOperationException(
                                "TEST: Unsupported left-hand operand type " +
                                lhReg.getType()
                            );
                    }
                    switch (operands.get(1).getType()) {
                        case Register:
                        {
                            Register rhReg = m_registers.get(
                                (RegId) operands.get(1).getEnclosed()
                            );
                            switch (rhReg.getType()) {
                                case Int4:
                                    int tmp = (int) lhReg.getValue() ^ (int) rhReg.getValue();
                                    m_eflags.put(Flag.ZF, tmp == 0);
                                    m_eflags.put(Flag.SF, tmp < 0);
                                    lhReg.set(tmp);
                                    break;
                                default:
                                    throw new UnsupportedOperationException(
                                        "TEST: Unsupported right-hand operand type " +
                                        rhReg.getType()
                                    );
                            }
                            break;
                        }
                        case Imm_Int4:
                            int tmp = (int) lhReg.getValue() ^ (int) operands.get(1).getEnclosed();
                            m_eflags.put(Flag.ZF, tmp == 0);
                            m_eflags.put(Flag.SF, tmp < 0);
                            lhReg.set(tmp);
                            break;
                        default:
                            throw new UnsupportedOperationException(
                                "TEST: Unsupported right-hand operand type " +
                                operands.get(1).getType()
                            );
                    }
                    m_eflags.put(Flag.OF, false);
                }
                // Logical AND
                case AND:
                {
                    Register lhReg = m_registers.get(
                        (RegId) operands.get(0).getEnclosed()
                    );
                    if (DEBUG) {
                        if (lhReg.getType() != DataType.Int4)
                            throw new UnsupportedOperationException(
                                "TEST: Unsupported left-hand operand type " +
                                lhReg.getType()
                            );
                    }
                    switch (operands.get(1).getType()) {
                        case Register:
                        {
                            Register rhReg = m_registers.get(
                                (RegId) operands.get(1).getEnclosed()
                            );
                            switch (rhReg.getType()) {
                                case Int4:
                                    int tmp = (int) lhReg.getValue() & (int) rhReg.getValue();
                                    m_eflags.put(Flag.ZF, tmp == 0);
                                    m_eflags.put(Flag.SF, tmp < 0);
                                    lhReg.set(tmp);
                                    break;
                                default:
                                    throw new UnsupportedOperationException(
                                        "TEST: Unsupported right-hand operand type " +
                                        rhReg.getType()
                                    );
                            }
                            break;
                        }
                        case Imm_Int4:
                            int tmp = (int) lhReg.getValue() & (int) operands.get(1).getEnclosed();
                            m_eflags.put(Flag.ZF, tmp == 0);
                            m_eflags.put(Flag.SF, tmp < 0);
                            lhReg.set(tmp);
                            break;
                        default:
                            throw new UnsupportedOperationException(
                                "TEST: Unsupported right-hand operand type " +
                                operands.get(1).getType()
                            );
                    }
                    m_eflags.put(Flag.OF, false);
                }
                // Jump to instruction (absolute)
                case JMP:
                {
                    Register iptr = m_registers.get(RegId.IP);
                    int offset = (int) operands
                                    .get(0)
                                    .getEnclosed();
                    iptr.set(instrAddr + offset);
                    jumped = true;
                }
                // Jump if less (SF != OF)
                case JL:
                {
                    if (m_eflags.get(Flag.SF) != m_eflags.get(Flag.OF)) {
                        Register iptr = m_registers.get(RegId.IP);
                        int offset = (int) operands
                                        .get(0)
                                        .getEnclosed();
                        iptr.set(instrAddr + offset);
                        jumped = true;
                    }
                }
                // Jump if less or equal (ZF = 1 or SF != OF)
                case JLE:
                {
                    if (m_eflags.get(Flag.ZF) || m_eflags.get(Flag.SF) != m_eflags.get(Flag.OF)) {
                        Register iptr = m_registers.get(RegId.IP);
                        int offset = (int) operands
                                        .get(0)
                                        .getEnclosed();
                        iptr.set(instrAddr + offset);
                        jumped = true;
                    }
                }
                // Jump if greater (ZF = 0 and SF = OF)
                case JG:
                {
                    if (!m_eflags.get(Flag.ZF) && m_eflags.get(Flag.SF) == m_eflags.get(Flag.OF)) {
                        Register iptr = m_registers.get(RegId.IP);
                        int offset = (int) operands
                                        .get(0)
                                        .getEnclosed();
                        iptr.set(instrAddr + offset);
                        jumped = true;
                    }
                }
                // Jump if greater or equal (SF = OF)
                case JGE:
                {
                    if (m_eflags.get(Flag.SF) == m_eflags.get(Flag.OF)) {
                        Register iptr = m_registers.get(RegId.IP);
                        int offset = (int) operands
                                        .get(0)
                                        .getEnclosed();
                        iptr.set(instrAddr + offset);
                        jumped = true;
                    }
                }
                // Jump if equal (ZF = 1)
                case JE:
                {
                    if (m_eflags.get(Flag.ZF)) {
                        Register iptr = m_registers.get(RegId.IP);
                        int offset = (int) operands
                                        .get(0)
                                        .getEnclosed();
                        iptr.set(instrAddr + offset);
                        jumped = true;
                    }
                }
                // Jump if not equal (ZF = 0)
                case JNE:
                {
                    if (!m_eflags.get(Flag.ZF)) {
                        Register iptr = m_registers.get(RegId.IP);
                        int offset = (int) operands
                                        .get(0)
                                        .getEnclosed();
                        iptr.set(instrAddr + offset);
                        jumped = true;
                    }
                }
                // Set byte if less (SF != OF)
                case SETL:
                {
                    if (m_eflags.get(Flag.SF) != m_eflags.get(Flag.OF)) {
                        Register dstReg = m_registers.get(
                            (RegId) operands
                                    .get(0)
                                    .getEnclosed()
                        );
                        byte imm = (byte) operands.get(1).getEnclosed();
                        
                    }
                }
                case PRNT:
                {
                    Operand src = operands.get(0);
                    sendToConsole((RegId) src.getEnclosed());
                    break;
                }
                case CLR:
                {
                    // Clears console, also sets accumulator to 0
                    m_console.setText("");
                    Register accum = m_registers.get(RegId.R1);
                    accum.set("", DataType.Imm_Str);
                    break;
                }
            }
            
            // Increment instruction pointer if a jump was not executed
            if (!jumped)
                m_registers
                    .get(RegId.IP)
                    .set(++instrAddr);
            else
                // Reset jump flag
                jumped = false;
        }
    }
    
    /**
     * Returns value in the accumulator (register R1) in the supplied StringBuilder
     * object.
     * @param sb StringBuilder object
     */
    public void getAccumulatorValue(StringBuilder sb)
    {
        Register accumulator = m_registers.get(RegId.R1);
        switch (accumulator.getType()) {
            case Int4:
                sb.append((int) accumulator.getValue());
            default:;
        }
    }
    
    private void sendToConsole(RegId src)
    {
        Register srcReg = m_registers.get(src);
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
