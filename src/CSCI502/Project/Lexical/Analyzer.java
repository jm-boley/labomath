package CSCI502.Project.Lexical;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.List;
import javax.swing.JTextField;

/**
 * Lexical analyzer. Streams characters from an input file and attempts to generate
 * a stream of tokens. Unrecognized character sequences which can not be resolved
 * to two or more alternate, recognizable sequences are subsumed and returned as
 * error tokens.
 * @author Joshua Boley
 */
public class Analyzer
{
    private PushbackInputStream m_ifs;   // Input stream (may be file or string from JTextField
    StateMachine m_stateMachine;    // State machine
    int startLineNo, startColNo,    // Parse starting line and column
        currLineNo, currColNo,      // Current line and column
        seqno;                      // Next token sequence number
    
    public Analyzer ()
    {
        m_stateMachine = new StateMachine();
        startLineNo = 0;
        startColNo = 0;
        currLineNo = 0;
        currColNo = 0;
        seqno = 0;
    }
    
    /**
     * Takes a file path and attempts to open it for parsing.
     * @param fpath Path to script file
     * @throws java.io.IOException 
     */
    public void init (String fpath) throws IOException
    {
        m_ifs = new PushbackInputStream(new FileInputStream(fpath));
        startLineNo = 0;
        startColNo = 0;
        currLineNo = 0;
        currColNo = 0;
        seqno = 0;
    }
    
    /**
     * Retrieves text from the command line and prepares for parsing.
     * @param tfCommandLine Command line text field.
     */
    public void init (JTextField tfCommandLine)
    {
        StringBuilder command = new StringBuilder();
        command.append(tfCommandLine.getText());
        command.append(";");
        InputStream in = new ByteArrayInputStream(
            command
                .toString()
                .getBytes()
            );
        m_ifs = new PushbackInputStream(in);
        startLineNo = 0;
        startColNo = 0;
        currLineNo = 0;
        currColNo = 0;
        seqno = 0;
    }

    /**
     * Resets the lexical analyzer for reuse
     */
    public void reset()
    {
        m_stateMachine.reset();
        startLineNo = 0;
        startColNo = 0;
        currLineNo = 0;
        currColNo = 0;
        seqno = 0;
    }
    
