package Runtime.JIT;

import Lexical.Analyzer;
import Lexical.BufferedTokenStream;
import Parsing.CNode;
import Parsing.ParseException;
import Parsing.Productions;
import Runtime.JIT.API.Instruction;
import Runtime.JIT.API.InstructionBuilder;
import Runtime.IO.CommandInputChannel;
import Runtime.IO.ConsoleOutputChannel;
import Runtime.IO.EditorInputChannel;
import Runtime.IO.InputChannel;
import Runtime.IO.OutputChannel;
import Runtime.Machine.StaticMemory;
import Runtime.Machine.VirtualCPU;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * Implements the JIT compiler runtime.
 * @author Joshua Boley
 */
public class Compiler
{
    private final Analyzer m_tokenizer;              // Lexical analyzer (tokenizer)
    private final BufferedTokenStream m_tokenStream; // Token stream output end, used by tokenizer
    private final VirtualCPU m_vCpu;                 // Virtual machine (CPU)
    private EditorInputChannel m_editorIn;           // Text editor input source
    private CommandInputChannel m_cmdIn;             // Command line input source
    private OutputChannel m_consoleOut;              // Console output sink
    
    public Compiler()
    {
        StaticMemory.initialize();
        m_tokenizer = new Analyzer();
        m_tokenStream = new BufferedTokenStream(m_tokenizer);
        m_vCpu = new VirtualCPU();
        m_editorIn = null;
        m_cmdIn = null;
        m_consoleOut = null;
    }
    
    /**
     * Initializes console output
     * @param console Console output area
     */
    public void initConsoleOut(JTextArea console)
    {
        m_consoleOut = new ConsoleOutputChannel(0, console);
        m_vCpu.initializeIO(m_consoleOut);
    }
    
    /**
     * Initializes the text editor input channel.
     * @param editor Text editor pane
     * @return Initialized input channel
     */
    public InputChannel initInputSource(JTextPane editor)
    {
        m_editorIn = new EditorInputChannel(0, editor);
        return m_editorIn;
    }
    
    /**
     * Initializes the command line input channel.
     * @param cmdline Command line input field
     * @return Initialized input channel
     */
    public InputChannel initInputSource(JTextField cmdline)
    {
        m_cmdIn = new CommandInputChannel(0, cmdline);
        return m_cmdIn;
    }
    
    /**
     * Executes the JIT compiler on a ready input source.
     * @throws IOException 
     */
    public void run() throws IOException
    {
        // Reinitialize tokenizer and generate new token stream
        m_tokenizer.reset();
        m_tokenStream.clear();

        // Initialize tokenizer to use ready input source
        boolean consoleIn = false;
        if (m_editorIn.isReady()) {
            m_editorIn.reset();
            m_tokenizer.init(m_editorIn);
        }
        else if (m_cmdIn.isReady()) {
            m_cmdIn.reset();
            m_tokenizer.init(m_cmdIn);
            consoleIn = true;
        }
        
        // Compile in-memory executable
        InstructionBuilder builder = new InstructionBuilder();
        CNode codeTree;
        if (consoleIn) {
            try {
                codeTree = Productions.commandLine(m_tokenStream);
            } catch (ParseException ex) {
                Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        }
        else
            codeTree = Productions.statementBlock(m_tokenStream);
        codeTree.execInstrGen(builder);
        List<Instruction> compiled = builder.commit();
        
        // Execute compiled program
        if (compiled != null)
            exec(compiled);
        else
            return;
        
        // If command entered on command line then grab result in the virtual
        // machine's accumulator and send to console
        if (consoleIn) {
            StringBuilder out = new StringBuilder();
            m_vCpu.getAccumulatorValue(out);
            out.append("\n");
            m_consoleOut.send(
                OutputChannel.Type.StdOut, out.toString()
            );
        }
    }
    
    /**
     * Executes a compiled program on the virtual machine
     * TODO: Move this into VirtualMachine class
     * @param program Compiled program
     */
    private void exec(List<Instruction> program)
    {
        m_vCpu.load(program);
        m_vCpu.execute();
    }
}
