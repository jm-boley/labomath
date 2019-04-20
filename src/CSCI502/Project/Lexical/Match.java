package CSCI502.Project.Lexical;

/**
 * Common regex match strings
 * @author Joshua Boley
 */
enum Match
{
    WS("\\s"),
    WS_NO_NEWLINE("(?!\\n)\\s"),
    ALL(".|\\n"),
    CHAR_DOT("\\."),
    NEWLINE("\\n"),
    ALPHA_NUM_US("[a-zA-Z0-9_]"),
    NUMERIC("[0-9]"),
    SCI_EXP("[Ee]"),
    NOT_RANGLE_BR("(?![>-]).|\\n"),
    DOUBLE_QUOT("\"");
    
    private final String regStr;
    private Match(String regStr)
    {
        this.regStr = regStr;
    }
    
    @Override
    public String toString()
    {
        return this.regStr;
    }
}
