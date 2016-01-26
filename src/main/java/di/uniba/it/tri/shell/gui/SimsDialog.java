/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.shell.gui;

import javax.swing.JComboBox;
import javax.swing.JSlider;

/**
 *
 * @author pierpaolo
 */
public class SimsDialog extends javax.swing.JDialog {

    /**
     * Creates new form SimsDialog
     */
    public SimsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public JComboBox<String> getComboBox1() {
        return comboBox1;
    }

    public JComboBox<String> getComboBox2() {
        return comboBox2;
    }

    public JSlider getSliderMax() {
        return sliderMax;
    }

    public void setSliderMax(JSlider sliderMax) {
        this.sliderMax = sliderMax;
    }

    public JSlider getSliderMin() {
        return sliderMin;
    }

    public void setSliderMin(JSlider sliderMin) {
        this.sliderMin = sliderMin;
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        comboBox1 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        comboBox2 = new javax.swing.JComboBox<>();
        sliderMin = new javax.swing.JSlider();
        sliderMax = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        okb = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Analyze TRI");
        setPreferredSize(new java.awt.Dimension(400, 250));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Time Interval 1");
        getContentPane().add(jLabel1, new java.awt.GridBagConstraints());

        comboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));
        getContentPane().add(comboBox1, new java.awt.GridBagConstraints());

        jLabel2.setText("Time Interval 2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        getContentPane().add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        getContentPane().add(comboBox2, gridBagConstraints);

        sliderMin.setMinorTickSpacing(10);
        sliderMin.setPaintTicks(true);
        sliderMin.setValue(0);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        getContentPane().add(sliderMin, gridBagConstraints);

        sliderMax.setMinorTickSpacing(10);
        sliderMax.setPaintTicks(true);
        sliderMax.setValue(100);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        getContentPane().add(sliderMax, gridBagConstraints);

        jLabel3.setText("Min");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        getContentPane().add(jLabel3, gridBagConstraints);

        jLabel4.setText("Max");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        getContentPane().add(jLabel4, gridBagConstraints);

        okb.setText("Close");
        okb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okbActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        getContentPane().add(okb, gridBagConstraints);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void okbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okbActionPerformed
        this.dispose();
    }//GEN-LAST:event_okbActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SimsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SimsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SimsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SimsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SimsDialog dialog = new SimsDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> comboBox1;
    private javax.swing.JComboBox<String> comboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton okb;
    private javax.swing.JSlider sliderMax;
    private javax.swing.JSlider sliderMin;
    // End of variables declaration//GEN-END:variables
}