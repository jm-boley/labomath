package Runtime;

import Runtime.IO.CommandInputChannel;
import Runtime.IO.ConsoleOutputChannel;
import Runtime.IO.EditorInputChannel;
import Runtime.IO.InputChannel;
import Runtime.IO.OutputChannel;
import Runtime.JIT.Compiler;
import Runtime.Machine.VirtualCPU;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * Implements the JIT compilation and code execution environment.
 * @author Joshua Boley
 */
public class VirtualMachine
    extends Thread
{
    private final Compiler m_compiler;  // JIT compiler
    private final VirtualCPU m_emuCpu;  // Emulated CPU
    private final List<InputChannel> m_inputSources;    // Input sources
    private final List<OutputChannel> m_outputSinks;    // Output sinks

    public VirtualMachine()
    {
        m_compiler = new Compiler();
        m_emuCpu = new VirtualCPU();
        m_inputSources = new ArrayList<>();
        m_outputSinks = new ArrayList<>();
    }
        
    /**
     * Initializes a text editor input channel for the GUI client. A reserved
     * CMID (channel-mapping identifier) is automatically used to associate this
     * input channel with the GUI client's output channel(s).
     * @param editor Text editor pane
     * @return Initialized input channel
     */
    public InputChannel initInputSource(JTextPane editor)
    {
        InputChannel editorIn = new EditorInputChannel(0, editor);
        m_inputSources.add(editorIn);
        return editorIn;
    }
    
    /**
     * Initializes a command line input channel for the GUI client. A reserved
     * CMID (channel-mapping identifier) is automatically used to associate this
     * input channel with the GUI client's output channel(s).
     * @param cmdline Command line input field
     * @return Initialized input channel
     */
    public InputChannel initInputSource(JTextField cmdline)
    {
        InputChannel cmdIn = new CommandInputChannel(0, cmdline);
        m_inputSources.add(cmdIn);
        return cmdIn;
    }
    
    /**
     * Initializes console output
     * @param console Console output area
     */
    public void initOutputSink(JTextArea console)
    {
        OutputChannel consoleOut = new ConsoleOutputChannel(0, console);
        m_outputSinks.add(consoleOut);
        //m_emuCpu.initializeIO(m_out); TODO: Create I/O mapping data structure
    }

    @Override
    public void run()
    {
        
    }
}
