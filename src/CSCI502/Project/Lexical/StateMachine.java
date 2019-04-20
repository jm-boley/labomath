package CSCI502.Project.Lexical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Node IDs of state submachines
 * @author Joshua Boley
 */
enum Submachine
{
    WTERMINAL(1),
    LESS_LESSEQ_ASSN_BCOM(2),
    GREATER_GREATEREQ(6),
    EQ(8),
    NOTEQ(9),
    AR_PLUS(11),
    AR_MINUS(12),
    AR_MULT(13),
    AR_DIV(14),
    AR_EXP(15),
    BOOL_NEG(16),
    BOOL_AND(17),
    BOOL_OR(18),
    LEFT_PAREN(19),
    RGHT_PAREN(20),
    LEFT_BRCK(21),
    RGHT_BRCK(22),
    LEFT_BRAC(23),
    RGHT_BRAC(24),
    DOT(25),
    COMMA(26),
    SEMICOLON(27),
    COLON(28),
    ATSIGN(29),
    IDENT(30),
    INT_REAL(31),
    REALS(33),
    STRING(37),
    LN_COMMENT(41),
    BLK_COMMENT(43);
    
    private final int code;
    private Submachine(int code)
    {
        this.code = code;
    }
    
    public int id()
    {
        return code;
    }
};

/**
 * Implements the state machine used by the lexical analyzer. Similar to a
 * classical finite automata, however there is no distinction between accepting and
 * non-accepting states. Instead, certain states representing a successful token parse
 * have an associated non-empty token that is recorded by the state machine. The
 * machine's last-reached token may be inspected with the token() function. The
 * advance() function continues parsing with the next supplied character. The isValid()
 * function may be used to determine if the machine is in a valid state or has "crashed"
 * (i.e., there are no more valid states that can be reached with more input). This may
 * be used in conjunction with token() to determine if parsing has finished producing
 * the current token, if any. The machine may be reset for the next token with the reset()
 * function.
 * @author Joshua Boley
 */
class StateMachine
{
    private final List<State> m_stateTable; // Transition function table
    private final List<State> m_history;    // State selection history
    private boolean m_valid,                // Indicates if the machine is still able to continue parsing
                    m_rewind;               // Indicates if the input needs to be retraversed
    private int m_currStateID;              // Current state node ID

    StateMachine()
    {
        m_stateTable = new ArrayList<>();
        m_history = new ArrayList<>();
        m_valid = true;
        m_rewind = false;
        m_currStateID = 0;
        
        init();
    }

    List<TSCode> getTSCodeChain()
    {
        List<TSCode> codeChain = new ArrayList<>();
        m_history.forEach((state) -> {
            codeChain.add(state.getTSCode ());
        });
        return codeChain;
    }
    
    TSCode  getCurrentTSCode() { return m_history.get(m_history.size() - 1).getTSCode (); }
    boolean stopped         () { return !m_valid; }
    boolean rewind          () { return m_rewind; }

    /**
     * Advances the state machine to its next state given an input character.
     * @param next Input
     */
    void advance(char next)
    {
        // Traverse to the next state
        int nextStateID = m_stateTable.get(m_currStateID).getNext(next);
        if (nextStateID == State.STOPPED) {
            m_valid = false;
            m_rewind = true;
        }
        else {
            m_currStateID = nextStateID;
            m_history.add(m_stateTable.get(m_currStateID));
            if (m_stateTable.get(m_currStateID).forceStop ())
                m_valid = false;
        }
    }
    
    /**
     * Resets the state machine
     */
    void reset()
    {
        m_history.clear();
        m_history.add(m_stateTable.get(0));
        m_valid = true;
        m_rewind = false;
        m_currStateID = 0;
    }

