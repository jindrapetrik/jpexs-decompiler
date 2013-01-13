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
package com.jpexs.asdec.gui;

import com.jpexs.asdec.SWF;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.tags.DefineBitsJPEG2Tag;
import com.jpexs.asdec.tags.DefineBitsJPEG3Tag;
import com.jpexs.asdec.tags.DefineBitsJPEG4Tag;
import com.jpexs.asdec.tags.DefineBitsLossless2Tag;
import com.jpexs.asdec.tags.DefineBitsLosslessTag;
import com.jpexs.asdec.tags.DefineBitsTag;
import com.jpexs.asdec.tags.DefineFont2Tag;
import com.jpexs.asdec.tags.DefineFont3Tag;
import com.jpexs.asdec.tags.DefineFontTag;
import com.jpexs.asdec.tags.DefineMorphShape2Tag;
import com.jpexs.asdec.tags.DefineMorphShapeTag;
import com.jpexs.asdec.tags.DefineTextTag;
import com.jpexs.asdec.tags.DoABCTag;
import com.jpexs.asdec.tags.DoActionTag;
import com.jpexs.asdec.tags.DoInitActionTag;
import com.jpexs.asdec.tags.EndTag;
import com.jpexs.asdec.tags.JPEGTablesTag;
import com.jpexs.asdec.tags.PlaceObject2Tag;
import com.jpexs.asdec.tags.PlaceObject3Tag;
import com.jpexs.asdec.tags.PlaceObjectTag;
import com.jpexs.asdec.tags.RemoveObject2Tag;
import com.jpexs.asdec.tags.RemoveObjectTag;
import com.jpexs.asdec.tags.ShowFrameTag;
import com.jpexs.asdec.tags.SymbolClassTag;
import com.jpexs.asdec.tags.Tag;
import com.jpexs.asdec.tags.base.BoundedTag;
import com.jpexs.asdec.tags.base.CharacterTag;
import com.jpexs.asdec.tags.base.FontTag;
import com.jpexs.asdec.types.GLYPHENTRY;
import com.jpexs.asdec.types.MATRIX;
import com.jpexs.asdec.types.RECT;
import com.jpexs.asdec.types.RGB;
import com.jpexs.asdec.types.TEXTRECORD;
import com.jpexs.flashplayer.FlashPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author JPEXS
 */
public class TagPanel extends JPanel implements ListSelectionListener {

   public JList tagList;
   public FlashPanel flashPanel;
   public JPanel displayPanel;
   public ImagePanel imagePanel;
   private SWF swf;
   final static String CARDFLASHPANEL = "Flash card";
   final static String CARDIMAGEPANEL = "Image card";
   final static String CARDEMPTYPANEL = "Empty card";
   private JPEGTablesTag jtt;

   public TagPanel(List<Tag> list, SWF swf) {
      this.swf = swf;
      for (Tag t : swf.tags) {
         if (t instanceof JPEGTablesTag) {
            jtt = (JPEGTablesTag) t;
         }
      }
      tagList = new JList(list.toArray(new Tag[list.size()]));
      tagList.addListSelectionListener(this);
      tagList.setPreferredSize(new Dimension(200, 1));
      tagList.setSize(200, 1);
      setLayout(new BorderLayout());
      flashPanel = new FlashPanel();
      displayPanel = new JPanel(new CardLayout());
      displayPanel.add(flashPanel, CARDFLASHPANEL);
      imagePanel = new ImagePanel();
      CardLayout cl = (CardLayout) (displayPanel.getLayout());
      cl.show(displayPanel, CARDEMPTYPANEL);
      displayPanel.add(imagePanel, CARDIMAGEPANEL);
      displayPanel.add(new JPanel(), CARDEMPTYPANEL);

      tagList.setBorder(BorderFactory.createLoweredBevelBorder());
      displayPanel.setBorder(BorderFactory.createLineBorder(Color.black));
      add(tagList, BorderLayout.WEST);
      add(displayPanel, BorderLayout.CENTER);
   }
   private File tempFile;

   public void showCard(String card) {
      CardLayout cl = (CardLayout) (displayPanel.getLayout());
      cl.show(displayPanel, card);
   }

