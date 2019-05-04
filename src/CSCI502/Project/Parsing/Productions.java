/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSCI502.Project.Parsing;

import CSCI502.Project.ExecLib.Operand;
import CSCI502.Project.Runtime.Machine.Register;
import CSCI502.Project.Lexical.BufferedTokenStream;
import CSCI502.Project.Lexical.TSCode;
import CSCI502.Project.Lexical.Token;
import java.util.function.BiFunction;

class ParseException
        extends Exception
{
    private final Token m_tok;
    
    ParseException(String message, Token unparsable)
    {
        super(message);
        m_tok = unparsable;
    }
    
    ParseException(String message, Throwable cause, Token unparsable)
    {
        super(message, cause);
        m_tok = unparsable;
    }
    
    Token getUnparsable()
    {
        return m_tok;
    }
}

abstract class StatusFlags
{
    static boolean parsingFailed = false;
}

/**
 * Recursive descent parser production rules implementation
 * @author Joshua Boley
 */
class Productions
{
    private static void discardStatement(BufferedTokenStream tokenStream) throws ParseException
    {
        Token nextToken;
        while ((nextToken = tokenStream.read()) != null && nextToken.getId() != TSCode.SEMICOLON) {}
        if (nextToken == null)
            throw new ParseException("EOF", null);
    }
    
    private static void consumeTrailingSemicolon(BufferedTokenStream tokenStream) throws ParseException
    {
        Token semicolToken = tokenStream.read();
        if (semicolToken == null)
            throw new ParseException("EOS", null);
        else if (semicolToken.getId() != TSCode.SEMICOLON)
            throw new ParseException("Unexpected symbol/keyword", semicolToken);
    }
    /**
     * Production rule for statement blocks. Only used with scripts.
     * @param tokenStream Token stream
     * @return Instruction generation tree
     */
    public static CNode statementBlock (BufferedTokenStream tokenStream)
    {
        BiFunction<CNode, InstructionBuilder, Integer> injected = (CNode thisNode, InstructionBuilder builder) -> {
            for (int i = 0; i < CNode.numChildren (thisNode); ++i)
                CNode.getChild (thisNode, i).execCodeGen (builder);
            return builder.getActiveCodeSegmentId ();
        };
        Token blockToken = new Token(TSCode.NONE);
        blockToken.setValue ("{}");
        CNode statementBlockRoot  = new CNode(blockToken, injected);

        Token nextToken;
        while ((nextToken = tokenStream.read()) != null && nextToken.getId() != TSCode.RBRACKET) {
            tokenStream.unread(nextToken);  // Put token back into stream for next production

            CNode statementNode = Productions.statement (tokenStream);
            if (statementNode == null)
                break;
            statementNode.setParent(statementBlockRoot);
            CNode.addChild(statementBlockRoot, statementNode);
        }

        return statementBlockRoot;
    }
    
    public static CNode statement (BufferedTokenStream tokenStream)
    {
        // Skip as many comment tokens as required, until code found or end of token stream reached
        Token nextToken;
        if ((nextToken = tokenStream.read()) == null || nextToken.getId() == TSCode.COMMENT || nextToken.getId() == TSCode.SEMICOLON)
            return null;

        CNode statementNode;
        // Attempt to parse assignment statement
        try {
            statementNode = Productions.assignment (tokenStream);
        }
        catch (ParseException ex) {
            // Throw away remainder of offending statement and continue
            try {
                discardStatement(tokenStream);
            }
            catch (ParseException ex2) {
                
            }
            StatusFlags.parsingFailed = true;
            return null;
        }

        // Attempt to parse print statement
        if (statementNode == null) {
            try {
                statementNode = Productions.print(tokenStream);
            }
            catch (ParseException ex) {
                try {
                    // Throw away remainder of offending statement and continue
                    discardStatement (tokenStream);
                } catch (ParseException ex1) {
                    
                }
                StatusFlags.parsingFailed = true;
                return null;
            }
        }

        // Attempt to parse arithmetic statement (command line)
        if (statementNode == null) {
            try {
                statementNode = Productions.arithmetic(tokenStream);
            }
            catch (ParseException ex) {
                try {
                    // Throw away remainder of offending statement and continue
                    discardStatement(tokenStream);
                } catch (ParseException ex1) {
                    
                }
                StatusFlags.parsingFailed = true;
                return null;
            }
        }

        // If no statements could be parsed then flag a compilation error and throw away remainder of current statement
        if (statementNode == null && !StatusFlags.parsingFailed) {
            try {
                // Syntax error, unrecognized keyword
                discardStatement(tokenStream);
            } catch (ParseException ex) {
                
            }
            StatusFlags.parsingFailed = true;
            return null;
        }

        return statementNode;
    }
    
