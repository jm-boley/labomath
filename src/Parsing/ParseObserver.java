package Parsing;

import Runtime.IO.OutputChannel;
import Runtime.IO.OutputChannel.Type;

/**
 * Parse process observer class. Client code may query if parsing has failed,
 * and code under observation may relay parse failure messages back to the
 * client's console.
 * @author Joshua Boley
 */
public class ParseObserver
{
    private boolean m_parseFailed;
    private final OutputChannel m_consoleOut;
    
    public ParseObserver(OutputChannel consoleOut)
    {
        m_parseFailed = false;
        m_consoleOut = consoleOut;
    }
    
    public boolean parseFailed()
    {
        return m_parseFailed;
    }
    
    void setParseFailed()
    {
        m_parseFailed = true;
    }
    
    void notifyObserver(String message)
    {
        m_consoleOut.send(Type.StdErr, message + "\n");
    }
}
