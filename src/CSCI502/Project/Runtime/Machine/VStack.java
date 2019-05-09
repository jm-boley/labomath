package CSCI502.Project.Runtime.Machine;

import CSCI502.Project.Runtime.Interface.DataType;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a virtual hardware stack for the machine. This object emulates
 * the hardware stack of the Intel x64 architecture. It is an idealized version
 * as objects stored on the stack do not need to be fragmented to 8-byte
 * chunks and there is no notion of memory alignment. Additionally, stack grows upwards,
 * not downwards as in x64 architecture.
 * @author Joshua Boley
 */
class VStack
{
    private static final int ALLOC_SZ = 10;     // Number of slots to allocate when growing the stack
    private final List<RegisterContent> m_stack;
    private final RegisterContent m_stackPointer;
    
    VStack(RegisterContent basePointer, RegisterContent stackPointer)
    {
        // Initialize base pointer, stack pointer
        basePointer.set(-1, DataType.Imm_Int4);
        stackPointer.set(-1, DataType.Imm_Int4);
        m_stackPointer = stackPointer;
        
        // Initialize stack storage
        m_stack = new ArrayList<>();
        for (int i = 0; i < ALLOC_SZ; ++i)
            m_stack.add(new RegisterContent(0, DataType.Imm_Int4));
    }
    
    /**
     * Pushes an object to the stack at the current stack pointer location.
     * Stack pointer is incremented. Additional storage is automatically allocated
     * as needed.
     * @param obj Object to store
     */
    void push(RegisterContent obj)
    {
        int stackAddr = (int) m_stackPointer.getValue();
        
        // Grow stack if more storage needed
        if (stackAddr >= m_stack.size()) {
            for (int i = 0; i < ALLOC_SZ; ++i)
                m_stack.add(new RegisterContent(0, DataType.Imm_Int4));
        }
        
        // Increment stack pointer and add object to stack
        m_stackPointer.set(++stackAddr, DataType.Imm_Int4);
        m_stack.add(stackAddr, obj);
    }

    /**
     * Pops the stored object at the current stack pointer index. Decrements the
     * stack pointer. Allocated stack size does not change.
     * @return Object referenced by stack pointer
     */
    RegisterContent pop()
    {
        int stackAddr = (int) m_stackPointer.getValue();
        if (stackAddr < 0)
            throw new RuntimeException("Stack underflow");

        // Get object from stack and decrement stack pointer
        RegisterContent popped = m_stack.get(stackAddr);
        m_stackPointer.set(stackAddr - 1, DataType.Imm_Int4);
        
        return popped;
    }
}
