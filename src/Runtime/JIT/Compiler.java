package Runtime.JIT;

import Lexical.Analyzer;
import Lexical.BufferedTokenStream;
import Parsing.CNode;
import Parsing.ParseObserver;
import Parsing.Productions;
import Runtime.IO.ConsoleOutputChannel;
import Runtime.JIT.API.Instruction;
import Runtime.JIT.API.InstructionBuilder;
import Runtime.IO.InputChannel;
import Runtime.IO.OutputChannel;
import Runtime.Machine.StaticMemory;
import java.io.IOException;
import java.util.List;

/**
 * Implements the JIT compiler runtime.
 * @author Joshua Boley
 */
public class Compiler
{
    private final Analyzer m_tokenizer;              // Lexical analyzer (tokenizer)
    private final BufferedTokenStream m_tokenStream; // Token stream output end, used by tokenizer
    private InputChannel m_chIn;                     // Code input channel
    private ConsoleOutputChannel m_consoleOut;              // Console output channel
    
    public Compiler()
    {
        StaticMemory.initialize();
        m_tokenizer = new Analyzer();
        m_tokenStream = new BufferedTokenStream(m_tokenizer);
        m_chIn = null;
        m_consoleOut = null;
    }

    public void setInputChannel(InputChannel in)
    {
        m_chIn = in;
    }
    
    public void setOutputChannel(ConsoleOutputChannel consoleOut)
    {
        m_consoleOut = consoleOut;
    }
    
    /**
     * Executes the JIT compiler on a ready input source.
     * @param isCommand Flag, indicates if source is command (affects parsing)
     * @return Compiled program
     * @throws IOException 
     */
    public List<Instruction> run(boolean isCommand) throws IOException
    {
        // Reinitialize tokenizer and token input stream buffer
        m_tokenizer.reset();
        m_tokenStream.clear();
        m_tokenizer.init(m_chIn);
        
        /*// Compile in-memory executable //*/
        
        // Build code generation tree
        CNode codeTree;
        ParseObserver observer = new ParseObserver(m_consoleOut);
        if (isCommand)
            codeTree = Productions.commandLine(m_tokenStream, observer);
        else
            codeTree = Productions.statementBlock(m_tokenStream, observer);
        if (observer.parseFailed()) {
            m_consoleOut.resetFontColor();
            return null;
        }
        
        // Perform code (tree) optimizations
        
        // Execute machine instruction generation and return compiled program
        InstructionBuilder builder = new InstructionBuilder();
        codeTree.execInstrGen(builder);
        return builder.commit();
    }
}