   @Override
   public void valueChanged(ListSelectionEvent e) {
      Tag tagObj = (Tag) tagList.getSelectedValue();
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
            int width = 8000;
            int height = 6000;
            RECT rct = new RECT();
            rct.Ymax = height;
            rct.Xmax = width;
            sos2.writeRECT(rct);
            sos2.writeUI8(0);
            sos2.writeUI8(30);
            sos2.writeUI16(1); //framecnt
            //sos2.writeTag(new SetBackgroundColorTag(new RGB(255, 0, 255)));
            for (Tag tag : swf.tags) {
               if ((!(tag instanceof PlaceObjectTag))
                       && (!(tag instanceof PlaceObject2Tag))
                       && (!(tag instanceof PlaceObject3Tag))
                       && (!(tag instanceof RemoveObjectTag))
                       && (!(tag instanceof RemoveObject2Tag))
                       && (!(tag instanceof DoActionTag))
                       && (!(tag instanceof DoInitActionTag))
                       && (!(tag instanceof DoABCTag))
                       && (!(tag instanceof SymbolClassTag))
                       && (!(tag instanceof ShowFrameTag))) {
                  sos2.writeTag(tag);
               }
            }


            int chtId = 0;
            if (tagObj instanceof CharacterTag) {
               chtId = ((CharacterTag) tagObj).getCharacterID();
            }

            //sos2.writeTag((Tag) tagObj);
            MATRIX mat = new MATRIX();
            mat.hasRotate = false;
            mat.hasScale = false;
            mat.translateX = 0;
            mat.translateY = 0;
            if (tagObj instanceof BoundedTag) {
               RECT r = ((BoundedTag) tagObj).getRect();
               mat.translateX = -r.Xmin;
               mat.translateY = -r.Ymin;
               mat.translateX = mat.translateX + width / 2 - r.getWidth() / 2;
               mat.translateY = mat.translateY + height / 2 - r.getHeight() / 2;
            } else {
               mat.translateX = width / 2;
               mat.translateY = height / 2;
            }
            if (tagObj instanceof FontTag) {

               int countGlyphs = 0;
               int fontId = 0;
               if (tagObj instanceof DefineFontTag) {
                  countGlyphs = ((DefineFontTag) tagObj).glyphShapeTable.length;
                  fontId = ((DefineFontTag) tagObj).fontId;
               }
               if (tagObj instanceof DefineFont2Tag) {
                  countGlyphs = ((DefineFont2Tag) tagObj).glyphShapeTable.length;
                  fontId = ((DefineFont2Tag) tagObj).fontId;
               }
               if (tagObj instanceof DefineFont3Tag) {
                  countGlyphs = ((DefineFont3Tag) tagObj).glyphShapeTable.length;
                  fontId = ((DefineFont3Tag) tagObj).fontId;
               }

               List<TEXTRECORD> rec = new ArrayList<TEXTRECORD>();
               TEXTRECORD tr = new TEXTRECORD();
               tr.fontId = fontId;
               tr.styleFlagsHasFont = true;
               tr.textHeight = 460;
               tr.glyphEntries = new GLYPHENTRY[countGlyphs];
               tr.styleFlagsHasColor = true;
               tr.textColor = new RGB(0, 0, 0);
               int adv = 300;
               int maxadv = 0;
               for (int f = 0; f < countGlyphs; f++) {
                  tr.glyphEntries[f] = new GLYPHENTRY();
                  tr.glyphEntries[f].glyphAdvance = adv;
                  adv += 300;
                  if (adv > maxadv) {
                     maxadv = adv;
                  }
                  tr.glyphEntries[f].glyphIndex = f;
               }
               rec.add(tr);
               sos2.writeTag(new DefineTextTag(999, new RECT(0, width, 0, height), new MATRIX(), SWFOutputStream.getNeededBitsU(countGlyphs - 1), SWFOutputStream.getNeededBitsU(maxadv), rec));
               sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, true, false, 1, 999, mat, null, 0, null, 0, null));
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
            flashPanel.displaySWF(tempFile.getAbsolutePath());

         } catch (Exception ex) {
            ex.printStackTrace();
         }

      }
   }
}
