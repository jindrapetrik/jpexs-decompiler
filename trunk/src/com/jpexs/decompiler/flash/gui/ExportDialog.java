package com.jpexs.decompiler.flash.gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 *
 * @author JPEXS
 */
public class ExportDialog extends JDialog {

   JComboBox images;
   JComboBox shapes;
   JComboBox actionScript;
   JComboBox movies;
   boolean cancelled = false;

   public ExportDialog() {
      setTitle("Export...");
      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            cancelled = true;
         }
      });
      setSize(200, 200);
      setLayout(null);
      JLabel shapesLabel = new JLabel("Shapes");
      shapesLabel.setBounds(10, 10, 60, 25);
      shapes = new JComboBox(new String[]{"SVG"});
      shapes.setBounds(75, 10, 95, 25);

      JLabel imagesLabel = new JLabel("Images");
      imagesLabel.setBounds(10, 35, 60, 25);
      images = new JComboBox(new String[]{"PNG/JPEG"});
      images.setBounds(75, 35, 95, 25);

      JLabel moviesLabel = new JLabel("Movies");
      moviesLabel.setBounds(10, 60, 60, 25);
      movies = new JComboBox(new String[]{"FLV (No audio)"});
      movies.setBounds(75, 60, 95, 25);
      
      JLabel actionScriptLabel = new JLabel("ActionScript");
      actionScriptLabel.setBounds(10, 85, 60, 25);
      actionScript = new JComboBox(new String[]{"AS", "PCODE"});
      actionScript.setBounds(75, 85, 95, 25);

      JButton okButton = new JButton("OK");
      okButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            setVisible(false);
         }
      });
      okButton.setBounds(20, 120, 75, 25);

      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            cancelled = true;
            setVisible(false);
         }
      });
      cancelButton.setBounds(95, 120, 75, 25);
      Container cnt = getContentPane();
      cnt.add(shapes);
      cnt.add(shapesLabel);
      cnt.add(images);
      cnt.add(imagesLabel);
      cnt.add(movies);
      cnt.add(moviesLabel);
      cnt.add(actionScript);
      cnt.add(actionScriptLabel);
      cnt.add(okButton);
      cnt.add(cancelButton);
      View.centerScreen(this);
      View.setWindowIcon(this);
      getRootPane().setDefaultButton(okButton);
      setModal(true);
   }

   @Override
   public void setVisible(boolean b) {
      if (b) {
         cancelled = false;
      }
      super.setVisible(b);
   }
}
