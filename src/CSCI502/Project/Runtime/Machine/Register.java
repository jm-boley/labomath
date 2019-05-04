package CSCI502.Project.Runtime.Machine;

/**
 * Virtual register identifiers
 * @author Joshua Boley
 */
public enum Register
{
    R1(0),
    R2(1),
    R3(2),
    R4(3),
    R5(4),
    R6(5),
    R7(6),
    R8(7);
    
    private final int m_id;
    private Register(int id)
    {
        m_id = id;
    }
    
    int id()
    {
        return m_id;
    }
}