    /**
     * Parses out the next token from the input stream.
     * @return Token
     * @throws IOException 
     */
    public Token getNextToken () throws IOException
    {
        if (m_ifs == null)
            throw new RuntimeException("Analyzer::getNextToken() called before init()");
        TSCode rc = TSCode.NONE;
        Token rtoken = Token.NONE;
        StringBuilder tokenCharStream = new StringBuilder();
        boolean returnToken = false, dropToken = false;

        int val;
        while ((val = m_ifs.read()) != -1) {
            // Get the next char from the file for parsing and advance the state machine
            char in = (char) val;

            // Advance the state machine and add character to token string if not LF/CR or not leading
            // whitespace (where tokenization state code will be NONE)
            m_stateMachine.advance (in);
            rc = m_stateMachine.getCurrentTSCode();
            if (rc != TSCode.NONE && in != '\n' && in != '\r')
                tokenCharStream.append(in);

            // If machine is stopped then check machine's current state to see if a valid token has been parsed
            if (m_stateMachine.stopped()) {
                if (rc == TSCode.UNKNOWN) {
                    // Parsing error, back up in the machine's code history until the last previous valid
                    // tokenization state code is found, also backing up through the file input stream
                    List<TSCode> codeHistory = m_stateMachine.getTSCodeChain ();
                    StringBuilder tokenCharStreamBackup = new StringBuilder(tokenCharStream);

                    // Immediately back up one place in input stream
                    m_ifs.unread(in);
                    --currColNo;
                    tokenCharStream.deleteCharAt(tokenCharStream.length() - 1);

                    for (int i = codeHistory.size() - 1; i >= 0; --i) {
                        // Remove last char in token character stream and move back one position in file
                        // input stream
                        int last = tokenCharStream.length() - 1;
                        m_ifs.unread(tokenCharStream.charAt(last));
                        tokenCharStream.deleteCharAt(last);
                        --currColNo;
                        --last;
                        
                        // Break if a valid token code is found (note: TSCode.NONE should never be
                        // encountered; finding one indicates a state transition design error)
                        if ((rc = codeHistory.get(last)) != TSCode.UNKNOWN)
                            break;
                    }

                    // If no parsable token has been identified, then create an unknown token with original
                    // character stream and advance input stream back to last-read char
                    if (rc == TSCode.UNKNOWN) {
                        String tokenStr = tokenCharStreamBackup.toString ();
                        rtoken = createToken (rc, startLineNo, startColNo, seqno, tokenStr);
                        for (int i = 0; i < tokenStr.length(); ++i)
                            m_ifs.read();
                    }
                    else
                        rtoken = createToken (rc, startLineNo, startColNo, seqno, tokenCharStream.toString());
                    m_stateMachine.reset ();
                    returnToken = true;
                }
                else {
                    // Check if input should be backed up a character
                    if (m_stateMachine.rewind() && in != '\n') {
                        --currColNo;
                        m_ifs.unread(in);
                        tokenCharStream.deleteCharAt(tokenCharStream.length() - 1);
                    }

                    // Normal stop without whitepace delimiter, get the current tokenization state code
                    // and create a new token
                    m_stateMachine.reset();

                    // If state machine has parsed a comment then throw it out and continue, otherwise
                    // break and return the token.
                    if (rc == TSCode.COMMENT) {
                        // Clear token character stream contents
                        tokenCharStream.delete(0, tokenCharStream.length() - 1);
                        dropToken = true;
                    }
                    else {
                        rtoken = createToken(rc, startLineNo, startColNo, seqno, tokenCharStream.toString());
                        returnToken = true;
                    }
                }
            }

            // Increment currLineNo, currColNo or roll to next line as appropriate
            if (in == '\n') {
                ++currLineNo;
                currColNo = 0;
            }
            else
                ++currColNo;

            // Increment token sequence number, modify start, current line and col no.s
            // as needed and drop the token or break from the loop and return the token
            if (dropToken || returnToken || (rc == TSCode.NONE && in == '\n')) {
                startLineNo = currLineNo;
                startColNo = currColNo;
                if (dropToken) {
                    dropToken = false;
                    continue;
                }
                else if (returnToken) {
                    ++seqno;
                    returnToken = false;
                    break;
                }
            }
        }

        // Check for incomplete string parse (EOF encountered before trailing double-quote)
        if (rc == TSCode.STRING_PARTIAL)
            rtoken = createToken (TSCode.STRING_INVALID, startLineNo, startColNo, seqno, tokenCharStream.toString());

        // Return an empty token if nothing could be parsed from the file
        return rtoken;
    }
    
    /**
     * Closes the input stream
     * @throws IOException 
     */
    public void cleanup () throws IOException
    {
        m_ifs.close();
    }
    
    /**
     * Creates a new token with the given parameters
     * @param tscode    Token code
     * @param lineno    Token's incident line number
     * @param colno     Token's incident column number
     * @param seqno     Token's sequence number
     * @param tokenStr  String capture
     * @return Token
     */
    private Token createToken(TSCode tscode, int lineno, int colno, int seqno, String tokenStr)
    {
        Token rtoken = new Token(tscode);
        rtoken.setLineNo(lineno);
        rtoken.setColNo(colno);
        rtoken.setSeqNo(seqno);

        // If a string token, then post-process escaped characters
        if (tscode == TSCode.STRING || tscode == TSCode.STRING_INVALID) {
            StringBuilder processed = new StringBuilder();
            boolean isEscaped = false;

            for (int i = 0; i < tokenStr.length(); ++i) {
                switch (tokenStr.charAt(i)) {
                case '\\':
                    isEscaped = true;
                    break;
                default:
                    if (isEscaped) {
                        switch (tokenStr.charAt(i)) {
                        case 'n':   // Newline
                            processed.append('\n');
                            break;
                        case 't':   // Tab
                            processed.append('\t');
                            break;
                        case 'r':   // Carriage return
                            processed.append('\r');
                            break;
                        case '\"':  // Double-quote
                            processed.append('\"');
                            break;
                        case '\\':  // Back-slash
                            processed.append('\\');
                            break;
                        case 'b':   // Backspace
                            processed.append('\b');
                            break;
                        default:
                            processed
                                .append('\\')
                                .append(tokenStr.charAt(i));
                        }
                        isEscaped = false;
                    }
                    else
                        processed.append(tokenStr.charAt(i));
                }
            }
            rtoken.setValue(processed.toString());
        }
        else
            rtoken.setValue(tokenStr);

        return rtoken;
    }
}
