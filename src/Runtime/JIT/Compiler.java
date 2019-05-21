package Runtime.JIT;

import Lexical.Analyzer;
import Lexical.BufferedTokenStream;
import Parsing.CNode;
import Parsing.ParseException;
import Parsing.Productions;
import Runtime.JIT.API.Instruction;
import Runtime.JIT.API.InstructionBuilder;
import Runtime.IO.InputChannel;
import Runtime.IO.OutputChannel;
import Runtime.Machine.StaticMemory;
import Runtime.Machine.VirtualCPU;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the JIT compiler runtime.
 * @author Joshua Boley
 */
public class Compiler
{
    private final Analyzer m_tokenizer;              // Lexical analyzer (tokenizer)
    private final BufferedTokenStream m_tokenStream; // Token stream output end, used by tokenizer
    private final VirtualCPU m_vCpu;                 // Virtual machine (CPU)
    private InputChannel m_chIn;                     // Text editor input source
    
    public Compiler()
    {
        StaticMemory.initialize();
        m_tokenizer = new Analyzer();
        m_tokenStream = new BufferedTokenStream(m_tokenizer);
        m_vCpu = new VirtualCPU();
        m_chIn = null;
    }

    public void setInputChannel(InputChannel in)
    {
        m_chIn = in;
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
        if (isCommand) {
            try {
                codeTree = Productions.commandLine(m_tokenStream);
            } catch (ParseException ex) {
                Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        else
            codeTree = Productions.statementBlock(m_tokenStream);
        
        // Perform code (tree) optimizations
        
        // Execute machine instruction generation and return compiled program
        InstructionBuilder builder = new InstructionBuilder();
        codeTree.execInstrGen(builder);
        return builder.commit();
        
        // Execute compiled program
//        if (compiled != null)
//            exec(compiled);
//        else
//            return;
        
        // If command entered on command line then grab result in the virtual
        // machine's accumulator and send to console
//        if (consoleIn) {
//            StringBuilder out = new StringBuilder();
//            m_vCpu.getAccumulatorValue(out);
//            out.append("\n");
//            m_consoleOut.send(
//                OutputChannel.Type.StdOut, out.toString()
//            );
//        }
    }
    
    /**
     * Executes a compiled program on the virtual machine
     * TODO: Move this into VirtualMachine class
     * @param program Compiled program
     */
//    private void exec(List<Instruction> program)
//    {
//        m_vCpu.load(program);
//        m_vCpu.execute();
//    }
}
