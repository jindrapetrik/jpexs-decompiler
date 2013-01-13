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
            int width = swf.displayRect.Xmax - swf.displayRect.Xmin;
            int height = swf.displayRect.Ymax - swf.displayRect.Ymin;
            sos2.writeRECT(swf.displayRect);
            sos2.writeUI8(0);
            sos2.writeUI8(swf.frameRate);
            sos2.writeUI16(100); //framecnt
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

               int countGlyphs = ((FontTag) tagObj).getGlyphShapeTable().length;
               int fontId = ((FontTag) tagObj).getFontId();
               int sloupcu=(int)Math.ceil(Math.sqrt(countGlyphs));
               int radku=(int)Math.ceil(((float)countGlyphs)/((float)sloupcu));
               int x=0;
               int y=1;
               for (int f = 0; f < countGlyphs; f++) {                                    
                  if(x>=sloupcu){
                     x=0;
                     y++;
                  }
                  List<TEXTRECORD> rec = new ArrayList<TEXTRECORD>();
                  TEXTRECORD tr = new TEXTRECORD();
                  int textHeight=height/radku;
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
                  mat.translateX=x*width/sloupcu;
                  mat.translateY=y*height/radku;
                  sos2.writeTag(new DefineTextTag(999 + f, new RECT(0, width, 0, height), new MATRIX(), SWFOutputStream.getNeededBitsU(countGlyphs - 1), SWFOutputStream.getNeededBitsU(0), rec));
                  sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, true, false, 1+f, 999 + f, mat, null, 0, null, 0, null));
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
            flashPanel.displaySWF(tempFile.getAbsolutePath());

         } catch (Exception ex) {
            ex.printStackTrace();
         }

      }
   }
}
