package GUI;

import Runtime.IO.InputChannel;
import Runtime.JIT.Compiler;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;

/**
 *
 * @author Joshua Boley
 */
public class TextEditorForm extends javax.swing.JInternalFrame {

    private final Compiler m_jit;
    private final InputChannel m_editorIn;
    private String m_filePath;
    private boolean m_changed;
    
    /**
     * Creates new form TextEditorForm
     * @param jitEnv
     */
    public TextEditorForm(Compiler jitEnv) {
        super();
        initComponents();
        
        m_filePath = null;
        m_changed = false;
        
        // Initialize JIT environment
        m_jit = jitEnv;
        m_editorIn = m_jit.initInputSource(tpTextEditor);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
        spTextEditor = new JScrollPane();
        tpTextEditor = new JTextPane();
        tbCommonFuncs = new JToolBar();
        bttnNewFile = new JButton();
        bttnOpenFile = new JButton();
        bttnSaveFile = new JButton();
        sepToolBarCommonFuncs = new JToolBar.Separator();
        bttnRun = new JButton();
        mbTextEditor = new JMenuBar();
        jmFile = new JMenu();
        miFileNew = new JMenuItem();
        miFileOpen = new JMenuItem();
        miFileSaveAs = new JMenuItem();
        miFileSave = new JMenuItem();
        miFileClose = new JMenuItem();
//        jmEdit = new JMenu();
        miEditCopy = new JMenuItem();
        miEditPaste = new JMenuItem();

        setPreferredSize(new java.awt.Dimension(710, 500));

        tpTextEditor.setFont(defaultFont);
        tpTextEditor.setBackground(new Color(250, 250, 250));
        tpTextEditor.setEnabled(false);
        tpTextEditor.setMaximumSize(new java.awt.Dimension(700, 500));
        tpTextEditor.setPreferredSize(new java.awt.Dimension(700, 500));
        tpTextEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent evt) {
                keyStrokeDetected(evt);
            }
        });
        spTextEditor.setViewportView(tpTextEditor);

        tbCommonFuncs.setRollover(true);

        bttnNewFile.setText("New");
        bttnNewFile.setFocusable(false);
        bttnNewFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bttnNewFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bttnNewFile.addActionListener((ActionEvent evt) -> {
            newFileActionPerformed(evt);
        });
        tbCommonFuncs.add(bttnNewFile);

        bttnOpenFile.setText("Open");
        bttnOpenFile.setFocusable(false);
        bttnOpenFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bttnOpenFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bttnOpenFile.addActionListener((ActionEvent evt) -> {
            fileOpenActionPerformed(evt);
        });
        tbCommonFuncs.add(bttnOpenFile);

        bttnSaveFile.setText("Save");
        bttnSaveFile.setFocusable(false);
        bttnSaveFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bttnSaveFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bttnSaveFile.addActionListener((ActionEvent evt) -> {
            fileSaveActionPerformed(evt);
        });
        tbCommonFuncs.add(bttnSaveFile);
        tbCommonFuncs.add(sepToolBarCommonFuncs);

        bttnRun.setText("Run");
        bttnRun.setFocusable(false);
        bttnRun.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bttnRun.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bttnRun.addActionListener((ActionEvent evt) -> {
            runBttnActionPerformed(evt);
        });
        tbCommonFuncs.add(bttnRun);

        jmFile.setText("File");

        miFileNew.setText("New");
        miFileNew.addActionListener((ActionEvent evt) -> {
            newFileActionPerformed(evt);
        });
        jmFile.add(miFileNew);

        miFileOpen.setText("Open");
        miFileOpen.addActionListener((ActionEvent evt) -> {
            fileOpenActionPerformed(evt);
        });
        jmFile.add(miFileOpen);

        miFileSaveAs.setText("Save As");
        jmFile.add(miFileSaveAs);

        miFileSave.setText("Save");
        miFileSave.addActionListener((ActionEvent evt) -> {
            fileSaveActionPerformed(evt);
        });
        jmFile.add(miFileSave);

        miFileClose.setText("Close");
        miFileClose.addActionListener((ActionEvent evt) -> {
            fileCloseActionPerformed(evt);
        });
        jmFile.add(miFileClose);

        mbTextEditor.add(jmFile);

