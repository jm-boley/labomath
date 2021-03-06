/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import Runtime.IO.InputChannel;
import Runtime.VirtualMachine;
import java.awt.Color;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Command line interface panel
 * @author Joshua Boley
 */
public class CmdLinePanel
        extends JPanel
        implements KeyListener, FocusListener
{
    private JScrollPane cmdLinePane;
    private JTextArea txtConsoleOut;
    private JTextField tfConsoleIn;
    private Label lblPrompt;
    public static final Color STD_FONT_CLR;
    
    private final VirtualMachine m_jit;
    private final InputChannel m_cmdlineIn;

    static {
        STD_FONT_CLR = new Color(245, 245, 245);
    }
    
    public CmdLinePanel(final VirtualMachine core)
    {
        super();
        initComponents();
        
        // Initialize JIT environment
        this.m_jit = core;
        m_cmdlineIn = m_jit.initLocalInputChannel(tfConsoleIn);
        m_jit.initLocalOutputChannel(txtConsoleOut);
    }
    
    private void initComponents()
    {
        setBorder(BorderFactory.createEtchedBorder());

        tfConsoleIn = new JTextField();
        cmdLinePane = new JScrollPane();
        txtConsoleOut = new JTextArea();
        lblPrompt = new Label();

        Font defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 15);
        tfConsoleIn.setToolTipText("Command Line");
        tfConsoleIn.setBorder(null);
        tfConsoleIn.setFont(defaultFont);
        tfConsoleIn.setBackground(Color.black);
        tfConsoleIn.setForeground(STD_FONT_CLR);
        tfConsoleIn.addKeyListener(this);

        txtConsoleOut.setColumns(20);
        txtConsoleOut.setLineWrap(true);
        txtConsoleOut.setRows(5);
        txtConsoleOut.setTabSize(4);
        txtConsoleOut.setBorder(null);
        txtConsoleOut.setEditable(false);
        txtConsoleOut.setFont(defaultFont);
        txtConsoleOut.setBackground(Color.black);
        txtConsoleOut.setForeground(STD_FONT_CLR);
        txtConsoleOut.addFocusListener(this);
        
        cmdLinePane.setBorder(null);
        cmdLinePane.setViewportView(txtConsoleOut);

        lblPrompt.setFont(defaultFont);
        lblPrompt.setBackground(Color.black);
        lblPrompt.setForeground(STD_FONT_CLR);
        lblPrompt.setText(">");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblPrompt, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                        .addComponent(tfConsoleIn, GroupLayout.PREFERRED_SIZE, 875, GroupLayout.PREFERRED_SIZE))
                    .addComponent(cmdLinePane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmdLinePane, GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(tfConsoleIn)
                    .addComponent(lblPrompt, GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                .addContainerGap())
        );

        lblPrompt.getAccessibleContext().setAccessibleName("lblPrompt");
    }
    
    @Override
    public void keyTyped(KeyEvent e)
    {}

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String cmd = tfConsoleIn.getText();
            txtConsoleOut.append("> " + cmd + "\n");
            m_cmdlineIn.inputReady();
            m_jit.run();
            txtConsoleOut.setForeground(STD_FONT_CLR);
            tfConsoleIn.setText("");
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {}

    @Override
    public void focusGained(FocusEvent e) {
        if (e.getSource() == txtConsoleOut)
            tfConsoleIn.requestFocusInWindow();
    }

    @Override
    public void focusLost(FocusEvent e)
    {}
    
}
