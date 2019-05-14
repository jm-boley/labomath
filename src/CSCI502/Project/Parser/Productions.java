/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSCI502.Project.Parser;

import CSCI502.Project.Runtime.Interface.DataType;
import CSCI502.Project.Runtime.Interface.Operand;
import CSCI502.Project.Runtime.Interface.SymbolParams;
import CSCI502.Project.Runtime.Interface.SymbolTable;
import CSCI502.Project.Runtime.Machine.RegId;
import CSCI502.Project.Lexical.BufferedTokenStream;
import CSCI502.Project.Lexical.TSCode;
import CSCI502.Project.Lexical.Token;
import CSCI502.Project.Parser.CompilerErrors.ErrMessage;
import CSCI502.Project.Parser.CompilerErrors.ErrType;
import CSCI502.Project.Parser.CompilerErrors.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Logger;

abstract class StatusFlags
{
    static boolean parsingFailed = false;
}

/**
 * Recursive descent parser production rules implementation
 * @author Joshua Boley
 */
abstract public class Productions
{
    /**
     * Discards the remaining tokens in the current statement (to and including semicolon)
     * @param tokenStream
     * @throws ParseException 
     */
    private static void discardStatement(BufferedTokenStream tokenStream) throws ParseException
    {
        Token nextToken;
        while ((nextToken = tokenStream.read()) != null && nextToken.getId() != TSCode.SEMICOLON) {}
        abortIfEOS(tokenStream);
    }
    
    /**
     * Consumes trailing semicolon
     * @param tokenStream
     * @throws ParseException 
     */
    private static void consumeTrailingSemicolon(BufferedTokenStream tokenStream) throws ParseException
    {
        Token semicolToken = tokenStream.read();
        if (semicolToken == null)
            throw new ParseException("EOS", null);
        else if (semicolToken.getId() != TSCode.SEMICOLON)
            throw new ParseException("Unexpected symbol/keyword", semicolToken);
    }
    
    private static void consumeLParen (BufferedTokenStream tokenStream, ErrType errType) throws ParseException
    {
        Token lparenToken = tokenStream.read();
        if (lparenToken.getId () != TSCode.LPAREN) {
            String errMessage = CompilerErrors.formatErrorMessage(
                lparenToken, Level.ERROR, errType,
                (errType == ErrType.ARITHMETIC) ? ErrMessage.UNMATCHED_LPAREN.toString() : ErrMessage.MISSING_LPAREN.toString()
            );
            throw new ParseException(errMessage, lparenToken);
        }
    }
    
    private static void consumeRParen (BufferedTokenStream tokenStream, ErrType errType) throws ParseException
    {
        Token lparenToken = tokenStream.read();
        if (lparenToken.getId () != TSCode.RPAREN) {
            String errMessage = CompilerErrors.formatErrorMessage(
                lparenToken, Level.ERROR, errType,
                (errType == ErrType.ARITHMETIC) ? ErrMessage.UNMATCHED_RPAREN.toString() : ErrMessage.MISSING_RPAREN.toString()
            );
            throw new ParseException(errMessage, lparenToken);
        }
    }
    
    private static boolean consumeListSeparator (BufferedTokenStream tokenStream) throws ParseException
    {
        Token separator = tokenStream.read();
        if (separator.getId() != TSCode.COMMA) {
            tokenStream.unread(separator);
            return false;
        }
        return false;
    }
    
    private static void abortIfEOS (BufferedTokenStream tokenStream) throws ParseException
    {
        if (tokenStream.atEOS()) {
            String errMessage = CompilerErrors.formatErrorMessage(
                Token.NONE, Level.ERROR, ErrType.COMP_U,
                "Unexpected end of file/line while processing instruction(s)."
            );
            throw new RuntimeException(errMessage);
        }
    }
    
    private static boolean isReserved(String symbol)
    {
        boolean rval = false;
        switch(symbol) {
            case "if":
            case "else":
            case "while":
            case "print":
            case "read":
                rval = true;
            default:
        }
        return rval;
    }
    