    void init()
    {
        /*
        // Configure state machine
        */
        /*/ Start state /*/
        State start = new State(TSCode.NONE);

        // Loop on leading whitespace input
        start.addTransition (Match.WS.toString(), 0);

        // Add initial transition for < (less than), <= (less than or equal to), <- (assign) and <<- (block comment open)
        start.addTransition ("<", Submachine.LESS_LESSEQ_ASSN_BCOM.id());

        // Add initial transition for > (greater than), >= (greater than or equal to)
        start.addTransition (">", Submachine.GREATER_GREATEREQ.id());

        // Add transitions for = (equal to), ~= (not equal to)
        start.addTransition ("=", Submachine.EQ.id());
        start.addTransition ("~", Submachine.NOTEQ.id());

        // Add transitions for binary arithmetic operators (+, -, *, /, ^)
        start.addTransition ("\\+", Submachine.AR_PLUS.id());
        start.addTransition ("\\-", Submachine.AR_MINUS.id());
        start.addTransition ("\\*", Submachine.AR_MULT.id());
        start.addTransition ("/", Submachine.AR_DIV.id());
        start.addTransition ("\\^", Submachine.AR_EXP.id());

        // Add transitions for boolean/logical operators (!, &, |)
        start.addTransition ("\\!", Submachine.BOOL_NEG.id());
        start.addTransition ("&", Submachine.BOOL_AND.id());
        start.addTransition ("\\|", Submachine.BOOL_OR.id());

        // Add transitions for parentheses, brackets and braces
        start.addTransition ("\\(", Submachine.LEFT_PAREN.id());
        start.addTransition ("\\)", Submachine.RGHT_PAREN.id());
        start.addTransition ("\\[", Submachine.LEFT_BRCK.id());
        start.addTransition ("\\]", Submachine.RGHT_BRCK.id());
        start.addTransition ("\\{", Submachine.LEFT_BRAC.id());
        start.addTransition ("\\}", Submachine.RGHT_BRAC.id());

        // Add transitions for dot (.), comma, semicolon, colon, at (@)
        start.addTransition ("\\.", Submachine.DOT.id());
        start.addTransition (",", Submachine.COMMA.id());
        start.addTransition (";", Submachine.SEMICOLON.id());
        start.addTransition (":", Submachine.COLON.id());
        start.addTransition ("@", Submachine.ATSIGN.id());

        // Add initial transition for parsing identifiers
        start.addTransition ("[a-zA-Z_]", Submachine.IDENT.id());

        // Add initial transition for parsing integers and reals
        start.addTransition (Match.NUMERIC.toString(), Submachine.INT_REAL.id());

        // Add initial transition for parsing strings
        start.addTransition (Match.DOUBLE_QUOT.toString(), Submachine.STRING.id());

        // Add initial transition for parsing line comments
        start.addTransition ("#", Submachine.LN_COMMENT.id());

        m_stateTable.add (start);                               // State [0]
        m_stateTable.add (State.EMPTY);                         // State [1]

        /*/ Submachine: <, <=, <-, <<- /*/
        State lessThan    = new State(TSCode.LESS),             // State [2]
              lessEq      = new State(TSCode.LESS_EQ, true),    // State [3]
              assign      = new State(TSCode.ASSIGN, true),     // State [4]
              blkComAngle = new State(TSCode.UNKNOWN);          // State [5]

        lessThan.addTransition ("=", 3);                    // '=' -> lessEq
        lessThan.addTransition ("-", 4);                    // '-' -> assign
        lessThan.addTransition ("<", 5);                    // '<' -> blkComAngle
        m_stateTable.add (lessThan);

        m_stateTable.add (lessEq);
        m_stateTable.add (assign);

        blkComAngle.addTransition ("-", Submachine.BLK_COMMENT.id());   // '-' -> Submachine block comment
        m_stateTable.add (blkComAngle);

        /*/ Submachine: >, >= /*/
        State greaterThan = new State(TSCode.GREATER),                // State [6]
              greaterEq   = new State(TSCode.GREATER_EQ, true);       // State [7]

        greaterThan.addTransition ("=", 7);                 // '=' -> greaterEq
        m_stateTable.add (greaterThan);

        m_stateTable.add (greaterEq);

        /*/ Submachines: =, ~= /*/
        State equalTo    = new State(TSCode.EQUAL, true),             // State [8]
              tilde      = new State(TSCode.UNKNOWN),                 // State [9]
              notEqualTo = new State(TSCode.NOT_EQUAL, true);         // State [10]

        m_stateTable.add (equalTo);

        tilde.addTransition ("=", 10);                      // '=' -> notEqualTo
        m_stateTable.add (tilde);

        m_stateTable.add (notEqualTo);

        /*/ Submachines: binary arithmetic operators /*/
        State plus     = new State(TSCode.PLUS, true),                // State [11]
              minus    = new State(TSCode.MINUS, true),               // State [12]
              multiply = new State(TSCode.MULT, true),                // State [13]
              divide   = new State(TSCode.DIV, true),                 // State [14]
              exponent = new State(TSCode.EXP, true);                 // State [15]
        Arrays.asList(plus, minus, multiply, divide, exponent).forEach((state) -> {
            m_stateTable.add (state);
        });

        /*/ Submachines: boolean/logical operators /*/
        State negate = new State(TSCode.NOT, true),                   // State [16]
              lAnd   = new State(TSCode.AND, true),                   // State [17]
              lOr    = new State(TSCode.OR, true);                    // State [18]
        Arrays.asList(negate, lAnd, lOr).forEach((state) -> {
            m_stateTable.add (state);
        });

        /*/ Submachines: parentheses, brackets and braces /*/
        State lParen = new State(TSCode.LPAREN, true),                // State [19]
              rParen = new State(TSCode.RPAREN, true),                // State [20]
              lBrack = new State(TSCode.LBRACKET, true),              // State [21]
              rBrack = new State(TSCode.RBRACKET, true),              // State [22]
              lBrace = new State(TSCode.LBRACE, true),                // State [23]
              rBrace = new State(TSCode.RBRACE, true);                // State [24]

        Arrays.asList(lParen, rParen, lBrack, rBrack, lBrace, rBrace).forEach((state) -> {
            m_stateTable.add (state);
        });

        /*/ Submachines: dot (.), comma, semicolon, colon, at (@) /*/
        State dot       = new State(TSCode.DOT),                      // State [25]
              comma     = new State(TSCode.COMMA, true),              // State [26]
              semicolon = new State(TSCode.SEMICOLON, true),          // State [27]
              colon     = new State(TSCode.COLON, true),              // State [28]
              at        = new State(TSCode.AT, true);                 // State [29]

        dot.addTransition (Match.NUMERIC.toString(), Submachine.REALS.id());  // <numeric> -> Submachine reals
        m_stateTable.add (dot);

        Arrays.asList(comma, semicolon, colon, at).forEach((state) -> {
            m_stateTable.add (state);
        });

        /*/ Submachine: identifiers /*/
        State identifier = new State(TSCode.IDENT);                   // State [30]

        identifier.addTransition (Match.ALPHA_NUM_US.toString(), 30); // <alphanumeric + underscore> -> loop
        m_stateTable.add (identifier);

        /*/ Submachines: integers, reals /*/
        State intNumeric  = new State(TSCode.INTEGER),                // State [31]
              realDot     = new State(TSCode.REAL),                   // State [32]
              realNumeric = new State(TSCode.REAL),                   // State [33]
              realExp     = new State(TSCode.UNKNOWN),                // State [34]
              realExpSign = new State(TSCode.UNKNOWN),                // State [35]
              realNumeric2 = new State(TSCode.REAL);                  // State [36]      

        intNumeric.addTransition (Match.NUMERIC.toString(), 31);      // <numeric> -> loop
        intNumeric.addTransition (Match.CHAR_DOT.toString(), 32);     // '.' -> realDot
        intNumeric.addTransition (Match.SCI_EXP.toString(), 34);      // <E|e> -> realExp
        m_stateTable.add (intNumeric);

        realDot.addTransition (Match.NUMERIC.toString(), 33);         // <numeric> -> realNumeric
        realDot.addTransition (Match.SCI_EXP.toString(), 34);         // <E|e> -> realExp
        m_stateTable.add (realDot);

        realNumeric.addTransition (Match.SCI_EXP.toString(), 34);     // <E|e> -> realExp
        realNumeric.addTransition (Match.NUMERIC.toString(), 33);     // <numeric> -> loop
        m_stateTable.add (realNumeric);

        realExp.addTransition (Match.NUMERIC.toString(), 36);         // <numeric> -> realNumeric2
        realExp.addTransition ("[+-]", 35);                           // <+|-> -> realExpSign
        m_stateTable.add (realExp);

        realExpSign.addTransition (Match.NUMERIC.toString(), 36);     // <numeric> -> realNumeric2
        m_stateTable.add (realExpSign);

        realNumeric2.addTransition (Match.NUMERIC.toString(), 36);    // <numeric> -> loop
        m_stateTable.add (realNumeric2);

        /*/ Submachine: strings /*/
        State strCatchAll = new State(TSCode.STRING_PARTIAL),         // State [37]
              invalidStr  = new State(TSCode.STRING_INVALID, true),   // State [38]
              closeQuote  = new State(TSCode.STRING, true),           // State [39]
              escapeChar  = new State(TSCode.STRING_PARTIAL);         // State [40]

        strCatchAll.addTransition ("(?![\"\\\\]).", 37);              // <everything but newline, " and \ (escape)> -> loop
        strCatchAll.addTransition ("\\\\", 40);                       // '\' -> escapeChar
        strCatchAll.addTransition (Match.NEWLINE.toString(), 38);     // '\n' -> invalidStr
        strCatchAll.addTransition (Match.DOUBLE_QUOT.toString(), 39); // '"' -> closeQuote
        m_stateTable.add (strCatchAll);

        m_stateTable.add (invalidStr);

        m_stateTable.add (closeQuote);

        escapeChar.addTransition (".", 37);			      // <everything but newline> -> strCatchAll
        escapeChar.addTransition (Match.NEWLINE.toString(), 38);      // '\n' -> invalidStr
        m_stateTable.add (escapeChar);

        /*/ Submachine: line comments /*/
        State lnComCatchAll = new State(TSCode.COMMENT),              // State [41]
              lnComClose    = new State(TSCode.COMMENT, true);        // State [42]

        lnComCatchAll.addTransition (".", 41);                        // <everything but newline> -> loop
        lnComCatchAll.addTransition (Match.NEWLINE.toString(), 42);   // '\n' -> lnComClose
        m_stateTable.add (lnComCatchAll);

        m_stateTable.add (lnComClose);

        /*/ Submachine: block comment (->>) /*/
        State blkComCatchall    = new State(TSCode.COMMENT),          // State [43]
              blkComCloseMinus  = new State(TSCode.COMMENT),          // State [44]
              blkComCloseAngle1 = new State(TSCode.COMMENT),          // State [45]
              blkComCloseAngle2 = new State(TSCode.COMMENT, true);    // State [46]

        blkComCatchall.addTransition ("(?!-).|\\n", 43);    // <everything but '-'> -> loop
        blkComCatchall.addTransition ("-", 44);             // '-' -> blkComCloseMinus
        m_stateTable.add (blkComCatchall);

        blkComCloseMinus.addTransition (Match.NOT_RANGLE_BR.toString(), 43);  // <everything but '>' and '-'> -> blkComCatchall
        blkComCloseMinus.addTransition (">", 45);                   // '>' -> blkComCloseAngle1
        blkComCloseMinus.addTransition ("-", 44);                   // '-' -> loop
        m_stateTable.add (blkComCloseMinus);

        blkComCloseAngle1.addTransition (Match.NOT_RANGLE_BR.toString(), 43); // <everything but '>' and '-'> -> blkComCatchall
        blkComCloseAngle1.addTransition (">", 46);                  // '>' -> blkComCloseAngle2
        blkComCloseAngle1.addTransition ("-", 44);                  // '-' -> blkComCloseMinus
        m_stateTable.add (blkComCloseAngle1);

        m_stateTable.add (blkComCloseAngle2);

        // Initialize history with start state
        m_history.add (m_stateTable.get(0));
    }
}
