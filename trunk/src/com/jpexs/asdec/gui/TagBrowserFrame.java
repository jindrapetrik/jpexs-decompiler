package com.jpexs.asdec.gui;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWF;
import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.tags.DefineFont2Tag;
import com.jpexs.asdec.tags.DefineFont3Tag;
import com.jpexs.asdec.tags.DefineFontTag;
import com.jpexs.asdec.tags.DefineMorphShape2Tag;
import com.jpexs.asdec.tags.DefineMorphShapeTag;
import com.jpexs.asdec.tags.DefineShapeTag;
import com.jpexs.asdec.tags.DefineSpriteTag;
import com.jpexs.asdec.tags.DefineTextTag;
import com.jpexs.asdec.tags.DoActionTag;
import com.jpexs.asdec.tags.EndTag;
import com.jpexs.asdec.tags.PlaceObject2Tag;
import com.jpexs.asdec.tags.PlaceObject3Tag;
import com.jpexs.asdec.tags.PlaceObjectTag;
import com.jpexs.asdec.tags.RemoveObject2Tag;
import com.jpexs.asdec.tags.RemoveObjectTag;
import com.jpexs.asdec.tags.SetBackgroundColorTag;
import com.jpexs.asdec.tags.ShowFrameTag;
import com.jpexs.asdec.tags.Tag;
import com.jpexs.asdec.tags.base.BoundedTag;
import com.jpexs.asdec.tags.base.CharacterTag;
import com.jpexs.asdec.tags.base.Container;
import com.jpexs.asdec.tags.base.FontTag;
import com.jpexs.asdec.types.GLYPHENTRY;
import com.jpexs.asdec.types.MATRIX;
import com.jpexs.asdec.types.RECT;
import com.jpexs.asdec.types.RGB;
import com.jpexs.asdec.types.TEXTRECORD;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 *
 * @author Jindra
 */
public class TagBrowserFrame extends JFrame implements TreeSelectionListener {

   JTree tagTree;
   FlashPanel fPanel;
   private SWF swf;

   public TagBrowserFrame(SWF swf) {
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setSize(800, 600);
      List<Object> objs = new ArrayList<Object>();
      objs.addAll(swf.tags);
      this.swf=swf;
      tagTree = new JTree(new TagTreeModel(swf.tags));
      getContentPane().setLayout(new BorderLayout());
      fPanel = new FlashPanel(400, 400);
      getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tagTree), new JScrollPane(fPanel)), BorderLayout.CENTER);
      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            fPanel.dispose();
         }
      });
      tagTree.addTreeSelectionListener(this);
   }
   
   private File tempFile;

   public static void main(String[] args) throws IOException {
      if(args.length<1){
         return;
      }
      View.setWinLookAndFeel();
      SWF swf = new SWF(new FileInputStream(args[0]));
      TagBrowserFrame tbf = new TagBrowserFrame(swf);
      tbf.setVisible(true);
      tbf.fPanel.displaySWF(args[0]);
   }

   @Override
   public void valueChanged(TreeSelectionEvent e) {
      Object obj = tagTree.getLastSelectedPathComponent();
      if (obj instanceof TagNode) {
         Object tagObj = ((TagNode) obj).tag;
         if (((tagObj instanceof CharacterTag)||(tagObj instanceof FontTag)) && (tagObj instanceof Tag)) {
            try {
               
               if(tempFile!=null){
                  tempFile.delete();
               }
               tempFile=new File("D:\\temp.swf");//File.createTempFile("temp", ".swf");
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
               sos2.writeTag(new SetBackgroundColorTag(new RGB(255, 0, 255)));
               for(Tag tag:swf.tags){
                  if((!(tag instanceof PlaceObjectTag))
                          &&(!(tag instanceof PlaceObject2Tag))
                          &&(!(tag instanceof PlaceObject3Tag))
                          &&(!(tag instanceof RemoveObjectTag))
                          &&(!(tag instanceof RemoveObject2Tag))
                          &&(!(tag instanceof DoActionTag))                          
                          &&(!(tag instanceof ShowFrameTag))
                          ){
                     sos2.writeTag(tag);
                  }
               }
               
                      
               int chtId=0;
               if(tagObj instanceof CharacterTag){
                  chtId=((CharacterTag) tagObj).getCharacterID();
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
               }else{
                  mat.translateX=width/2; 
                  mat.translateY=height/2;
               }           
               if(tagObj instanceof FontTag){
                  
                  int countGlyphs=0;                                   
                  int fontId=0;
                  if(tagObj instanceof DefineFontTag){
                     countGlyphs=((DefineFontTag)tagObj).glyphShapeTable.length;
                     fontId=((DefineFontTag)tagObj).fontId;
                  }
                  if(tagObj instanceof DefineFont2Tag){
                     countGlyphs=((DefineFont2Tag)tagObj).glyphShapeTable.length;
                     fontId=((DefineFont2Tag)tagObj).fontId;
                  }
                  if(tagObj instanceof DefineFont3Tag){
                     countGlyphs=((DefineFont3Tag)tagObj).glyphShapeTable.length;
                     fontId=((DefineFont3Tag)tagObj).fontId;
                  }
                  
                  List<TEXTRECORD> rec=new ArrayList<TEXTRECORD>();
                  TEXTRECORD tr=new TEXTRECORD();
                  tr.fontId=fontId;
                  tr.styleFlagsHasFont=true;
                  tr.textHeight=460;           
                  tr.glyphEntries=new GLYPHENTRY[countGlyphs];
                  tr.styleFlagsHasColor=true;
                  tr.textColor=new RGB(0,0,0);
                  int adv=300;
                  int maxadv=0;
                  for(int f=0;f<countGlyphs;f++){                     
                     tr.glyphEntries[f]=new GLYPHENTRY();
                     tr.glyphEntries[f].glyphAdvance=adv;
                     adv+=300;
                     if(adv>maxadv){
                        maxadv=adv;
                     }
                     tr.glyphEntries[f].glyphIndex=f;
                  }
                  rec.add(tr);
                  System.out.println("countGlyphs="+countGlyphs);                  
                  sos2.writeTag(new DefineTextTag(999, new RECT(0,width,0,height), new MATRIX(), SWFOutputStream.getNeededBitsU(countGlyphs-1), SWFOutputStream.getNeededBitsU(maxadv), rec));
                  sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, true, false, 1, 999, mat, null, 0, null, 0, null));
                  sos2.writeTag(new ShowFrameTag());
               }else
               if ((tagObj instanceof DefineMorphShapeTag) || (tagObj instanceof DefineMorphShape2Tag)) {
                  sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null));
                  sos2.writeTag(new ShowFrameTag());
                  int numFrames = 100;
                  for (int ratio = 0; ratio < 65536; ratio += 65536 / numFrames) {
                     sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, false, true, 1, chtId, mat, null, ratio, null, 0, null));
                     sos2.writeTag(new ShowFrameTag());
                  }
               } else {
                  sos2.writeTag(new PlaceObjectTag(chtId, 1, mat, null));
                  sos2.writeTag(new ShowFrameTag());
               }
               
               sos2.writeTag(new EndTag());
               byte data[] = baos.toByteArray();

               sos.writeUI32(sos.getPos() + data.length + 4);
               sos.write(data);
               fos.close();
               fPanel.displaySWF(tempFile.getAbsolutePath());               
               (new Thread(){

                  @Override
                  public void run() {
                     while(!tagTree.requestFocusInWindow())
                        ;
                  }
               }).start();
               
            } catch (Exception ex) {
               ex.printStackTrace();
            }
         }
      }
   }
}
