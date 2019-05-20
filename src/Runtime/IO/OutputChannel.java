package Runtime.IO;

/**
 * Abstract base class encapsulating an output resource or collection point for
 * complex output, such as a variable list panel update. Deriving classes should
 overload the send() method. A channel-mapping identifier (CMID),
 provided to the client application requesting an output channel from the
 virtual machine, is required to build an output channel object.
 * @author Joshua Boley
 * @param <_OutputT>
 */
public abstract class OutputChannel<_OutputT>
{
    public enum Type
    {
        StdOut, // Standard output
        StdErr, // Standard error
        Update  // GUI component update
    }
    
    private final int m_cmid;           // Channel-mapping identifier
    protected final _OutputT m_output;
    
    public OutputChannel(int cmid, final _OutputT output)
    {
        m_cmid = cmid;
        m_output = output;
    }
    
    public int getChannelMapping()
    {
        return m_cmid;
    }
    
    public abstract void sendClear();
    public abstract void send(Type otype, Object output);
}
