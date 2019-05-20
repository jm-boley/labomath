package Runtime.IO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.swing.JTextField;

/**
 * Input channel encapsulating the command line.
 * @author Joshua Boley
 */
public class CommandInputChannel
    extends InputChannel<JTextField>
{
    public CommandInputChannel(int cmid, final JTextField input)
    {
        super(cmid, input);
    }

    @Override
    public InputStream getInputStream()
    {
        String cmdString = m_input.getText() + ";";
        return new ByteArrayInputStream(cmdString.getBytes());
    }
}
