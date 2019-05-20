package Runtime.IO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.swing.JTextPane;

/**
 * Input channel encapsulating the text editor.
 * @author Joshua Boley
 */
public class EditorInputChannel
    extends InputChannel<JTextPane>
{
    public EditorInputChannel(int cmid, final JTextPane input)
    {
        super(cmid, input);
    }

    @Override
    public InputStream getInputStream()
    {
        return new ByteArrayInputStream(m_input.getText().getBytes());
    }
}
