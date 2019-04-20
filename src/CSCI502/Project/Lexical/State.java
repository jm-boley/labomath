/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSCI502.Project.Lexical;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.util.Pair;

/**
 * State machine node.
 * @author Joshua Boley
 */
public class State
{
    public static final int STOPPED;    // Code returned from getNext() when no other state can be
                                        // reached
    public static final int ERROR;      // Code returned from getNext() when there was an error parsing a token
    public static final State EMPTY;    // Generic empty state
    
    static {
        STOPPED = Integer.MAX_VALUE - 1;
        ERROR = Integer.MAX_VALUE;
        EMPTY = new State();
    }

    private final TSCode m_returned;
    private final boolean m_forceStop;
    private final List<Pair<String, Integer>> m_transitions;
    
    public State ()
    {
        m_returned = TSCode.NONE;
        m_forceStop = false;
        m_transitions = new ArrayList<>();
    }
    
    public State (TSCode returned)
    {
        m_returned = returned;
        m_forceStop = false;
        m_transitions = new ArrayList<>();
    }
    
    public State (TSCode returned, boolean forceStop)
    {
        m_returned = returned;
        m_forceStop = forceStop;
        m_transitions = new ArrayList<>();
    }
    
//    public State (State rhs)
//    {
//        m_returned = rhs.m_returned;
//        m_forceStop = rhs.m_forceStop;
//        m_transitions = new ArrayList<>();
//        for (Pair<String, Integer> transition : rhs.m_transitions) {
//            
//        }
//    }

    /**
     * Adds a transition to a new state to the transition vector. Used during initialization
     * @param match Match pattern (regex)
     * @param nextStateId ID (index) of the following state
     */
    public void addTransition (String match, int nextStateId)
    {
         m_transitions.add(new Pair(match, nextStateId));
    }

    /**
     * Gets the following state given an input. If no state can be reached, returns STOPPED.
     * @param input Character in input stream
     * @return ID (index) of the following state, or STOPPED
     */
    int getNext (char input)
    {
        // Search transitions for match
        for (Pair<String, Integer> transition : m_transitions) {
            Pattern r = Pattern.compile(transition.getKey());
            Matcher m = r.matcher("" + input);
            if (m.find())
                return transition.getValue();
        }
        // If none found return STOP code
        return STOPPED;
    }

    /**
     * Gets the token attached to the current state. The empty token (with token type id NONE)
     * is returned if this state does not imply a known token.
     * @return Tokenization state code for this state.
     */
    TSCode getTSCode () { return m_returned; }

    /**
     * Returns whether this is a halting state.
     * @return True if halting state, false otherwise.
     */
    boolean forceStop () { return m_forceStop; }

    /*/ Serialization /*/
    @Override
    public String toString ()
    {
        return null;
    }
}