    static CNode assignment(BufferedTokenStream tokenStream) throws ParseException
    {
        // Get lvalue node
        Token begin = tokenStream.read();   // Keep reference to start position in the token stream in case parsing fails
        if (begin == null) {
            // Print unexpected end of token stream error and throw runtime_error
            throw new ParseException("EOF", null);
        }
        tokenStream.mark(begin);
        tokenStream.unread(begin);
        CNode lvalNode = Productions.lvalue (tokenStream);
        if (lvalNode == null) {
            tokenStream.rewind(begin);
            tokenStream.unmark();
            return null;
        }

        // Extract assignment operator
        Token assignToken = tokenStream.read();
        if (assignToken == null)
            throw new ParseException("EOF", null);
        if (assignToken.getId () != TSCode.ASSIGN) {
            tokenStream.rewind(begin);
            tokenStream.unmark();
            return null;
        }
        
        // Get rvalue node, abort with error if expression can't be parsed
        CNode rvalNode = Productions.rvalue(tokenStream);
        if (rvalNode == null) {
            tokenStream.rewind(begin);
            tokenStream.unmark();
            return null;
        }
        
        consumeTrailingSemicolon(tokenStream);

        // Perform type safety check
        // TODO: Expand this to include type conversion node generation where types may be implicitly converted
        if (!lvalNode.getValType().equals(rvalNode.getValType()))
            throw new ParseException("Type mismatch in assignment", null);

        BiFunction<CNode, InstructionBuilder, Integer> injected = (CNode thisNode, InstructionBuilder builder) -> {
            CNode.getChild (thisNode, 1).execCodeGen (builder);   // Get rval (result in R1)
            builder.MOV (new Operand(Register.R1), new Operand(Register.R2));
            CNode.getChild (thisNode, 0).execCodeGen (builder);   // Get lval address (result in R1)
            builder.MOV(new Operand(Register.R1), new Operand(Register.R2));

            return builder.getActiveCodeSegmentId ();
        };
        CNode assignNode = new CNode(assignToken, injected);
        lvalNode.setParent(assignNode);
        rvalNode.setParent(assignNode);
        CNode.addChild(assignNode, lvalNode);
        CNode.addChild(assignNode, rvalNode);

        return assignNode;
    }
    
    static CNode arithmetic (BufferedTokenStream tokenStream) throws ParseException
    {
        throw new ParseException("", new Token(TSCode.NONE));
    }
    
    static CNode print (BufferedTokenStream tokenStream) throws ParseException
    {
        throw new ParseException("", new Token(TSCode.NONE));
    }
    
    static CNode lvalue (BufferedTokenStream tokenStream) throws ParseException
    {
        throw new ParseException("", new Token(TSCode.NONE));
    }
    
    static CNode rvalue (BufferedTokenStream tokenStream) throws ParseException
    {
        throw new ParseException("", new Token(TSCode.NONE));
    }
    
    public static CNode addSub (BufferedTokenStream tokenStream) throws ParseException
    {
        throw new ParseException("", new Token(TSCode.NONE));
    }
    
    public static CNode multDivMod (BufferedTokenStream tokenStream) throws ParseException
    {
        throw new ParseException("", new Token(TSCode.NONE));
    }
    
    public static CNode power (BufferedTokenStream tokenStream) throws ParseException
    {
        throw new ParseException("", new Token(TSCode.NONE));
    }
    
    public static CNode negatable (BufferedTokenStream tokenStream) throws ParseException
    {
        throw new ParseException("", new Token(TSCode.NONE));
    }
    
    public static CNode varnumeric (BufferedTokenStream tokenStream) throws ParseException
    {
        throw new ParseException("", new Token(TSCode.NONE));
    }
}
