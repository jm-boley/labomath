package Runtime.IO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Input channel encapsulating source code file on a mounted file system.
 * @author Joshua Boley
 */
public class FileInputChannel
    extends InputChannel<PushbackInputStream>
{
    public FileInputChannel(int cmid)
    {
        super(cmid, null);
    }
    
    /**
     * Opens a new source file on this input channel.
     * @param sourceCodePath Path to source code file
     * @throws IOException 
     */
    public void open(final String sourceCodePath) throws IOException
    {
        if (m_input != null)
            m_input.close();
        m_input = new PushbackInputStream(new FileInputStream(sourceCodePath));
    }
    
    /**
     * Closes the current source file if open
     * @throws java.io.IOException
     */
    public void close() throws IOException
    {
        if (m_input != null) {
            m_input.close();
            m_input = null;
        }
    }

    @Override
    public InputStream getInputStream() {
        return m_input;
    }    
}
