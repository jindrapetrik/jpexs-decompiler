/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.gui.player.FlashPlayerPanel;
import com.jpexs.decompiler.flash.Main;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author JPEXS
 */
public class TagPanel extends JPanel implements ListSelectionListener {

   public JList tagList;
   public FlashPlayerPanel flashPanel;
   public JPanel displayPanel;
   public ImagePanel imagePanel;
   private SWF swf;
   final static String CARDFLASHPANEL = "Flash card";
   final static String CARDIMAGEPANEL = "Image card";
   final static String CARDEMPTYPANEL = "Empty card";
   private JPEGTablesTag jtt;
   private HashMap<Integer, CharacterTag> characters;

   static {
      try {
         File.createTempFile("temp", ".swf").delete(); //First call to this is slow, so make it first
      } catch (IOException ex) {
         Logger.getLogger(TagPanel.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   private void parseCharacters(List<Object> list) {
      for (Object t : list) {
         if (t instanceof CharacterTag) {
            characters.put(((CharacterTag) t).getCharacterID(), (CharacterTag) t);
         }
         if (t instanceof Container) {
            parseCharacters(((Container) t).getSubItems());
         }
      }
   }
   
   private JFrame frame;

   public TagPanel(JFrame frame,List<Tag> list, SWF swf) {
      this.frame=frame;   
      this.swf = swf;
      for (Tag t : swf.tags) {
         if (t instanceof JPEGTablesTag) {
            jtt = (JPEGTablesTag) t;
         }
      }
      characters = new HashMap<Integer, CharacterTag>();
      List<Object> list2 = new ArrayList<Object>();
      list2.addAll(swf.tags);
      parseCharacters(list2);
      tagList = new JList(list.toArray(new Tag[list.size()]));
      tagList.addListSelectionListener(this);

      setLayout(new BorderLayout());
      try {
         flashPanel = new FlashPlayerPanel(frame);
      } catch (FlashUnsupportedException fue) {
         
      }
      displayPanel = new JPanel(new CardLayout());
      if (flashPanel != null) {
         displayPanel.add(flashPanel, CARDFLASHPANEL);
      } else {
         JPanel swtPanel = new JPanel(new BorderLayout());
         swtPanel.add(new JLabel("<html><center>Preview of this object is not available on this platform. (Windows only)</center></html>", JLabel.CENTER), BorderLayout.CENTER);
         swtPanel.setBackground(Color.white);
         displayPanel.add(swtPanel, CARDFLASHPANEL);
      }
      imagePanel = new ImagePanel();
      displayPanel.add(imagePanel, CARDIMAGEPANEL);
      displayPanel.add(new JPanel(), CARDEMPTYPANEL);
      CardLayout cl = (CardLayout) (displayPanel.getLayout());
      cl.show(displayPanel, CARDEMPTYPANEL);
      tagList.setBorder(BorderFactory.createLoweredBevelBorder());
      displayPanel.setBorder(BorderFactory.createLineBorder(Color.black));
      add(new JScrollPane(tagList), BorderLayout.WEST);
      add(displayPanel, BorderLayout.CENTER);
   }
   private File tempFile;

   public void showCard(String card) {
      CardLayout cl = (CardLayout) (displayPanel.getLayout());
      cl.show(displayPanel, card);
   }

   private Object oldValue;
   
   @Override
   public void valueChanged(ListSelectionEvent e) {
      Tag tagObj = (Tag) tagList.getSelectedValue();
      if(tagObj==oldValue){
         return;
      }
      oldValue=tagObj;
      if (tagObj instanceof DefineBitsTag) {
         showCard(CARDIMAGEPANEL);
         imagePanel.setImage(((DefineBitsTag) tagObj).getFullImageData(jtt));
      } else if (tagObj instanceof DefineBitsJPEG2Tag) {
         showCard(CARDIMAGEPANEL);
         imagePanel.setImage(((DefineBitsJPEG2Tag) tagObj).imageData);
      } else if (tagObj instanceof DefineBitsJPEG3Tag) {
         showCard(CARDIMAGEPANEL);
         imagePanel.setImage(((DefineBitsJPEG3Tag) tagObj).imageData);
      } else if (tagObj instanceof DefineBitsJPEG4Tag) {
         showCard(CARDIMAGEPANEL);
         imagePanel.setImage(((DefineBitsJPEG4Tag) tagObj).imageData);
      } else if (tagObj instanceof DefineBitsLosslessTag) {
         showCard(CARDIMAGEPANEL);
         imagePanel.setImage(((DefineBitsLosslessTag) tagObj).getImage());
      } else if (tagObj instanceof DefineBitsLossless2Tag) {
         showCard(CARDIMAGEPANEL);
         imagePanel.setImage(((DefineBitsLossless2Tag) tagObj).getImage());
      } else if (((tagObj instanceof CharacterTag) || (tagObj instanceof FontTag)) && (tagObj instanceof Tag)) {
         try {

            if (tempFile != null) {
               tempFile.delete();
            }
            tempFile = File.createTempFile("temp", ".swf");
            tempFile.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(tempFile);
            SWFOutputStream sos = new SWFOutputStream(fos, 10);
            sos.write("FWS".getBytes());
            sos.write(13);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SWFOutputStream sos2 = new SWFOutputStream(baos, 10);
            int width = swf.displayRect.Xmax - swf.displayRect.Xmin;
            int height = swf.displayRect.Ymax - swf.displayRect.Ymin;
            sos2.writeRECT(swf.displayRect);
            sos2.writeUI8(0);
            sos2.writeUI8(swf.frameRate);
            sos2.writeUI16(100); //framecnt
            sos2.writeTag(new SetBackgroundColorTag(new RGB(255, 255, 255)));
            if (tagObj instanceof DefineBitsTag) {
               if (jtt != null) {
                  sos2.writeTag(jtt);
               }
            } else if (tagObj instanceof AloneTag) {
            } else {
               Set<Integer> needed = tagObj.getNeededCharacters();
               for (int n : needed) {
                  sos2.writeTag(characters.get(n));
               }
            }

            sos2.writeTag(tagObj);

            int chtId = 0;
            if (tagObj instanceof CharacterTag) {
               chtId = ((CharacterTag) tagObj).getCharacterID();
            }

            MATRIX mat = new MATRIX();
            mat.hasRotate = false;
            mat.hasScale = false;
            mat.translateX = 0;
            mat.translateY = 0;
            if (tagObj instanceof BoundedTag) {
               RECT r = ((BoundedTag) tagObj).getRect(characters);
               mat.translateX = -r.Xmin;
               mat.translateY = -r.Ymin;
               mat.translateX = mat.translateX + width / 2 - r.getWidth() / 2;
               mat.translateY = mat.translateY + height / 2 - r.getHeight() / 2;
            } else {
               mat.translateX = width / 4;
               mat.translateY = height / 4;
            }
            if (tagObj instanceof FontTag) {

               int countGlyphs = ((FontTag) tagObj).getGlyphShapeTable().length;
               int fontId = ((FontTag) tagObj).getFontId();
               int sloupcu = (int) Math.ceil(Math.sqrt(countGlyphs));
               int radku = (int) Math.ceil(((float) countGlyphs) / ((float) sloupcu));
               int x = 0;
               int y = 1;
               for (int f = 0; f < countGlyphs; f++) {
                  if (x >= sloupcu) {
                     x = 0;
                     y++;
                  }
                  List<TEXTRECORD> rec = new ArrayList<TEXTRECORD>();
                  TEXTRECORD tr = new TEXTRECORD();
                  int textHeight = height / radku;
                  tr.fontId = fontId;
                  tr.styleFlagsHasFont = true;
                  tr.textHeight = textHeight;
                  tr.glyphEntries = new GLYPHENTRY[1];
                  tr.styleFlagsHasColor = true;
                  tr.textColor = new RGB(0, 0, 0);
                  tr.glyphEntries[0] = new GLYPHENTRY();
                  tr.glyphEntries[0].glyphAdvance = 0;
                  tr.glyphEntries[0].glyphIndex = f;
                  rec.add(tr);
                  mat.translateX = x * width / sloupcu;
                  mat.translateY = y * height / radku;
                  sos2.writeTag(new DefineTextTag(999 + f, new RECT(0, width, 0, height), new MATRIX(), SWFOutputStream.getNeededBitsU(countGlyphs - 1), SWFOutputStream.getNeededBitsU(0), rec));
                  sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, true, false, 1 + f, 999 + f, mat, null, 0, null, 0, null));
                  x++;
               }
               sos2.writeTag(new ShowFrameTag());
            } else if ((tagObj instanceof DefineMorphShapeTag) || (tagObj instanceof DefineMorphShape2Tag)) {
               sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null));
               sos2.writeTag(new ShowFrameTag());
               int numFrames = 100;
               for (int ratio = 0; ratio < 65536; ratio += 65536 / numFrames) {
                  sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, false, true, 1, chtId, mat, null, ratio, null, 0, null));
                  sos2.writeTag(new ShowFrameTag());
               }
            } else {
               sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null));
               sos2.writeTag(new ShowFrameTag());
            }

            sos2.writeTag(new EndTag());
            byte data[] = baos.toByteArray();

            sos.writeUI32(sos.getPos() + data.length + 4);
            sos.write(data);
            fos.close();
            showCard(CARDFLASHPANEL);
            if (flashPanel != null) {
               if (flashPanel instanceof FlashPlayerPanel) {
                  flashPanel.displaySWF(tempFile.getAbsolutePath());
               }
            }

         } catch (Exception ex) {
            Logger.getLogger(TagPanel.class.getName()).log(Level.SEVERE, null, ex);
         }

      }
   }
}
