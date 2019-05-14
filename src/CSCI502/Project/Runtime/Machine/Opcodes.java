package CSCI502.Project.Runtime.Machine;

/**
 * Virtual CPU operation codes.
 * @author boley
 */
public enum Opcodes
{
    MOV,    // Move data between virtual registers and/or memory
    ADD,    // Addition operation
    SUB,    // Subtraction operation
    MULT,   // Multiplication operation
    DIV,    // Division operation
    EXP,    // Exponentiation operation (x^y)
    NEG,    // Negation operation
    SAR,    // Arithmetic right-shift
    SAL,    // Arithmetic left-shift
    SLR,    // Logical (bitwise) right-shift
    SLL,    // Logical (bitwise) left-shift

    PUSH,   // Push to virtual hardware stack
    POP,    // Pop from virtual hardware stack
    
    CMP,    // Arithmetic comparison
    TEST,   // Logical comparison
    OR,     // Logical OR
    XOR,    // Logical Xclusive OR
    AND,    // Logical AND
    
    JMP,    // Jump to instruction (absolute)
    JL,     // Jump if less (SF != OF)
    JLE,    // Jump if less or equal (ZF = 1 or SF != OF)
    JG,     // Jump if greater (ZF = 0 and SF = OF)
    JGE,    // Jump if greater or equal (SF = OF)
    JE,     // Jump if equal (ZF = 1)
    JNE,    // Jump if not equal (ZF = 0)
    
    SETL,   // Set byte if less (SF != OF)
    SETLE,  // Set byte if less or equal (ZF = 1 or SF != OF)
    SETG,   // Set byte if greater (ZF = 0 and SF = OF)
    SETGE,  // Set byte if greater or equal (SF = OF)
    SETE,   // Set byte if equal (ZF = 1)
    SETNE,  // Set byte if not equal (ZF != 1)
    
    PRNT,   // Print contents of a register
    CLR;    // Clear output console
}
