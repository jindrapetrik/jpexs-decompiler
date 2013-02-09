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

import com.jpexs.asdec.Configuration;
import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWF;
import com.jpexs.asdec.abc.gui.ABCPanel;
import com.jpexs.asdec.abc.gui.DeobfuscationDialog;
import com.jpexs.asdec.abc.gui.TreeLeafScript;
import com.jpexs.asdec.action.gui.ActionPanel;
import com.jpexs.asdec.helpers.Helper;
import com.jpexs.asdec.tags.DefineBitsJPEG2Tag;
import com.jpexs.asdec.tags.DefineBitsJPEG3Tag;
import com.jpexs.asdec.tags.DefineBitsJPEG4Tag;
import com.jpexs.asdec.tags.DefineBitsLossless2Tag;
import com.jpexs.asdec.tags.DefineBitsLosslessTag;
import com.jpexs.asdec.tags.DefineBitsTag;
import com.jpexs.asdec.tags.DefineButton2Tag;
import com.jpexs.asdec.tags.DefineButtonTag;
import com.jpexs.asdec.tags.DefineEditTextTag;
import com.jpexs.asdec.tags.DefineFont2Tag;
import com.jpexs.asdec.tags.DefineFont3Tag;
import com.jpexs.asdec.tags.DefineFont4Tag;
import com.jpexs.asdec.tags.DefineFontTag;
import com.jpexs.asdec.tags.DefineMorphShape2Tag;
import com.jpexs.asdec.tags.DefineMorphShapeTag;
import com.jpexs.asdec.tags.DefineShape2Tag;
import com.jpexs.asdec.tags.DefineShape3Tag;
import com.jpexs.asdec.tags.DefineShape4Tag;
import com.jpexs.asdec.tags.DefineShapeTag;
import com.jpexs.asdec.tags.DefineSpriteTag;
import com.jpexs.asdec.tags.DefineText2Tag;
import com.jpexs.asdec.tags.DefineTextTag;
import com.jpexs.asdec.tags.DoABCTag;
import com.jpexs.asdec.tags.DoInitActionTag;
import com.jpexs.asdec.tags.ExportAssetsTag;
import com.jpexs.asdec.tags.JPEGTablesTag;
import com.jpexs.asdec.tags.ShowFrameTag;
import com.jpexs.asdec.tags.Tag;
import com.jpexs.asdec.tags.base.ASMSource;
import com.jpexs.asdec.tags.base.Container;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;

/**
 *
 * @author Jindra
 */
public class MainFrame extends JFrame implements ActionListener {

   private SWF swf;
   public ABCPanel abcPanel;
   public ActionPanel actionPanel;
   private JTabbedPane tabPane;
   public LoadingPanel loadingPanel = new LoadingPanel(20, 20);
   public JLabel statusLabel = new JLabel("");
   public JPanel statusPanel = new JPanel();
   public JProgressBar progressBar = new JProgressBar(0, 100);
   private DeobfuscationDialog deobfuscationDialog;

   public void setPercent(int percent) {
      progressBar.setValue(percent);
      progressBar.setVisible(true);
   }

   public void hidePercent() {
      if (progressBar.isVisible()) {
         progressBar.setVisible(false);
      }
   }

