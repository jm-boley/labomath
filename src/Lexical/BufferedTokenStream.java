package Lexical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a buffered container for a dynamically updated sequence of Token
 * objects and provides an interface for incremental reading and backtracking
 * as tokens are parsed by the lexical analyzer.
 * @author Joshua Boley
 */
public class BufferedTokenStream
{
    private final Analyzer m_analyzer;
    private final List<Token> m_tokenBuffer;
    private int m_index;
    private boolean m_eos;
    private Token m_marked;
    private static final int DEFAULT_BUFF_SZ = 5;
    
    public BufferedTokenStream(Analyzer analyzer)
    {
        m_analyzer = analyzer;
        m_tokenBuffer = new ArrayList<>();
        m_index = 0;
        m_eos = false;
        m_marked = null;
    }
    
    public Token read()
    {
        if (m_eos)
            return null;
        Token rval;
        if (m_index == m_tokenBuffer.size()) {
            // Get next token from analyzer
            try {
                rval = m_analyzer.getNextToken();
            } catch (IOException ex) {
                Logger.getLogger(BufferedTokenStream.class.getName()).log(Level.SEVERE, null, ex);
                m_eos = true;
                return null;
            }
            
            if (m_tokenBuffer.size() >= DEFAULT_BUFF_SZ) {
                // If the first token in the buffer hasn't been marked then remove it
                if (m_tokenBuffer.get(0) != m_marked) {
                    m_tokenBuffer.remove(m_tokenBuffer.get(0));
                    // If possible shrink the buffer by 1
                    if (m_tokenBuffer.size() > DEFAULT_BUFF_SZ && m_tokenBuffer.get(0) != m_marked) {
                        m_tokenBuffer.remove(m_tokenBuffer.get(0));
                        --m_index;
                    }
                }
                // Otherwise we'll need to grow the buffer
                else
                    ++m_index;
            }
            if (!rval.equals(Token.NONE)) {
                m_tokenBuffer.add(rval);
                if (m_index < DEFAULT_BUFF_SZ)
                    ++m_index;
            }
            else {
                rval = null;
                m_eos = true;
            }
        }
        else {
            /* If current read index is less than index of last token read into buffer
               (i.e., from unread() or rewind()) then return the token from the buffer at
               that index */
            rval = m_tokenBuffer.get(m_index++);
        }
        
        return rval;
    }
    
    public boolean unread(Token putback)
    {
        if (m_tokenBuffer.get(m_index - 1) != putback)
            return false;
        --m_index;
        return true;
    }
    
    public boolean rewind(Token rewindMark)
    {
        int currIdx = (m_index < m_tokenBuffer.size()) ? m_index : --m_index;
        while (currIdx >= 0 && m_tokenBuffer.get(currIdx) != rewindMark)
            --currIdx;
        if (m_tokenBuffer.get(currIdx) != rewindMark)
            return false;
        m_index = currIdx;
        m_eos = false;
        return true;
    }
    
    public boolean atEOS()
    {
        return m_eos;
    }
    
    public boolean mark(Token marker)
    {
        for (Token candidate : m_tokenBuffer) {
            if (candidate == marker) {
                m_marked = marker;
                return true;
            }
        }
        return false;
    }
    
    public void unmark()
    {
        m_marked = null;
    }
    
    public void clear()
    {
        m_tokenBuffer.clear();
        m_index = 0;
        m_eos = false;
        m_marked = null;
    }
}
