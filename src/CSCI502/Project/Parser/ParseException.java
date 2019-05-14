package CSCI502.Project.Parser;

import CSCI502.Project.Lexical.Token;

/**
 * Parsing exception class, includes offending token
 * @author Joshua Boley
 */
public class ParseException
        extends Exception
{
    private final Token m_tok;
    
    public ParseException(String message, Token unparsable)
    {
        super(message);
        m_tok = unparsable;
    }
    
    public ParseException(String message, Throwable cause, Token unparsable)
    {
        super(message, cause);
        m_tok = unparsable;
    }
    
    public Token getUnparsable()
    {
        return m_tok;
    }
}

