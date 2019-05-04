package CSCI502.Project.Runtime;

import CSCI502.Project.Lexical.Analyzer;
import CSCI502.Project.Lexical.BufferedTokenStream;
import CSCI502.Project.Lexical.Token;
import CSCI502.Project.Runtime.Machine.VirtualMachine;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author boley
 */
public class Interpreter
{
    private final Analyzer tokenizer;
    private final BufferedTokenStream m_tokenStream;
    private final VirtualMachine m_machine;
    
    // DEBUG
    private JTextArea m_console;
    
    public Interpreter()
    {
        tokenizer = new Analyzer();
        m_tokenStream = new BufferedTokenStream(tokenizer);
        m_machine = new VirtualMachine();
        m_console = null;
    }
    
    public void initConsoleOut(JTextArea console)
    {
        m_console = console;
        m_machine.initializeIO(console);
    }
    
    public void run (Object input) throws IOException
    {
        // Reinitialize tokenizer and generate new token stream
        tokenizer.reset();
        m_tokenStream.clear();
        if (input instanceof String)
            tokenizer.init((String) input);
        else if (input instanceof JTextField)
            tokenizer.init((JTextField) input);
        debugGenerateTokenStream();
    }
    
    private void debugGenerateTokenStream() throws IOException
    {
        Token token;
        while ((token = m_tokenStream.read()) != null) {
            m_console.append(token.toString() + "\n");
        }
    }
}