//        jmEdit.setText("Edit");
//
//        miEditCopy.setText("Copy");
//        jmEdit.add(miEditCopy);
//
//        miEditPaste.setText("Paste");
//        jmEdit.add(miEditPaste);
//
//        mbTextEditor.add(jmEdit);

        setJMenuBar(mbTextEditor);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(spTextEditor, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(tbCommonFuncs, GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(tbCommonFuncs, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(spTextEditor, GroupLayout.PREFERRED_SIZE, 673, GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }

    private void keyStrokeDetected(KeyEvent evt)
    {
        m_changed = true;
    }
    
    private void newFileActionPerformed(ActionEvent evt)
    {
        if (!tpTextEditor.isEnabled()) {
            tpTextEditor.setBackground(Color.WHITE);
            tpTextEditor.setEnabled(true);
            tpTextEditor.requestFocusInWindow();
        }
        else {
            if (m_changed) {
                queryAndSaveIf(evt);
                m_changed = false;
            }
            tpTextEditor.setText("");
        }
    }

    private void fileOpenActionPerformed(ActionEvent evt)
    {
        if (m_changed) {
            boolean cancelled = queryAndSaveIf(evt);
            if (cancelled)
                return;
            m_changed = false;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open file");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        
        int selection = fileChooser.showOpenDialog(this);
        if (selection == JFileChooser.APPROVE_OPTION) {
            File openFile = fileChooser.getSelectedFile();
            FileReader fr;
            try {
                fr = new FileReader(openFile);
                try (BufferedReader br = new BufferedReader(fr)) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                        sb.append(line).append("\n");
                    tpTextEditor.setText(sb.toString());
                    m_filePath = openFile.getAbsolutePath();
                    fr.close();
                } catch (IOException ex) {
                    Logger.getLogger(TextEditorForm.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TextEditorForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!tpTextEditor.isEnabled()) {
                tpTextEditor.setBackground(Color.WHITE);
                tpTextEditor.setEnabled(true);
            }
        }
    }
    
    private void fileSaveAsActionPerformed(ActionEvent evt)
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save to file");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        
        int selection = fileChooser.showSaveDialog(this);
        if (selection == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            FileWriter fw;
            try {
                fw = new FileWriter(saveFile);
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write(tpTextEditor.getText());
                    m_filePath = saveFile.getAbsolutePath();
                }
                fw.close();
                m_changed = false;
            }
            catch (IOException ex) {
                Logger.getLogger(TextEditorForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void fileSaveActionPerformed(ActionEvent evt)
    {
        if (m_filePath == null) {
            fileSaveAsActionPerformed(evt);
            return;
        }
        File saveFile = new File(m_filePath);
        FileWriter fw;
        try {
            fw = new FileWriter(saveFile);
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(tpTextEditor.getText());
            }
            fw.close();
            m_changed = false;
        }
        catch (IOException ex) {
            Logger.getLogger(TextEditorForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void fileCloseActionPerformed(ActionEvent evt)
    {
        if (m_changed) {
            boolean cancelled = queryAndSaveIf(evt);
            if (cancelled)
                return;
            m_changed = false;
        }
        tpTextEditor.setText("");
        tpTextEditor.setBackground(new Color(250, 250, 250));
        tpTextEditor.setEnabled(false);
    }
    
    private void runBttnActionPerformed(ActionEvent evt)
    {
        try {
            m_editorIn.inputReady();
            m_jit.run();
        } catch (IOException ex) {
            Logger.getLogger(TextEditorForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean queryAndSaveIf(ActionEvent evt)
    {
        int selection = JOptionPane.showConfirmDialog(this,
            "Do wish to save your work?", "Select an option",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE
        );
        if (selection == 0)
            fileSaveActionPerformed(evt);
        return (selection == 2);    // 2 - Cancel
    }

    // GUI elements                  
    private JButton bttnNewFile;
    private JButton bttnOpenFile;
    private JButton bttnRun;
    private JButton bttnSaveFile;
    private JMenu jmEdit;
    private JMenu jmFile;
    private JMenuBar mbTextEditor;
    private JMenuItem miEditCopy;
    private JMenuItem miEditPaste;
    private JMenuItem miFileClose;
    private JMenuItem miFileNew;
    private JMenuItem miFileOpen;
    private JMenuItem miFileSaveAs;
    private JMenuItem miFileSave;
    private JToolBar.Separator sepToolBarCommonFuncs;
    private JScrollPane spTextEditor;
    private JToolBar tbCommonFuncs;
    private JTextPane tpTextEditor;
}
