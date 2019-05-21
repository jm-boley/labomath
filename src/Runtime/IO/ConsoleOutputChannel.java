package Runtime.IO;

import GUI.CmdLinePanel;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 * Output channel encapsulating the GUI application's built-in console.
 * @author Joshua Boley
 */
public class ConsoleOutputChannel
    extends OutputChannel<JTextArea>
{
    public ConsoleOutputChannel(int cmid, JTextArea output)
    {
        super(cmid, output);
    }

    /**
     * Clears the built-in console
     */
    @Override
    public void sendClear()
    {
        m_output.setText("");
    }
    
    /**
     * Appends new output to the console
     * @param otype Output type, controls the formatting of console text
     * @param output New line(s) of content
     */
    @Override
    public void send(Type otype, Object output)
    {
        switch (otype) {
            case StdOut:
                m_output.setForeground(new Color(245, 245, 245));
                break;
            case StdErr:
                m_output.setForeground(Color.RED);
                break;
            default:
                Logger.getLogger(ConsoleOutputChannel.class.getName()).log(Level.SEVERE, null, "Unsupported output type " + otype.name());
                throw new RuntimeException("");
        }
        m_output.append((String) output);
    }
    
    public void resetFontColor()
    {
        m_output.setForeground(CmdLinePanel.STD_FONT_CLR);
    }
}
