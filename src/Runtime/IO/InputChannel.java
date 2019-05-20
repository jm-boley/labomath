package Runtime.IO;

import java.io.InputStream;

/**
 * Abstract base class encapsulating user input sources. Deriving classes
 * should override the getInput() method to return user input as a string.
 * A channel-mapping identifier (CMID), provided to the client application
 * requesting an input channel to the virtual machine, is required to build
 * an input channel object.
 * @author Joshua Boley
 * @param <_InputT>
 */
public abstract class InputChannel<_InputT>
{
    private final int m_cmid;
    protected _InputT m_input;
    private boolean m_ready;
    
    public InputChannel(int cmid, _InputT input)
    {
        m_cmid = cmid;
        m_input = input;
        m_ready = false;
    }
    
    public void inputReady()
    {
        m_ready = true;
    }
    
    public int getChannelMapping()
    {
        return m_cmid;
    }
    
    public boolean isReady()
    {
        return m_ready;
    }
    
    public void reset()
    {
        m_ready = false;
    }
    
    /**
     *
     * @return
     */
    public abstract InputStream getInputStream();
}
