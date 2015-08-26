/*
 *  Copyright (C) 2010-2015 JPEXS
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
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.gui.editor.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.gui.player.FlashPlayerPanel;
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.gui.player.PlayerControls;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        setLeftComponent(viewerCards);

        createParametersPanel();

        showCardLeft(FLASH_VIEWER_CARD);
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

        metadataEditor.setFont(new Font("Monospaced", Font.PLAIN, metadataEditor.getFont().getSize()));
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
        metadataEditButton.setVisible(!edit);
        metadataSaveButton.setVisible(edit);
        boolean metadataModified = isMetadataModified();
        metadataCancelButton.setVisible(edit);
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
        parametersPanel.setVisible(true);
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

    public void showTextPanel(TextTag textTag) {
        if (mainPanel.isInternalFlashViewerSelected() /*|| ft instanceof GFxDefineCompactedFont*/) {
            showImagePanel(MainPanel.makeTimelined(textTag), textTag.getSwf(), 0);
        }

        showCardRight(CARDTEXTPANEL);
        parametersPanel.setVisible(true);
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
        metadataEditor.setEditable(Configuration.editorMode.get());
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
        genericEditButton.setVisible(true);
        genericSaveButton.setVisible(false);
        genericCancelButton.setVisible(false);
        genericTagPanel.setEditMode(false, tag);
        parametersPanel.setVisible(false);
    }

    public void setImageReplaceButtonVisible(boolean show, boolean showAlpha) {
        replaceImageButton.setVisible(show);
        replaceImageAlphaButton.setVisible(showAlpha);
        prevFontsButton.setVisible(false);
        nextFontsButton.setVisible(false);
    }

    private static Tag classicTag(Tag t) {
        if (t instanceof DefineCompactedFont) {
            return ((DefineCompactedFont) t).toClassicFont();
        }
        return t;
    }

    public void createAndShowTempSwf(TreeItem tagObj) {
        SWF swf = null;
        try {
            if (tempFile != null) {
                tempFile.delete();
            }

            tempFile = File.createTempFile("ffdec_view_", ".swf");
            tempFile.deleteOnExit();

            Color backgroundColor = View.getSwfBackgroundColor();

            if (tagObj instanceof Tag) {
                Tag tag = (Tag) tagObj;
                swf = tag.getSwf();
                if (tag instanceof FontTag) { //Fonts are always black on white
                    backgroundColor = View.getDefaultBackgroundColor();
                }
            } else if (tagObj instanceof Frame) {
                Frame fn = (Frame) tagObj;
                swf = fn.getSwf();
                if (fn.timeline.timelined == swf) {
                    for (Tag t : swf.tags) {
                        if (t instanceof SetBackgroundColorTag) {
                            backgroundColor = ((SetBackgroundColorTag) t).backgroundColor.toColor();
                            break;
                        }
                    }
                }
            }

            int frameCount = 1;
            float frameRate = swf.frameRate;
            HashMap<Integer, VideoFrameTag> videoFrames = new HashMap<>();
            if (tagObj instanceof DefineVideoStreamTag) {
                DefineVideoStreamTag vs = (DefineVideoStreamTag) tagObj;
                SWF.populateVideoFrames(vs.getCharacterId(), swf.tags, videoFrames);
                frameCount = videoFrames.size();
            }

            List<SoundStreamBlockTag> soundFrames = new ArrayList<>();
            if (tagObj instanceof SoundStreamHeadTypeTag) {
                soundFrames = ((SoundStreamHeadTypeTag) tagObj).getBlocks();
                frameCount = soundFrames.size();
            }

            if ((tagObj instanceof DefineMorphShapeTag) || (tagObj instanceof DefineMorphShape2Tag)) {
                frameRate = MainPanel.MORPH_SHAPE_ANIMATION_FRAME_RATE;
                frameCount = (int) (MainPanel.MORPH_SHAPE_ANIMATION_LENGTH * frameRate);
            }

            if (tagObj instanceof DefineSoundTag) {
                frameCount = 1;
            }

            if (tagObj instanceof DefineSpriteTag) {
                frameCount = ((DefineSpriteTag) tagObj).frameCount;
            }

            byte[] data;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                SWFOutputStream sos2 = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
                RECT outrect = new RECT(swf.displayRect);

                if (tagObj instanceof FontTag) {
                    outrect.Xmin = 0;
                    outrect.Ymin = 0;
                    outrect.Xmax = FontTag.PREVIEWSIZE * 20;
                    outrect.Ymax = FontTag.PREVIEWSIZE * 20;
                }
                int width = outrect.getWidth();
                int height = outrect.getHeight();

                sos2.writeRECT(outrect);
                sos2.writeFIXED8(frameRate);
                sos2.writeUI16(frameCount); //framecnt

                /*FileAttributesTag fa = new FileAttributesTag();
                 sos2.writeTag(fa);
                 */
                new SetBackgroundColorTag(swf, new RGB(backgroundColor)).writeTag(sos2);

                if (tagObj instanceof Frame) {
                    Frame fn = (Frame) tagObj;
                    Timelined parent = fn.timeline.timelined;
                    List<Tag> subs = fn.timeline.tags;
                    List<Integer> doneCharacters = new ArrayList<>();
                    int frameCnt = 0;
                    for (Tag t : subs) {
                        if (t instanceof ShowFrameTag) {
                            frameCnt++;
                            continue;
                        }
                        if (frameCnt > fn.frame) {
                            break;
                        }

                        if (t instanceof DoActionTag || t instanceof DoInitActionTag) {
                            // todo: Maybe DoABC tags should be removed, too
                            continue;
                        }

                        Set<Integer> needed = new HashSet<>();
                        t.getNeededCharactersDeep(needed);
                        for (int n : needed) {
                            if (!doneCharacters.contains(n)) {
                                classicTag(swf.getCharacter(n)).writeTag(sos2);
                                doneCharacters.add(n);
                            }
                        }
                        if (t instanceof CharacterTag) {
                            int characterId = ((CharacterTag) t).getCharacterId();
                            if (!doneCharacters.contains(characterId)) {
                                doneCharacters.add(((CharacterTag) t).getCharacterId());
                            }
                        }
                        classicTag(t).writeTag(sos2);

                        if (parent != null) {
                            if (t instanceof PlaceObjectTypeTag) {
                                PlaceObjectTypeTag pot = (PlaceObjectTypeTag) t;
                                int chid = pot.getCharacterId();
                                int depth = pot.getDepth();
                                MATRIX mat = pot.getMatrix();
                                if (mat == null) {
                                    mat = new MATRIX();
                                }
                                mat = Helper.deepCopy(mat);
                                RECT r = parent.getRect();
                                mat.translateX += width / 2 - r.getWidth() / 2;
                                mat.translateY += height / 2 - r.getHeight() / 2;
                                new PlaceObject2Tag(swf, false, false, false, false, false, true, false, true, depth, chid, mat, null, 0, null, 0, null).writeTag(sos2);

                            }
                        }
                    }
                    new ShowFrameTag(swf).writeTag(sos2);
                } else {

                    boolean isSprite = false;
                    if (tagObj instanceof DefineSpriteTag) {
                        isSprite = true;
                    }
                    int chtId = 0;
                    if (tagObj instanceof CharacterTag) {
                        chtId = ((CharacterTag) tagObj).getCharacterId();
                    }

                    if (tagObj instanceof DefineBitsTag) {
                        JPEGTablesTag jtt = swf.getJtt();
                        if (jtt != null) {
                            jtt.writeTag(sos2);
                        }
                    } else if (tagObj instanceof AloneTag) {
                    } else {
                        Set<Integer> needed = new HashSet<>();
                        ((Tag) tagObj).getNeededCharactersDeep(needed);
                        for (int n : needed) {
                            if (isSprite && chtId == n) {
                                continue;
                            }

                            CharacterTag characterTag = swf.getCharacter(n);
                            if (characterTag instanceof DefineBitsTag) {
                                JPEGTablesTag jtt = swf.getJtt();
                                if (jtt != null) {
                                    jtt.writeTag(sos2);
                                }
                            }

                            classicTag(characterTag).writeTag(sos2);
                        }
                    }

                    classicTag((Tag) tagObj).writeTag(sos2);

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
                        mat.translateX = width / 4;
                        mat.translateY = height / 4;
                    }
                    if (tagObj instanceof FontTag) {

                        FontTag ft = (FontTag) classicTag((Tag) tagObj);

                        int countGlyphsTotal = ft.getGlyphShapeTable().size();
                        int countGlyphs = Math.min(SHAPERECORD.MAX_CHARACTERS_IN_FONT_PREVIEW, countGlyphsTotal);
                        int fontId = ft.getFontId();
                        int cols = (int) Math.ceil(Math.sqrt(countGlyphs));
                        int rows = (int) Math.ceil(((float) countGlyphs) / ((float) cols));
                        if (rows == 0) {
                            rows = 1;
                            cols = 1;
                        }
                        int x = 0;
                        int y = 0;
                        int firstGlyphIndex = fontPageNum * SHAPERECORD.MAX_CHARACTERS_IN_FONT_PREVIEW;
                        countGlyphs = Math.min(SHAPERECORD.MAX_CHARACTERS_IN_FONT_PREVIEW, countGlyphsTotal - firstGlyphIndex);
                        List<SHAPE> shapes = ft.getGlyphShapeTable();
                        int maxw = 0;
                        for (int f = firstGlyphIndex; f < firstGlyphIndex + countGlyphs; f++) {
                            RECT b = shapes.get(f).getBounds();
                            if (b.Xmin == Integer.MAX_VALUE) {
                                continue;
                            }
                            if (b.Ymin == Integer.MAX_VALUE) {
                                continue;
                            }
                            int w = (int) (b.getWidth() / ft.getDivider());
                            if (w > maxw) {
                                maxw = w;
                            }
                            x++;
                        }

                        x = 0;

                        int BORDER = 3 * 20;

                        int textHeight = height / rows;

                        while (maxw * textHeight / 1024.0 > width / cols - 2 * BORDER) {
                            textHeight--;
                        }

                        MATRIX tmat = new MATRIX();
                        for (int f = firstGlyphIndex; f < firstGlyphIndex + countGlyphs; f++) {
                            if (x >= cols) {
                                x = 0;
                                y++;
                            }
                            List<TEXTRECORD> rec = new ArrayList<>();
                            TEXTRECORD tr = new TEXTRECORD();

                            RECT b = shapes.get(f).getBounds();
                            int xmin = b.Xmin == Integer.MAX_VALUE ? 0 : (int) (b.Xmin / ft.getDivider());
                            xmin *= textHeight / 1024.0;
                            int ymin = b.Ymin == Integer.MAX_VALUE ? 0 : (int) (b.Ymin / ft.getDivider());
                            ymin *= textHeight / 1024.0;
                            int w = (int) (b.getWidth() / ft.getDivider());
                            w *= textHeight / 1024.0;
                            int h = (int) (b.getHeight() / ft.getDivider());
                            h *= textHeight / 1024.0;

                            tr.fontId = fontId;
                            tr.styleFlagsHasFont = true;
                            tr.textHeight = textHeight;
                            tr.xOffset = -xmin;
                            tr.yOffset = 0;
                            tr.styleFlagsHasXOffset = true;
                            tr.styleFlagsHasYOffset = true;
                            tr.glyphEntries = new ArrayList<>(1);
                            tr.styleFlagsHasColor = true;
                            tr.textColor = new RGB(0, 0, 0);
                            GLYPHENTRY ge = new GLYPHENTRY();

                            double ga = ft.getGlyphAdvance(f);
                            int cw = ga == -1 ? w : (int) (ga / ft.getDivider() * textHeight / 1024.0);

                            ge.glyphAdvance = 0;
                            ge.glyphIndex = f;
                            tr.glyphEntries.add(ge);
                            rec.add(tr);

                            tmat.translateX = x * width / cols + width / cols / 2 - w / 2;
                            tmat.translateY = y * height / rows + height / rows / 2;
                            new DefineTextTag(swf, 999 + f, new RECT(0, cw, ymin, ymin + h), new MATRIX(), rec).writeTag(sos2);
                            new PlaceObject2Tag(swf, false, false, false, true, false, true, true, false, 1 + f, 999 + f, tmat, null, 0, null, 0, null).writeTag(sos2);
                            x++;
                        }
                        new ShowFrameTag(swf).writeTag(sos2);
                    } else if ((tagObj instanceof DefineMorphShapeTag) || (tagObj instanceof DefineMorphShape2Tag)) {
                        new PlaceObject2Tag(swf, false, false, false, true, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null).writeTag(sos2);
                        new ShowFrameTag(swf).writeTag(sos2);
                        for (int ratio = 0; ratio < 65536; ratio += 65536 / frameCount) {
                            new PlaceObject2Tag(swf, false, false, false, true, false, true, false, true, 1, chtId, mat, null, ratio, null, 0, null).writeTag(sos2);
                            new ShowFrameTag(swf).writeTag(sos2);
                        }
                    } else if (tagObj instanceof SoundStreamHeadTypeTag) {
                        for (SoundStreamBlockTag blk : soundFrames) {
                            blk.writeTag(sos2);
                            new ShowFrameTag(swf).writeTag(sos2);
                        }
                    } else if (tagObj instanceof DefineSoundTag) {
                        ExportAssetsTag ea = new ExportAssetsTag(swf);
                        DefineSoundTag ds = (DefineSoundTag) tagObj;
                        ea.tags.add(ds.soundId);
                        ea.names.add("my_define_sound");
                        ea.writeTag(sos2);
                        List<Action> actions;
                        DoActionTag doa;

                        doa = new DoActionTag(swf, null);
                        actions = ASMParser.parse(0, false,
                                "ConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\"\n"
                                + "Push \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\" 0.0 \"Sound\"\n"
                                + "NewObject\n"
                                + "SetMember\n"
                                + "Push \"my_define_sound\" 1 \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"attachSound\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "Stop", swf.version, false);
                        doa.setActions(actions);
                        doa.writeTag(sos2);
                        new ShowFrameTag(swf).writeTag(sos2);

                        actions = ASMParser.parse(0, false,
                                "ConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\" \"start\"\n"
                                + "StopSounds\n"
                                + "Push \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\" 0.0 \"Sound\"\n"
                                + "NewObject\n"
                                + "SetMember\n"
                                + "Push \"my_define_sound\" 1 \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"attachSound\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "Push 9999 0.0 2 \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"start\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "Stop", swf.version, false);
                        doa.setActions(actions);
                        doa.writeTag(sos2);
                        new ShowFrameTag(swf).writeTag(sos2);

                        actions = ASMParser.parse(0, false,
                                "ConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\" \"onSoundComplete\" \"start\" \"execParam\"\n"
                                + "StopSounds\n"
                                + "Push \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\" 0.0 \"Sound\"\n"
                                + "NewObject\n"
                                + "SetMember\n"
                                + "Push \"my_define_sound\" 1 \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"attachSound\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "Push \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"onSoundComplete\"\n"
                                + "DefineFunction2 \"\" 0 2 false true true false true false true false false  {\n"
                                + "Push 0.0 register1 \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"start\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "}\n"
                                + "SetMember\n"
                                + "Push \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"execParam\"\n"
                                + "GetMember\n"
                                + "Push 1 \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"start\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "Stop", swf.version, false);
                        doa.setActions(actions);
                        doa.writeTag(sos2);
                        new ShowFrameTag(swf).writeTag(sos2);

                        actions = ASMParser.parse(0, false,
                                "StopSounds\n"
                                + "Stop", swf.version, false);
                        doa.setActions(actions);
                        doa.writeTag(sos2);
                        new ShowFrameTag(swf).writeTag(sos2);

                        new ShowFrameTag(swf).writeTag(sos2);
                    } else if (tagObj instanceof DefineVideoStreamTag) {

                        new PlaceObject2Tag(swf, false, false, false, false, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null).writeTag(sos2);
                        List<VideoFrameTag> frs = new ArrayList<>(videoFrames.values());
                        Collections.sort(frs, new Comparator<VideoFrameTag>() {
                            @Override
                            public int compare(VideoFrameTag o1, VideoFrameTag o2) {
                                return o1.frameNum - o2.frameNum;
                            }
                        });
                        boolean first = true;
                        int ratio = 0;
                        for (VideoFrameTag f : frs) {
                            if (!first) {
                                ratio++;
                                new PlaceObject2Tag(swf, false, false, false, true, false, false, false, true, 1, 0, null, null, ratio, null, 0, null).writeTag(sos2);
                            }
                            f.writeTag(sos2);
                            new ShowFrameTag(swf).writeTag(sos2);
                            first = false;
                        }
                    } else if (tagObj instanceof DefineSpriteTag) {
                        DefineSpriteTag s = (DefineSpriteTag) tagObj;
                        Tag lastTag = null;
                        for (Tag t : s.subTags) {
                            if (t instanceof EndTag) {
                                break;
                            } else if (t instanceof PlaceObjectTypeTag) {
                                PlaceObjectTypeTag pt = (PlaceObjectTypeTag) t;
                                MATRIX m = pt.getMatrix();
                                MATRIX m2 = new Matrix(m).preConcatenate(new Matrix(mat)).toMATRIX();
                                pt.writeTagWithMatrix(sos2, m2);
                                lastTag = t;
                            } else {
                                t.writeTag(sos2);
                                lastTag = t;
                            }
                        }
                        if (!s.subTags.isEmpty() && (lastTag != null) && (!(lastTag instanceof ShowFrameTag))) {
                            new ShowFrameTag(swf).writeTag(sos2);
                        }
                    } else {
                        new PlaceObject2Tag(swf, false, false, false, true, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null).writeTag(sos2);
                        new ShowFrameTag(swf).writeTag(sos2);
                    }

                } // not showframe

                new EndTag(swf).writeTag(sos2);
                data = baos.toByteArray();
            }

            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                SWFOutputStream sos = new SWFOutputStream(fos, Math.max(10, swf.version));
                sos.write("FWS".getBytes());
                sos.write(swf.version);
                sos.writeUI32(sos.getPos() + data.length + 4);
                sos.write(data);
                fos.flush();
            }
            if (flashPanel != null) {
                flashPanel.displaySWF(tempFile.getAbsolutePath(), backgroundColor, frameRate);
            }
            showFlashViewerPanel();
        } catch (IOException | ActionParseException ex) {
            Logger.getLogger(PreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showSwf(SWF swf) {
        Color backgroundColor = View.getDefaultBackgroundColor();
        for (Tag t : swf.tags) {
            if (t instanceof SetBackgroundColorTag) {
                backgroundColor = ((SetBackgroundColorTag) t).backgroundColor.toColor();
                break;
            }
        }

        if (tempFile != null) {
            tempFile.delete();
        }
        try {
            tempFile = File.createTempFile("ffdec_view_", ".swf");
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                swf.saveTo(fos);
            }
            flashPanel.displaySWF(tempFile.getAbsolutePath(), backgroundColor, swf.frameRate);
        } catch (IOException iex) {
            Logger.getLogger(PreviewPanel.class.getName()).log(Level.SEVERE, "Cannot create tempfile", iex);
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
        genericTagPanel.save();
        Tag tag = genericTagPanel.getTag();
        SWF swf = tag.getSwf();
        swf.clearImageCache();
        swf.updateCharacters();
        tag.getTimelined().resetTimeline();
        swf.assignClassesToSymbols();
        swf.assignExportNamesToSymbols();
        mainPanel.repaintTree();
        mainPanel.setTagTreeSelectedNode(tag);
        genericEditButton.setVisible(true);
        genericSaveButton.setVisible(false);
        genericCancelButton.setVisible(false);
        genericTagPanel.setEditMode(false, null);
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
