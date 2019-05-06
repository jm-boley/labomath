package CSCI502.Project.Runtime;

import CSCI502.Project.ExecLib.Instruction;
import CSCI502.Project.ExecLib.VariableStorage;
import CSCI502.Project.Lexical.Analyzer;
import CSCI502.Project.Lexical.BufferedTokenStream;
import CSCI502.Project.Lexical.Token;
import CSCI502.Project.Parsing.CNode;
import CSCI502.Project.Parsing.InstructionBuilder;
import CSCI502.Project.Parsing.ParseException;
import CSCI502.Project.Parsing.Productions;
import CSCI502.Project.Runtime.Machine.VirtualMachine;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Joshua Boley
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
        VariableStorage.initialize();
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

        CNode codeTree = null;
        if (input instanceof String) {
            tokenizer.init((String) input);
            codeTree = Productions.statementBlock(m_tokenStream);
        }
        else if (input instanceof JTextField) {
            tokenizer.init((JTextField) input);
            try {
                codeTree = Productions.commandLine(m_tokenStream);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (codeTree == null)
            return;
        InstructionBuilder builder = new InstructionBuilder();
        codeTree.execInstrGen(builder);
        List<Instruction> program = builder.commit();
        m_machine.load(program);
        m_machine.execute();
        StringBuilder sb = new StringBuilder();
        m_machine.getAccumulatorValue(sb);
        sb.append("\n");
        m_console.append(sb.toString());
    }
    
    private void debugGenerateTokenStream() throws IOException
    {
        Token token;
        while ((token = m_tokenStream.read()) != null) {
            m_console.append(token.toString() + "\n");
        }
    }
}
