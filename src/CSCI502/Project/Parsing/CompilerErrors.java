/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSCI502.Project.Parsing;

import CSCI502.Project.Lexical.Token;

/**
 *
 * @author boley
 */
abstract class CompilerErrors
{
    static enum Level
    {
        ERROR("Error"),
        WARNING("Warning");
        
        private final String val;
        private Level(String val)
        {
            this.val = val;
        }
        
        @Override
        public String toString()
        {
            return val;
        }
    }
    static enum ErrType
    {
        ARITHMETIC("Arithmetic"),
        BOOLEAN("Boolean"),
        FUNCTION_CALL("Method reference"),
        SYMBOL_REF("Symbol reference"),
        ASSIGNMENT("Assignment"),
        COMP_U("Input"),
        ILLEGAL_EXPR("Illegal expression");

        private final String val;
        private ErrType(String val)
        {
            this.val = val;
        }
        
        @Override
        public String toString()
        {
            return val;
        }
    }
    
    static enum ErrMessage
    {
        INVALID_NUMERIC("Numeric operand out of sequence"),
        MISSING_BIN_RHO("Missing right-hand operand for binary operator"),
        MISSING_UNARY_RHO("Missing right-hand operand for unary operator"),
        UNEXPECTED_TOKEN("Unexpected/out-of-sequence arithmetic keyword/operator"),
        UNMATCHED_LPAREN("Unmatched left parenthesis"),
        UNMATCHED_RPAREN("Unmatched right parenthesis"),
        
        UNKNOWN_TYPE("Unable to deduce type from right-hand expression"),
        RESERVED_KEYWORD("Illegal use of reserved keyword"),
        MISSING_LPAREN("Expected left parenthesis to follow method name"),
        MISSING_RPAREN("Expected right parenthesis to follow method list of method arguments"),
        TYPE_MISMATCH("Unable to convert between types"),
        METHOD_ARGUMENT("Unable to evaluate method argument"),
        UNDEFINED_SYMBOL("Unrecognized variable name");
        
        private final String val;
        private ErrMessage(String val)
        {
            this.val = val;
        }
        
        @Override
        public String toString()
        {
            return val;
        }
    }
    
    static String formatErrorMessage (Token token, Level level, ErrType errType, String message)
    {
        StringBuilder errorStrBuilder = new StringBuilder();
        errorStrBuilder
            .append(level).append(":Type=").append(errType)
            .append(" [").append(token.getLineNo()).append(":").append(token.getColNo()).append("]: ")
            .append(message).append(".");
        if (!token.equals(Token.NONE)) {
            errorStrBuilder
                .append(" (").append(token.getValue()).append(")");
        }
        return errorStrBuilder.toString();
    }
}
