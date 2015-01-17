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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

/**
 *
 * @author JPEXS
 */
public class PreviewPanel extends JSplitPane implements ActionListener {

    private static final String FLASH_VIEWER_CARD = "FLASHVIEWER";
    private static final String DRAW_PREVIEW_CARD = "DRAWPREVIEW";
    private static final String GENERIC_TAG_CARD = "GENERICTAG";
    private static final String BINARY_TAG_CARD = "BINARYTAG";

    private static final String CARDTEXTPANEL = "Text card";
    private static final String CARDFONTPANEL = "Font card";

    private static final String ACTION_EDIT_GENERIC_TAG = "EDITGENERICTAG";
    private static final String ACTION_SAVE_GENERIC_TAG = "SAVEGENERICTAG";
    private static final String ACTION_CANCEL_GENERIC_TAG = "CANCELGENERICTAG";

    private static final String ACTION_PREV_FONTS = "PREVFONTS";
    private static final String ACTION_NEXT_FONTS = "NEXTFONTS";

    private final MainPanel mainPanel;
    private final JPanel viewerCards;

    private final FlashPlayerPanel flashPanel;
    private File tempFile;

    private ImagePanel imagePanel;
    private PlayerControls imagePlayControls;
    private MediaDisplay media;

    private BinaryPanel binaryPanel;
    private GenericTagPanel genericTagPanel;

    private JPanel displayWithPreview;

    // Image tag buttons
    private JButton replaceImageButton;
    private JButton prevFontsButton;
    private JButton nextFontsButton;

    // Binary tag buttons
    private JButton replaceBinaryButton;

    // Generic tag buttons
    private JButton editButton;
    private JButton saveButton;
    private JButton cancelButton;

    private JPanel parametersPanel;
    private FontPanel fontPanel;
    private int fontPageNum;
    private TextPanel textPanel;

    private boolean splitsInited;

