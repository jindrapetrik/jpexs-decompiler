/*
 *  Copyright (C) 2010-2018 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFHeader;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.PreviewExporter;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.gui.debugger.DebuggerTools;
import com.jpexs.decompiler.flash.gui.editor.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.gui.player.FlashPlayerPanel;
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.gui.player.PlayerControls;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.helpers.SerializableImage;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author JPEXS
 */
public class PreviewPanel extends JPersistentSplitPane implements TagEditorPanel {

    private static final String FLASH_VIEWER_CARD = "FLASHVIEWER";

    private static final String DRAW_PREVIEW_CARD = "DRAWPREVIEW";

    private static final String GENERIC_TAG_CARD = "GENERICTAG";

    private static final String BINARY_TAG_CARD = "BINARYTAG";

    private static final String METADATA_TAG_CARD = "METADATATAG";

    private static final String EMPTY_CARD = "EMPTY";

    private static final String CARDTEXTPANEL = "Text card";

    private static final String CARDFONTPANEL = "Font card";

    private final MainPanel mainPanel;

    private final JPanel viewerCards;

    private final FlashPlayerPanel flashPanel;

    private File tempFile;

    private ImagePanel imagePanel;

    private PlayerControls imagePlayControls;

    private MediaDisplay media;

    private BinaryPanel binaryPanel;

    private LineMarkedEditorPane metadataEditor;

    private GenericTagPanel genericTagPanel;

    private JPanel displayWithPreview;

    // Image tag buttons
    private JButton replaceImageButton;

    private JButton replaceImageAlphaButton;

    private JButton prevFontsButton;

    private JButton nextFontsButton;

    // Binary tag buttons
    private JButton replaceBinaryButton;

    // Metadata editor buttons
    private JButton metadataEditButton;

    private JButton metadataSaveButton;

    private JButton metadataCancelButton;

    // Generic tag buttons
    private JButton genericEditButton;

    private JButton genericSaveButton;

    private JButton genericCancelButton;

    private JPanel parametersPanel;

    private FontPanel fontPanel;

    private int fontPageNum;

    private TextPanel textPanel;

    private MetadataTag metadataTag;

    private boolean readOnly = false;

