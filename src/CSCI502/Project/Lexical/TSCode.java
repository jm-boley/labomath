package CSCI502.Project.Lexical;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tokenization state code. Identifies a token implicit to a state when the machine reaches it, as
 * well as intermediate states leading to tokens for special-case processing (i.e., strings &
 * comments).
 * @author Joshua Boley
 */
public enum TSCode
{
    PLUS, MINUS, MULT, DIV, IDENT, EXP, LESS, LESS_EQ, GREATER, GREATER_EQ, EQUAL, NOT_EQUAL,
    ASSIGN, NOT, LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET, AND, OR, DOT, AT, INTEGER,
    STRING, STRING_PARTIAL, STRING_INVALID, COLON, SEMICOLON, COMMA, REAL, UNKNOWN, NONE, COMMENT;
    
    private static final Map<TSCode, String> tsCodeSerialized = new EnumMap<TSCode, String> (TSCode.class);
    static {
        tsCodeSerialized.put(AND, "AND");
        tsCodeSerialized.put(ASSIGN, "ASSIGN");
        tsCodeSerialized.put(AT, "AT");
        tsCodeSerialized.put(COLON, "COLON");
        tsCodeSerialized.put(COMMA, "COMMA");
        tsCodeSerialized.put(COMMENT, "COMMENT");
        tsCodeSerialized.put(DIV, "DIV");
        tsCodeSerialized.put(DOT, "DOT");
        tsCodeSerialized.put(EQUAL, "EQUAL");
        tsCodeSerialized.put(EXP, "EXP");
        tsCodeSerialized.put(GREATER, "GREATER");
        tsCodeSerialized.put(GREATER_EQ, "GREATER_EQ");
        tsCodeSerialized.put(IDENT, "IDENT");
        tsCodeSerialized.put(INTEGER, "INTEGER");
        tsCodeSerialized.put(LBRACE, "LBRACE");
        tsCodeSerialized.put(LBRACKET, "LBRACKET");
        tsCodeSerialized.put(LESS, "LESS");
        tsCodeSerialized.put(LESS_EQ, "LESS_EQ");
        tsCodeSerialized.put(LPAREN, "LPAREN");
        tsCodeSerialized.put(MINUS, "MINUS");
        tsCodeSerialized.put(MULT, "MULT");
        tsCodeSerialized.put(NONE, "NONE");
        tsCodeSerialized.put(NOT, "NOT");
        tsCodeSerialized.put(NOT_EQUAL, "NOT_EQUAL");
        tsCodeSerialized.put(OR, "OR");
        tsCodeSerialized.put(PLUS, "PLUS");
        tsCodeSerialized.put(RBRACE, "RBRACE");
        tsCodeSerialized.put(RBRACKET, "RBRACKET");
        tsCodeSerialized.put(REAL, "REAL");
        tsCodeSerialized.put(RPAREN, "RPAREN");
        tsCodeSerialized.put(SEMICOLON, "SEMICOLON");
        tsCodeSerialized.put(STRING, "STRING");
        tsCodeSerialized.put(STRING_INVALID, "STRING_INVALID");
        tsCodeSerialized.put(STRING_PARTIAL, "STRING_PARTIAL");
        tsCodeSerialized.put(UNKNOWN, "UNKNOWN");
    }
    /**
     * Serializes the token state code to string.
     * @param tsCode Token state code
     * @return Serialized string representation
     */
    public static String serialize (TSCode tsCode)
    {
        return tsCodeSerialized.get(tsCode);
    }
}