    public PreviewPanel(MainPanel mainPanel, FlashPlayerPanel flashPanel) {
        super(JSplitPane.HORIZONTAL_SPLIT);
        this.mainPanel = mainPanel;
        this.flashPanel = flashPanel;

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if (tempFile != null) {
                    try {
                        tempFile.delete();
                    } catch (Exception ex) {

                    }
                }
            }

        });

        addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (splitsInited && getRightComponent().isVisible()) {
                    Configuration.guiPreviewSplitPaneDividerLocation.set((int) pce.getNewValue());
                }
            }
        });

        viewerCards = new JPanel();
        viewerCards.setLayout(new CardLayout());

        viewerCards.add(createFlashPlayerPanel(flashPanel), FLASH_VIEWER_CARD);
        viewerCards.add(createImagesCard(), DRAW_PREVIEW_CARD);
        viewerCards.add(createBinaryCard(), BINARY_TAG_CARD);
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
        replaceImageButton = new JButton(mainPanel.translate("button.replace"), View.getIcon("edit16"));
        replaceImageButton.setMargin(new Insets(3, 3, 3, 10));
        replaceImageButton.setActionCommand(MainPanel.ACTION_REPLACE);
        replaceImageButton.addActionListener(mainPanel);
        replaceImageButton.setVisible(false);

        prevFontsButton = new JButton(mainPanel.translate("button.prev"), View.getIcon("prev16"));
        prevFontsButton.setMargin(new Insets(3, 3, 3, 10));
        prevFontsButton.setActionCommand(ACTION_PREV_FONTS);
        prevFontsButton.addActionListener(this);
        prevFontsButton.setVisible(false);

        nextFontsButton = new JButton(mainPanel.translate("button.next"), View.getIcon("next16"));
        nextFontsButton.setMargin(new Insets(3, 3, 3, 10));
        nextFontsButton.setActionCommand(ACTION_NEXT_FONTS);
        nextFontsButton.addActionListener(this);
        nextFontsButton.setVisible(false);

        ButtonsPanel imageButtonsPanel = new ButtonsPanel();
        imageButtonsPanel.add(replaceImageButton);
        imageButtonsPanel.add(prevFontsButton);
        imageButtonsPanel.add(nextFontsButton);
        return imageButtonsPanel;
    }

    private JPanel createBinaryButtonsPanel() {
        replaceBinaryButton = new JButton(mainPanel.translate("button.replace"), View.getIcon("edit16"));
        replaceBinaryButton.setMargin(new Insets(3, 3, 3, 10));
        replaceBinaryButton.setActionCommand(MainPanel.ACTION_REPLACE);
        replaceBinaryButton.addActionListener(mainPanel);

        ButtonsPanel binaryButtonsPanel = new ButtonsPanel();
        binaryButtonsPanel.add(replaceBinaryButton);
        return binaryButtonsPanel;
    }

    private JPanel createGenericTagButtonsPanel() {
        editButton = new JButton(mainPanel.translate("button.edit"), View.getIcon("edit16"));
        editButton.setMargin(new Insets(3, 3, 3, 10));
        editButton.setActionCommand(ACTION_EDIT_GENERIC_TAG);
        editButton.addActionListener(this);
        saveButton = new JButton(mainPanel.translate("button.save"), View.getIcon("save16"));
        saveButton.setMargin(new Insets(3, 3, 3, 10));
        saveButton.setActionCommand(ACTION_SAVE_GENERIC_TAG);
        saveButton.addActionListener(this);
        saveButton.setVisible(false);
        cancelButton = new JButton(mainPanel.translate("button.cancel"), View.getIcon("cancel16"));
        cancelButton.setMargin(new Insets(3, 3, 3, 10));
        cancelButton.setActionCommand(ACTION_CANCEL_GENERIC_TAG);
        cancelButton.addActionListener(this);
        cancelButton.setVisible(false);

        ButtonsPanel genericTagButtonsPanel = new ButtonsPanel();
        genericTagButtonsPanel.add(editButton);
        genericTagButtonsPanel.add(saveButton);
        genericTagButtonsPanel.add(cancelButton);
        return genericTagButtonsPanel;
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
             selectColorButton.addActionListener(mainPanel);
             selectColorButton.setActionCommand(MainPanel.ACTION_SELECT_BKCOLOR);
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
            swtPanel.setBackground(View.DEFAULT_BACKGROUND_COLOR);
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

    private JPanel createBinaryCard() {
        JPanel binaryCard = new JPanel(new BorderLayout());
        binaryPanel = new BinaryPanel(mainPanel);
        binaryCard.add(binaryPanel, BorderLayout.CENTER);
        binaryCard.add(createBinaryButtonsPanel(), BorderLayout.SOUTH);
        return binaryCard;
    }

    private JPanel createGenericTagCard() {
        JPanel genericTagCard = new JPanel(new BorderLayout());
        genericTagPanel = new GenericTagTreePanel();
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

    public void setSplitsInited() {
        splitsInited = true;
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
        setDividerLocation(Configuration.guiPreviewSplitPaneDividerLocation.get(getWidth() / 2));
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
        setDividerLocation(Configuration.guiPreviewSplitPaneDividerLocation.get(getWidth() / 2));
        textPanel.setText(textTag.getFormattedText());
    }

    public void setEditText(boolean edit) {
        textPanel.setEditText(edit);
    }

    public void clear() {
        imagePanel.stop();
        if (media != null) {
            media.pause();
        }

        binaryPanel.setBinaryData(null);
        genericTagPanel.clear();
        fontPanel.clear();
    }

    public void showBinaryPanel(DefineBinaryDataTag binaryDataTag) {
        showCardLeft(BINARY_TAG_CARD);
        binaryPanel.setBinaryData(binaryDataTag);
        parametersPanel.setVisible(false);
    }

    public void showGenericTagPanel(Tag tag) {
        showCardLeft(GENERIC_TAG_CARD);
        editButton.setVisible(true);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        genericTagPanel.setEditMode(false, tag);
        parametersPanel.setVisible(false);
    }

    public void setImageReplaceButtonVisible(boolean show) {
        replaceImageButton.setVisible(show);
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
        SWF swf;
        try {
            if (tempFile != null) {
                tempFile.delete();
            }
            tempFile = File.createTempFile("ffdec_view_", ".swf");
            tempFile.deleteOnExit();

            Color backgroundColor = View.swfBackgroundColor;

            if (tagObj instanceof FontTag) { //Fonts are always black on white
                backgroundColor = View.DEFAULT_BACKGROUND_COLOR;
            }

            if (tagObj instanceof Frame) {
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
            } else {
                Tag tag = (Tag) tagObj;
                swf = tag.getSwf();
            }

            int frameCount = 1;
            int frameRate = swf.frameRate;
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
                frameCount = MainPanel.MORPH_SHAPE_ANIMATION_LENGTH * frameRate;
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
                sos2.writeUI8(0);
                sos2.writeUI8(frameRate);
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
                                if (parent instanceof BoundedTag) {
                                    RECT r = ((BoundedTag) parent).getRect(new HashSet<BoundedTag>());
                                    mat.translateX = mat.translateX + width / 2 - r.getWidth() / 2;
                                    mat.translateY = mat.translateY + height / 2 - r.getHeight() / 2;
                                } else {
                                    mat.translateX += width / 2;
                                    mat.translateY += height / 2;
                                }
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
                            classicTag(swf.getCharacter(n)).writeTag(sos2);
                        }
                    }

                    classicTag((Tag) tagObj).writeTag(sos2);

                    MATRIX mat = new MATRIX();
                    mat.hasRotate = false;
                    mat.hasScale = false;
                    mat.translateX = 0;
                    mat.translateY = 0;
                    if (tagObj instanceof BoundedTag) {
                        RECT r = ((BoundedTag) tagObj).getRect(new HashSet<BoundedTag>());
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
                            tr.glyphEntries = new GLYPHENTRY[1];
                            tr.styleFlagsHasColor = true;
                            tr.textColor = new RGB(0, 0, 0);
                            tr.glyphEntries[0] = new GLYPHENTRY();

                            double ga = ft.getGlyphAdvance(f);
                            int cw = ga == -1 ? w : (int) (ga / ft.getDivider() * textHeight / 1024.0);

                            tr.glyphEntries[0].glyphAdvance = 0;
                            tr.glyphEntries[0].glyphIndex = f;
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
                                "ActionConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\"\n"
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
                                "ActionConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\" \"start\"\n"
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
                                "ActionConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\" \"onSoundComplete\" \"start\" \"execParam\"\n"
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
                        if (flashPanel != null) {
                            //flashPanel.specialPlayback = true;
                        }
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

                }//not showframe

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
        Color backgroundColor = View.DEFAULT_BACKGROUND_COLOR;
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
            swf.saveTo(new BufferedOutputStream(new FileOutputStream(tempFile)));
            flashPanel.displaySWF(tempFile.getAbsolutePath(), backgroundColor, swf.frameRate);
        } catch (IOException iex) {
            Logger.getLogger(PreviewPanel.class.getName()).log(Level.SEVERE, "Cannot create tempfile", iex);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_EDIT_GENERIC_TAG: {
                TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
                if (item == null) {
                    return;
                }

                if (item instanceof Tag) {
                    editButton.setVisible(false);
                    saveButton.setVisible(true);
                    cancelButton.setVisible(true);
                    //genericTagPanel.generateEditControls((Tag) item, false);
                    genericTagPanel.setEditMode(true, (Tag) item);
                }
            }
            break;
            case ACTION_SAVE_GENERIC_TAG: {
                genericTagPanel.save();
                Tag tag = genericTagPanel.getTag();
                SWF swf = tag.getSwf();
                swf.clearImageCache();
                swf.updateCharacters();
                tag.getTimelined().getTimeline().reset();
                mainPanel.refreshTree();
                mainPanel.setTagTreeSelectedNode(tag);
                editButton.setVisible(true);
                saveButton.setVisible(false);
                cancelButton.setVisible(false);
                genericTagPanel.setEditMode(false, null);
            }
            break;
            case ACTION_CANCEL_GENERIC_TAG: {
                editButton.setVisible(true);
                saveButton.setVisible(false);
                cancelButton.setVisible(false);
                genericTagPanel.setEditMode(false, null);
            }
            break;
            case ACTION_PREV_FONTS: {
                FontTag fontTag = fontPanel.getFontTag();
                int pageCount = getFontPageCount(fontTag);
                fontPageNum = (fontPageNum + pageCount - 1) % pageCount;
                if (mainPanel.isInternalFlashViewerSelected() /*|| ft instanceof GFxDefineCompactedFont*/) {
                    imagePanel.setTimelined(MainPanel.makeTimelined(fontTag), fontTag.getSwf(), fontPageNum);
                }
            }
            break;
            case ACTION_NEXT_FONTS: {
                FontTag fontTag = fontPanel.getFontTag();
                int pageCount = getFontPageCount(fontTag);
                fontPageNum = (fontPageNum + 1) % pageCount;
                if (mainPanel.isInternalFlashViewerSelected() /*|| ft instanceof GFxDefineCompactedFont*/) {
                    imagePanel.setTimelined(MainPanel.makeTimelined(fontTag), fontTag.getSwf(), fontPageNum);
                }
            }
            break;
        }
    }
}
