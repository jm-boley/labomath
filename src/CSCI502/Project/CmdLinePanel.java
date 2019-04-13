/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSCI502.Project;

import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javafx.scene.input.KeyCode;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 *
 * @author boley
 */
public class CmdLinePanel
        extends JPanel
        implements KeyListener
{
    private int cursorOffset = 0;
    private JTextArea taCmdWindow;
    
    CmdLinePanel ()
    {
        this.setLayout(new GridLayout(2, 1));
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        int selectionStart = taCmdWindow.getSelectionStart(),
            selectionEnd = taCmdWindow.getSelectionEnd();
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && selectionStart > 2) {
            e.consume();
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        
    }
}