    private static void binaryOpTypeCheck (CNode lhs, CNode rhs, Token token, ErrType errType) throws ParseException
    {
	if (!lhs.getValType().equals(rhs.getValType())) {
            String errMessage = CompilerErrors.formatErrorMessage(
                token, Level.ERROR, errType,
                "Operand type mistmatch in binary operation"
            );
            throw new ParseException(errMessage, token);
	}
    }
    
    /**
     * Production rule for the command line. Throws a ParseException if script commands such as print or
     * logic control structure keywords are found.
     * @param tokenStream
     * @return
     * @throws ParseException 
     */
    public static CNode commandLine (BufferedTokenStream tokenStream) throws ParseException
    {
        if (tokenStream.atEOS())
            return null;
        
        // Filter out script functions; command line should be used only for math and variable assignment
//        Token begin = tokenStream.read(),
//              token = begin;
//        tokenStream.mark(begin);
//        do {
//            if (token.getId() == TSCode.IDENT && isReserved(token.getValue())) {
//                String errMessage = CompilerErrors.formatErrorMessage(
//                    token, Level.ERROR, ErrType.ILLEGAL_EXPR,
//                    "Script functions and logic unavailable on the command line"
//                );
//                throw new ParseException(errMessage, token);
//            }
//            token = tokenStream.read();
//        }
//        while (!tokenStream.atEOS());
//        tokenStream.rewind(begin);
//        tokenStream.unmark();
        return Productions.statement(tokenStream);
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
                CNode.getChild (thisNode, i).execInstrGen (builder);
            return builder.getActiveCodeSegmentId ();
        };
        Token blockToken = new Token(TSCode.NONE);
        blockToken.setValue ("{}");
        CNode statementBlockRoot  = new CNode(blockToken, injected);

        Token nextToken;
        while ((nextToken = tokenStream.read()) != null && nextToken.getId() != TSCode.RBRACKET) {
            tokenStream.unread(nextToken);  // Put token back into stream for next production

            CNode statementNode = null;
            try {
                statementNode = Productions.statement (tokenStream);
            } catch (ParseException ex) {
                Logger.getLogger(Productions.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                try {
                    discardStatement (tokenStream);
                } catch (ParseException ex1) {
                    Logger.getLogger(Productions.class.getName()).log(java.util.logging.Level.SEVERE, null, ex1);
                }
                StatusFlags.parsingFailed = true;
            }
            if (statementNode == null)
                break;
            statementNode.setParent(statementBlockRoot);
            CNode.addChild(statementBlockRoot, statementNode);
        }

        return statementBlockRoot;
    }
    
    public static CNode statement (BufferedTokenStream tokenStream) throws ParseException
    {
        // Skip as many comment tokens as required, until code found or end of token stream reached
        Token nextToken;
        if ((nextToken = tokenStream.read()) == null || nextToken.getId() == TSCode.COMMENT || nextToken.getId() == TSCode.SEMICOLON)
            return null;
        abortIfEOS(tokenStream);
        tokenStream.unread(nextToken);
        
        CNode statementNode = null;
        // Attempt to parse print statement
        if (nextToken.getId() == TSCode.IDENT) {
            switch(nextToken.getValue()) {
                case "print":
                    if (tokenStream.atEOS())
                        throw new RuntimeException("EOS reached after " + nextToken + " parsed");
                    statementNode = Productions.print(tokenStream);
                    break;
                case "clear":
                    statementNode = Productions.clear(tokenStream);
                default:;
            }
        }

        // Attempt to parse assignment statement
        if (statementNode == null)
            statementNode = Productions.assignment (tokenStream);

        // Attempt to parse arithmetic statement (rval)
        if (statementNode == null)
            statementNode = Productions.rvalue(tokenStream);

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
        tokenStream.mark(begin);
        tokenStream.unread(begin);
        CNode lvalNode = Productions.lvalue (tokenStream);
        if (lvalNode == null) {
            tokenStream.rewind(begin);
            tokenStream.unmark();
            return null;
        }

        abortIfEOS(tokenStream);

        // Extract assignment operator
        Token assignToken = tokenStream.read();
        if (assignToken.getId () != TSCode.ASSIGN) {
            tokenStream.rewind(begin);
            tokenStream.unmark();
            return null;
        }
        
        abortIfEOS(tokenStream);
        
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
            // Build rval evaluation code and move to R2
            CNode.getChild (thisNode, 1).execInstrGen (builder);
            builder.MOV(new Operand(RegId.R2), new Operand(RegId.R1));
            
            // Build lval assignment code
            CNode.getChild (thisNode, 0).execInstrGen (builder);

            return builder.getActiveCodeSegmentId ();
        };
        CNode assignNode = new CNode(assignToken, injected);
        lvalNode.setParent(assignNode);
        rvalNode.setParent(assignNode);
        CNode.addChild(assignNode, lvalNode);
        CNode.addChild(assignNode, rvalNode);

