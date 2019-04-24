package CSCI502.Project.Parsing;

import CSCI502.Project.Lexical.Analyzer;
import CSCI502.Project.Lexical.Token;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;

/**
 *
 * @author boley
 */
public class Interpreter
{
    private final Analyzer tokenizer;
    
    public Interpreter()
    {
        tokenizer = new Analyzer();
    }
    
    public String run (Object input) throws IOException
    {
        // Reinitialize tokenizer and generate new token stream
        tokenizer.reset();
        if (input instanceof String)
            tokenizer.init((String) input);
        else if (input instanceof JTextField)
            tokenizer.init((JTextField) input);
        List<Token> tokenStream = generateTokenStream();
        
        // DEBUG
        StringBuilder tokenStreamStr = new StringBuilder();
        tokenStream.forEach((token) -> {
            tokenStreamStr
                    .append(token.toString())
                    .append("\n");
        });
        
        return tokenStreamStr.toString();
    }
    
    private List<Token> generateTokenStream() throws IOException
    {
        List<Token> stream = new ArrayList<>();
        Token token;
        while ((token = tokenizer.getNextToken()) != Token.NONE)
            stream.add(token);
        return stream;
    }
}
