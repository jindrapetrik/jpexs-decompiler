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

   boolean cancelled = false;
   String options[][] = {
      {"SVG"},
      {"PNG/JPEG"},
      {"FLV (No audio)"},
      {"FLV (Audio only)"},
      {"AS", "PCODE"}
   };
   String optionNames[] = {
      "Shapes",
      "Images",
      "Movies",
      "Sounds",
      "ActionScript"
   };
   public static final int OPTION_SHAPES = 0;
   public static final int OPTION_IMAGES = 1;
   public static final int OPTION_MOVIES = 2;
   public static final int OPTION_SOUNDS = 3;
   public static final int OPTION_ACTIONSCRIPT = 4;
   private JComboBox combos[];

   public int getOption(int index) {
      return combos[index].getSelectedIndex();
   }

   public ExportDialog() {
      setTitle("Export...");
      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            cancelled = true;
         }
      });

      setLayout(null);

      Container cnt = getContentPane();
      combos = new JComboBox[optionNames.length];
      int top = 10;
      for (int i = 0; i < optionNames.length; i++) {
         JLabel lab = new JLabel(optionNames[i]);
         lab.setBounds(10, top, 60, 25);
         cnt.add(lab);
         combos[i] = new JComboBox(options[i]);
         combos[i].setBounds(75, top, 105, 25);
         cnt.add(combos[i]);
         top += 25;
      }
      top += 10;


      JButton okButton = new JButton("OK");
      okButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            setVisible(false);
         }
      });
      okButton.setBounds(25, top, 75, 25);

      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            cancelled = true;
            setVisible(false);
         }
      });
      cancelButton.setBounds(100, top, 75, 25);

      cnt.add(okButton);
      cnt.add(cancelButton);
      setSize(210, top + 70);
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