        return assignNode;
    }
    
    static CNode print (BufferedTokenStream tokenStream) throws ParseException
    {
        Token printToken = tokenStream.read();
        abortIfEOS(tokenStream);
        
        // Note print token will have already been consumed, so immediately parse left parenthesis
        consumeLParen(tokenStream, ErrType.FUNCTION_CALL);
        abortIfEOS(tokenStream);
        
        // Parse comma-separated list of rval expressions
        List<CNode> rvalNodes = new ArrayList<>();
        boolean inList = false, separatorFound = false;
        Token argListToken;
        while ((argListToken = tokenStream.read()).getId() != TSCode.RPAREN && argListToken.getId() != TSCode.SEMICOLON) {
            if (inList && !separatorFound) {
                String errMessage = CompilerErrors.formatErrorMessage(
                    argListToken, Level.ERROR, ErrType.FUNCTION_CALL,
                    "Expected argument list separator"
                );
                throw new ParseException(errMessage, argListToken);
            }
            separatorFound = false;
            if (argListToken.getId() != TSCode.COMMA)
                tokenStream.unread(argListToken);
            CNode rvalNode = Productions.rvalue (tokenStream);
            if (rvalNode == null) {
                String errMessage = CompilerErrors.formatErrorMessage(
                    Token.NONE, Level.ERROR, ErrType.ILLEGAL_EXPR,
                    ErrMessage.METHOD_ARGUMENT.toString()
                );
                throw new ParseException(errMessage, null);
            }
            rvalNodes.add(rvalNode);
            abortIfEOS(tokenStream);
            inList = separatorFound = consumeListSeparator (tokenStream);
        }
        abortIfEOS(tokenStream);
        
//        consumeRParen(tokenStream, ErrType.FUNCTION_CALL);
//        abortIfEOS(tokenStream);
        
        consumeTrailingSemicolon(tokenStream);
        
        // Build code subtree
        BiFunction<CNode, InstructionBuilder, Integer> injected = (CNode thisNode, InstructionBuilder builder) -> {
            for (int i = 0; i < CNode.numChildren (thisNode); ++i) {
                // Execute child node code generation
                CNode rvalNode = CNode.getChild (thisNode, i);
                rvalNode.execInstrGen (builder);
                builder
                    .PRINT (new Operand(RegId.R1));
            }

            return builder.getActiveCodeSegmentId ();
        };
        CNode printNode = new CNode (printToken, injected);
        for (CNode rvalNode : rvalNodes) {
                rvalNode.setParent(printNode);
                CNode.addChild(printNode, rvalNode);
        }

        return printNode;
    }
    
    static CNode clear(BufferedTokenStream tokenStream) throws ParseException
    {
        Token printToken = tokenStream.read();
        
        // The clear command takes an empty argument list, so just discard tokens up to and including semicolon
        consumeLParen(tokenStream, ErrType.FUNCTION_CALL);
        consumeRParen(tokenStream, ErrType.FUNCTION_CALL);
        consumeTrailingSemicolon(tokenStream);
        
        BiFunction<CNode, InstructionBuilder, Integer> injected = (CNode thisNode, InstructionBuilder builder) -> {
            builder.CLEAR();
            return builder.getActiveCodeSegmentId();
        };
        return new CNode(printToken, injected);
    }
    
    static CNode lvalue (BufferedTokenStream tokenStream) throws ParseException
    {
        Token varToken = tokenStream.read();
        if (varToken.getId() != TSCode.IDENT || isReserved(varToken.getValue()))
            return null;
        final String symbol = varToken.getValue();
        
        // Create new symbol if needed
        if (!SymbolTable.isRegistered(symbol)) {
            // TODO: Add handling for type deduction
            SymbolTable.registerVariable(symbol, DataType.Int4);
        }

        // Create lvalue assignment node
        BiFunction<CNode, InstructionBuilder, Integer> injected = (CNode thisNode, InstructionBuilder builder) -> {
            // Get relative address of symbol and move contents of register R1 to storage
            SymbolParams symParams = SymbolTable.getVariableParams(symbol);
            Operand ref = new Operand(symParams.getType(), symParams.getOffset());
            builder.MOV(ref, new Operand(RegId.R1));
            
            return builder.getActiveCodeSegmentId();
        };
        return new CNode(varToken, injected, DataType.Int4);
    }
    
    static CNode rvalue (BufferedTokenStream tokenStream) throws ParseException
    {
        // Try to parse string literal expression
        CNode expressionNode;
        if ((expressionNode = Productions.stringLiteralRef(tokenStream)) != null)
            return expressionNode;
        
        // Try to parse arithmetic/logical expression
        if ((expressionNode = Productions.addSub(tokenStream)) != null)
            return expressionNode;

        // If parsing failed go back to start position
        return null;
    }
    
    static CNode stringLiteralRef (BufferedTokenStream tokenStream) throws ParseException
    {
        Token strToken = tokenStream.read();
        // Return null if this is not a string literal
        if (strToken.getId () != TSCode.STRING) {
            tokenStream.unread(strToken);
            return null;
        }
        abortIfEOS(tokenStream);

        // Create string literal
        final String literalVal = strToken.getValue();
        BiFunction<CNode, InstructionBuilder, Integer> injected = (CNode node, InstructionBuilder builder) -> {
            builder
                .MOV  (new Operand(RegId.R1), new Operand(literalVal))
                .PUSH (RegId.R1);

            return builder.getActiveCodeSegmentId ();
        };
        return new CNode(strToken, injected, DataType.Imm_Str);
    }

    static CNode addSub (BufferedTokenStream tokenStream) throws ParseException
    {
        // Return nullptr immediately if there are no tokens to process
        if (tokenStream.atEOS())
            return null;

        // Process left subtree and set as current root
        CNode lsubtree = Productions.multDivMod(tokenStream),
              currentroot = lsubtree;

        // Exit immediately if lsubtree parsing failed
        if (lsubtree == null)
            return null;

        // While there are tokens left (there won't be if higher precendence operator rules have consumed
        // the token stream) process addition, subtraction tokens
        Token token;
        while (!tokenStream.atEOS()) {
            boolean breakAndReturn = false;
            token = tokenStream.read();
            switch (token.getId ()) {
                case PLUS:  // Addition ('+') token
                {
                    BiFunction<CNode, InstructionBuilder, Integer> injected =
                        (CNode node, InstructionBuilder builder) -> {
                            CNode.getChild(node, 0)
                                .execInstrGen(builder);
                            builder.PUSH (RegId.R1);
                            CNode.getChild(node, 1).execInstrGen(builder);
                            
                            builder
                                .MOV(new Operand(RegId.R3), new Operand(RegId.R1))
                                .POP(RegId.R1)
                                .ADD(RegId.R1, RegId.R3);

                            return builder.getActiveCodeSegmentId ();
                        };
                    lsubtree = currentroot;
                    currentroot = new CNode(token, injected, lsubtree.getValType());
                    break;
                }
                case MINUS: // Subtraction ('-') token
                {
                    BiFunction<CNode, InstructionBuilder, Integer> injected =
                        (CNode node, InstructionBuilder builder) -> {
                            CNode.getChild(node, 0)
                                .execInstrGen(builder);
                            builder.PUSH (RegId.R1);
                            CNode.getChild(node, 1).execInstrGen(builder);
                            
                            builder
                                .MOV(new Operand(RegId.R3), new Operand(RegId.R1))
                                .POP(RegId.R1)
                                .SUB(RegId.R1, RegId.R3);

                            return builder.getActiveCodeSegmentId ();
                        };
                    lsubtree = currentroot;
                    currentroot = new CNode(token, injected, lsubtree.getValType());
                    break;
                }
                default:
                    if (token.getId () == TSCode.INTEGER || token.getId () == TSCode.REAL) {
                        // Syntax error if integer or float is found, print error, discard token and continue
                        String errMessage = CompilerErrors.formatErrorMessage(
                            token, Level.ERROR, ErrType.ARITHMETIC,
                            ErrMessage.INVALID_NUMERIC.toString()
                        );
                        throw new ParseException(errMessage, token);
                    }
                    else {
                        // Unrecognized token, set break and return flag
                        tokenStream.unread(token);
                        breakAndReturn = true;
                    }
            }

            // If an unrecognized token was encountered then assume it will be consumed by the calling
            // production and break out of token process loop
            if (breakAndReturn)
                break;

            // Increment token stream iterator and process right subtree
            lsubtree.setParent(currentroot);
            CNode.addChild(currentroot, lsubtree);
            CNode rsubtree = Productions.multDivMod(tokenStream);

            // If right subtree is empty then we ran out of operand tokens before operators,
            // print error message and return the left subtree
            if (rsubtree == null) {
                String errMessage = CompilerErrors.formatErrorMessage(
                    token, Level.ERROR, ErrType.ARITHMETIC,
                    ErrMessage.MISSING_BIN_RHO.toString()
                );
                throw new ParseException(errMessage, token);
            }

            // Perform type safety check
            binaryOpTypeCheck(lsubtree, rsubtree, token, ErrType.ARITHMETIC);

            // Add right subtree
            CNode.addChild(currentroot, rsubtree);
            rsubtree.setParent(currentroot);
        }

        return currentroot;
    }
    
    static CNode multDivMod (BufferedTokenStream tokenStream) throws ParseException
    {
        // Return nullptr immediately if there are no tokens to process
        if (tokenStream.atEOS())
            return null;

        // Process left subtree and set as current root
        CNode lsubtree = Productions.power(tokenStream),
              currentroot = lsubtree;

        // Exit immediately if lsubtree parsing failed
        if (lsubtree == null)
            return null;

        // While there are tokens left (there won't be if higher precendence operator rules have consumed
        // the token stream) process multiplication, division and modulus operators
        Token token;
        while (!tokenStream.atEOS()) {
            boolean breakAndReturn = false;
            token = tokenStream.read();
            switch (token.getId ()) {
                case MULT:  // Multiplication ('*') token
                {
                    BiFunction<CNode, InstructionBuilder, Integer> injected =
                        (CNode node, InstructionBuilder builder) -> {
                            CNode.getChild(node, 0)
                                .execInstrGen(builder);
                            builder.PUSH (RegId.R1);
                            CNode.getChild(node, 1).execInstrGen(builder);
                            
                            builder
                                .MOV(new Operand(RegId.R3), new Operand(RegId.R1))
                                .POP(RegId.R1)
                                .MUL(RegId.R1, RegId.R3);

                            return builder.getActiveCodeSegmentId ();
                        };
                    lsubtree = currentroot;
                    currentroot = new CNode(token, injected, lsubtree.getValType());
                    break;
                }
                case DIV:   // Division ('/') token
                {
                    BiFunction<CNode, InstructionBuilder, Integer> injected =
                        (CNode node, InstructionBuilder builder) -> {
                            CNode.getChild(node, 0)
                                .execInstrGen(builder);
                            builder.PUSH (RegId.R1);
                            CNode.getChild(node, 1).execInstrGen(builder);
                            
                            builder
                                .MOV(new Operand(RegId.R3), new Operand(RegId.R1))
                                .POP(RegId.R1)
                                .DIV(RegId.R1, RegId.R3);

                            return builder.getActiveCodeSegmentId ();
                        };
                    lsubtree = currentroot;
                    currentroot = new CNode(token, injected, lsubtree.getValType());
                    break;
                }
                case IDENT: // Mod keyword
                {
                    // If not the mod keyword then set break and return flag
                    if (!token.getValue().equals("mod")) {
                        breakAndReturn = true;
                        break;
                    }

                    BiFunction<CNode, InstructionBuilder, Integer> injected =
                        (CNode node, InstructionBuilder builder) -> {
                            CNode.getChild(node, 0)
                                .execInstrGen(builder);
                            builder.PUSH(RegId.R1);
                            CNode.getChild(node, 1).execInstrGen(builder);

                            Operand r1 = new Operand(RegId.R1);
                            builder
                                .MOV(new Operand(RegId.R3), r1)
                                .POP(RegId.R1)
                                .DIV(RegId.R1, RegId.R3)
                                .MOV(r1, new Operand(RegId.R4));

                            return builder.getActiveCodeSegmentId ();
                        };
                    lsubtree = currentroot;
                    currentroot = new CNode(token, injected, lsubtree.getValType());
                    break;
                }
                default:
                    if (token.getId() == TSCode.INTEGER || token.getId() == TSCode.REAL) {
                        // Syntax error if integer or float is found, print error, discard token and continue
                        String errMessage = CompilerErrors.formatErrorMessage(
                            token, Level.ERROR, ErrType.ARITHMETIC,
                            ErrMessage.INVALID_NUMERIC.toString()
                        );
                        throw new ParseException(errMessage, token);
                    }
                    else {
                        // Unrecognized token, set break and return flag
                        tokenStream.unread(token);
                        breakAndReturn = true;
                    }
            }

            // If an unrecognized token was encountered then assume it will be consumed by the calling
            // production and break out of token process loop
            if (breakAndReturn)
                break;

            // Increment token stream iterator and process right subtree
            lsubtree.setParent(currentroot);
            CNode.addChild(currentroot, lsubtree);
            CNode rsubtree = Productions.power(tokenStream);

            // If right subtree is empty then we ran out of operand tokens before operators,
            // print error message and return the left subtree
            if (rsubtree == null) {
                String errMessage = CompilerErrors.formatErrorMessage(
                    token, Level.ERROR, ErrType.ARITHMETIC,
                    ErrMessage.MISSING_BIN_RHO.toString()
                );
                throw new ParseException(errMessage, token);
            }

            // Perform type safety check
            binaryOpTypeCheck(lsubtree, rsubtree, token, ErrType.ARITHMETIC);

            // Add right subtree
            CNode.addChild(currentroot, rsubtree);
            rsubtree.setParent(currentroot);
        }

        return currentroot;
    }
    
    static CNode power (BufferedTokenStream tokenStream) throws ParseException
    {
        if (tokenStream.atEOS())
            return null;
        
        CNode lsubtree = Productions.negatable(tokenStream),
              current = lsubtree;
        if (lsubtree == null)
            return null;
        
        Token token;
        while (!tokenStream.atEOS()) {
            boolean breakAndReturn = false;
            token = tokenStream.read();
            switch (token.getId()) {
                case EXP:
                    BiFunction<CNode, InstructionBuilder, Integer> injected =
                        (CNode node, InstructionBuilder builder) -> {
                            // Exponentials are right-associative, so go rh child first
                            CNode.getChild(node, 1)
                                .execInstrGen(builder);
                            builder.PUSH (RegId.R1);
                            CNode.getChild(node, 0).execInstrGen(builder);
                            
                            builder
                                .POP(RegId.R3)
                                .EXP(RegId.R1, RegId.R3);

                            return builder.getActiveCodeSegmentId ();
                        };
                    lsubtree = current;
                    current = new CNode(token, injected, lsubtree.getValType());
                    break;
                default:
                    if (token.getId() == TSCode.INTEGER || token.getId() == TSCode.REAL) {
                        // Syntax error if integer or float is found, print error, discard token and continue
                        String errMessage = CompilerErrors.formatErrorMessage(
                            token, Level.ERROR, ErrType.ARITHMETIC,
                            ErrMessage.INVALID_NUMERIC.toString()
                        );
                        throw new ParseException(errMessage, token);
                    }
                    else {
                        // Unrecognized token, set break and return flag
                        tokenStream.unread(token);
                        breakAndReturn = true;
                    }
            }
            
            // If an unrecognized token was encountered then assume it will be consumed by the calling
            // production and break out of token process loop
            if (breakAndReturn)
                break;
            
            // Add left subtree
            lsubtree.setParent(current);
            CNode.addChild(current, lsubtree);

            CNode rsubtree = Productions.power(tokenStream);

            // If right subtree is empty then we ran out of operand tokens before operators,
            // print error message and return the left subtree
            if (rsubtree == null) {
                String errMessage = CompilerErrors.formatErrorMessage(
                    token, Level.ERROR, ErrType.ARITHMETIC,
                    ErrMessage.MISSING_BIN_RHO.toString()
                );
                throw new ParseException(errMessage, token);
            }

            // Perform type safety check
            binaryOpTypeCheck(lsubtree, rsubtree, token, ErrType.ARITHMETIC);

            // Add right subtree
            CNode.addChild(current, rsubtree);
            rsubtree.setParent(current);
        }
        
        return current;
    }
    
    static CNode negatable (BufferedTokenStream tokenStream) throws ParseException
    {
        // Return nullptr immediately if there are no tokens to process
        if (tokenStream.atEOS())
            return null;

        CNode currentroot, subtree;

        Token token = tokenStream.read();
        abortIfEOS(tokenStream);
        switch (token.getId ()) {
            case LPAREN:    // Left parenthesis '('
            {
                currentroot = Productions.addSub(tokenStream);
                Token closeToken = tokenStream.read();
                if (closeToken.getId() != TSCode.RPAREN) {
                    String errMessage = CompilerErrors.formatErrorMessage(
                        token, Level.ERROR, ErrType.ARITHMETIC,
                        ErrMessage.UNMATCHED_RPAREN.toString()
                    );
                    throw new ParseException(errMessage, closeToken);
                }
                break;
            }
            case MINUS:     // Unary negation operator '-'
            {
                BiFunction<CNode, InstructionBuilder, Integer> injected =
                    (CNode node, InstructionBuilder builder) -> {
                        CNode.getChild(node, 0)
                            .execInstrGen(builder);
                        builder.NEG(RegId.R1);

                        return builder.getActiveCodeSegmentId ();
                    };

                // If next token is + or -, issue compilation error
                Token nextToken;
                if ((nextToken = tokenStream.read()).getId() == TSCode.PLUS || nextToken.getId() == TSCode.MINUS) {
                    String errMessage = CompilerErrors.formatErrorMessage(
                            nextToken, Level.ERROR, ErrType.ARITHMETIC,
                            ErrMessage.UNEXPECTED_TOKEN.toString()
                    );
                    throw new ParseException(errMessage, nextToken);
                }
                abortIfEOS(tokenStream);
                tokenStream.unread(nextToken);
                subtree = Productions.negatable(tokenStream);

                // If unary minus operator unaccompanied by operand (i.e., end of token stream) then
                // print error message, break and return nullptr
                if (subtree == null) {
                    String errMessage = CompilerErrors.formatErrorMessage(
                        token, Level.ERROR, ErrType.ARITHMETIC,
                        ErrMessage.MISSING_UNARY_RHO.toString()
                    );
                    throw new ParseException(errMessage, token);
                }

                currentroot = new CNode(token, injected, subtree.getValType ());
                subtree.setParent(currentroot);
                CNode.addChild(currentroot, subtree);
                break;
            }
            case PLUS:     // Unary positive operator '+'
            {
                // If next token is + or -, issue compilation error
                Token nextToken;
                if ((nextToken = tokenStream.read()).getId() == TSCode.PLUS || nextToken.getId() == TSCode.MINUS) {
                    String errMessage = CompilerErrors.formatErrorMessage(
                            token, Level.ERROR, ErrType.ARITHMETIC,
                            ErrMessage.UNEXPECTED_TOKEN.toString()
                    );
                    throw new ParseException(errMessage, nextToken);
                }
                abortIfEOS(tokenStream);
                tokenStream.unread(nextToken);
                
                currentroot = Productions.negatable(tokenStream);

                // If unary minus operator unaccompanied by recognizable operand then
                // print error message, break and return nullptr
                if (currentroot == null) {
                    String errMessage = CompilerErrors.formatErrorMessage(
                        token, Level.ERROR, ErrType.ARITHMETIC,
                        ErrMessage.MISSING_UNARY_RHO.toString()
                    );
                    throw new ParseException(errMessage, token);
                }

                break;
            }
            default:
                tokenStream.unread(token);
                currentroot = Productions.varnumeric(tokenStream);
        }

        return currentroot;
    }
    
    static CNode varnumeric (BufferedTokenStream tokenStream) throws ParseException
    {
        Token token = tokenStream.read();
        abortIfEOS(tokenStream);
        
        CNode varNumericNode = null;
        switch (token.getId ()) {
            case INTEGER:
            {
                final Integer val = Integer.parseInt(token.getValue());
                BiFunction<CNode, InstructionBuilder, Integer> injected =
                    (CNode thisNode, InstructionBuilder builder) -> {
                        builder.MOV (new Operand(RegId.R1), new Operand(val));

                        return builder.getActiveCodeSegmentId ();
                    };
                varNumericNode = new CNode(token, injected, DataType.Int4);
                break;
            }
            case IDENT:
                // Determine if is a boolean constant identifier, process as a variable identifier if not
                if (token.getValue().equals("true") || token.getValue().equals("false")) {
                   final Boolean boolImm = token.getValue().equals("true");
//                   BiFunction<CNode, InstructionBuilder, Integer> injected =
//                       (CNode thisNode, InstructionBuilder builder) -> {
//                            builder
//                                .MOV  (new Operand(RegId.R1), new Operand(boolImm))
//                                .TEST (RegId.R1, true);
//
//                            return builder.getActiveCodeSegmentId ();
//                       };
//                    varNumericNode = new CNode(token, injected, DataType.Bool);
                }
                else {
                    tokenStream.unread(token);
                    varNumericNode = Productions.variableDeref (tokenStream);
                }
            default:;
        }

        return varNumericNode;
    }
    
    static CNode variableDeref(BufferedTokenStream tokenStream) throws ParseException
    {
        Token varToken = tokenStream.read();
        if (varToken.getId() != TSCode.IDENT) {
            tokenStream.unread(varToken);
            return null;
        }

        // If this is a reserved keyword then it isn't a valid variable reference (could be a function name)
        final String name = varToken.getValue();
        if (isReserved (name))
            return null;

        // Validate variable name
        if (!SymbolTable.isRegistered(name)) {
            // Print error message and return null
            String errMessage = CompilerErrors.formatErrorMessage(
                varToken, Level.ERROR, ErrType.SYMBOL_REF,
                ErrMessage.UNDEFINED_SYMBOL.toString()
            );
            throw new ParseException(errMessage, varToken);
        }

        BiFunction<CNode, InstructionBuilder, Integer> injected = (CNode thisNode, InstructionBuilder builder) -> {
            SymbolParams symbolDefinition = SymbolTable.getVariableParams(name);
            builder.MOV(new Operand(RegId.R1),
                    new Operand(symbolDefinition.getType(), symbolDefinition.getOffset())
                );
            
            return builder.getActiveCodeSegmentId ();
        };
        return new CNode(varToken, injected, SymbolTable.getVariableParams(name).getType());
    }
}
