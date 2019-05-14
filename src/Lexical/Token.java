package Lexical;

/**
 *
 * @author boley
 */
public class Token
{
    public static final Token NONE;
    public static final Token UNKNOWN;

    static {
        NONE = new Token(TSCode.NONE);
        UNKNOWN = new Token(TSCode.UNKNOWN);
    }
    
    private final TSCode m_tid; // Token type ID
    private int m_lineno,   // Line number at which token was encountered
                m_colno,    // Column number at which token was encountered
                m_seqno;    // Token's sequence number
    private String m_value; // Value of the token

    public Token (TSCode type)
    {
        m_tid = type;
    }
    
//    public Token (Token rhs)
//    {
//        
//    }

//    Token& operator= (const Token& other);

    /*/ Equivalence ops /*/
    public boolean equals(Token rhs) { return m_tid == rhs.m_tid; }

    /*/ Getters /*/
    public TSCode  getId     () { return m_tid; }
    public int     getLineNo () { return m_lineno; }
    public int     getColNo  () { return m_colno; }
    public int     getSeqNo  () { return m_seqno; }
    public String  getValue  () { return m_value; }

    /*/ Setters /*/
    public void setLineNo   (int lineno)    { m_lineno = lineno; }
    public void setColNo    (int colno)     { m_colno = colno; }
    public void setSeqNo    (int seqno)     { m_seqno = seqno; }
    public void setValue    (String value)  { m_value = value; }

    @Override
    public String toString()
    {
        StringBuilder oss = new StringBuilder();
        oss
            .append("Token:{ ").append(TSCode.serialize(m_tid))
            .append(" [").append(m_lineno).append(", ").append(m_colno)
            .append("] seq#:").append(m_seqno)
            .append(" val:").append(m_value)
            .append(" }");
        return oss.toString();
    }
}
