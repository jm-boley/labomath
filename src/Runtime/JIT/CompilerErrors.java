package Runtime.JIT;

import Lexical.TSCode;
import Lexical.Token;

/**
 * Parsing errors formatting helper class, contains common messages, error level
 * and types easily build consistent and standards-formatted error messages.
 * @author Joshua Boley
 */
public abstract class CompilerErrors
{
    public static enum Level
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
    public static enum ErrType
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
    
    public static enum ErrMessage
    {
        INVALID_NUMERIC("Numeric operand out of sequence"),
        MISSING_BIN_RHO("Missing right-hand operand for binary operator"),
        MISSING_UNARY_RHO("Missing right-hand operand for unary operator"),
        UNEXPECTED_ARITH_TOKEN("Unexpected/out-of-sequence arithmetic keyword/operator"),
        UNEXPECTED_KEYWORD("Unexpected symbol/keyword"),
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
    
    public static String formatErrorMessage (Token token, Level level, ErrType errType, String message)
    {
        return format(token, level, errType, message);
    }
    
    public static String formatErrorMessage (Token token, Level level, ErrType errType, ErrMessage message)
    {
        return format(token, level, errType, message.toString());
    }
    
    private static String format(Token token, Level level, ErrType errType, String message)
    {
        StringBuilder errorStrBuilder = new StringBuilder();
        errorStrBuilder
            .append(level).append(":Type=").append(errType);
        if (token != null)
            errorStrBuilder
                .append(" [").append(token.getLineNo()).append(":").append(token.getColNo()).append("]");
        errorStrBuilder
            .append(": ").append(message);
        if (token != null && !token.equals(Token.NONE)) {
            String tokenStr = token.getValue();
            if (token.getId() == TSCode.STRING)
                tokenStr = "\"" + tokenStr.replace("\n", "\\n") + "\"";
            errorStrBuilder
                .append(" (").append(tokenStr).append(")");
        }
        errorStrBuilder.append(".");
        return errorStrBuilder.toString();
    }
}