    private final int dividerSize;

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        setDividerSize(this.readOnly ? 0 : dividerSize);
        if (readOnly) {
            parametersPanel.setVisible(false);
        }
    }

    public PreviewPanel(MainPanel mainPanel, FlashPlayerPanel flashPanel) {
        super(JSplitPane.HORIZONTAL_SPLIT, Configuration.guiPreviewSplitPaneDividerLocationPercent);
        this.mainPanel = mainPanel;
        this.flashPanel = flashPanel;

        viewerCards = new JPanel();
        viewerCards.setLayout(new CardLayout());

        viewerCards.add(createFlashPlayerPanel(flashPanel), FLASH_VIEWER_CARD);
        viewerCards.add(createImagesCard(), DRAW_PREVIEW_CARD);
        viewerCards.add(createBinaryCard(), BINARY_TAG_CARD);
        viewerCards.add(createMetadataCard(), METADATA_TAG_CARD);
        viewerCards.add(createGenericTagCard(), GENERIC_TAG_CARD);
        viewerCards.add(createEmptyCard(), EMPTY_CARD);
        setLeftComponent(viewerCards);

        createParametersPanel();

        showCardLeft(FLASH_VIEWER_CARD);

        dividerSize = getDividerSize();
    }

    private JPanel createEmptyCard() {
        JPanel ret = new JPanel();
        ret.add(new JLabel("-"));
        return ret;
    }

    private void createParametersPanel() {
        displayWithPreview = new JPanel(new CardLayout());

        textPanel = new TextPanel(mainPanel);
        displayWithPreview.add(textPanel, CARDTEXTPANEL);

        fontPanel = new FontPanel(mainPanel);
        displayWithPreview.add(fontPanel, CARDFONTPANEL);

        JLabel paramsLabel = new HeaderLabel(mainPanel.translate("parameters"));
        paramsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //paramsLabel.setBorder(new BevelBorder(BevelBorder.RAISED));

        parametersPanel = new JPanel(new BorderLayout());
        parametersPanel.add(paramsLabel, BorderLayout.NORTH);
        parametersPanel.add(displayWithPreview, BorderLayout.CENTER);
        setRightComponent(parametersPanel);
    }

    private JPanel createImageButtonsPanel() {
        replaceImageButton = new JButton(mainPanel.translate("button.replace"), View.getIcon("replaceimage16"));
        replaceImageButton.setMargin(new Insets(3, 3, 3, 10));
        replaceImageButton.addActionListener(mainPanel::replaceButtonActionPerformed);
        replaceImageButton.setVisible(false);

        replaceImageAlphaButton = new JButton(mainPanel.translate("button.replaceAlphaChannel"), View.getIcon("replacealpha16"));
        replaceImageAlphaButton.setMargin(new Insets(3, 3, 3, 10));
        replaceImageAlphaButton.addActionListener(mainPanel::replaceAlphaButtonActionPerformed);
        replaceImageAlphaButton.setVisible(false);

        prevFontsButton = new JButton(mainPanel.translate("button.prev"), View.getIcon("prev16"));
        prevFontsButton.setMargin(new Insets(3, 3, 3, 10));
        prevFontsButton.addActionListener(this::prevFontsButtonActionPerformed);
        prevFontsButton.setVisible(false);

        nextFontsButton = new JButton(mainPanel.translate("button.next"), View.getIcon("next16"));
        nextFontsButton.setMargin(new Insets(3, 3, 3, 10));
        nextFontsButton.addActionListener(this::nextFontsButtonActionPerformed);
        nextFontsButton.setVisible(false);

        ButtonsPanel imageButtonsPanel = new ButtonsPanel();
        imageButtonsPanel.add(replaceImageButton);
        imageButtonsPanel.add(replaceImageAlphaButton);
        imageButtonsPanel.add(prevFontsButton);
        imageButtonsPanel.add(nextFontsButton);
        return imageButtonsPanel;
    }

    private JPanel createBinaryButtonsPanel() {
        replaceBinaryButton = new JButton(mainPanel.translate("button.replace"), View.getIcon("edit16"));
        replaceBinaryButton.setMargin(new Insets(3, 3, 3, 10));
        replaceBinaryButton.addActionListener(mainPanel::replaceButtonActionPerformed);

        ButtonsPanel binaryButtonsPanel = new ButtonsPanel();
        binaryButtonsPanel.add(replaceBinaryButton);
        return binaryButtonsPanel;
    }

    private JPanel createGenericTagButtonsPanel() {
        genericEditButton = new JButton(mainPanel.translate("button.edit"), View.getIcon("edit16"));
        genericEditButton.setMargin(new Insets(3, 3, 3, 10));
        genericEditButton.addActionListener(this::editGenericTagButtonActionPerformed);
        genericSaveButton = new JButton(mainPanel.translate("button.save"), View.getIcon("save16"));
        genericSaveButton.setMargin(new Insets(3, 3, 3, 10));
        genericSaveButton.addActionListener(this::saveGenericTagButtonActionPerformed);
        genericSaveButton.setVisible(false);
        genericCancelButton = new JButton(mainPanel.translate("button.cancel"), View.getIcon("cancel16"));
        genericCancelButton.setMargin(new Insets(3, 3, 3, 10));
        genericCancelButton.addActionListener(this::cancelGenericTagButtonActionPerformed);
        genericCancelButton.setVisible(false);

        ButtonsPanel genericTagButtonsPanel = new ButtonsPanel();
        genericTagButtonsPanel.add(genericEditButton);
        genericTagButtonsPanel.add(genericSaveButton);
        genericTagButtonsPanel.add(genericCancelButton);
        return genericTagButtonsPanel;
    }

    private JPanel createMetadataButtonsPanel() {
        metadataEditButton = new JButton(mainPanel.translate("button.edit"), View.getIcon("edit16"));
        metadataEditButton.setMargin(new Insets(3, 3, 3, 10));
        metadataEditButton.addActionListener(this::editMetadataButtonActionPerformed);
        metadataSaveButton = new JButton(mainPanel.translate("button.save"), View.getIcon("save16"));
        metadataSaveButton.setMargin(new Insets(3, 3, 3, 10));
        metadataSaveButton.addActionListener(this::saveMetadataButtonActionPerformed);
        metadataSaveButton.setVisible(false);
        metadataCancelButton = new JButton(mainPanel.translate("button.cancel"), View.getIcon("cancel16"));
        metadataCancelButton.setMargin(new Insets(3, 3, 3, 10));
        metadataCancelButton.addActionListener(this::cancelMetadataButtonActionPerformed);
        metadataCancelButton.setVisible(false);

        ButtonsPanel metadataTagButtonsPanel = new ButtonsPanel();
        metadataTagButtonsPanel.add(metadataEditButton);
        metadataTagButtonsPanel.add(metadataSaveButton);
        metadataTagButtonsPanel.add(metadataCancelButton);
        return metadataTagButtonsPanel;
    }

    private JPanel createFlashPlayerPanel(FlashPlayerPanel flashPanel) {
        JPanel pan = new JPanel(new BorderLayout());
        JLabel prevLabel = new HeaderLabel(mainPanel.translate("swfpreview"));
        prevLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //prevLabel.setBorder(new BevelBorder(BevelBorder.RAISED));

        pan.add(prevLabel, BorderLayout.NORTH);

        Component leftComponent;
        if (flashPanel != null) {
            JPanel flashPlayPanel = new JPanel(new BorderLayout());
            flashPlayPanel.add(flashPanel, BorderLayout.CENTER);

            /*JPanel bottomPanel = new JPanel(new BorderLayout());
             JPanel buttonsPanel = new JPanel(new FlowLayout());
             JButton selectColorButton = new JButton(View.getIcon("color16"));
             selectColorButton.addActionListener(mainPanel::selectBkColor);
             selectColorButton.setToolTipText(AppStrings.translate("button.selectbkcolor.hint"));
             buttonsPanel.add(selectColorButton);
             bottomPanel.add(buttonsPanel, BorderLayout.EAST);

             flashPlayPanel.add(bottomPanel, BorderLayout.SOUTH);*/
            JPanel flashPlayPanel2 = new JPanel(new BorderLayout());
            flashPlayPanel2.add(flashPlayPanel, BorderLayout.CENTER);
            flashPlayPanel2.add(new PlayerControls(mainPanel, flashPanel), BorderLayout.SOUTH);
            leftComponent = flashPlayPanel2;
        } else {
            JPanel swtPanel = new JPanel(new BorderLayout());
            swtPanel.add(new JLabel("<html><center>" + mainPanel.translate("notavailonthisplatform") + "</center></html>", JLabel.CENTER), BorderLayout.CENTER);
            swtPanel.setBackground(View.getDefaultBackgroundColor());
            leftComponent = swtPanel;
        }

        pan.add(leftComponent, BorderLayout.CENTER);
        return pan;
    }

    private JPanel createImagesCard() {
        JPanel shapesCard = new JPanel(new BorderLayout());
        JPanel previewPanel = new JPanel(new BorderLayout());

        JPanel previewCnt = new JPanel(new BorderLayout());
        imagePanel = new ImagePanel();
        imagePanel.setLoop(Configuration.loopMedia.get());
        previewCnt.add(imagePanel, BorderLayout.CENTER);
        previewCnt.add(imagePlayControls = new PlayerControls(mainPanel, imagePanel), BorderLayout.SOUTH);
        imagePlayControls.setMedia(imagePanel);
        previewPanel.add(previewCnt, BorderLayout.CENTER);
        JLabel prevIntLabel = new HeaderLabel(mainPanel.translate("swfpreview.internal"));
        prevIntLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //prevIntLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        previewPanel.add(prevIntLabel, BorderLayout.NORTH);

        shapesCard.add(previewPanel, BorderLayout.CENTER);

        shapesCard.add(createImageButtonsPanel(), BorderLayout.SOUTH);
        return shapesCard;
    }

    private JPanel createMetadataCard() {
        JPanel metadataCard = new JPanel(new BorderLayout());
        metadataEditor = new LineMarkedEditorPane();
        metadataCard.add(new JScrollPane(metadataEditor), BorderLayout.CENTER);
        //metadataEditor.setContentType("text/xml");
        metadataEditor.setEditable(false);

        metadataEditor.setFont(Configuration.getSourceFont());
        metadataEditor.changeContentType("text/xml");
        metadataEditor.addTextChangedListener(this::metadataTextChanged);

        metadataCard.add(createMetadataButtonsPanel(), BorderLayout.SOUTH);
        return metadataCard;
    }

    private boolean isMetadataModified() {
        return metadataSaveButton.isVisible() && metadataSaveButton.isEnabled();
    }

    private void setMetadataModified(boolean value) {
        metadataSaveButton.setEnabled(value);
        metadataCancelButton.setEnabled(value);
    }

    private void metadataTextChanged() {
        setMetadataModified(true);
    }

    private void updateMetadataButtonsVisibility() {
        boolean edit = metadataEditor.isEditable();
        boolean editorMode = Configuration.editorMode.get();
        metadataEditButton.setVisible(!readOnly && !edit);
        metadataSaveButton.setVisible(!readOnly && edit);
        boolean metadataModified = isMetadataModified();
        metadataCancelButton.setVisible(!readOnly && edit);
        metadataCancelButton.setEnabled(metadataModified || !editorMode);
    }

    private JPanel createBinaryCard() {
        JPanel binaryCard = new JPanel(new BorderLayout());
        binaryPanel = new BinaryPanel(mainPanel);
        binaryCard.add(binaryPanel, BorderLayout.CENTER);
        binaryCard.add(createBinaryButtonsPanel(), BorderLayout.SOUTH);
        return binaryCard;
    }

    private JPanel createGenericTagCard() {
        JPanel genericTagCard = new JPanel(new BorderLayout());
        genericTagPanel = new GenericTagTreePanel(mainPanel);
        genericTagCard.add(genericTagPanel, BorderLayout.CENTER);
        genericTagCard.add(createGenericTagButtonsPanel(), BorderLayout.SOUTH);
        return genericTagCard;
    }

    private void showCardLeft(String card) {
        CardLayout cl = (CardLayout) (viewerCards.getLayout());
        cl.show(viewerCards, card);
    }

    private void showCardRight(String card) {
        CardLayout cl = (CardLayout) (displayWithPreview.getLayout());
        cl.show(displayWithPreview, card);
    }

    public TextPanel getTextPanel() {
        return textPanel;
    }

    public void setParametersPanelVisible(boolean show) {
        parametersPanel.setVisible(show);
    }

    public void showFlashViewerPanel() {
        parametersPanel.setVisible(false);
        showCardLeft(FLASH_VIEWER_CARD);
    }

    public void showImagePanel(Timelined timelined, SWF swf, int frame) {
        showCardLeft(DRAW_PREVIEW_CARD);
        parametersPanel.setVisible(false);
        imagePlayControls.setMedia(imagePanel);
        imagePanel.setTimelined(timelined, swf, frame);
    }

    public void showImagePanel(SerializableImage image) {
        showCardLeft(DRAW_PREVIEW_CARD);
        parametersPanel.setVisible(false);
        imagePlayControls.setMedia(imagePanel);
        imagePanel.setImage(image);
    }

    public void showTextComparePanel(TextTag textTag, TextTag newTextTag) {
        imagePanel.setText(textTag, newTextTag);
    }

    public void setMedia(MediaDisplay media) {
        this.media = media;
        imagePlayControls.setMedia(media);
    }

    public void showFontPanel(FontTag fontTag) {
        fontPageNum = 0;
        showFontPage(fontTag);

        showCardRight(CARDFONTPANEL);
        if (!readOnly) {
            parametersPanel.setVisible(true);
        }
        fontPanel.showFontTag(fontTag);

        int pageCount = getFontPageCount(fontTag);
        if (pageCount > 1) {
            prevFontsButton.setVisible(true);
            nextFontsButton.setVisible(true);
        }
    }

    private void showFontPage(FontTag fontTag) {
        if (mainPanel.isInternalFlashViewerSelected() /*|| ft instanceof GFxDefineCompactedFont*/) {
            showImagePanel(MainPanel.makeTimelined(fontTag), fontTag.getSwf(), fontPageNum);
        }
    }

    public static int getFontPageCount(FontTag fontTag) {
        int pageCount = (fontTag.getGlyphShapeTable().size() - 1) / SHAPERECORD.MAX_CHARACTERS_IN_FONT_PREVIEW + 1;
        if (pageCount < 1) {
            pageCount = 1;
        }
        return pageCount;
    }

    public void showEmpty() {
        showCardLeft(EMPTY_CARD);
    }

    public void showTextPanel(TextTag textTag) {
        if (mainPanel.isInternalFlashViewerSelected() /*|| ft instanceof GFxDefineCompactedFont*/) {
            showImagePanel(MainPanel.makeTimelined(textTag), textTag.getSwf(), 0);
        }

        showCardRight(CARDTEXTPANEL);
        if (!readOnly) {
            parametersPanel.setVisible(true);
        }
        textPanel.setText(textTag);
    }

    public void focusTextPanel() {
        textPanel.focusTextValue();
    }

    public void clear() {
        imagePanel.clearAll();
        if (media != null) {
            try {
                media.close();
            } catch (IOException ex) {
                // ignore
            }
        }

        binaryPanel.setBinaryData(null);
        genericTagPanel.clear();
        fontPanel.clear();
    }

    public void closeTag() {
        textPanel.closeTag();
    }

    public static String formatMetadata(String input, int indent) {
        input = input.replace("> <", "><");
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            StringWriter sw = new StringWriter();
            xmlOutput.setWriter(sw);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + indent);
            transformer.transform(xmlInput, xmlOutput);

            return xmlOutput.getWriter().toString();
        } catch (IllegalArgumentException | TransformerException e) {
            return input;
        }
    }

    public void showMetaDataPanel(MetadataTag metadataTag) {
        showCardLeft(METADATA_TAG_CARD);
        this.metadataTag = metadataTag;
        metadataEditor.setEditable(!readOnly && !metadataTag.isReadOnly() && Configuration.editorMode.get());
        metadataEditor.setText(formatMetadata(metadataTag.xmlMetadata, 4));
        setMetadataModified(false);
        updateMetadataButtonsVisibility();
        parametersPanel.setVisible(false);
    }

    public void showBinaryPanel(DefineBinaryDataTag binaryDataTag) {
        showCardLeft(BINARY_TAG_CARD);
        binaryPanel.setBinaryData(binaryDataTag);
        parametersPanel.setVisible(false);
    }

    public void showGenericTagPanel(Tag tag) {
        showCardLeft(GENERIC_TAG_CARD);
        genericEditButton.setVisible(!tag.isReadOnly());
        genericSaveButton.setVisible(false);
        genericCancelButton.setVisible(false);
        genericTagPanel.setEditMode(false, tag);
        parametersPanel.setVisible(false);
    }

    public void setImageReplaceButtonVisible(boolean show, boolean showAlpha) {
        if (readOnly) {
            show = false;
            showAlpha = false;
        }
        replaceImageButton.setVisible(show);
        replaceImageAlphaButton.setVisible(showAlpha);
        prevFontsButton.setVisible(false);
        nextFontsButton.setVisible(false);
    }

    public void createAndShowTempSwf(TreeItem treeItem) {
        try {
            if (tempFile != null) {
                tempFile.delete();
            }

            tempFile = File.createTempFile("ffdec_view_", ".swf");
            tempFile.deleteOnExit();

            Color backgroundColor = View.getSwfBackgroundColor();

            if (treeItem instanceof Tag) {
                Tag tag = (Tag) treeItem;
                if (tag instanceof FontTag) { //Fonts are always black on white
                    backgroundColor = View.getDefaultBackgroundColor();
                }
            } else if (treeItem instanceof Frame) {
                Frame fn = (Frame) treeItem;
                SWF sourceSwf = fn.getSwf();
                if (fn.timeline.timelined == sourceSwf) {
                    SetBackgroundColorTag setBgColorTag = sourceSwf.getBackgroundColor();
                    if (setBgColorTag != null) {
                        backgroundColor = setBgColorTag.backgroundColor.toColor();
                    }
                }
            }

            SWFHeader header;
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                header = new PreviewExporter().exportSwf(fos, treeItem, backgroundColor, fontPageNum);
            }

            if (flashPanel != null) {
                flashPanel.displaySWF(tempFile.getAbsolutePath(), backgroundColor, header.frameRate);
            }

            showFlashViewerPanel();
        } catch (IOException | ActionParseException ex) {
            Logger.getLogger(PreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showSwf(SWF swf) {
        Color backgroundColor = View.getDefaultBackgroundColor();
        SetBackgroundColorTag setBgColorTag = swf.getBackgroundColor();
        if (setBgColorTag != null) {
            backgroundColor = setBgColorTag.backgroundColor.toColor();
        }

        if (tempFile != null) {
            tempFile.delete();
        }
        try {
            tempFile = File.createTempFile("ffdec_view_", ".swf");
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                swf.saveTo(fos, false);
            }
            //Inject Loader
            if (swf.isAS3() && Configuration.autoOpenLoadedSWFs.get() && !Configuration.internalFlashViewer.get() && !DebuggerTools.hasDebugger(swf)) {
                SWF instrSWF;
                try (InputStream fis = new BufferedInputStream(new FileInputStream(tempFile))) {
                    instrSWF = new SWF(fis, false, false);
                }

                DebuggerTools.switchDebugger(instrSWF);
                DebuggerTools.injectDebugLoader(instrSWF);
                try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                    instrSWF.saveTo(fos);
                }
            }
            flashPanel.displaySWF(tempFile.getAbsolutePath(), backgroundColor, swf.frameRate);
        } catch (IOException iex) {
            Logger.getLogger(PreviewPanel.class.getName()).log(Level.SEVERE, "Cannot create tempfile", iex);
        } catch (InterruptedException ex) {

        }
    }

    private void editMetadataButtonActionPerformed(ActionEvent evt) {
        TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
        if (item == null) {
            return;
        }

        if (item instanceof MetadataTag) {
            metadataEditor.setEditable(true);
            updateMetadataButtonsVisibility();
        }
    }

    private void saveMetadataButtonActionPerformed(ActionEvent evt) {
        metadataTag.xmlMetadata = metadataEditor.getText().replaceAll(">\r?\n<", "> <");
        metadataTag.setModified(true);
        metadataEditor.setEditable(Configuration.editorMode.get());
        setMetadataModified(false);
        updateMetadataButtonsVisibility();
        mainPanel.repaintTree();
    }

    private void cancelMetadataButtonActionPerformed(ActionEvent evt) {
        metadataEditor.setEditable(false);
        metadataEditor.setText(formatMetadata(metadataTag.xmlMetadata, 4));
        metadataEditor.setEditable(Configuration.editorMode.get());
        setMetadataModified(false);
        updateMetadataButtonsVisibility();
    }

    private void editGenericTagButtonActionPerformed(ActionEvent evt) {
        TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
        if (item == null) {
            return;
        }

        if (item instanceof TagScript) {
            item = ((TagScript) item).getTag();
        }

        if (item instanceof Tag) {
            genericEditButton.setVisible(false);
            genericSaveButton.setVisible(true);
            genericCancelButton.setVisible(true);
            genericTagPanel.setEditMode(true, (Tag) item);
        }
    }

    private void saveGenericTagButtonActionPerformed(ActionEvent evt) {
        if (genericTagPanel.save()) {
            Tag tag = genericTagPanel.getTag();
            SWF swf = tag.getSwf();
            swf.clearImageCache();
            swf.updateCharacters();
            tag.getTimelined().resetTimeline();
            swf.assignClassesToSymbols();
            swf.assignExportNamesToSymbols();
            mainPanel.refreshTree(swf);
            mainPanel.setTagTreeSelectedNode(tag);
            genericEditButton.setVisible(true);
            genericSaveButton.setVisible(false);
            genericCancelButton.setVisible(false);
            genericTagPanel.setEditMode(false, null);
        }
    }

    private void cancelGenericTagButtonActionPerformed(ActionEvent evt) {
        genericEditButton.setVisible(true);
        genericSaveButton.setVisible(false);
        genericCancelButton.setVisible(false);
        genericTagPanel.setEditMode(false, null);
    }

    private void prevFontsButtonActionPerformed(ActionEvent evt) {
        FontTag fontTag = fontPanel.getFontTag();
        int pageCount = getFontPageCount(fontTag);
        fontPageNum = (fontPageNum + pageCount - 1) % pageCount;
        if (mainPanel.isInternalFlashViewerSelected() /*|| ft instanceof GFxDefineCompactedFont*/) {
            imagePanel.setTimelined(MainPanel.makeTimelined(fontTag, fontPageNum), fontTag.getSwf(), 0);
        }
    }

    private void nextFontsButtonActionPerformed(ActionEvent evt) {
        FontTag fontTag = fontPanel.getFontTag();
        int pageCount = getFontPageCount(fontTag);
        fontPageNum = (fontPageNum + 1) % pageCount;
        if (mainPanel.isInternalFlashViewerSelected() /*|| ft instanceof GFxDefineCompactedFont*/) {
            imagePanel.setTimelined(MainPanel.makeTimelined(fontTag, fontPageNum), fontTag.getSwf(), 0);
        }
    }

    @Override
    public boolean tryAutoSave() {
        // todo: implement
        return textPanel.tryAutoSave() && false;
    }

    @Override
    public boolean isEditing() {
        return textPanel.isEditing()
                || (genericSaveButton.isVisible() && genericSaveButton.isEnabled())
                || (metadataSaveButton.isVisible() && metadataSaveButton.isEnabled());
    }
}
