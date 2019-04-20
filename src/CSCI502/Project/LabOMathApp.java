/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSCI502.Project;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;

import CSCI502.Project.Lexical.Analyzer;

/**
 *
 * @author boley
 */
public class LabOMathApp
        extends JFrame
        implements ActionListener
{
    public static void main(String[] args)
    {
        EventQueue.invokeLater(() -> {
            LabOMathApp cmdlineApp = new LabOMathApp();
            cmdlineApp
                    .createAndShowGUI()
                    .setVisible(true);
        });
    }
    
    private Interpreter core;
    
    LabOMathApp()
    {
        super ("Labomath v0.1");
        core = new Interpreter();
    }

    LabOMathApp createAndShowGUI()
    {
        JPanel cmdlinePanel = new CmdLinePanel(this.core);
        Container me = this.getContentPane();
        me.add(cmdlinePanel);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        return this;
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        
    }
    
}

//public class AnimationApp
//        extends JFrame
//        implements ActionListener, ListSelectionListener
//{
//    private JButton bttnStart,      // Start button
//                    bttnPausRes,    // Pause/resume button
//                    bttnStop;       // Stop button
//    private JList listAnims;        // JList containing animation data objects
//    private AnimationPanel animPanel;
//                                    // Animation panel
//
//
//    AnimationApp()
//    {
//        super("Animation");
//    }
//    
//    AnimationApp createAndShowGUI()
//    {
//        // Create and configure animation selection components
//        JLabel selectionLabel = new JLabel("Animations");
//        loadAnimationData();
//        this.listAnims.getSelectionModel().addListSelectionListener(this);
//        JScrollPane animationSelectionPanel = new JScrollPane(this.listAnims);
//        Border scpBorder = animationSelectionPanel.getBorder(),
//               margin = new EmptyBorder(0, 5, 0, 5);
//        animationSelectionPanel.setBorder(new CompoundBorder(scpBorder, margin));
//
//        // Create and configure animation control buttons
//        this.bttnStart = new JButton("Start");
//        this.bttnPausRes = new JButton("Pause");
//        this.bttnStop = new JButton("Stop");
//        this.bttnStart.addActionListener(this);
//        this.bttnPausRes.addActionListener(this);
//        this.bttnStop.addActionListener(this);
//        this.bttnStart.setEnabled(false);
//        this.bttnPausRes.setEnabled(false);
//        this.bttnStop.setEnabled(false);
//
//        // Create the animation panel
//        this.animPanel = new AnimationPanel ();
//
//        // Configure GUI layout
//        GroupLayout layout = new GroupLayout(this.getContentPane());
//        this.getContentPane().setLayout(layout);
//        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(layout.createParallelGroup(Alignment.LEADING)
//                    .addComponent(animationSelectionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(selectionLabel))
//                .addPreferredGap(ComponentPlacement.RELATED)
//                .addGroup(layout.createParallelGroup(Alignment.LEADING)
//                    .addGroup(layout.createSequentialGroup()
//                        .addComponent(this.bttnStart, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
//                        .addComponent(this.bttnPausRes, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
//                        .addComponent(this.bttnStop, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                    .addComponent(this.animPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addContainerGap(68, Short.MAX_VALUE))
//        );
//        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(layout.createSequentialGroup()
//                        .addComponent(selectionLabel)
//                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                        .addComponent(animationSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 600, GroupLayout.PREFERRED_SIZE))
//                    .addGroup(layout.createSequentialGroup()
//                        .addComponent(this.animPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                            .addComponent(this.bttnStop, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addComponent(this.bttnPausRes, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addComponent(this.bttnStart, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
//                .addContainerGap())
//        );
//        
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        this.pack();
//        
//        return this;
//    }
//    
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        if (e.getSource() == this.bttnStart) {
//            // Disable start button, enable pause/resume and stop buttons
//            this.bttnStart.setEnabled(false);
//            this.bttnPausRes.setEnabled(true);
//            this.bttnStop.setEnabled(true);
//            // Begin animation
//            this.animPanel.start();
//        } else if (e.getSource() == this.bttnPausRes) {
//            if (this.bttnPausRes.getText().equals("Pause")) {
//                // Set pause/resume button text to Resume and pause animation
//                this.bttnPausRes.setText("Resume");
//                this.animPanel.pause();
//            }
//            else {
//                // Set pause/resume button text to Pause and resume animation
//                this.bttnPausRes.setText("Pause");
//                this.animPanel.resume();
//            }
//        } else if (e.getSource() == this.bttnStop) {
//            // Enable start button, disable pause/resume and stop buttons
//            this.bttnStart.setEnabled(true);
//            this.bttnPausRes.setEnabled(false);
//            this.bttnPausRes.setText("Pause");
//            this.bttnStop.setEnabled(false);
//            // Stop animation
//            this.animPanel.stop();
//        }
//    }
//
//    @Override
//    public void valueChanged(ListSelectionEvent e) {
//        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
//        if (!lsm.isSelectionEmpty()) {
//            // Stop current animation (if any) and reset button states to defaults
//            this.animPanel.stop();
//            this.bttnStart.setEnabled(true);
//            this.bttnPausRes.setEnabled(false);
//            this.bttnPausRes.setText("Pause");
//            this.bttnStop.setEnabled(false);
//            
//            // Get the selected animation object and pass to the animation panel
//            Animation selectedAnim = (Animation) this.listAnims.getSelectedValue();
//            this.animPanel.loadAnimation(selectedAnim);
//        }
//    }
//    
//    private void loadAnimationData()
//    {
//        try (BufferedReader rdr = Files.newBufferedReader(
//                Paths.get("Animations/animations.txt")
//                ))
//        {
//            String line;
//            List<Animation> animations = new ArrayList<>();
//            while ((line = rdr.readLine()) != null) {
//                String params[] = line.split(":");
//                Animation animObj = new Animation(
//                        params[0].replaceAll("\\ ", "_"),
//                        Integer.parseInt(params[1]),
//                        Integer.parseInt(params[2]),
//                        Integer.parseInt(params[3]),
//                        Integer.parseInt(params[4])
//                );
//                animations.add(animObj);
//            }
//            this.listAnims = new JList(animations.toArray());
//        } catch (IOException ex) {
//            Logger.getLogger(AnimationApp.class.getName())
//                    .log(Level.SEVERE, null, ex);
//            JOptionPane.showMessageDialog(this, "Error loading animation configuration file.");
//        }
//    }
//}
