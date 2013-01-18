package com.jpexs.asdec.gui;

import com.jpexs.asdec.Configuration;
import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWF;
import com.jpexs.asdec.abc.gui.ABCPanel;
import com.jpexs.asdec.abc.gui.TreeLeafScript;
import com.jpexs.asdec.action.gui.ActionPanel;
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

   public void setPercent(int percent){
      progressBar.setValue(percent);
      progressBar.setVisible(true);
   }
   
   public void hidePercent(){
      if(progressBar.isVisible()){
         progressBar.setVisible(false);
      }
   }
   
   public void setStatus(String s) {
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
      miExportAllAS.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/exportas16.png")));
      miExportAllAS.setActionCommand("EXPORT");
      miExportAllAS.addActionListener(this);

      JMenuItem miExportAllPCode = new JMenuItem("PCode...");
      miExportAllPCode.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/exportpc16.png")));
      miExportAllPCode.setActionCommand("EXPORTPCODE");
      miExportAllPCode.addActionListener(this);

      JMenuItem miExportImages = new JMenuItem("Images...");
      miExportImages.setActionCommand("EXPORTIMAGES");
      miExportImages.addActionListener(this);

      menuExportAll.add(miExportAllAS);
      menuExportAll.add(miExportAllPCode);
      menuExportAll.add(miExportImages);


      JMenu menuExportSel = new JMenu("Export selection");
      JMenuItem miExportSelAS = new JMenuItem("ActionScript...");
      miExportSelAS.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/exportas16.png")));
      miExportSelAS.setActionCommand("EXPORTSEL");
      miExportSelAS.addActionListener(this);

      JMenuItem miExportSelPCode = new JMenuItem("PCode...");
      miExportSelPCode.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/exportpc16.png")));
      miExportSelPCode.setActionCommand("EXPORTPCODESEL");
      miExportSelPCode.addActionListener(this);

      JMenuItem miExportSelImages = new JMenuItem("Images...");
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
      JCheckBoxMenuItem miSubLimiter = new JCheckBoxMenuItem("Enable sub limiter");
      miSubLimiter.setActionCommand("SUBLIMITER");
      miSubLimiter.addActionListener(this);

      JMenuItem miRenameIdentifiers = new JMenuItem("Rename identifiers");
      miRenameIdentifiers.setActionCommand("RENAMEIDENTIFIERS");
      miRenameIdentifiers.addActionListener(this);


      menuDeobfuscation.add(miSubLimiter);
      menuDeobfuscation.add(miRenameIdentifiers);



      JMenu menuTools = new JMenu("Tools");
      JMenuItem miProxy = new JMenuItem("Proxy");
      miProxy.setActionCommand("SHOWPROXY");
      miProxy.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/proxy16.png")));
      miProxy.addActionListener(this);
      menuTools.add(miProxy);

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
         tabPane.addTab("ActionScript3", abcPanel = new ABCPanel(abcList));
      } else {
         actionPanel = new ActionPanel(swf.tags);
         if (actionPanel.tagTree.getRowCount() > 1) {
            tabPane.addTab("ActionScript", actionPanel);
         }
         menuDeobfuscation.setEnabled(false);
      }

      if (!shapes.isEmpty()) {
         tabPane.addTab("Shapes", shapesTagPanel = new TagPanel(shapes, swf));
      }
      if (!morphShapes.isEmpty()) {
         tabPane.addTab("MorphShapes", morphshapesTagPanel = new TagPanel(morphShapes, swf));
      }
      if (!images.isEmpty()) {
         tabPane.addTab("Images", imagesTagPanel = new TagPanel(images, swf));
      }
      if (!sprites.isEmpty()) {
         tabPane.addTab("Sprites", spritesTagPanel = new TagPanel(sprites, swf));
      }
      if (!fonts.isEmpty()) {
         tabPane.addTab("Fonts", fontsTagPanel = new TagPanel(fonts, swf));
      }
      if (!texts.isEmpty()) {
         tabPane.addTab("Texts", textsTagPanel = new TagPanel(texts, swf));
      }
      if (!buttons.isEmpty()) {
         tabPane.addTab("Buttons", buttonsTagPanel = new TagPanel(buttons, swf));
      }
      /*tabPane.addTab("Tags", new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tagTree), new JScrollPane(fPanel)));*/

      //tabPane.setTabPlacement(JTabbedPane.TOP);



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
               }
            }).start();

         }

      }

      if (e.getActionCommand().equals("CHECKUPDATES")) {
         if (!Main.checkForUpdates()) {
            JOptionPane.showMessageDialog(null, "No new version available.");
         }
      }
      if (e.getActionCommand().equals("RENAMEIDENTIFIERS")) {
         if (JOptionPane.showConfirmDialog(null, "Following procedure can damage SWF file which can be then unplayable.\r\nUSE IT ON YOUR OWN RISK. Do you want to continue?", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            int pocet = 0;
            Main.startWork("Renaming identifiers...");
            for (DoABCTag tag : abcPanel.list) {
               pocet += tag.abc.deobfuscateIdentifiers();
            }
            JOptionPane.showMessageDialog(null, "Identifiers renamed: " + pocet);
            abcPanel.reload();
            Main.stopWork();
         }
      }

   }
}