   private static void addTab(JTabbedPane tabbedPane, Component tab, String title, Icon icon) {
      tabbedPane.add(tab);

      JLabel lbl = new JLabel(title);
      lbl.setIcon(icon);
      lbl.setIconTextGap(5);
      lbl.setHorizontalTextPosition(SwingConstants.RIGHT);

      tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, lbl);
   }

   public void setStatus(String s) {
      statusLabel.setText(s);
   }
   public void setWorkStatus(String s) {
      if (s.equals("")) {
         loadingPanel.setVisible(false);
      } else {
         loadingPanel.setVisible(true);
      }
      statusLabel.setText(s);
   }
   private TagPanel imagesTagPanel;
   private TagPanel shapesTagPanel;
   private TagPanel morphshapesTagPanel;
   private TagPanel spritesTagPanel;
   private TagPanel textsTagPanel;
   private TagPanel buttonsTagPanel;
   private TagPanel fontsTagPanel;

   public MainFrame(SWF swf) {
      setSize(1000, 700);
      tabPane = new JTabbedPane();
      View.setWindowIcon(this);
      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            if (Main.proxyFrame != null) {
               if (Main.proxyFrame.isVisible()) {
                  return;
               }
            }
            Main.exit();
         }
      });
      setTitle(Main.applicationName + " - " + Main.getFileTitle());
      JMenuBar menuBar = new JMenuBar();

      JMenu menuFile = new JMenu("File");
      JMenuItem miOpen = new JMenuItem("Open...");
      miOpen.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/open16.png")));
      miOpen.setActionCommand("OPEN");
      miOpen.addActionListener(this);
      JMenuItem miSave = new JMenuItem("Save");
      miSave.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/save16.png")));
      miSave.setActionCommand("SAVE");
      miSave.addActionListener(this);
      JMenuItem miSaveAs = new JMenuItem("Save as...");
      miSaveAs.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/saveas16.png")));
      miSaveAs.setActionCommand("SAVEAS");
      miSaveAs.addActionListener(this);
      JMenu menuExportAll = new JMenu("Export all");
      JMenuItem miExportAllAS = new JMenuItem("ActionScript...");
      miExportAllAS.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/as16.png")));
      miExportAllAS.setActionCommand("EXPORT");
      miExportAllAS.addActionListener(this);

      JMenuItem miExportAllPCode = new JMenuItem("PCode...");
      miExportAllPCode.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/pcode16.png")));
      miExportAllPCode.setActionCommand("EXPORTPCODE");
      miExportAllPCode.addActionListener(this);

      JMenuItem miExportImages = new JMenuItem("Images...");
      miExportImages.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/image16.png")));
      miExportImages.setActionCommand("EXPORTIMAGES");
      miExportImages.addActionListener(this);

      menuExportAll.add(miExportAllAS);
      menuExportAll.add(miExportAllPCode);
      menuExportAll.add(miExportImages);


      JMenu menuExportSel = new JMenu("Export selection");
      JMenuItem miExportSelAS = new JMenuItem("ActionScript...");
      miExportSelAS.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/as16.png")));
      miExportSelAS.setActionCommand("EXPORTSEL");
      miExportSelAS.addActionListener(this);

      JMenuItem miExportSelPCode = new JMenuItem("PCode...");
      miExportSelPCode.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/pcode16.png")));
      miExportSelPCode.setActionCommand("EXPORTPCODESEL");
      miExportSelPCode.addActionListener(this);

      JMenuItem miExportSelImages = new JMenuItem("Images...");
      miExportSelImages.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/image16.png")));
      miExportSelImages.setActionCommand("EXPORTIMAGESSEL");
      miExportSelImages.addActionListener(this);

      menuExportSel.add(miExportSelAS);
      menuExportSel.add(miExportSelPCode);
      menuExportSel.add(miExportSelImages);


      menuFile.add(miOpen);
      menuFile.add(miSave);
      menuFile.add(miSaveAs);
      menuFile.add(menuExportAll);
      menuFile.add(menuExportSel);
      menuFile.addSeparator();
      JMenuItem miClose = new JMenuItem("Exit");
      miClose.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/exit16.png")));
      miClose.setActionCommand("EXIT");
      miClose.addActionListener(this);
      menuFile.add(miClose);
      menuBar.add(menuFile);
      JMenu menuDeobfuscation = new JMenu("Deobfuscation");




      JMenuItem miDeobfuscation = new JMenuItem("PCode deobfuscation...");
      miDeobfuscation.setActionCommand("DEOBFUSCATE");
      miDeobfuscation.addActionListener(this);

      JCheckBoxMenuItem miSubLimiter = new JCheckBoxMenuItem("Enable sub limiter");
      miSubLimiter.setActionCommand("SUBLIMITER");
      miSubLimiter.addActionListener(this);

      JMenuItem miRenameIdentifiers = new JMenuItem("Rename identifiers");
      miRenameIdentifiers.setActionCommand("RENAMEIDENTIFIERS");
      miRenameIdentifiers.addActionListener(this);

      JMenuItem miRemoveDeadCode = new JMenuItem("Remove dead code");
      miRemoveDeadCode.setActionCommand("REMOVEDEADCODE");
      miRemoveDeadCode.addActionListener(this);

      JMenuItem miRemoveDeadCodeAll = new JMenuItem("Remove all dead code");
      miRemoveDeadCodeAll.setActionCommand("REMOVEDEADCODEALL");
      miRemoveDeadCodeAll.addActionListener(this);

      JMenuItem miTraps = new JMenuItem("Remove traps");
      miTraps.setActionCommand("REMOVETRAPS");
      miTraps.addActionListener(this);

      JMenuItem miTrapsAll = new JMenuItem("Remove all traps");
      miTrapsAll.setActionCommand("REMOVETRAPSALL");
      miTrapsAll.addActionListener(this);

      JMenuItem miControlFlow = new JMenuItem("Restore control flow");
      miControlFlow.setActionCommand("RESTORECONTROLFLOW");
      miControlFlow.addActionListener(this);

      JMenuItem miControlFlowAll = new JMenuItem("Restore all control flow");
      miControlFlowAll.setActionCommand("RESTORECONTROLFLOWALL");
      miControlFlowAll.addActionListener(this);

      menuDeobfuscation.add(miRenameIdentifiers);
      //menuDeobfuscation.add(miSubLimiter);
      menuDeobfuscation.add(miDeobfuscation);
      /*menuDeobfuscation.add(miDeobfuscate);
      menuDeobfuscation.addSeparator();*/     
      /*menuDeobfuscation.add(miRemoveDeadCode);
      menuDeobfuscation.add(miRemoveDeadCodeAll);
      menuDeobfuscation.add(miTraps);
      menuDeobfuscation.add(miTrapsAll);
      menuDeobfuscation.add(miControlFlow);
      menuDeobfuscation.add(miControlFlowAll);
*/
      JMenu menuTools = new JMenu("Tools");
      JMenuItem miProxy = new JMenuItem("Proxy");
      miProxy.setActionCommand("SHOWPROXY");
      miProxy.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/proxy16.png")));
      miProxy.addActionListener(this);
      menuTools.add(miProxy);

      //menuTools.add(menuDeobfuscation);
      menuTools.add(menuDeobfuscation);
      menuBar.add(menuTools);

      JMenu menuHelp = new JMenu("Help");
      JMenuItem miAbout = new JMenuItem("About...");
      miAbout.setActionCommand("ABOUT");
      miAbout.addActionListener(this);

      JMenuItem miCheckUpdates = new JMenuItem("Check for updates...");
      miCheckUpdates.setActionCommand("CHECKUPDATES");
      miCheckUpdates.addActionListener(this);
      menuHelp.add(miAbout);
      menuHelp.add(miCheckUpdates);
      menuBar.add(menuHelp);

      setJMenuBar(menuBar);
      List<Object> objs = new ArrayList<Object>();
      objs.addAll(swf.tags);
      this.swf = swf;
      getContentPane().setLayout(new BorderLayout());
      List<Tag> shapes = new ArrayList<Tag>();
      List<Tag> images = new ArrayList<Tag>();
      List<Tag> morphShapes = new ArrayList<Tag>();
      List<Tag> sprites = new ArrayList<Tag>();
      List<Tag> fonts = new ArrayList<Tag>();
      List<Tag> texts = new ArrayList<Tag>();
      List<Tag> buttons = new ArrayList<Tag>();
      List<DoABCTag> abcList = new ArrayList<DoABCTag>();
      getShapes(objs, shapes);
      getImages(objs, images);
      getMorphShapes(objs, morphShapes);
      getSprites(objs, sprites);
      getFonts(objs, fonts);
      getTexts(objs, texts);
      getButtons(objs, buttons);
      getActionScript3(objs, abcList);

      getContentPane().add(tabPane, BorderLayout.CENTER);

      if (!abcList.isEmpty()) {
         addTab(tabPane, abcPanel = new ABCPanel(abcList), "ActionScript3", new ImageIcon(this.getClass().getClassLoader().getResource("com/jpexs/asdec/gui/graphics/as16.png")));
      } else {
         actionPanel = new ActionPanel(swf.tags);
         if (actionPanel.tagTree.getRowCount() > 1) {
            addTab(tabPane, actionPanel, "ActionScript", new ImageIcon(this.getClass().getClassLoader().getResource("com/jpexs/asdec/gui/graphics/as16.png")));
         }
         miDeobfuscation.setEnabled(false);
      }

      if (!shapes.isEmpty()) {
         addTab(tabPane, shapesTagPanel = new TagPanel(shapes, swf), "Shapes", new ImageIcon(this.getClass().getClassLoader().getResource("com/jpexs/asdec/gui/graphics/shape16.png")));
      }
      if (!morphShapes.isEmpty()) {
         addTab(tabPane, morphshapesTagPanel = new TagPanel(morphShapes, swf), "MorphShapes", new ImageIcon(this.getClass().getClassLoader().getResource("com/jpexs/asdec/gui/graphics/morphshape16.png")));
      }
      if (!images.isEmpty()) {
         addTab(tabPane, imagesTagPanel = new TagPanel(images, swf), "Images", new ImageIcon(this.getClass().getClassLoader().getResource("com/jpexs/asdec/gui/graphics/image16.png")));
      }
      if (!sprites.isEmpty()) {
         addTab(tabPane, spritesTagPanel = new TagPanel(sprites, swf), "Sprites", new ImageIcon(this.getClass().getClassLoader().getResource("com/jpexs/asdec/gui/graphics/sprite16.png")));
      }
      if (!fonts.isEmpty()) {
         addTab(tabPane, fontsTagPanel = new TagPanel(fonts, swf), "Fonts", new ImageIcon(this.getClass().getClassLoader().getResource("com/jpexs/asdec/gui/graphics/font16.png")));
      }
      if (!texts.isEmpty()) {
         addTab(tabPane, textsTagPanel = new TagPanel(texts, swf), "Texts", new ImageIcon(this.getClass().getClassLoader().getResource("com/jpexs/asdec/gui/graphics/text16.png")));
      }
      if (!buttons.isEmpty()) {
         addTab(tabPane, buttonsTagPanel = new TagPanel(buttons, swf), "Buttons", new ImageIcon(this.getClass().getClassLoader().getResource("com/jpexs/asdec/gui/graphics/button16.png")));
      }


      loadingPanel.setPreferredSize(new Dimension(30, 30));
      statusPanel = new JPanel();
      statusPanel.setPreferredSize(new Dimension(1, 30));
      statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
      statusPanel.setLayout(new BorderLayout());
      statusPanel.add(loadingPanel, BorderLayout.WEST);
      statusPanel.add(statusLabel, BorderLayout.CENTER);
      loadingPanel.setVisible(false);
      add(statusPanel, BorderLayout.SOUTH);
      View.centerScreen(this);
      Main.stopWork();

   }

   @Override
   public void setVisible(boolean b) {
      super.setVisible(b);
      if (b) {
         if (abcPanel != null) {
            abcPanel.initSplits();
         }
         if (actionPanel != null) {
            actionPanel.initSplits();
         }
      }
   }

   public static void getShapes(List<Object> list, List<Tag> shapes) {
      for (Object t : list) {
         if (t instanceof Container) {
            getShapes(((Container) t).getSubItems(), shapes);
         }
         if ((t instanceof DefineShapeTag)
                 || (t instanceof DefineShape2Tag)
                 || (t instanceof DefineShape3Tag)
                 || (t instanceof DefineShape4Tag)) {
            shapes.add((Tag) t);
         }
      }
   }

   public static void getFonts(List<Object> list, List<Tag> fonts) {
      for (Object t : list) {
         if (t instanceof Container) {
            getFonts(((Container) t).getSubItems(), fonts);
         }
         if ((t instanceof DefineFontTag)
                 || (t instanceof DefineFont2Tag)
                 || (t instanceof DefineFont3Tag)
                 || (t instanceof DefineFont4Tag)) {
            fonts.add((Tag) t);
         }
      }
   }

   public static void getActionScript3(List<Object> list, List<DoABCTag> actionScripts) {
      for (Object t : list) {
         if (t instanceof Container) {
            getActionScript3(((Container) t).getSubItems(), actionScripts);
         }
         if (t instanceof DoABCTag) {
            actionScripts.add((DoABCTag) t);
         }
      }
   }

   public static void getMorphShapes(List<Object> list, List<Tag> morphShapes) {
      for (Object t : list) {
         if (t instanceof Container) {
            getMorphShapes(((Container) t).getSubItems(), morphShapes);
         }
         if ((t instanceof DefineMorphShapeTag) || (t instanceof DefineMorphShape2Tag)) {
            morphShapes.add((Tag) t);
         }
      }
   }

   public static void getImages(List<Object> list, List<Tag> images) {
      for (Object t : list) {
         if (t instanceof Container) {
            getImages(((Container) t).getSubItems(), images);
         }
         if ((t instanceof DefineBitsTag)
                 || (t instanceof DefineBitsJPEG2Tag)
                 || (t instanceof DefineBitsJPEG3Tag)
                 || (t instanceof DefineBitsJPEG4Tag)
                 || (t instanceof DefineBitsLosslessTag)
                 || (t instanceof DefineBitsLossless2Tag)) {
            images.add((Tag) t);
         }
      }
   }

   public static void getTexts(List<Object> list, List<Tag> texts) {
      for (Object t : list) {
         if (t instanceof Container) {
            getTexts(((Container) t).getSubItems(), texts);
         }
         if ((t instanceof DefineTextTag)
                 || (t instanceof DefineText2Tag)
                 || (t instanceof DefineEditTextTag)) {
            texts.add((Tag) t);
         }
      }
   }

   public static void getSprites(List<Object> list, List<Tag> sprites) {
      for (Object t : list) {
         if (t instanceof Container) {
            getSprites(((Container) t).getSubItems(), sprites);
         }
         if (t instanceof DefineSpriteTag) {
            sprites.add((Tag) t);
         }
      }
   }

   public static void getButtons(List<Object> list, List<Tag> buttons) {
      for (Object t : list) {
         if (t instanceof Container) {
            getButtons(((Container) t).getSubItems(), buttons);
         }
         if ((t instanceof DefineButtonTag) || (t instanceof DefineButton2Tag)) {
            buttons.add((Tag) t);
         }
      }
   }

   public static List<TagNode> createTagList(List<Object> list) {
      List<TagNode> ret = new ArrayList<TagNode>();
      int frame = 1;
      List<TagNode> frames = new ArrayList<TagNode>();
      List<TagNode> shapes = new ArrayList<TagNode>();
      List<TagNode> morphShapes = new ArrayList<TagNode>();
      List<TagNode> sprites = new ArrayList<TagNode>();
      List<TagNode> buttons = new ArrayList<TagNode>();
      List<TagNode> images = new ArrayList<TagNode>();
      List<TagNode> fonts = new ArrayList<TagNode>();
      List<TagNode> texts = new ArrayList<TagNode>();


      List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();
      for (Object t : list) {
         if (t instanceof ExportAssetsTag) {
            exportAssetsTags.add((ExportAssetsTag) t);
         }
         if ((t instanceof DefineFontTag)
                 || (t instanceof DefineFont2Tag)
                 || (t instanceof DefineFont3Tag)
                 || (t instanceof DefineFont4Tag)) {
            fonts.add(new TagNode(t));
         }
         if ((t instanceof DefineTextTag)
                 || (t instanceof DefineText2Tag)
                 || (t instanceof DefineEditTextTag)) {
            texts.add(new TagNode(t));
         }

         if ((t instanceof DefineBitsTag)
                 || (t instanceof DefineBitsJPEG2Tag)
                 || (t instanceof DefineBitsJPEG3Tag)
                 || (t instanceof DefineBitsJPEG4Tag)
                 || (t instanceof DefineBitsLosslessTag)
                 || (t instanceof DefineBitsLossless2Tag)) {
            images.add(new TagNode(t));
         }
         if ((t instanceof DefineShapeTag)
                 || (t instanceof DefineShape2Tag)
                 || (t instanceof DefineShape3Tag)
                 || (t instanceof DefineShape4Tag)) {
            shapes.add(new TagNode(t));
         }

         if ((t instanceof DefineMorphShapeTag) || (t instanceof DefineMorphShape2Tag)) {
            morphShapes.add(new TagNode(t));
         }

         if (t instanceof DefineSpriteTag) {
            sprites.add(new TagNode(t));
         }
         if ((t instanceof DefineButtonTag) || (t instanceof DefineButton2Tag)) {
            buttons.add(new TagNode(t));
         }
         if (t instanceof ShowFrameTag) {
            TagNode tti = new TagNode("frame" + frame);

            /*           for (int r = ret.size() - 1; r >= 0; r--) {
             if (!(ret.get(r).tag instanceof DefineSpriteTag)) {
             if (!(ret.get(r).tag instanceof DefineButtonTag)) {
             if (!(ret.get(r).tag instanceof DefineButton2Tag)) {
             if (!(ret.get(r).tag instanceof DoInitActionTag)) {
             tti.subItems.add(ret.get(r));
             ret.remove(r);
             }
             }
             }
             }
             }*/
            frame++;
            frames.add(tti);
         } /*if (t instanceof ASMSource) {
          TagNode tti = new TagNode(t);
          ret.add(tti);
          } else */
         if (t instanceof Container) {
            TagNode tti = new TagNode(t);
            if (((Container) t).getItemCount() > 0) {
               List<Object> subItems = ((Container) t).getSubItems();
               tti.subItems = createTagList(subItems);
            }
            //ret.add(tti);
         }
      }

      TagNode textsNode = new TagNode("texts");
      textsNode.subItems.addAll(texts);

      TagNode imagesNode = new TagNode("images");
      imagesNode.subItems.addAll(images);

      TagNode fontsNode = new TagNode("fonts");
      fontsNode.subItems.addAll(fonts);


      TagNode spritesNode = new TagNode("sprites");
      spritesNode.subItems.addAll(sprites);

      TagNode shapesNode = new TagNode("shapes");
      shapesNode.subItems.addAll(shapes);

      TagNode morphShapesNode = new TagNode("morphshapes");
      morphShapesNode.subItems.addAll(morphShapes);

      TagNode buttonsNode = new TagNode("buttons");
      buttonsNode.subItems.addAll(buttons);

      TagNode framesNode = new TagNode("frames");
      framesNode.subItems.addAll(frames);
      ret.add(shapesNode);
      ret.add(morphShapesNode);;
      ret.add(spritesNode);
      ret.add(textsNode);
      ret.add(imagesNode);
      ret.add(buttonsNode);
      ret.add(fontsNode);
      ret.add(framesNode);
      for (int i = ret.size() - 1; i >= 0; i--) {
         if (ret.get(i).tag instanceof DefineSpriteTag) {
            ((DefineSpriteTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof DefineButtonTag) {
            ((DefineButtonTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof DefineButton2Tag) {
            ((DefineButton2Tag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof DoInitActionTag) {
            ((DoInitActionTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof ASMSource) {
            ASMSource ass = (ASMSource) ret.get(i).tag;
            if (ass.containsSource()) {
               continue;
            }
         }
         if (ret.get(i).subItems.isEmpty()) {
            //ret.remove(i);
         }
      }
      return ret;
   }

   public boolean confirmExperimental() {
      return JOptionPane.showConfirmDialog(null, "Following procedure can damage SWF file which can be then unplayable.\r\nUSE IT ON YOUR OWN RISK. Do you want to continue?", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
   }

   public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals("EXIT")) {
         setVisible(false);
         if (Main.proxyFrame != null) {
            if (Main.proxyFrame.isVisible()) {
               return;
            }
         }
         Main.exit();
      }
      if (Main.isWorking()) {
         return;
      }

      if (e.getActionCommand().equals("ABOUT")) {
         Main.about();
      }


      if (e.getActionCommand().equals("SHOWPROXY")) {
         Main.showProxy();
      }

      if (e.getActionCommand().equals("SUBLIMITER")) {
         if (e.getSource() instanceof JCheckBoxMenuItem) {
            Main.setSubLimiter(((JCheckBoxMenuItem) e.getSource()).getState());
         }

      }

      if (e.getActionCommand().equals("SAVE")) {
         try {
            Main.saveFile(Main.file);
         } catch (IOException ex) {
            Logger.getLogger(com.jpexs.asdec.abc.gui.ABCPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Cannot save file", "Error", JOptionPane.ERROR_MESSAGE);
         }
      }
      if (e.getActionCommand().equals("SAVEAS")) {
         if (Main.saveFileDialog()) {
            setTitle(Main.applicationName + " - " + Main.getFileTitle());
         }
      }
      if (e.getActionCommand().equals("OPEN")) {
         Main.openFileDialog();

      }

      if (e.getActionCommand().startsWith("EXPORT")) {
         JFileChooser chooser = new JFileChooser();
         chooser.setCurrentDirectory(new java.io.File((String) Configuration.getConfig("lastExportDir", ".")));
         chooser.setDialogTitle("Select directory to export");
         chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         chooser.setAcceptAllFileFilterUsed(false);
         if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final long timeBefore=System.currentTimeMillis();
            Main.startWork("Exporting...");
            final String selFile = chooser.getSelectedFile().getAbsolutePath();
            Configuration.setConfig("lastExportDir", chooser.getSelectedFile().getParentFile().getAbsolutePath());
            final boolean isPcode = e.getActionCommand().startsWith("EXPORTPCODE");
            final boolean onlySel = e.getActionCommand().endsWith("SEL");
            final boolean images = e.getActionCommand().startsWith("EXPORTIMAGES");
            (new Thread() {
               @Override
               public void run() {
                  try {
                     if (onlySel) {
                        if (images) {
                           if (imagesTagPanel != null) {
                              List<Tag> list = new ArrayList<Tag>();

                              Object lob[] = imagesTagPanel.tagList.getSelectedValues();
                              for (Object o : lob) {
                                 if (o instanceof Tag) {
                                    list.add((Tag) o);
                                 }
                              }
                              JPEGTablesTag jtt = null;
                              for (Tag t : swf.tags) {
                                 if (t instanceof JPEGTablesTag) {
                                    jtt = (JPEGTablesTag) t;
                                    break;
                                 }
                              }
                              SWF.exportImages(selFile, list, jtt);
                           }
                        } else if (abcPanel != null) {
                           List<TreeLeafScript> tlsList = abcPanel.classTree.getSelectedScripts();
                           if (tlsList.isEmpty()) {
                              JOptionPane.showMessageDialog(null, "No script selected!");
                           }
                           for (int i = 0; i < tlsList.size(); i++) {
                              TreeLeafScript tls = tlsList.get(i);
                              Main.startWork("Exporting " + (i + 1) + "/" + tlsList.size() + " " + tls.abc.script_info[tls.scriptIndex].getPath(tls.abc) + " ...");
                              tls.abc.script_info[tls.scriptIndex].export(tls.abc, abcPanel.list, selFile, isPcode);
                           }
                        } else if (actionPanel != null) {
                           List<com.jpexs.asdec.action.TagNode> nodes = actionPanel.getSelectedNodes();
                           if (nodes.isEmpty()) {
                              JOptionPane.showMessageDialog(null, "No nodes selected!");
                           }
                           com.jpexs.asdec.action.gui.TagTreeModel ttm = (com.jpexs.asdec.action.gui.TagTreeModel) actionPanel.tagTree.getModel();
                           List<com.jpexs.asdec.action.TagNode> allnodes = ttm.getNodeList();
                           com.jpexs.asdec.action.TagNode.setExport(allnodes, false);
                           com.jpexs.asdec.action.TagNode.setExport(nodes, true);
                           com.jpexs.asdec.action.TagNode.exportNode(allnodes, selFile, isPcode);
                        }
                     } else {
                        if (images) {
                           Main.swf.exportImages(selFile);
                        } else {
                           Main.swf.exportActionScript(selFile, isPcode);
                        }
                     }
                  } catch (Exception ignored) {
                     JOptionPane.showMessageDialog(null, "Cannot write to the file");
                  }
                  Main.stopWork();
                  long timeAfter=System.currentTimeMillis();
                  long timeMs=timeAfter-timeBefore;
                  long timeS=timeMs/1000;
                  timeMs=timeMs%1000;
                  long timeM=timeS/60;
                  timeS=timeS%60;
                  long timeH=timeM/60;
                  timeM=timeM%60;
                  String timeStr="";
                  if(timeH>0){
                     timeStr+=Helper.padZeros(timeH, 2)+":";
                  }
                  timeStr+=Helper.padZeros(timeM, 2)+":";                  
                  timeStr+=Helper.padZeros(timeS, 2)+"."+Helper.padZeros(timeMs,3);
                  setStatus("Exported in "+timeStr);
               }
            }).start();

         }

      }

      if (e.getActionCommand().equals("CHECKUPDATES")) {
         if (!Main.checkForUpdates()) {
            JOptionPane.showMessageDialog(null, "No new version available.");
         }
      }

      if (e.getActionCommand().startsWith("RESTORECONTROLFLOW")) {
         Main.startWork("Restoring control flow...");
         final boolean all = e.getActionCommand().endsWith("ALL");
         if ((!all) || confirmExperimental()) {
            new SwingWorker() {
               @Override
               protected Object doInBackground() throws Exception {
                  int cnt = 0;
                  if (all) {
                     for (DoABCTag tag : abcPanel.list) {
                        tag.abc.restoreControlFlow();
                     }
                  } else {
                     int bi = abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex();
                     if (bi != -1) {
                        abcPanel.abc.bodies[bi].restoreControlFlow(abcPanel.abc.constants);
                     }
                     abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc);
                  }
                  Main.stopWork();
                  JOptionPane.showMessageDialog(null, "Control flow restored");
                  abcPanel.reload();
                  return true;
               }
            }.execute();
         }
      }

      if (e.getActionCommand().startsWith("REMOVETRAPS")) {
         Main.startWork("Removing traps...");
         final boolean all = e.getActionCommand().endsWith("ALL");
         if ((!all) || confirmExperimental()) {
            new SwingWorker() {
               @Override
               protected Object doInBackground() throws Exception {
                  int cnt = 0;
                  if (all) {
                     for (DoABCTag tag : abcPanel.list) {
                        cnt += tag.abc.removeTraps();
                     }
                  } else {
                     int bi = abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex();
                     if (bi != -1) {
                        cnt += abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants);
                     }
                     abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc);
                  }
                  Main.stopWork();
                  JOptionPane.showMessageDialog(null, "Traps removed: " + cnt);
                  abcPanel.reload();
                  return true;
               }
            }.execute();
         }
      }

      if (e.getActionCommand().startsWith("REMOVEDEADCODE")) {
         Main.startWork("Removing dead code...");
         final boolean all = e.getActionCommand().endsWith("ALL");
         if ((!all) || confirmExperimental()) {
            new SwingWorker() {
               @Override
               protected Object doInBackground() throws Exception {
                  int cnt = 0;
                  if (all) {
                     for (DoABCTag tag : abcPanel.list) {
                        cnt += tag.abc.removeDeadCode();
                     }
                  } else {
                     int bi = abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex();
                     if (bi != -1) {
                        cnt += abcPanel.abc.bodies[bi].removeDeadCode(abcPanel.abc.constants);
                     }
                     abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc);
                  }
                  Main.stopWork();
                  JOptionPane.showMessageDialog(null, "Instructions removed: " + cnt);
                  abcPanel.reload();
                  return true;
               }
            }.execute();
         }
      }

      if (e.getActionCommand().equals("RENAMEIDENTIFIERS")) {
         if (confirmExperimental()) {

            Main.startWork("Renaming identifiers...");
            new SwingWorker() {
               @Override
               protected Object doInBackground() throws Exception {
                  int cnt = 0;
                  HashMap<String,String> namesMap=new HashMap<String,String>();
                  for (DoABCTag tag : abcPanel.list) {
                     cnt += tag.abc.deobfuscateIdentifiers(namesMap);
                  }
                  Main.stopWork();
                  JOptionPane.showMessageDialog(null, "Identifiers renamed: " + cnt);
                  abcPanel.reload();
                  return true;
               }
            }.execute();


         }
      }

      if (e.getActionCommand().startsWith("DEOBFUSCATE")) {         
         if(deobfuscationDialog==null){
            deobfuscationDialog = new DeobfuscationDialog();
         }
         deobfuscationDialog.setVisible(true);
         if (deobfuscationDialog.ok) {
            Main.startWork("Deobfuscating...");
            new SwingWorker() {
               @Override
               protected Object doInBackground() throws Exception {
                  if (deobfuscationDialog.processAllCheckbox.isSelected()) {
                     for (DoABCTag tag : abcPanel.list) {
                        if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_REMOVE_DEAD_CODE) {
                           tag.abc.removeDeadCode();
                        } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_REMOVE_TRAPS) {
                           tag.abc.removeTraps();
                        } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_RESTORE_CONTROL_FLOW) {
                           tag.abc.removeTraps();
                           tag.abc.restoreControlFlow();
                        }
                     }
                  } else {
                     int bi = abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex();
                     if (bi != -1) {
                        if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_REMOVE_DEAD_CODE) {
                           abcPanel.abc.bodies[bi].removeDeadCode(abcPanel.abc.constants);
                        } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_REMOVE_TRAPS) {
                           abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants);
                        } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_RESTORE_CONTROL_FLOW) {
                           abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants);
                           abcPanel.abc.bodies[bi].restoreControlFlow(abcPanel.abc.constants);
                        }
                     }
                     abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc);
                  }
                  Main.stopWork();
                  JOptionPane.showMessageDialog(null, "Deobfuscation complete");
                  abcPanel.reload();
                  return true;
               }
            }.execute();
         }
      }
   }
}
