package Runtime.Machine;

import Runtime.JIT.API.DataType;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a virtual hardware stack for the machine. This object emulates
 * the hardware stack of the Intel x86_64 processor family. It is an idealized version
 * as large objects stored on the stack do not need to be fragmented to 8-byte
 * chunks and there are no inherent memory alignment considerations. Stack also grows
 * upwards, not downwards through its address space.
 * @author Joshua Boley
 */
class VHdwStack
{
    private static final int ALLOC_SZ = 10;     // Number of slots to allocate when growing the stack
    private final List<Register> m_stack;
    private final Register m_stackPointer;
    
    VHdwStack(Register basePointer, Register stackPointer)
    {
        // Initialize base pointer, stack pointer
        basePointer.set(-1, DataType.Imm_Int4);
        stackPointer.set(-1, DataType.Imm_Int4);
        m_stackPointer = stackPointer;
        
        // Initialize stack storage
        m_stack = new ArrayList<>();
        for (int i = 0; i < ALLOC_SZ; ++i)
            m_stack.add(new Register(0, DataType.Imm_Int4));
    }
    
    /**
     * Pushes an object to the stack at the current stack pointer location.
     * Stack pointer is incremented. Additional storage is automatically allocated
     * as needed.
     * @param obj Object to store
     */
    void push(Register obj)
    {
        int stackAddr = (int) m_stackPointer.getValue();
        
        // Grow stack if more storage needed
        if (stackAddr >= m_stack.size() - 1) {
            for (int i = 0; i < ALLOC_SZ; ++i)
                m_stack.add(new Register(0, DataType.Imm_Int4));
        }
        
        // Increment stack pointer and add object to stack
        m_stackPointer.set(++stackAddr, DataType.Imm_Int4);
        if (stackAddr >= m_stack.size()) {
            boolean breakHere = true;
        }
        m_stack.set(stackAddr, obj);
    }

    /**
     * Pops the stored object at the current stack pointer index. Decrements the
     * stack pointer. Allocated stack size does not change.
     * @return Object referenced by stack pointer
     */
    Register pop()
    {
        int stackAddr = (int) m_stackPointer.getValue();
        if (stackAddr < 0)
            throw new RuntimeException("Stack underflow");

        // Get object from stack and decrement stack pointer
        Register popped = m_stack.get(stackAddr);
        m_stackPointer.set(stackAddr - 1, DataType.Imm_Int4);
        
        return popped;
    }
}
