/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSCI502.Project;

/**
 *
 * @author boley
 */
public class TextEditorForm extends javax.swing.JInternalFrame {

    /**
     * Creates new form TextEditorForm
     */
    public TextEditorForm() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        mbTextEditor = new javax.swing.JMenuBar();
        jmFile = new javax.swing.JMenu();
        miFileNew = new javax.swing.JMenuItem();
        miFileOpen = new javax.swing.JMenuItem();
        miFileSave = new javax.swing.JMenuItem();
        miFileClose = new javax.swing.JMenuItem();
        jmEdit = new javax.swing.JMenu();
        miEditCopy = new javax.swing.JMenuItem();
        miEditPaste = new javax.swing.JMenuItem();
        jmRun = new javax.swing.JMenu();
        miRunRun = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();

        jScrollPane1.setViewportView(jTextPane1);

        jmFile.setText("File");

        miFileNew.setText("New");
        jmFile.add(miFileNew);

        miFileOpen.setText("Open");
        jmFile.add(miFileOpen);

        miFileSave.setText("Save");
        jmFile.add(miFileSave);

        miFileClose.setText("Close");
        jmFile.add(miFileClose);

        mbTextEditor.add(jmFile);

        jmEdit.setText("Edit");

        miEditCopy.setText("Copy");
        jmEdit.add(miEditCopy);

        miEditPaste.setText("Paste");
        jmEdit.add(miEditPaste);

        mbTextEditor.add(jmEdit);

        jmRun.setText("Run");

        miRunRun.setText("Run");
        jmRun.add(miRunRun);

        jMenuItem1.setText("Run from File");
        jmRun.add(jMenuItem1);

        mbTextEditor.add(jmRun);

        setJMenuBar(mbTextEditor);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 742, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JMenu jmEdit;
    private javax.swing.JMenu jmFile;
    private javax.swing.JMenu jmRun;
    private javax.swing.JMenuBar mbTextEditor;
    private javax.swing.JMenuItem miEditCopy;
    private javax.swing.JMenuItem miEditPaste;
    private javax.swing.JMenuItem miFileClose;
    private javax.swing.JMenuItem miFileNew;
    private javax.swing.JMenuItem miFileOpen;
    private javax.swing.JMenuItem miFileSave;
    private javax.swing.JMenuItem miRunRun;
    // End of variables declaration//GEN-END:variables
}