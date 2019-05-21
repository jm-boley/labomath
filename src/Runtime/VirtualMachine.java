package Runtime;

import Runtime.IO.CommandInputChannel;
import Runtime.IO.ConsoleOutputChannel;
import Runtime.IO.EditorInputChannel;
import Runtime.IO.InputChannel;
import Runtime.IO.OutputChannel;
import Runtime.IONode.SinkType;
import Runtime.IONode.SourceType;
import Runtime.JIT.API.Instruction;
import Runtime.JIT.Compiler;
import Runtime.Machine.VirtualCPU;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * Implements the JIT compilation and code execution environment.
 * @author Joshua Boley
 */
public class VirtualMachine
//    extends Thread
{
    private enum IOType
    {
        in,
        out;
    }

    private static int nextCMID;                        // Next available CMID
    private final Compiler m_compiler;                  // JIT compiler
    private final VirtualCPU m_vCpu;                    // Emulated CPU
    private final List<InputChannel> m_inputSources;    // Input sources
    private final List<OutputChannel> m_outputSinks;    // Output sinks
    private final Map<Integer, EnumMap<IOType, List<IONode>>> m_IOMap;
                                                        // Client IONode map

    static {
        nextCMID = 0;
    }
    
    public VirtualMachine()
    {
        m_compiler = new Compiler();
        m_vCpu = new VirtualCPU();
        m_inputSources = new ArrayList<>();
        m_outputSinks = new ArrayList<>();
        m_IOMap = new HashMap<>();
    }
        
    /**
     * Initializes a text editor input channel for the GUI client. A reserved
     * CMID (channel-mapping identifier) is automatically used to associate this
     * input channel with the GUI client's output channel(s).
     * @param editor Text editor pane
     * @return Initialized input channel
     */
    public synchronized InputChannel initLocalInputChannel(JTextPane editor)
    {
        // Create nested I/O node map structures as required
        if (!m_IOMap.containsKey(0))
            m_IOMap.put(0, new EnumMap<>(IOType.class));
        EnumMap<IOType, List<IONode>> clientMap = m_IOMap.get(0);
        if (!clientMap.containsKey(IOType.in))
            clientMap.put(IOType.in, new ArrayList<>());
        
        // Verify that a duplicate is not about to be created, log and throw
        // if it is
        clientMap.get(IOType.in).forEach((IONode chNode) -> {
            if (chNode.getSource() == SourceType.editor) {
                Logger.getLogger(VirtualMachine.class.getName()).log(
                    Level.SEVERE, null,
                    "Attempted to create duplicate local editor input channel"
                );
                throw new RuntimeException("");
            }
        });
        
        // Create new editor input channel
        int cmid = nextCMID++;
        InputChannel editorIn = new EditorInputChannel(cmid, editor);
        m_inputSources.add(editorIn);
        
        // Create I/O node
        clientMap.get(IOType.in).add(
            new IONode(cmid, SourceType.editor, SinkType.none)
        );
        
        return editorIn;
    }
    
    /**
     * Initializes a command line input channel for the GUI client. A reserved
     * CMID (channel-mapping identifier) is automatically used to associate this
     * input channel with the GUI client's output channel(s).
     * @param cmdline Command line input field
     * @return Initialized input channel
     */
    public synchronized InputChannel initLocalInputChannel(JTextField cmdline)
    {
        // Create nested I/O node map structures as required
        if (!m_IOMap.containsKey(0))
            m_IOMap.put(0, new EnumMap<>(IOType.class));
        EnumMap<IOType, List<IONode>> clientMap = m_IOMap.get(0);
        if (!clientMap.containsKey(IOType.in))
            clientMap.put(IOType.in, new ArrayList<>());
        
        // Verify that a duplicate is not about to be created, log and throw
        // if it is
        clientMap.get(IOType.in).forEach((IONode chNode) -> {
            if (chNode.getSource() == SourceType.console) {
                Logger.getLogger(VirtualMachine.class.getName()).log(
                    Level.SEVERE, null,
                    "Attempted to create duplicate local console input channel"
                );
                throw new RuntimeException("");
            }
        });

        int cmid = nextCMID++;
        InputChannel cmdIn = new CommandInputChannel(cmid, cmdline);
        m_inputSources.add(cmdIn);
        
        // Create I/O node
        clientMap.get(IOType.in).add(
            new IONode(cmid, SourceType.console, SinkType.none)
        );
        
        return cmdIn;
    }
    
    /**
     * Initializes console output
     * @param console Console output area
     */
    public synchronized void initLocalOutputChannel(JTextArea console)
    {
        // Create nested I/O node map structures as required
        if (!m_IOMap.containsKey(0))
            m_IOMap.put(0, new EnumMap<>(IOType.class));
        EnumMap<IOType, List<IONode>> clientMap = m_IOMap.get(0);
        if (!clientMap.containsKey(IOType.out))
            clientMap.put(IOType.out, new ArrayList<>());
        
        // Verify that a duplicate is not about to be created, log and throw
        // if it is
        clientMap.get(IOType.out).forEach((IONode chNode) -> {
            if (chNode.getTarget() == SinkType.console) {
                Logger.getLogger(VirtualMachine.class.getName()).log(
                    Level.SEVERE, null,
                    "Attempted to create duplicate local console output channel"
                );
                throw new RuntimeException("");
            }
        });

        // Create output channel
        int cmid = nextCMID++;
        OutputChannel consoleOut = new ConsoleOutputChannel(cmid, console);
        m_outputSinks.add(consoleOut);
        
        // Create I/O node
        clientMap.get(IOType.out).add(
            new IONode(cmid, SourceType.none, SinkType.console)
        );
    }

//    @Override
    public void run()
    {
        m_IOMap.forEach((Integer clientId, EnumMap<IOType, List<IONode>> clientChannels) -> {
            // Check input channels
            InputChannel in = null;
            IONode srcNode = null;
            for (IONode channel : clientChannels.get(IOType.in)) {
                for (InputChannel candidate : m_inputSources) {
                    if (candidate.getChannelMapping() == channel.getCMID() && candidate.isReady()) {
                        candidate.reset();
                        in = candidate;
                        srcNode = channel;
                        break;
                    }
                }
            }
            if (in == null)
                return;
            
            // Get corresponding output channels (local/remote console, local/remote GUI update)
            OutputChannel consoleOut = null,
                          guiOut = null;
            for (IONode channel : clientChannels.get(IOType.out)) {
                for (OutputChannel candidate : m_outputSinks) {
                    if (candidate.getChannelMapping() == channel.getCMID()) {
                        switch (channel.getTarget()) {
                            case console:
                            case remote_console:
                                consoleOut = candidate;
                                break;
                            case gui:
                            case remote_gui:
                                guiOut = candidate;
                        }
                    }
                }
            }
            if (consoleOut == null)
                return;
            
            // Initialize compiler and VCPU
            m_compiler.setInputChannel(in);
            boolean isCommand = false;
            switch (srcNode.getSource()) {
                case console:
                case remote_console:
                    isCommand = true;
                default:;   // Do nothing
            }
            
            // Compile program from source
            List<Instruction> program = null;
            try {
                program = m_compiler.run(isCommand);
            } catch (IOException ex) {
                Logger.getLogger(VirtualMachine.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
            if (program == null)
                return;
            
            // Execute program on VCPU
            m_vCpu.initializeIO(consoleOut);
            m_vCpu.load(program);
            m_vCpu.execute();
        
            // If command entered on command line then grab result in the virtual
            // machine's accumulator and send to client's console
            if (isCommand) {
                StringBuilder out = new StringBuilder();
                m_vCpu.getAccumulatorValue(out);
                out.append("\n");
                consoleOut.send(
                    OutputChannel.Type.StdOut, out.toString()
                );
            }
        });
    }
}
