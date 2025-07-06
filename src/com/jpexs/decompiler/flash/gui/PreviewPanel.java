/*
 *  Copyright (C) 2010-2025 JPEXS
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

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFHeader;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.PreviewExporter;
import com.jpexs.decompiler.flash.exporters.amf.amf0.Amf0Exporter;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.gui.editor.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.gui.hexview.HexView;
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.gui.player.PlayerControls;
import com.jpexs.decompiler.flash.gui.soleditor.Cookie;
import com.jpexs.decompiler.flash.gui.soleditor.SolEditorFrame;
import com.jpexs.decompiler.flash.importers.amf.AmfParseException;
import com.jpexs.decompiler.flash.importers.amf.amf0.Amf0Importer;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3Importer;
import com.jpexs.decompiler.flash.math.BezierUtils;
import com.jpexs.decompiler.flash.sol.SolFile;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.ProductInfoTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.tags.base.BinaryDataInterface;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLE;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE2;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import com.jpexs.helpers.SerializableImage;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author JPEXS
 */
public class PreviewPanel extends JPersistentSplitPane implements TagEditorPanel {

    private static final String FLASH_VIEWER_CARD = "FLASHVIEWER";

    private static final String DRAW_PREVIEW_CARD = "DRAWPREVIEW";

    private static final String GENERIC_TAG_CARD = "GENERICTAG";

    private static final String BINARY_TAG_CARD = "BINARYTAG";

    private static final String PRODUCTINFO_TAG_CARD = "PRODUCTINFOTAG";

    private static final String UNKNOWN_TAG_CARD = "UNKNOWNTAG";

    private static final String METADATA_TAG_CARD = "METADATATAG";

    private static final String EMPTY_CARD = "EMPTY";

    private static final String COOKIE_CARD = "COOKIE";

    private static final String CARDTEXTPANEL = "Text card";

    private static final String CARDFONTPANEL = "Font card";

    private static final String DISPLAYEDIT_TAG_CARD = "PLACETAG";

    private final MainPanel mainPanel;

    private final JPanel viewerCards;

    private File tempFile;

    private ImagePanel imagePanel;

    private PlayerControls imagePlayControls;

    private MediaDisplay media;

    private BinaryPanel binaryPanel;

    private LineMarkedEditorPane cookieEditor;

    private JTextField cookieFilenameField;

    private JLabel amfVersionLabel;

    private LineMarkedEditorPane metadataEditor;

    private GenericTagPanel genericTagPanel;

    private GenericTagPanel displayEditGenericPanel;

    private JSplitPane displayEditSplitPane;

    private JPanel displayWithPreview;

    // Image tag buttons
    private JButton replaceShapeButton;

    private JButton replaceMorphShapeButton;

    private JButton replaceMorphShapeUpdateBoundsButton;

    private JButton replaceShapeUpdateBoundsButton;

    private JButton replaceSoundButton;

    private JButton replaceImageButton;

    private JButton replaceImageAlphaButton;

    private JButton replaceSpriteButton;

    private JButton replaceMovieButton;

    private JButton prevFontsButton;

    private JButton nextFontsButton;

    // Binary tag buttons
    private JButton replaceBinaryButton;

    // Unknown tag buttons
    private JButton replaceUnknownButton;

    // Metadata editor buttons
    private JButton metadataEditButton;

    private JButton metadataSaveButton;

    private JButton metadataCancelButton;

    // Generic tag buttons
    private JButton genericEditButton;

    private JButton genericSaveButton;

    private JButton genericCancelButton;

    private JButton displayEditTransformButton;

    private JButton displayEditEditButton;

    private JButton displayEditSaveButton;

    private JButton displayEditCancelButton;

    private JButton displayEditEditPointsButton;

    private JPanel morphShowPanel;

    private JToggleButton displayEditShowAnimationButton;

    private JToggleButton displayEditShowStartButton;

    private JToggleButton displayEditShowEndButton;

    private JButton cookieEditButton;

    private JButton cookieSaveButton;

    private JButton cookieCancelButton;

    private Component morphShowSpace;

    private JPanel parametersPanel;

    private FontPanel fontPanel;

    private int fontPageNum;

    private TextPanel textPanel;

    private MetadataTag metadataTag;

    private Cookie cookie;

    private boolean readOnly = false;

    private ImagePanel displayEditImagePanel;

    private final int dividerSize;

    private Tag displayEditTag;

    private HexView unknownHexView;

    private final int EDIT_TRANSFORM = 1;
    private final int EDIT_RAW = 2;
    private final int EDIT_POINTS = 3;
    private int displayEditMode = EDIT_RAW;

    private final int MORPH_ANIMATE = 0;
    private final int MORPH_START = 1;
    private final int MORPH_END = 2;
    private int morphDisplayMode = MORPH_ANIMATE;

    private List<SHAPERECORD> oldShapeRecords;
    private RECT oldShapeBounds;
    private RECT oldShapeEdgeBounds;

    private List<SHAPERECORD> oldEndShapeRecords;
    private RECT oldEndShapeBounds;
    private RECT oldEndShapeEdgeBounds;

    //used only for flash player
    private TreeItem currentItem;

    private JLabel productValueLabel = new JLabel();
    private JLabel editionValueLabel = new JLabel();
    private JLabel versionValueLabel = new JLabel();
    private JLabel buildValueLabel = new JLabel();
    private JLabel compileDateValueLabel = new JLabel();

    private JButton imageTransformButton;

    private JButton imageTransformSaveButton;
    private JButton imageTransformCancelButton;

    private TransformPanel imageTransformPanel;

    private TransformPanel displayEditTransformPanel;

    private FasterScrollPane displayEditTransformScrollPane;

    private FasterScrollPane imageTransformScrollPane;

    private JPersistentSplitPane displayEditTransformSplitPane;

    private JPersistentSplitPane imageTransformSplitPane;

    private DocumentListener cookieDocumentListener;

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        setDividerSize(this.readOnly ? 0 : dividerSize);
        if (readOnly) {
            parametersPanel.setVisible(false);
        }
    }

    public PreviewPanel(MainPanel mainPanel) {
        super(JSplitPane.HORIZONTAL_SPLIT, Configuration.guiPreviewSplitPaneDividerLocationPercent);
        this.mainPanel = mainPanel;

        viewerCards = new JPanel();
        viewerCards.setLayout(new CardLayout());

        viewerCards.add(createFlashPlayerPanel(), FLASH_VIEWER_CARD);
        viewerCards.add(createImagesCard(), DRAW_PREVIEW_CARD);
        viewerCards.add(createBinaryCard(), BINARY_TAG_CARD);
        viewerCards.add(createProductInfoCard(), PRODUCTINFO_TAG_CARD);
        viewerCards.add(createUnknownCard(), UNKNOWN_TAG_CARD);
        viewerCards.add(createMetadataCard(), METADATA_TAG_CARD);
        viewerCards.add(createCookieCard(), COOKIE_CARD);
        viewerCards.add(createGenericTagCard(), GENERIC_TAG_CARD);
        viewerCards.add(createDisplayEditTagCard(), DISPLAYEDIT_TAG_CARD);
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

    public FontPanel getFontPanel() {
        return fontPanel;
    }

    private void createParametersPanel() {
        displayWithPreview = new JPanel(new CardLayout());

        textPanel = new TextPanel(mainPanel, null);
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
        replaceSoundButton = new JButton(mainPanel.translate("button.replace"), View.getIcon("importsound16"));
        replaceSoundButton.setMargin(new Insets(3, 3, 3, 10));
        replaceSoundButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceButtonActionPerformed(mainPanel.getCurrentTree().getSelected());
            }
        });
        replaceSoundButton.setVisible(false);

        replaceImageButton = new JButton(mainPanel.translate("button.replace"), View.getIcon("replaceimage16"));
        replaceImageButton.setMargin(new Insets(3, 3, 3, 10));
        replaceImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceButtonActionPerformed(mainPanel.getCurrentTree().getSelected());
            }
        });
        replaceImageButton.setVisible(false);

        replaceImageAlphaButton = new JButton(mainPanel.translate("button.replaceAlphaChannel"), View.getIcon("replacealpha16"));
        replaceImageAlphaButton.setMargin(new Insets(3, 3, 3, 10));
        replaceImageAlphaButton.addActionListener(mainPanel::replaceAlphaButtonActionPerformed);
        replaceImageAlphaButton.setVisible(false);

        replaceSpriteButton = new JButton(mainPanel.translate("button.replaceWithGif"), View.getIcon("replacesprite16"));
        replaceSpriteButton.setMargin(new Insets(3, 3, 3, 10));
        replaceSpriteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceSpriteWithGifButtonActionPerformed(mainPanel.getCurrentTree().getCurrentTreeItem());
            }
        });
        replaceSpriteButton.setVisible(false);

        replaceMovieButton = new JButton(mainPanel.translate("button.replace"), View.getIcon("importmovie16"));
        replaceMovieButton.setMargin(new Insets(3, 3, 3, 10));
        replaceMovieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceButtonActionPerformed(mainPanel.getCurrentTree().getSelected());
            }
        });
        replaceMovieButton.setVisible(false);

        prevFontsButton = new JButton(mainPanel.translate("button.prev"), View.getIcon("prev16"));
        prevFontsButton.setMargin(new Insets(3, 3, 3, 10));
        prevFontsButton.addActionListener(this::prevFontsButtonActionPerformed);
        prevFontsButton.setVisible(false);

        nextFontsButton = new JButton(mainPanel.translate("button.next"), View.getIcon("next16"));
        nextFontsButton.setMargin(new Insets(3, 3, 3, 10));
        nextFontsButton.addActionListener(this::nextFontsButtonActionPerformed);
        nextFontsButton.setVisible(false);

        ButtonsPanel imageButtonsPanel = new ButtonsPanel();
        imageButtonsPanel.add(replaceSoundButton);
        //imageButtonsPanel.add(replaceShapeButton);
        //imageButtonsPanel.add(replaceShapeUpdateBoundsButton);
        imageButtonsPanel.add(replaceImageButton);
        imageButtonsPanel.add(replaceImageAlphaButton);
        imageButtonsPanel.add(replaceSpriteButton);
        imageButtonsPanel.add(replaceMovieButton);
        imageButtonsPanel.add(prevFontsButton);
        imageButtonsPanel.add(nextFontsButton);
        return imageButtonsPanel;
    }

    private JPanel createBinaryButtonsPanel() {
        replaceBinaryButton = new JButton(mainPanel.translate("button.replace"), View.getIcon("edit16"));
        replaceBinaryButton.setMargin(new Insets(3, 3, 3, 10));
        replaceBinaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceButtonActionPerformed(mainPanel.getCurrentTree().getSelected());
            }
        });

        ButtonsPanel binaryButtonsPanel = new ButtonsPanel();
        binaryButtonsPanel.add(replaceBinaryButton);
        return binaryButtonsPanel;
    }

    private JPanel createUnknownButtonsPanel() {
        replaceUnknownButton = new JButton(mainPanel.translate("button.replace"), View.getIcon("edit16"));
        replaceUnknownButton.setMargin(new Insets(3, 3, 3, 10));
        replaceUnknownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceButtonActionPerformed(mainPanel.getCurrentTree().getSelected());
            }
        });

        ButtonsPanel unknownButtonsPanel = new ButtonsPanel();
        unknownButtonsPanel.add(replaceBinaryButton);
        return unknownButtonsPanel;
    }

    private JPanel createGenericTagButtonsPanel() {
        genericEditButton = new JButton(mainPanel.translate("button.edit"), View.getIcon("edit16"));
        genericEditButton.setMargin(new Insets(3, 3, 3, 10));
        genericEditButton.addActionListener(this::editGenericTagButtonActionPerformed);
        genericSaveButton = new JButton(mainPanel.translate("button.save"), View.getIcon("save16"));
        genericSaveButton.setMargin(new Insets(3, 3, 3, 10));
        genericSaveButton.addActionListener(this::saveGenericTagButtonActionPerformed);
        genericCancelButton = new JButton(mainPanel.translate("button.cancel"), View.getIcon("cancel16"));
        genericCancelButton.setMargin(new Insets(3, 3, 3, 10));
        genericCancelButton.addActionListener(this::cancelGenericTagButtonActionPerformed);

        if (Configuration.editorMode.get()) {
            genericEditButton.setVisible(false);
            genericSaveButton.setVisible(true);
            genericSaveButton.setEnabled(false);
            genericCancelButton.setVisible(true);
            genericCancelButton.setEnabled(false);
        } else {
            genericEditButton.setVisible(true);
            genericSaveButton.setVisible(false);
            genericCancelButton.setVisible(false);
        }

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

    private JPanel createFlashPlayerPanel() {
        JPanel pan = new JPanel(new BorderLayout());
        JLabel prevLabel = new HeaderLabel(mainPanel.translate("swfpreview"));
        prevLabel.setHorizontalAlignment(SwingConstants.CENTER);

        pan.add(prevLabel, BorderLayout.NORTH);
        JPanel swtPanel = new JPanel(new GridBagLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton flashProjectorButton = new JButton(mainPanel.translate("button.showin.flashprojector"));
        flashProjectorButton.addActionListener(this::flashProjectorActionPerformed);
        buttonsPanel.add(flashProjectorButton);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        swtPanel.add(buttonsPanel, gbc);

        pan.add(swtPanel, BorderLayout.CENTER);
        return pan;
    }

    private void flashProjectorActionPerformed(ActionEvent e) {
        createAndRunTempSwf(currentItem);
    }

    private void setStatus(String status) {
        imagePlayControls.setStatus(status);
    }

    private void setNoStatus() {
        setStatus("");
    }

    private JPanel createImagesCard() {
        JPanel shapesCard = new JPanel(new BorderLayout());
        JPanel previewPanel = new JPanel(new BorderLayout());

        JPanel previewCnt = new JPanel(new BorderLayout());
        imagePanel = new ImagePanel();

        imagePanel.addTextChangedListener(new Runnable() {
            @Override
            public void run() {
                textPanel.refresh();
            }            
        });
        
        imagePanel.addPlaceObjectSelectedListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaceObjectTypeTag placeObject = imagePanel.getPlaceTagUnderCursor();
                if (placeObject != null) {
                    mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), placeObject);
                }
            }
        });

        imagePanel.setLoop(Configuration.loopMedia.get());

        imageTransformPanel = new TransformPanel(imagePanel);
        previewCnt.add(imageTransformSplitPane = new JPersistentSplitPane(JPersistentSplitPane.HORIZONTAL_SPLIT, imagePanel,
                imageTransformScrollPane = new FasterScrollPane(imageTransformPanel),
                Configuration.guiSplitPaneTransform2DividerLocationPercent)
        );
        imageTransformScrollPane.setVisible(false);

        JPanel buttonsPanel = new JPanel(new FlowLayout());

        imageTransformButton = new JButton(mainPanel.translate("button.transform"), View.getIcon("freetransform16"));
        imageTransformButton.setMargin(new Insets(3, 3, 3, 10));
        imageTransformButton.addActionListener(this::transformImageButtonActionPerformed);

        imageTransformSaveButton = new JButton(mainPanel.translate("button.save"), View.getIcon("save16"));
        imageTransformSaveButton.setMargin(new Insets(3, 3, 3, 10));
        imageTransformSaveButton.addActionListener(this::saveImageTransformButtonActionPerformed);

        imageTransformCancelButton = new JButton(mainPanel.translate("button.cancel"), View.getIcon("cancel16"));
        imageTransformCancelButton.setMargin(new Insets(3, 3, 3, 10));
        imageTransformCancelButton.addActionListener(this::cancelImageTransformButtonActionPerformed);

        buttonsPanel.add(imageTransformButton);
        buttonsPanel.add(imageTransformSaveButton);
        buttonsPanel.add(imageTransformCancelButton);

        imageTransformSaveButton.setVisible(false);
        imageTransformCancelButton.setVisible(false);

        previewCnt.add(imagePlayControls = new PlayerControls(mainPanel, imagePanel, buttonsPanel), BorderLayout.SOUTH);
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
        metadataCard.add(new FasterScrollPane(metadataEditor), BorderLayout.CENTER);
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

    private boolean isCookieModified() {
        return cookieSaveButton.isVisible() && cookieSaveButton.isEnabled();
    }

    private void setMetadataModified(boolean value) {
        metadataSaveButton.setEnabled(value);
        metadataCancelButton.setEnabled(value);
    }

    private void setCookieModified(boolean value) {
        cookieSaveButton.setEnabled(value);
        cookieCancelButton.setEnabled(value);
    }

    private void metadataTextChanged() {
        setMetadataModified(true);
        mainPanel.setEditingStatus();
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

    private void updateCookieButtonsVisibility() {
        boolean edit = cookieEditor.isEditable();
        boolean editorMode = Configuration.editorMode.get();
        cookieEditButton.setVisible(!readOnly && !edit);
        cookieSaveButton.setVisible(!readOnly && edit);
        boolean cookieModified = isCookieModified();
        cookieCancelButton.setVisible(!readOnly && edit);
        cookieCancelButton.setEnabled(cookieModified || !editorMode);
    }

    private JPanel createBinaryCard() {
        JPanel binaryCard = new JPanel(new BorderLayout());
        binaryPanel = new BinaryPanel(mainPanel);
        binaryCard.add(binaryPanel, BorderLayout.CENTER);
        binaryCard.add(createBinaryButtonsPanel(), BorderLayout.SOUTH);
        return binaryCard;
    }

    private JPanel createCookieCard() {
        JPanel cookieCard = new JPanel(new BorderLayout());
        cookieFilenameField = new JTextField(30);
        amfVersionLabel = new JLabel();
        cookieEditor = new LineMarkedEditorPane();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel(AppStrings.translate(SolEditorFrame.class, "filename")));
        topPanel.add(cookieFilenameField);
        topPanel.add(new JLabel(AppStrings.translate(SolEditorFrame.class, "amfVersion")));
        topPanel.add(amfVersionLabel);

        cookieCard.add(topPanel, BorderLayout.NORTH);
        cookieCard.add(new FasterScrollPane(cookieEditor), BorderLayout.CENTER);
        cookieCard.add(createCookieButtonsPanel(), BorderLayout.SOUTH);

        cookieEditor.setContentType("text/javascript");

        cookieDocumentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setCookieModified(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setCookieModified(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setCookieModified(true);
            }

        };
        return cookieCard;
    }

    private JPanel createCookieButtonsPanel() {
        cookieEditButton = new JButton(mainPanel.translate("button.edit"), View.getIcon("edit16"));
        cookieEditButton.setMargin(new Insets(3, 3, 3, 10));
        cookieEditButton.addActionListener(this::editCookieButtonActionPerformed);
        cookieSaveButton = new JButton(mainPanel.translate("button.save"), View.getIcon("save16"));
        cookieSaveButton.setMargin(new Insets(3, 3, 3, 10));
        cookieSaveButton.addActionListener(this::saveCookieButtonActionPerformed);
        cookieSaveButton.setVisible(false);
        cookieCancelButton = new JButton(mainPanel.translate("button.cancel"), View.getIcon("cancel16"));
        cookieCancelButton.setMargin(new Insets(3, 3, 3, 10));
        cookieCancelButton.addActionListener(this::cancelCookieButtonActionPerformed);
        cookieCancelButton.setVisible(false);

        ButtonsPanel metadataTagButtonsPanel = new ButtonsPanel();
        metadataTagButtonsPanel.add(cookieEditButton);
        metadataTagButtonsPanel.add(cookieSaveButton);
        metadataTagButtonsPanel.add(cookieCancelButton);
        return metadataTagButtonsPanel;
    }

    private void editCookieButtonActionPerformed(ActionEvent evt) {
        TreeItem item = mainPanel.getCurrentTree().getCurrentTreeItem();
        if (item == null) {
            return;
        }

        if (item instanceof Cookie) {
            cookieEditor.setEditable(true);
            cookieFilenameField.setEditable(true);
            updateCookieButtonsVisibility();
            mainPanel.setEditingStatus();
        }
    }

    private void saveCookieButtonActionPerformed(ActionEvent evt) {
        //cookie.setModified(true);

        String amfText = cookieEditor.getText();
        int amfVersion = Integer.parseInt(amfVersionLabel.getText());
        Map<String, Object> amfValues = null;
        try {
            switch (amfVersion) {
                case 0:
                    Amf0Importer a0i = new Amf0Importer();
                    amfValues = a0i.stringToAmfMap(amfText);
                    break;
                case 3:
                    Amf3Importer a3i = new Amf3Importer();
                    amfValues = a3i.stringToAmfMap(amfText);
                    break;
            }

            SolFile solFile = new SolFile(cookieFilenameField.getText(), amfVersion, amfValues);
            try (FileOutputStream fos = new FileOutputStream(cookie.getSolFile())) {
                solFile.writeTo(fos);
            }
        } catch (AmfParseException ex) {
            cookieEditor.gotoLine((int) ex.line);
            cookieEditor.markError();
            ViewMessages.showMessageDialog(this, AppStrings.translate(SolEditorFrame.class, "error.parse").replace("%reason%", ex.text).replace("%line%", "" + ex.line), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException ex) {
            ViewMessages.showMessageDialog(this, ex.getLocalizedMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        cookieEditor.setEditable(Configuration.editorMode.get());
        cookieFilenameField.setEditable(false);
        setCookieModified(false);
        updateCookieButtonsVisibility();
        mainPanel.repaintTree();
        mainPanel.clearEditingStatus();
    }

    private void cancelCookieButtonActionPerformed(ActionEvent evt) {
        cookieEditor.setEditable(false);
        cookieFilenameField.setEditable(false);
        readCookie();
        metadataEditor.setEditable(Configuration.editorMode.get());
        setCookieModified(false);
        updateCookieButtonsVisibility();
        mainPanel.clearEditingStatus();
    }

    private JPanel createProductInfoCard() {
        JPanel productInfoCard = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel tablePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel productLabel = new JLabel(AppStrings.translate("productinfo.product"));
        productLabel.setHorizontalAlignment(JLabel.RIGHT);
        JLabel editionLabel = new JLabel(AppStrings.translate("productinfo.edition"));
        editionLabel.setHorizontalAlignment(JLabel.RIGHT);
        JLabel versionLabel = new JLabel(AppStrings.translate("productinfo.version"));
        versionLabel.setHorizontalAlignment(JLabel.RIGHT);
        JLabel buildLabel = new JLabel(AppStrings.translate("productinfo.build"));
        buildLabel.setHorizontalAlignment(JLabel.RIGHT);
        JLabel compileDateLabel = new JLabel(AppStrings.translate("productinfo.compileDate"));
        compileDateLabel.setHorizontalAlignment(JLabel.RIGHT);

        c.insets = new Insets(3, 3, 3, 3);

        c.weightx = 1;
        c.weighty = 1;

        c.gridy = 0;

        c.gridx = 0;
        tablePanel.add(productLabel, c);
        c.gridx = 1;
        tablePanel.add(productValueLabel, c);

        c.gridy++;
        c.gridx = 0;
        tablePanel.add(editionLabel, c);
        c.gridx = 1;
        tablePanel.add(editionValueLabel, c);

        c.gridy++;
        c.gridx = 0;
        tablePanel.add(versionLabel, c);
        c.gridx = 1;
        tablePanel.add(versionValueLabel, c);

        c.gridy++;
        c.gridx = 0;
        tablePanel.add(buildLabel, c);
        c.gridx = 1;
        tablePanel.add(buildValueLabel, c);

        c.gridy++;
        c.gridx = 0;
        tablePanel.add(compileDateLabel, c);
        c.gridx = 1;
        tablePanel.add(compileDateValueLabel, c);

        productInfoCard.add(tablePanel);

        return productInfoCard;
    }

    private JPanel createUnknownCard() {
        JPanel unknownCard = new JPanel(new BorderLayout());
        unknownHexView = new HexView();
        unknownCard.add(new FasterScrollPane(unknownHexView), BorderLayout.CENTER);
        unknownCard.add(createUnknownButtonsPanel(), BorderLayout.SOUTH);
        return unknownCard;
    }

    private JPanel createGenericTagCard() {
        JPanel genericTagCard = new JPanel(new BorderLayout());
        genericTagPanel = new GenericTagTreePanel(mainPanel);
        genericTagCard.add(genericTagPanel, BorderLayout.CENTER);
        genericTagCard.add(createGenericTagButtonsPanel(), BorderLayout.SOUTH);
        addGenericListener();
        return genericTagCard;
    }

    private JPanel createDisplayEditTagCard() {
        JPanel displayEditTagCard = new JPanel(new BorderLayout());

        JPanel previewPanel = new JPanel(new BorderLayout());

        JPanel previewCnt = new JPanel(new BorderLayout());
        displayEditImagePanel = new ImagePanel();

        displayEditImagePanel.addPlaceObjectSelectedListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaceObjectTypeTag placeObject = displayEditImagePanel.getPlaceTagUnderCursor();
                if (placeObject != null) {
                    mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), placeObject);
                }
            }
        });

        if (Configuration.editorMode.get()) {
            displayEditImagePanel.addBoundsChangeListener(new BoundsChangeListener() {
                @Override
                public void boundsChanged(Rectangle2D newBounds, Point2D registrationPoint, RegistrationPointPosition registrationPointPosition) {
                    if (displayEditSaveButton.isVisible()) {
                        displayEditSaveButton.setEnabled(true);
                    }
                }
            });
        }

        displayEditImagePanel.addPointUpdateListener(new PointUpdateListener() {
            @Override
            public void pointsUpdated(List<DisplayPoint> points) {

                List<SHAPERECORD> selectedRecords = new ArrayList<>();

                if (displayEditTag instanceof ShapeTag) {
                    ShapeTag shape = (ShapeTag) displayEditTag;
                    selectedRecords = shape.shapes.shapeRecords;
                }
                if (displayEditTag instanceof MorphShapeTag) {
                    MorphShapeTag morphShape = (MorphShapeTag) displayEditTag;
                    if (morphDisplayMode == MORPH_START) {
                        selectedRecords = morphShape.startEdges.shapeRecords;
                    }
                    if (morphDisplayMode == MORPH_END) {
                        selectedRecords = morphShape.endEdges.shapeRecords;
                    }
                }

                int pointsPos = 0;
                int x = 0;
                int y = 0;
                StyleChangeRecord lastStyleChangeRecord = null;
                boolean wasMoveTo = false;
                for (int i = 0; i < selectedRecords.size(); i++) {
                    SHAPERECORD rec = selectedRecords.get(i);
                    if (rec instanceof StyleChangeRecord) {
                        StyleChangeRecord scr = (StyleChangeRecord) rec;
                        lastStyleChangeRecord = scr;
                        if (scr.stateMoveTo) {
                            scr.moveDeltaX = points.get(pointsPos).x;
                            scr.moveDeltaY = points.get(pointsPos).y;
                            scr.calculateBits();
                            pointsPos++;
                            wasMoveTo = true;
                        }
                    }
                    if (((rec instanceof StraightEdgeRecord) || (rec instanceof CurvedEdgeRecord)) && !wasMoveTo) {
                        if (lastStyleChangeRecord != null) {
                            lastStyleChangeRecord.moveDeltaX = points.get(pointsPos).x;
                            lastStyleChangeRecord.moveDeltaY = points.get(pointsPos).y;
                            if (lastStyleChangeRecord.moveDeltaX != 0 || lastStyleChangeRecord.moveDeltaY != 0) {
                                lastStyleChangeRecord.stateMoveTo = true;
                                lastStyleChangeRecord.calculateBits();
                            }
                            pointsPos++;
                            wasMoveTo = true;
                        }
                    }
                    if (rec instanceof StraightEdgeRecord) {
                        StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                        ser.generalLineFlag = true;
                        ser.deltaX = points.get(pointsPos).x - x;
                        ser.deltaY = points.get(pointsPos).y - y;
                        ser.simplify();
                        ser.calculateBits();
                        pointsPos += 1;
                    }
                    if (rec instanceof CurvedEdgeRecord) {
                        CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                        cer.controlDeltaX = points.get(pointsPos).x - x;
                        cer.controlDeltaY = points.get(pointsPos).y - y;
                        cer.anchorDeltaX = points.get(pointsPos + 1).x - points.get(pointsPos).x;
                        cer.anchorDeltaY = points.get(pointsPos + 1).y - points.get(pointsPos).y;
                        cer.calculateBits();
                        pointsPos += 2;
                    }
                    x = rec.changeX(x);
                    y = rec.changeY(y);
                }
                if (displayEditTag instanceof ShapeTag) {
                    ShapeTag shape = (ShapeTag) displayEditTag;
                    shape.updateBounds();
                }
                if (displayEditTag instanceof MorphShapeTag) {
                    MorphShapeTag morphShape = (MorphShapeTag) displayEditTag;
                    if (morphDisplayMode == MORPH_START) {
                        morphShape.updateStartBounds();
                    }
                    if (morphDisplayMode == MORPH_END) {
                        morphShape.updateEndBounds();
                    }
                }
                displayEditTag.getSwf().clearShapeCache();
                displayEditImagePanel.repaint();
            }

            @Override
            public boolean edgeSplit(int position, double splitPoint) {

                List<SHAPERECORD> selectedRecords = new ArrayList<>();
                List<SHAPERECORD> otherRecords = null;

                if (displayEditTag instanceof ShapeTag) {
                    ShapeTag shape = (ShapeTag) displayEditTag;
                    selectedRecords = shape.shapes.shapeRecords;
                }
                if (displayEditTag instanceof MorphShapeTag) {
                    MorphShapeTag morphShape = (MorphShapeTag) displayEditTag;
                    if (morphDisplayMode == MORPH_START) {
                        selectedRecords = morphShape.startEdges.shapeRecords;
                        otherRecords = morphShape.endEdges.shapeRecords;
                    }
                    if (morphDisplayMode == MORPH_END) {
                        selectedRecords = morphShape.endEdges.shapeRecords;
                        otherRecords = morphShape.startEdges.shapeRecords;
                    }
                }

                Reference<Integer> importantRecordPosRef = new Reference<>(0);

                if (splitRecords(importantRecordPosRef, selectedRecords, position, splitPoint) && otherRecords != null) {
                    int importantRecordPos = importantRecordPosRef.getVal();
                    int otherPosition = 0;
                    int otherImportantRecordPos = 0;
                    boolean wasMoveTo = false;
                    StyleChangeRecord lastStyleChangeRecord = null;
                    for (int i = 0; i < otherRecords.size(); i++) {
                        SHAPERECORD rec = otherRecords.get(i);
                        if (rec instanceof StyleChangeRecord) {
                            StyleChangeRecord scr = (StyleChangeRecord) rec;
                            lastStyleChangeRecord = scr;
                            if (scr.stateMoveTo) {
                                otherPosition++;
                                otherImportantRecordPos++;
                                wasMoveTo = true;
                            }
                        }

                        if (((rec instanceof StraightEdgeRecord) || (rec instanceof CurvedEdgeRecord)) && !wasMoveTo) {
                            if (lastStyleChangeRecord != null) {
                                otherPosition++;
                                otherImportantRecordPos++;
                                wasMoveTo = true;
                            }
                        }
                        if (rec instanceof StraightEdgeRecord) {
                            otherPosition++;
                            otherImportantRecordPos++;
                        }

                        if (rec instanceof CurvedEdgeRecord) {
                            otherPosition += 2;
                            otherImportantRecordPos++;
                        }
                        if (otherImportantRecordPos == importantRecordPos) {
                            break;
                        }
                    }
                    splitRecords(importantRecordPosRef, otherRecords, otherPosition, splitPoint);
                }
                refreshHilightedPoints();
                clearCache();
                displayEditImagePanel.repaint();
                return false;
            }

            private boolean splitRecords(Reference<Integer> importantRecordPosRef, List<SHAPERECORD> selectedRecords, int position, double splitPoint) {
                int pointsPos = 0;
                int x = 0;
                int y = 0;
                int importantRecordPos = 0;
                boolean wasMoveTo = false;
                StyleChangeRecord lastStyleChangeRecord = null;
                for (int i = 0; i < selectedRecords.size(); i++) {
                    SHAPERECORD rec = selectedRecords.get(i);
                    if (rec instanceof StyleChangeRecord) {
                        StyleChangeRecord scr = (StyleChangeRecord) rec;
                        lastStyleChangeRecord = scr;
                        if (scr.stateMoveTo) {
                            pointsPos++;
                            importantRecordPos++;
                            wasMoveTo = true;
                        }
                    }
                    if (((rec instanceof StraightEdgeRecord) || (rec instanceof CurvedEdgeRecord)) && !wasMoveTo) {
                        if (lastStyleChangeRecord != null) {
                            pointsPos++;
                            importantRecordPos++;
                            wasMoveTo = true;
                        }
                    }
                    if (rec instanceof StraightEdgeRecord) {
                        StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                        if (pointsPos == position) {
                            StraightEdgeRecord newSer = new StraightEdgeRecord();
                            newSer.generalLineFlag = true;
                            newSer.deltaX = (int) Math.round(ser.deltaX * (1 - splitPoint));
                            newSer.deltaY = (int) Math.round(ser.deltaY * (1 - splitPoint));
                            newSer.simplify();
                            ser.generalLineFlag = true;
                            ser.deltaX -= newSer.deltaX;
                            ser.deltaY -= newSer.deltaY;
                            ser.simplify();
                            selectedRecords.add(i + 1, newSer);
                            importantRecordPosRef.setVal(importantRecordPos);
                            return true;
                        }
                        pointsPos += 1;
                        importantRecordPos++;
                        ser.simplify();
                    }
                    if (rec instanceof CurvedEdgeRecord) {
                        CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                        if (pointsPos == position) {
                            Point2D p0 = new Point2D.Double(x, y);
                            Point2D p1 = new Point2D.Double(x + cer.controlDeltaX, y + cer.controlDeltaY);
                            Point2D p2 = new Point2D.Double(x + cer.controlDeltaX + cer.anchorDeltaX, y + cer.controlDeltaY + cer.anchorDeltaY);
                            List<Point2D> v = new ArrayList<>();
                            v.add(p0);
                            v.add(p1);
                            v.add(p2);
                            BezierUtils bu = new BezierUtils();
                            List<Point2D> left = new ArrayList<>();
                            List<Point2D> right = new ArrayList<>();
                            bu.subdivide(v, splitPoint, left, right);
                            cer.controlDeltaX = (int) Math.round(left.get(1).getX() - left.get(0).getX());
                            cer.controlDeltaY = (int) Math.round(left.get(1).getY() - left.get(0).getY());
                            cer.anchorDeltaX = (int) Math.round(left.get(2).getX() - left.get(1).getX());
                            cer.anchorDeltaY = (int) Math.round(left.get(2).getY() - left.get(1).getY());

                            cer.calculateBits();

                            CurvedEdgeRecord newCer = new CurvedEdgeRecord();
                            newCer.controlDeltaX = (int) Math.round(right.get(1).getX() - right.get(0).getX());
                            newCer.controlDeltaY = (int) Math.round(right.get(1).getY() - right.get(0).getY());
                            newCer.anchorDeltaX = (int) Math.round(right.get(2).getX() - right.get(1).getX());
                            newCer.anchorDeltaY = (int) Math.round(right.get(2).getY() - right.get(1).getY());
                            selectedRecords.add(i + 1, newCer);
                            importantRecordPosRef.setVal(importantRecordPos);
                            newCer.calculateBits();
                            return true;
                        }
                        pointsPos += 2;
                        importantRecordPos++;
                    }
                    x = rec.changeX(x);
                    y = rec.changeY(y);
                }
                return false;
            }

            private void clearCache() {
                if (displayEditTag instanceof ShapeTag) {
                    ShapeTag shape = (ShapeTag) displayEditTag;
                    shape.shapes.clearCachedOutline();
                }
                displayEditTag.getSwf().clearShapeCache();
            }

            @Override
            public boolean pointRemoved(int position) {

                List<SHAPERECORD> selectedRecords = new ArrayList<>();
                List<SHAPERECORD> otherRecords = null;

                if (displayEditTag instanceof ShapeTag) {
                    ShapeTag shape = (ShapeTag) displayEditTag;
                    selectedRecords = shape.shapes.shapeRecords;
                }
                if (displayEditTag instanceof MorphShapeTag) {
                    MorphShapeTag morphShape = (MorphShapeTag) displayEditTag;
                    if (morphDisplayMode == MORPH_START) {
                        selectedRecords = morphShape.startEdges.shapeRecords;
                        otherRecords = morphShape.endEdges.shapeRecords;
                    }
                    if (morphDisplayMode == MORPH_END) {
                        selectedRecords = morphShape.endEdges.shapeRecords;
                        otherRecords = morphShape.startEdges.shapeRecords;
                    }
                }

                Reference<Integer> importantRecordPosRef = new Reference<>(0);
                if (removePoint(importantRecordPosRef, selectedRecords, position) && otherRecords != null) {
                    int importantRecordPos = importantRecordPosRef.getVal();
                    int otherPosition = 0;
                    int otherImportantRecordPos = 0;
                    StyleChangeRecord lastStyleChangeRecord = null;
                    boolean wasMoveTo = false;
                    for (int i = 0; i < otherRecords.size(); i++) {
                        SHAPERECORD rec = otherRecords.get(i);
                        if (rec instanceof StyleChangeRecord) {
                            StyleChangeRecord scr = (StyleChangeRecord) rec;
                            lastStyleChangeRecord = scr;
                            if (scr.stateMoveTo) {
                                otherPosition++;
                                otherImportantRecordPos++;
                                wasMoveTo = true;
                            }
                        }
                        if (((rec instanceof StraightEdgeRecord) || (rec instanceof CurvedEdgeRecord)) && !wasMoveTo) {
                            if (lastStyleChangeRecord != null) {
                                otherPosition++;
                                otherImportantRecordPos++;
                                wasMoveTo = true;
                            }
                        }
                        if (rec instanceof StraightEdgeRecord) {
                            otherPosition++;
                            otherImportantRecordPos++;
                        }

                        if (rec instanceof CurvedEdgeRecord) {
                            otherPosition += 2;
                            otherImportantRecordPos++;
                        }
                        if (otherImportantRecordPos == importantRecordPos) {
                            break;
                        }
                    }
                    removePoint(importantRecordPosRef, otherRecords, otherPosition);
                }
                refreshHilightedPoints();
                clearCache();
                displayEditImagePanel.repaint();
                return true;
            }

            private boolean removePoint(Reference<Integer> importantRecordPosRef, List<SHAPERECORD> selectedRecords, int position) {
                int pointsPos = 0;
                int importantRecordPos = 0;
                int x = 0;
                int y = 0;

                StyleChangeRecord lastStyleChangeRecord = null;
                boolean wasMoveTo = false;
                for (int i = 0; i < selectedRecords.size(); i++) {
                    SHAPERECORD rec = selectedRecords.get(i);
                    SHAPERECORD prevRec = i == 0 ? null : selectedRecords.get(i - 1);
                    SHAPERECORD nextRec = i + 1 < selectedRecords.size() ? selectedRecords.get(i + 1) : null;

                    if (rec instanceof StyleChangeRecord) {
                        StyleChangeRecord scr = (StyleChangeRecord) rec;
                        lastStyleChangeRecord = scr;
                        if (scr.stateMoveTo) {
                            pointsPos++;
                            importantRecordPos++;
                            wasMoveTo = true;
                        }
                    }
                    if (((rec instanceof StraightEdgeRecord) || (rec instanceof CurvedEdgeRecord)) && !wasMoveTo) {
                        if (lastStyleChangeRecord != null) {
                            pointsPos++;
                            importantRecordPos++;
                            wasMoveTo = true;
                        }
                    }
                    if (rec instanceof StraightEdgeRecord) {
                        StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                        if (pointsPos == position) {
                            if (nextRec instanceof StraightEdgeRecord) {
                                StraightEdgeRecord nextSer = (StraightEdgeRecord) nextRec;
                                nextSer.generalLineFlag = true;
                                nextSer.deltaX += ser.deltaX;
                                nextSer.deltaY += ser.deltaY;
                                selectedRecords.remove(i);

                                importantRecordPosRef.setVal(importantRecordPos);
                                return true;
                            }
                            if (nextRec instanceof CurvedEdgeRecord) {
                                CurvedEdgeRecord cer = (CurvedEdgeRecord) nextRec;
                                ser.generalLineFlag = true;
                                ser.deltaX += cer.controlDeltaX + cer.anchorDeltaX;
                                ser.deltaY += cer.controlDeltaY + cer.anchorDeltaY;
                                selectedRecords.remove(i + 1);
                                importantRecordPosRef.setVal(importantRecordPos);
                                return true;
                            }
                        }
                        pointsPos += 1;
                        ser.simplify();
                        importantRecordPos++;
                    }
                    if (rec instanceof CurvedEdgeRecord) {
                        CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                        //delete control point -> make it straight edge
                        if (pointsPos == position) {
                            StraightEdgeRecord ser = new StraightEdgeRecord();
                            ser.generalLineFlag = true;
                            ser.deltaX = cer.controlDeltaX + cer.anchorDeltaX;
                            ser.deltaY = cer.controlDeltaY + cer.anchorDeltaY;
                            ser.simplify();
                            selectedRecords.set(i, ser);
                            //No need to update otherRecords
                            importantRecordPosRef.setVal(importantRecordPos);
                            return false;
                        }
                        if (position == pointsPos + 1) {
                            if (nextRec instanceof CurvedEdgeRecord) {
                                CurvedEdgeRecord nextCer = (CurvedEdgeRecord) nextRec;
                                StraightEdgeRecord ser = new StraightEdgeRecord();
                                ser.generalLineFlag = true;
                                ser.deltaX = cer.controlDeltaX + cer.anchorDeltaX + nextCer.controlDeltaX + nextCer.anchorDeltaX;
                                ser.deltaY = cer.controlDeltaY + cer.anchorDeltaY + nextCer.controlDeltaY + nextCer.anchorDeltaY;
                                ser.simplify();
                                selectedRecords.set(i, ser);
                                selectedRecords.remove(i + 1);
                                importantRecordPosRef.setVal(importantRecordPos);
                                return true;
                            }
                            if (nextRec instanceof StraightEdgeRecord) {
                                StraightEdgeRecord nextSer = (StraightEdgeRecord) nextRec;
                                nextSer.generalLineFlag = true;
                                nextSer.deltaX += cer.controlDeltaX + cer.anchorDeltaX;
                                nextSer.deltaY += cer.controlDeltaY + cer.anchorDeltaY;
                                nextSer.simplify();
                                selectedRecords.remove(i);
                                importantRecordPosRef.setVal(importantRecordPos);
                                return true;
                            }
                        }
                        pointsPos += 2;
                        importantRecordPos++;
                    }
                    x = rec.changeX(x);
                    y = rec.changeY(y);
                }
                return false;
            }

        });

        displayEditTransformPanel = new TransformPanel(displayEditImagePanel);
        //imagePanel.setLoop(Configuration.loopMedia.get());
        previewCnt.add(displayEditTransformSplitPane = new JPersistentSplitPane(
                JPersistentSplitPane.HORIZONTAL_SPLIT,
                displayEditImagePanel,
                displayEditTransformScrollPane = new FasterScrollPane(displayEditTransformPanel),
                Configuration.guiSplitPaneTransform1DividerLocationPercent));
        PlayerControls placeImagePlayControls = new PlayerControls(mainPanel, displayEditImagePanel, null);
        previewCnt.add(placeImagePlayControls, BorderLayout.SOUTH);
        Dimension transDimension = displayEditTransformPanel.getPreferredSize();
        displayEditTransformScrollPane.setPreferredSize(new Dimension(transDimension.width + UIManager.getInt("ScrollBar.width") + 2, transDimension.height));
        displayEditTransformScrollPane.setVisible(false);
        placeImagePlayControls.setMedia(displayEditImagePanel);
        previewPanel.add(previewCnt, BorderLayout.CENTER);
        JLabel prevIntLabel = new HeaderLabel(mainPanel.translate("swfpreview.internal"));
        prevIntLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewPanel.add(prevIntLabel, BorderLayout.NORTH);

        displayEditGenericPanel = new GenericTagTreePanel(mainPanel);
        addPlaceGenericListener();
        displayEditSplitPane = new JPersistentSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, displayEditGenericPanel, Configuration.guiSplitPanePlaceDividerLocationPercent);

        displayEditTagCard.add(displayEditSplitPane, BorderLayout.CENTER);
        //placeSplitPane.setDividerLocation(800);
        displayEditTagCard.add(createDisplayEditTagButtonsPanel(), BorderLayout.SOUTH);

        ((GenericTagTreePanel) displayEditGenericPanel).addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (e.getNewLeadSelectionPath() == null) {
                    displayEditImagePanel.setStatus("");
                    displayEditImagePanel.setHilightedEdge(null);
                    return;
                }
                JTree tree = (JTree) e.getSource();
                Object obj = e.getPath().getLastPathComponent();
                if (obj instanceof GenericTagTreePanel.FieldNode) {
                    GenericTagTreePanel.FieldNode fieldNode = (GenericTagTreePanel.FieldNode) obj;
                    Object val = fieldNode.getValue(0);
                    if (val instanceof SHAPERECORD) {
                        Object parent = fieldNode.getParentObject();
                        if (parent == null) {
                            return;
                        }
                        int x = 0;
                        int y = 0;
                        TreeModel model = tree.getModel();
                        int fillStyle0 = 0;
                        int fillStyle1 = 0;
                        int lineStyle = 0;
                        int stylesIndex = -1;
                        for (int i = 0; i < model.getChildCount(parent); i++) {
                            Object child = model.getChild(parent, i);
                            GenericTagTreePanel.FieldNode childFN = (GenericTagTreePanel.FieldNode) child;
                            SHAPERECORD rec = (SHAPERECORD) childFN.getValue(0);
                            if (rec instanceof StyleChangeRecord) {
                                StyleChangeRecord scr = (StyleChangeRecord) rec;
                                if (scr.stateNewStyles) {
                                    fillStyle0 = 0;
                                    fillStyle1 = 0;
                                    lineStyle = 0;
                                    stylesIndex++;
                                }
                                if (scr.stateFillStyle0) {
                                    fillStyle0 = scr.fillStyle0;
                                }
                                if (scr.stateFillStyle1) {
                                    fillStyle1 = scr.fillStyle1;
                                }
                                if (scr.stateLineStyle) {
                                    lineStyle = scr.lineStyle;
                                }
                            }
                            if (rec == val) {
                                String edgeStatus = "";
                                if (rec instanceof StraightEdgeRecord) {
                                    StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                                    Point point1 = new Point(x, y);
                                    Point point2 = new Point(x + ser.deltaX, y + ser.deltaY);
                                    Point[] hilightedPoint = new Point[]{point1, point2};
                                    displayEditImagePanel.setHilightedEdge(hilightedPoint);
                                    edgeStatus = AppStrings.translate("shaperecords.edge.straight")
                                            .replace("%x1%", "" + point1.x)
                                            .replace("%y1%", "" + point1.y)
                                            .replace("%x2%", "" + point2.x)
                                            .replace("%y2%", "" + point2.y);
                                } else if (rec instanceof CurvedEdgeRecord) {
                                    CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                                    Point point1 = new Point(x, y);
                                    Point point2 = new Point(x + cer.controlDeltaX, y + cer.controlDeltaY);
                                    Point point3 = new Point(x + cer.controlDeltaX + cer.anchorDeltaX, y + cer.controlDeltaY + cer.anchorDeltaY);
                                    Point[] hilightedPoint = new Point[]{point1, point2, point3};
                                    displayEditImagePanel.setHilightedEdge(hilightedPoint);
                                    edgeStatus = AppStrings.translate("shaperecords.edge.curved")
                                            .replace("%x1%", "" + point1.x)
                                            .replace("%y1%", "" + point1.y)
                                            .replace("%x2%", "" + point2.x)
                                            .replace("%y2%", "" + point2.y)
                                            .replace("%x3%", "" + point3.x)
                                            .replace("%y3%", "" + point3.y);
                                } else if (rec instanceof StyleChangeRecord) {
                                    StyleChangeRecord scr = (StyleChangeRecord) rec;
                                    List<String> styleStatusParts = new ArrayList<>();
                                    if (scr.stateMoveTo) {
                                        Point point1 = new Point(scr.moveDeltaX, scr.moveDeltaY);
                                        Point[] hilightedPoint = new Point[]{point1};
                                        displayEditImagePanel.setHilightedEdge(hilightedPoint);
                                        styleStatusParts.add(AppStrings.translate("shaperecords.edge.style.move")
                                                .replace("%x%", "" + point1.x)
                                                .replace("%y%", "" + point1.y));
                                    } else {
                                        Point point1 = new Point(x, y);
                                        Point[] hilightedPoint = new Point[]{point1};
                                        displayEditImagePanel.setHilightedEdge(hilightedPoint);
                                    }
                                    if (scr.stateNewStyles) {
                                        int shapeNum = 0;
                                        if (displayEditTag instanceof ShapeTag) {
                                            shapeNum = ((ShapeTag) displayEditTag).getShapeNum();
                                        }
                                        if (displayEditTag instanceof MorphShapeTag) {
                                            shapeNum = ((MorphShapeTag) displayEditTag).getShapeNum();
                                            if (shapeNum == 2) {
                                                shapeNum = 3;
                                            } else {
                                                shapeNum = 1;
                                            }
                                        }
                                        styleStatusParts.add(AppStrings.translate("shaperecords.edge.style.newstyles")
                                                .replace("%numfillstyles%", "" + scr.fillStyles.fillStyles.length)
                                                .replace("%numlinestyles%", "" + (shapeNum < 3 ? scr.lineStyles.lineStyles.length : scr.lineStyles.lineStyles2.length))
                                        );
                                    }
                                    if (scr.stateFillStyle0) {
                                        styleStatusParts.add(AppStrings.translate("shaperecords.edge.style.fillstyle0")
                                                .replace("%value%", "" + scr.fillStyle0));
                                    }
                                    if (scr.stateFillStyle1) {
                                        styleStatusParts.add(AppStrings.translate("shaperecords.edge.style.fillstyle1")
                                                .replace("%value%", "" + scr.fillStyle1));
                                    }
                                    String styleDetails = String.join(", ", styleStatusParts);
                                    edgeStatus = AppStrings.translate("shaperecords.edge.style").replace("%details%", styleDetails);
                                } else if (rec instanceof EndShapeRecord) {
                                    Point point1 = new Point(x, y);
                                    Point[] hilightedPoint = new Point[]{point1};
                                    displayEditImagePanel.setHilightedEdge(hilightedPoint);
                                    edgeStatus = AppStrings.translate("shaperecords.edge.end");
                                } else {
                                    displayEditImagePanel.setHilightedEdge(null);
                                    displayEditImagePanel.setStatus("");
                                    break;
                                }

                                String status = AppStrings.translate("shaperecords.status")
                                        .replace("%fillstyle0%", "" + fillStyle0)
                                        .replace("%fillstyle1%", "" + fillStyle1)
                                        .replace("%linestyle%", "" + lineStyle)
                                        .replace("%stylesindex%", "" + stylesIndex)
                                        .replace("%edge%", edgeStatus);
                                displayEditImagePanel.setStatus(status);
                                break;
                            }
                            x = rec.changeX(x);
                            y = rec.changeY(y);
                        }
                    } else {
                        displayEditImagePanel.setStatus("");
                        displayEditImagePanel.setHilightedEdge(null);
                    }

                } else {
                    displayEditImagePanel.setStatus("");
                    displayEditImagePanel.setHilightedEdge(null);
                }
            }
        });

        return displayEditTagCard;
    }

    private JPanel createDisplayEditTagButtonsPanel() {

        displayEditTransformButton = new JButton(mainPanel.translate("button.transform"), View.getIcon("freetransform16"));
        displayEditTransformButton.setMargin(new Insets(3, 3, 3, 10));
        displayEditTransformButton.addActionListener(this::transformDisplayEditTagButtonActionPerformed);
        displayEditEditButton = new JButton(mainPanel.translate("button.edit"), View.getIcon("edit16"));
        displayEditEditButton.setMargin(new Insets(3, 3, 3, 10));
        displayEditEditButton.addActionListener(this::editDisplayEditTagButtonActionPerformed);
        displayEditSaveButton = new JButton(mainPanel.translate("button.save"), View.getIcon("save16"));
        displayEditSaveButton.setMargin(new Insets(3, 3, 3, 10));
        displayEditSaveButton.addActionListener(this::saveDisplayEditTagButtonActionPerformed);
        displayEditCancelButton = new JButton(mainPanel.translate("button.cancel"), View.getIcon("cancel16"));
        displayEditCancelButton.setMargin(new Insets(3, 3, 3, 10));
        displayEditCancelButton.addActionListener(this::cancelDisplayEditTagButtonActionPerformed);

        displayEditEditPointsButton = new JButton(mainPanel.translate("button.edit.points"), View.getIcon("pointsedit16"));
        displayEditEditPointsButton.setMargin(new Insets(3, 3, 3, 10));
        displayEditEditPointsButton.addActionListener(this::editPointsDisplayEditTagButtonActionPerformed);

        displayEditShowAnimationButton = new JToggleButton(mainPanel.translate("button.morph.animation"));
        displayEditShowAnimationButton.setMargin(new Insets(3, 3, 3, 10));
        displayEditShowAnimationButton.addActionListener(this::showAnimationDisplayEditTagButtonActionPerformed);

        displayEditShowStartButton = new JToggleButton(mainPanel.translate("button.morph.start"));
        displayEditShowStartButton.setMargin(new Insets(3, 3, 3, 10));
        displayEditShowStartButton.addActionListener(this::showStartDisplayEditTagButtonActionPerformed);

        displayEditShowEndButton = new JToggleButton(mainPanel.translate("button.morph.end"));
        displayEditShowEndButton.setMargin(new Insets(3, 3, 3, 10));
        displayEditShowEndButton.addActionListener(this::showEndDisplayEditTagButtonActionPerformed);

        morphShowSpace = Box.createHorizontalStrut(10);

        morphShowPanel = new JPanel(new FlowLayout());
        morphShowPanel.add(morphShowSpace);
        morphShowPanel.add(new JLabel(mainPanel.translate("button.morph.show")));
        morphShowPanel.add(displayEditShowAnimationButton);
        morphShowPanel.add(displayEditShowStartButton);
        morphShowPanel.add(displayEditShowEndButton);

        ButtonGroup morphGroup = new ButtonGroup();
        morphGroup.add(displayEditShowAnimationButton);
        morphGroup.add(displayEditShowStartButton);
        morphGroup.add(displayEditShowEndButton);

        morphShowPanel.setVisible(false);

        replaceShapeButton = new JButton(mainPanel.translate("button.replace"), View.getIcon("importshape16"));
        replaceShapeButton.setMargin(new Insets(3, 3, 3, 10));
        replaceShapeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceButtonActionPerformed(mainPanel.getCurrentTree().getSelected());
            }
        });
        replaceShapeButton.setVisible(false);

        replaceMorphShapeButton = new JButton(mainPanel.translate("button.replace"), View.getIcon("importmorphshape16"));
        replaceMorphShapeButton.setMargin(new Insets(3, 3, 3, 10));
        replaceMorphShapeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceMorphShape((MorphShapeTag) mainPanel.getCurrentTree().getCurrentTreeItem(), false, true);
            }
        });
        replaceMorphShapeButton.setVisible(false);

        replaceMorphShapeUpdateBoundsButton = new JButton(mainPanel.translate("button.replaceNoFill"), View.getIcon("importmorphshape16"));
        replaceMorphShapeUpdateBoundsButton.setMargin(new Insets(3, 3, 3, 10));
        replaceMorphShapeUpdateBoundsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceMorphShape((MorphShapeTag) mainPanel.getCurrentTree().getCurrentTreeItem(), false, false);
            }
        });
        replaceMorphShapeUpdateBoundsButton.setVisible(false);

        replaceShapeUpdateBoundsButton = new JButton(mainPanel.translate("button.replaceNoFill"), View.getIcon("importshape16"));
        replaceShapeUpdateBoundsButton.setMargin(new Insets(3, 3, 3, 10));
        replaceShapeUpdateBoundsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceNoFill(mainPanel.getCurrentTree().getCurrentTreeItem());
            }
        });
        replaceShapeUpdateBoundsButton.setVisible(false);

        if (Configuration.editorMode.get()) {
            displayEditEditButton.setVisible(false);
            displayEditSaveButton.setVisible(true);
            displayEditSaveButton.setEnabled(false);
            displayEditCancelButton.setVisible(true);
            displayEditCancelButton.setEnabled(false);
        } else {
            displayEditEditButton.setVisible(true);
            displayEditSaveButton.setVisible(false);
            displayEditCancelButton.setVisible(false);
        }

        /*JButton fixPathsButton = new JButton("Fix paths");
        fixPathsButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                ShapeTag shape = (ShapeTag) displayEditTag;
                ShapeFixer fixer = new ShapeFixer();
                List<Point> newPoints1 = new ArrayList<>();
                List<Point> newPoints2 = new ArrayList<>();
                List<ShapeRecordAdvanced> shapeRecordsAdvanced = new ArrayList<>();
                for (SHAPERECORD rec:shape.shapes.shapeRecords) {
                    ShapeRecordAdvanced arec = ShapeRecordAdvanced.createFromSHAPERECORD(rec);
                    if (arec != null) {
                        shapeRecordsAdvanced.add(arec);
                    }
                }
                List<ShapeRecordAdvanced> fixed = fixer.fixShape(shapeRecordsAdvanced);
                
                List<SHAPERECORD> newRecords=new ArrayList<>();
                
                for (ShapeRecordAdvanced arec:fixed) {
                    newRecords.add(arec.toBasicRecord());
                }
                newRecords.add(new EndShapeRecord());
                shape.shapes.shapeRecords = newRecords;
                //displayEditImagePanel.setShowPoints(newPoints1, newPoints2);
                displayEditTag.getSwf().clearShapeCache();
                displayEditImagePanel.repaint();
                refreshHilightedPoints();
            }            
        });*/
        ButtonsPanel displayEditButtonsPanel = new ButtonsPanel();
        displayEditButtonsPanel.add(displayEditTransformButton);
        displayEditButtonsPanel.add(displayEditEditButton);
        displayEditButtonsPanel.add(displayEditSaveButton);
        displayEditButtonsPanel.add(displayEditCancelButton);
        displayEditButtonsPanel.add(displayEditEditPointsButton);
        //displayEditButtonsPanel.add(fixPathsButton);
        displayEditButtonsPanel.add(replaceShapeButton);
        displayEditButtonsPanel.add(replaceShapeUpdateBoundsButton);
        displayEditButtonsPanel.add(replaceMorphShapeButton);
        displayEditButtonsPanel.add(replaceMorphShapeUpdateBoundsButton);
        displayEditButtonsPanel.add(morphShowPanel);
        return displayEditButtonsPanel;
    }

    private void showCardLeft(String card) {
        CardLayout cl = (CardLayout) (viewerCards.getLayout());
        cl.show(viewerCards, card);

        //stop sounds when switching panels
        imagePanel.stop();
        displayEditImagePanel.stop();
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

    public void showImagePanel(Timelined timelined, SWF swf, int frame, boolean showObjectsUnderCursor, boolean autoPlay, boolean frozen, boolean alwaysDisplay, boolean muted, boolean mutable, boolean allowFreeTransform, boolean allowZoom, boolean frozenButtons, boolean canHaveRuler) {
        showCardLeft(DRAW_PREVIEW_CARD);
        parametersPanel.setVisible(false);
        imagePlayControls.setMedia(imagePanel);
        imageTransformButton.setVisible(allowFreeTransform);
        if ((timelined instanceof Tag) && ((Tag) timelined).isReadOnly()) {
            imageTransformButton.setVisible(false);
        }
        imageTransformSaveButton.setVisible(false);
        imageTransformCancelButton.setVisible(false);
        imagePanel.setTimelined(timelined, swf, frame, showObjectsUnderCursor, autoPlay, frozen, alwaysDisplay, muted, mutable, allowZoom, frozenButtons, canHaveRuler);
        if (canHaveRuler) {
            if (timelined instanceof Tag) {
                imagePanel.setGuidesCharacter(swf, ((CharacterTag) timelined).getCharacterId());
            } else {
                imagePanel.setGuidesCharacter(swf, -1);
            }
        }
    }

    public void showImagePanel(SerializableImage image) {
        showCardLeft(DRAW_PREVIEW_CARD);
        imageTransformButton.setVisible(false);
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
        showImagePanel(TimelinedMaker.makeTimelined(fontTag), fontTag.getSwf(), fontPageNum, true, true, true, true, true, false, false, false, true, false);
    }

    public static int getFontPageCount(FontTag fontTag) {
        int pageCount = (fontTag.getGlyphShapeTable().size() - 1) / SHAPERECORD.MAX_CHARACTERS_IN_FONT_PREVIEW + 1;
        if (pageCount < 1) {
            pageCount = 1;
        }
        return pageCount;
    }

    public void showEmpty() {
        setParametersPanelVisible(false);
        showCardLeft(EMPTY_CARD);
    }

    public void showTextPanel(TextTag textTag) {
        showImagePanel(TimelinedMaker.makeTimelined(textTag), textTag.getSwf(), 0, true, true, true, true, true, false, false, true, true, true);

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
        displayEditImagePanel.clearAll();
        if (media != null) {
            try {
                media.close();
            } catch (IOException ex) {
                // ignore
            }
        }

        binaryPanel.setBinaryData(null);
        genericTagPanel.clear();
        displayEditGenericPanel.clear();
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

    private void readCookie() {
        try (FileInputStream fis = new FileInputStream(cookie.getSolFile())) {
            SolFile solFile = new SolFile(fis);
            switch (solFile.getAmfVersion()) {
                case 0:
                    cookieEditor.setText(Amf0Exporter.amfMapToString(solFile.getAmfValues(), 0, "\r\n"));
                    break;
                case 3:
                    cookieEditor.setText(Amf3Exporter.amfMapToString(solFile.getAmfValues(), "  ", "\r\n", 0));
                    break;
            }
            cookieFilenameField.setText(solFile.getFileName());
            amfVersionLabel.setText("" + solFile.getAmfVersion());
        } catch (Exception ex) {
            cookieEditor.setText("//Error: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    public void showCookiePanel(Cookie cookie) {
        showCardLeft(COOKIE_CARD);
        this.cookie = cookie;
        cookieEditor.setEditable(!readOnly && Configuration.editorMode.get());
        cookieFilenameField.setEditable(!readOnly && Configuration.editorMode.get());
        readCookie();
        cookieEditor.getDocument().addDocumentListener(cookieDocumentListener);
        cookieFilenameField.getDocument().addDocumentListener(cookieDocumentListener);
        setCookieModified(false);
        updateCookieButtonsVisibility();
        parametersPanel.setVisible(false);
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

    public void showBinaryPanel(BinaryDataInterface binaryData) {
        showCardLeft(BINARY_TAG_CARD);
        binaryPanel.setBinaryData(binaryData);
        parametersPanel.setVisible(false);
    }

    public void showProductInfoPanel(ProductInfoTag productInfoTag) {
        showCardLeft(PRODUCTINFO_TAG_CARD);
        if (productInfoTag.productID == 0L) {
            productValueLabel.setText(AppStrings.translate("productinfo.product.unknown"));
        } else if (productInfoTag.productID == 1L) {
            productValueLabel.setText("Macromedia Flex for J2EE");
        } else if (productInfoTag.productID == 2L) {
            productValueLabel.setText("Macromedia Flex for .NET");
        } else if (productInfoTag.productID == 3L) {
            productValueLabel.setText("Apache/Adobe Flex");
        } else {
            productValueLabel.setText("(" + productInfoTag.productID + ")");
        }

        if (productInfoTag.edition == 0L) {
            editionValueLabel.setText("Developer Edition");
        } else if (productInfoTag.edition == 1L) {
            editionValueLabel.setText("Full Commercial Edition");
        } else if (productInfoTag.edition == 2L) {
            editionValueLabel.setText("Non Commercial Edition");
        } else if (productInfoTag.edition == 3L) {
            editionValueLabel.setText("Educational Edition");
        } else if (productInfoTag.edition == 4L) {
            editionValueLabel.setText("Not For Resale (NFR) Edition");
        } else if (productInfoTag.edition == 5L) {
            editionValueLabel.setText("Trial Edition");
        } else if (productInfoTag.edition == 6L) {
            editionValueLabel.setText(AppStrings.translate("productinfo.edition.none"));
        } else {
            editionValueLabel.setText("(" + productInfoTag.productID + ")");
        }

        versionValueLabel.setText("" + productInfoTag.majorVersion + "." + productInfoTag.minorVersion);
        BigInteger buildBigInteger = new BigInteger("" + productInfoTag.buildHigh);
        buildBigInteger = buildBigInteger.shiftLeft(32).add(new BigInteger("" + productInfoTag.buildLow));
        buildValueLabel.setText("" + buildBigInteger);

        long compilationDate = (productInfoTag.compilationDateHigh << 32) + productInfoTag.compilationDateLow;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        compileDateValueLabel.setText(df.format(new Date(compilationDate)) + " UTC");
        parametersPanel.setVisible(false);
    }

    public void showUnknownPanel(UnknownTag unknownTag) {
        showCardLeft(UNKNOWN_TAG_CARD);
        if (unknownTag == null) {
            unknownHexView.setData(new byte[0], null, null);
        } else {
            unknownHexView.setData(unknownTag.unknownData.getRangeData(), null, null);
        }
        unknownHexView.repaint();
        parametersPanel.setVisible(false);
    }

    private void addPlaceGenericListener() {
        ((GenericTagTreePanel) displayEditGenericPanel).addTreeModelListener(new TreeModelListener() {
            private void changed() {
                displayEditSaveButton.setEnabled(true);
                displayEditCancelButton.setEnabled(true);
                displayEditTransformButton.setVisible(false);
                mainPanel.setEditingStatus();
                if (Configuration.editorMode.get()) {
                    displayEditEditPointsButton.setVisible(false);
                    displayEditTransformButton.setVisible(false);
                    replaceShapeButton.setVisible(false);
                    replaceShapeUpdateBoundsButton.setVisible(false);
                    replaceMorphShapeButton.setVisible(false);
                    replaceMorphShapeUpdateBoundsButton.setVisible(false);
                }
            }

            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                changed();
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                changed();
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                changed();
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                changed();
            }

        });
    }

    private void addGenericListener() {
        ((GenericTagTreePanel) genericTagPanel).addTreeModelListener(new TreeModelListener() {
            private void changed() {
                genericSaveButton.setEnabled(true);
                genericCancelButton.setEnabled(true);
                mainPanel.setEditingStatus();
            }

            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                changed();
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                changed();
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                changed();
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                changed();
            }

        });
    }

    public void showGenericTagPanel(Tag tag) {
        showCardLeft(GENERIC_TAG_CARD);
        genericEditButton.setEnabled(true);
        if (Configuration.editorMode.get()) {
            genericTagPanel.setEditMode(!tag.isReadOnly(), tag);
            genericSaveButton.setVisible(!tag.isReadOnly());
            genericCancelButton.setVisible(!tag.isReadOnly());
        } else {
            genericEditButton.setVisible(!tag.isReadOnly());
            genericTagPanel.setEditMode(false, tag);
            genericSaveButton.setVisible(false);
            genericCancelButton.setVisible(false);
        }
        parametersPanel.setVisible(false);
    }

    public void showDisplayEditTagPanel(Tag tag, int frame) {
        showCardLeft(DISPLAYEDIT_TAG_CARD);
        displayEditTag = tag;
        displayEditSplitPane.setDividerLocation(0.6);
        displayEditGenericPanel.setVisible(!readOnly);

        if (Configuration.editorMode.get()) {
            displayEditGenericPanel.setEditMode(!tag.isReadOnly(), tag);
            displayEditEditButton.setVisible(false);
            displayEditSaveButton.setVisible(!tag.isReadOnly());
            displayEditCancelButton.setVisible(!tag.isReadOnly());
            displayEditSaveButton.setEnabled(false);
            displayEditCancelButton.setEnabled(false);
        } else {
            displayEditGenericPanel.setEditMode(false, tag);
            displayEditEditButton.setVisible(!tag.isReadOnly() && !readOnly);
            displayEditEditButton.setEnabled(true);
            displayEditSaveButton.setVisible(false);
            displayEditCancelButton.setVisible(false);
        }

        displayEditImagePanel.selectDepth(-1);
        if (tag instanceof ShapeTag) {
            Timelined tim = TimelinedMaker.makeTimelined(tag);
            displayEditImagePanel.setTimelined(tim, ((Tag) tag).getSwf(), 0, true, Configuration.autoPlayPreviews.get(), !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), false, true, true, true);
            displayEditImagePanel.setGuidesCharacter(tag.getSwf(), ((CharacterTag) tag).getCharacterId());
        }
        if (tag instanceof MorphShapeTag) {
            Timelined tim = TimelinedMaker.makeTimelined(tag);
            displayEditImagePanel.setTimelined(tim, ((Tag) tag).getSwf(), -1, true, Configuration.autoPlayPreviews.get(), !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), false, true, true, true);
            displayEditImagePanel.setGuidesCharacter(tag.getSwf(), ((CharacterTag) tag).getCharacterId());
            morphDisplayMode = MORPH_ANIMATE;
            displayEditShowAnimationButton.setSelected(true);
        }
        if (tag instanceof PlaceObjectTypeTag) {
            displayEditImagePanel.setTimelined(((Tag) tag).getTimelined(), ((Tag) tag).getSwf(), frame, true, Configuration.autoPlayPreviews.get(), !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), true, true, true, true);
            Timelined tim = ((Tag) tag).getTimelined();
            if (tim instanceof Tag) {
                displayEditImagePanel.setGuidesCharacter(tag.getSwf(), ((CharacterTag) tim).getCharacterId());
            } else {
                displayEditImagePanel.setGuidesCharacter(tag.getSwf(), -1);
            }

            PlaceObjectTypeTag place = (PlaceObjectTypeTag) tag;
            displayEditImagePanel.selectDepth(place.getDepth());
        }
        parametersPanel.setVisible(false);
        displayEditTransformButton.setVisible(!tag.isReadOnly() && !readOnly);
    }

    public void setImageReplaceButtonVisible(boolean showImage, boolean showAlpha, boolean showShape, boolean showSound, boolean showMovie, boolean showMorphShape, boolean showSprite) {
        if (readOnly) {
            showImage = false;
            showAlpha = false;
            showShape = false;
            showSound = false;
            showMovie = false;
            showSprite = false;
            showMorphShape = false;
        }
        replaceImageButton.setVisible(showImage);
        replaceImageAlphaButton.setVisible(showAlpha);
        replaceSpriteButton.setVisible(showSprite);
        replaceShapeButton.setVisible(showShape);
        replaceMorphShapeButton.setVisible(showMorphShape);
        morphShowPanel.setVisible(showMorphShape);
        displayEditEditPointsButton.setVisible(showShape || showMorphShape);
        replaceShapeUpdateBoundsButton.setVisible(showShape);
        replaceMorphShapeUpdateBoundsButton.setVisible(showMorphShape);
        replaceSoundButton.setVisible(showSound);
        replaceMovieButton.setVisible(showMovie);
        prevFontsButton.setVisible(false);
        nextFontsButton.setVisible(false);
    }

    private void createAndRunTempSwf(TreeItem treeItem) {
        try {
            File extTempFile = File.createTempFile("ffdec_viewext_", ".swf");
            extTempFile.deleteOnExit();

            if (treeItem instanceof SWF) {
                SWF swf = (SWF) treeItem;
                try (FileOutputStream fos = new FileOutputStream(extTempFile)) {
                    swf.saveTo(fos);
                }
            } else {
                Color backgroundColor = View.getSwfBackgroundColor();

                if (treeItem instanceof Tag) {
                    Tag tag = (Tag) treeItem;
                    if (tag instanceof FontTag) { //Fonts are always black on white
                        backgroundColor = View.getDefaultBackgroundColor();
                    }
                } else if (treeItem instanceof Frame) {
                    Frame fn = (Frame) treeItem;
                    SWF sourceSwf = (SWF) fn.getOpenable();
                    if (fn.timeline.timelined == sourceSwf) {
                        SetBackgroundColorTag setBgColorTag = sourceSwf.getBackgroundColor();
                        if (setBgColorTag != null) {
                            backgroundColor = setBgColorTag.backgroundColor.toColor();
                        }
                    }
                }

                SWFHeader header;
                try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(extTempFile))) {
                    header = new PreviewExporter().exportSwf(fos, treeItem, backgroundColor, fontPageNum, true);
                }
            }
            Main.runAsync(extTempFile);
        } catch (IOException | ActionParseException ex) {
            Logger.getLogger(PreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                SWF sourceSwf = (SWF) fn.getOpenable();
                if (fn.timeline.timelined == sourceSwf) {
                    SetBackgroundColorTag setBgColorTag = sourceSwf.getBackgroundColor();
                    if (setBgColorTag != null) {
                        backgroundColor = setBgColorTag.backgroundColor.toColor();
                    }
                }
            }

            SWFHeader header;
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                header = new PreviewExporter().exportSwf(fos, treeItem, backgroundColor, fontPageNum, false);
            }

            this.currentItem = treeItem;

            showFlashViewerPanel();
        } catch (IOException | ActionParseException ex) {
            Logger.getLogger(PreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void editMetadataButtonActionPerformed(ActionEvent evt) {
        TreeItem item = mainPanel.getCurrentTree().getCurrentTreeItem();
        if (item == null) {
            return;
        }

        if (item instanceof MetadataTag) {
            metadataEditor.setEditable(true);
            updateMetadataButtonsVisibility();
            mainPanel.setEditingStatus();
        }
    }

    private void saveMetadataButtonActionPerformed(ActionEvent evt) {
        metadataTag.xmlMetadata = metadataEditor.getText().replaceAll(">\r?\n<", "> <");
        metadataTag.setModified(true);
        metadataEditor.setEditable(Configuration.editorMode.get());
        setMetadataModified(false);
        updateMetadataButtonsVisibility();
        mainPanel.repaintTree();
        mainPanel.clearEditingStatus();
    }

    private void cancelMetadataButtonActionPerformed(ActionEvent evt) {
        metadataEditor.setEditable(false);
        metadataEditor.setText(formatMetadata(metadataTag.xmlMetadata, 4));
        metadataEditor.setEditable(Configuration.editorMode.get());
        setMetadataModified(false);
        updateMetadataButtonsVisibility();
        mainPanel.clearEditingStatus();
    }

    private void editGenericTagButtonActionPerformed(ActionEvent evt) {
        TreeItem item = mainPanel.getCurrentTree().getCurrentTreeItem();
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

            mainPanel.setEditingStatus();
        }
    }

    private void saveGenericTag(boolean refreshTree) {
        if (genericTagPanel.save()) {
            Tag tag = genericTagPanel.getTag();
            SWF swf = tag.getSwf();
            swf.clearImageCache();
            if (tag instanceof ShapeTag) {
                ShapeTag shape = (ShapeTag) tag;
                shape.shapes.clearCachedOutline();
            }
            swf.clearShapeCache();
            swf.updateCharacters();
            tag.getTimelined().resetTimeline();
            swf.assignClassesToSymbols();
            swf.assignExportNamesToSymbols();
            if (Configuration.editorMode.get()) {
                genericEditButton.setVisible(false);
                genericSaveButton.setVisible(true);
                genericSaveButton.setEnabled(false);
                genericCancelButton.setVisible(true);
                genericCancelButton.setEnabled(false);
            } else {
                genericEditButton.setVisible(true);
                genericSaveButton.setVisible(false);
                genericCancelButton.setVisible(false);
            }
            genericTagPanel.setEditMode(false, null);
            if (refreshTree) {
                mainPanel.refreshTree(swf);
            }
            mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), tag);
            mainPanel.clearEditingStatus();
        }
    }

    private void saveGenericTagButtonActionPerformed(ActionEvent evt) {
        saveGenericTag(true);
    }

    private void cancelGenericTagButtonActionPerformed(ActionEvent evt) {
        if (Configuration.editorMode.get()) {
            genericTagPanel.setEditMode(true, null);
            genericEditButton.setVisible(false);
            genericSaveButton.setVisible(true);
            genericSaveButton.setEnabled(false);
            genericCancelButton.setVisible(true);
            genericCancelButton.setEnabled(false);
        } else {
            genericTagPanel.setEditMode(false, null);
            genericEditButton.setVisible(true);
            genericSaveButton.setVisible(false);
            genericCancelButton.setVisible(false);
        }
        mainPanel.clearEditingStatus();
    }

    private void transformStyles(Matrix matrix, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles, int shapeNum) {
        List<FILLSTYLE> fillStyleToTransform = new ArrayList<>();
        for (FILLSTYLE fs : fillStyles.fillStyles) {
            fillStyleToTransform.add(fs);
        }
        if (shapeNum >= 4) {
            for (LINESTYLE2 ls : lineStyles.lineStyles2) {
                if (ls.hasFillFlag) {
                    fillStyleToTransform.add(ls.fillType);
                }
            }
        }

        for (FILLSTYLE fs : fillStyleToTransform) {
            switch (fs.fillStyleType) {
                case FILLSTYLE.CLIPPED_BITMAP:
                case FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP:
                case FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP:
                case FILLSTYLE.REPEATING_BITMAP:
                    fs.bitmapMatrix = new Matrix(fs.bitmapMatrix).preConcatenate(matrix).toMATRIX();
                    break;
                case FILLSTYLE.LINEAR_GRADIENT:
                case FILLSTYLE.RADIAL_GRADIENT:
                case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                    fs.gradientMatrix = new Matrix(fs.gradientMatrix).preConcatenate(matrix).toMATRIX();
                    break;
            }
        }
    }

    private void transformMorphStyles(Matrix matrix, MORPHFILLSTYLEARRAY fillStyles, MORPHLINESTYLEARRAY lineStyles, int morphShapeNum, boolean doStart, boolean doEnd) {
        List<MORPHFILLSTYLE> fillStyleToTransform = new ArrayList<>();
        for (MORPHFILLSTYLE fs : fillStyles.fillStyles) {
            fillStyleToTransform.add(fs);
        }

        if (morphShapeNum == 2) {
            for (MORPHLINESTYLE2 ls : lineStyles.lineStyles2) {
                if (ls.hasFillFlag) {
                    fillStyleToTransform.add(ls.fillType);
                }
            }
        }

        for (MORPHFILLSTYLE fs : fillStyleToTransform) {
            switch (fs.fillStyleType) {
                case FILLSTYLE.CLIPPED_BITMAP:
                case FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP:
                case FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP:
                case FILLSTYLE.REPEATING_BITMAP:
                    if (doStart) {
                        fs.startBitmapMatrix = new Matrix(fs.startBitmapMatrix).preConcatenate(matrix).toMATRIX();
                    }
                    if (doEnd) {
                        fs.endBitmapMatrix = new Matrix(fs.endBitmapMatrix).preConcatenate(matrix).toMATRIX();
                    }
                    break;
                case FILLSTYLE.LINEAR_GRADIENT:
                case FILLSTYLE.RADIAL_GRADIENT:
                case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                    if (doStart) {
                        fs.startGradientMatrix = new Matrix(fs.startGradientMatrix).preConcatenate(matrix).toMATRIX();
                    }
                    if (doEnd) {
                        fs.endGradientMatrix = new Matrix(fs.endGradientMatrix).preConcatenate(matrix).toMATRIX();
                    }
                    break;
            }
        }
    }

    private void transformSHAPE(Matrix matrix, SHAPE shape, int shapeNum) {
        int x = 0;
        int y = 0;
        StyleChangeRecord lastStyleChangeRecord = null;
        boolean wasMoveTo = false;
        for (SHAPERECORD rec : shape.shapeRecords) {
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                lastStyleChangeRecord = scr;
                if (scr.stateNewStyles) {
                    transformStyles(matrix, scr.fillStyles, scr.lineStyles, shapeNum);
                }
                if (scr.stateMoveTo) {
                    Point nextPoint = new Point(scr.moveDeltaX, scr.moveDeltaY);
                    x = scr.changeX(x);
                    y = scr.changeY(y);
                    Point nextPoint2 = matrix.transform(nextPoint);
                    scr.moveDeltaX = nextPoint2.x;
                    scr.moveDeltaY = nextPoint2.y;
                    scr.calculateBits();
                    wasMoveTo = true;
                }
            }

            if (((rec instanceof StraightEdgeRecord) || (rec instanceof CurvedEdgeRecord)) && !wasMoveTo) {
                if (lastStyleChangeRecord != null) {
                    Point nextPoint2 = matrix.transform(new Point(x, y));
                    if (nextPoint2.x != 0 || nextPoint2.y != 0) {
                        lastStyleChangeRecord.stateMoveTo = true;
                        lastStyleChangeRecord.moveDeltaX = nextPoint2.x;
                        lastStyleChangeRecord.moveDeltaY = nextPoint2.y;
                        lastStyleChangeRecord.calculateBits();
                        wasMoveTo = true;
                    }
                }
            }
            if (rec instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                ser.generalLineFlag = true;
                ser.vertLineFlag = false;
                Point currentPoint = new Point(x, y);
                Point nextPoint = new Point(x + ser.deltaX, y + ser.deltaY);
                x = ser.changeX(x);
                y = ser.changeY(y);
                Point currentPoint2 = matrix.transform(currentPoint);
                Point nextPoint2 = matrix.transform(nextPoint);
                ser.deltaX = nextPoint2.x - currentPoint2.x;
                ser.deltaY = nextPoint2.y - currentPoint2.y;
                ser.simplify();
            }
            if (rec instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                Point currentPoint = new Point(x, y);
                Point controlPoint = new Point(x + cer.controlDeltaX, y + cer.controlDeltaY);
                Point anchorPoint = new Point(x + cer.controlDeltaX + cer.anchorDeltaX, y + cer.controlDeltaY + cer.anchorDeltaY);
                x = cer.changeX(x);
                y = cer.changeY(y);

                Point currentPoint2 = matrix.transform(currentPoint);
                Point controlPoint2 = matrix.transform(controlPoint);
                Point anchorPoint2 = matrix.transform(anchorPoint);

                cer.controlDeltaX = controlPoint2.x - currentPoint2.x;
                cer.controlDeltaY = controlPoint2.y - currentPoint2.y;
                cer.anchorDeltaX = anchorPoint2.x - controlPoint2.x;
                cer.anchorDeltaY = anchorPoint2.y - controlPoint2.y;
                cer.calculateBits();
            }
        }
    }

    private RECT transformRECT(Matrix matrix, RECT rect) {
        ExportRectangle shapeRect = matrix.transform(new ExportRectangle(rect));
        return new RECT(
                (int) Math.round(shapeRect.xMin),
                (int) Math.round(shapeRect.xMax),
                (int) Math.round(shapeRect.yMin),
                (int) Math.round(shapeRect.yMax)
        );
    }

    private boolean checkShapeLarge(List<SHAPERECORD> shapeRecords) {
        for (SHAPERECORD rec : shapeRecords) {
            if (rec.isTooLarge()) {
                ViewMessages.showMessageDialog(this, AppStrings.translate("error.shapeTooLarge"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                return true;
            }
        }
        return false;
    }

    private boolean checkRectLarge(RECT rect) {
        if (rect.isTooLarge()) {
            ViewMessages.showMessageDialog(this, AppStrings.translate("error.shapeTooLarge"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }

    private void saveDisplayEditTag(boolean refreshTree) {
        if (displayEditMode == EDIT_TRANSFORM) {
            Matrix matrix = displayEditImagePanel.getNewMatrix();
            if (displayEditTag instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag placeTag = (PlaceObjectTypeTag) displayEditTag;
                Matrix origMatrix = new Matrix(placeTag.getMatrix());
                placeTag.setMatrix(matrix.concatenate(origMatrix).toMATRIX());
                placeTag.setPlaceFlagHasMatrix(true);
            }
            if (displayEditTag instanceof ShapeTag) {
                ShapeTag shape = (ShapeTag) displayEditTag;

                RECT newShapeBounds = transformRECT(matrix, shape.shapeBounds);
                if (checkRectLarge(newShapeBounds)) {
                    return;
                }
                RECT newEdgeBounds = null;
                if (shape instanceof DefineShape4Tag) {
                    DefineShape4Tag shape4 = (DefineShape4Tag) shape;
                    newEdgeBounds = transformRECT(matrix, shape4.edgeBounds);
                    if (checkRectLarge(newEdgeBounds)) {
                        return;
                    }
                }

                oldShapeRecords = Helper.deepCopy(shape.shapes.shapeRecords);
                transformSHAPE(matrix, shape.shapes, shape.getShapeNum());
                if (checkShapeLarge(shape.shapes.shapeRecords)) {
                    shape.shapes.shapeRecords = oldShapeRecords;
                    return;
                }
                oldShapeRecords = null;
                transformStyles(matrix, shape.shapes.fillStyles, shape.shapes.lineStyles, shape.getShapeNum());

                shape.shapeBounds = newShapeBounds;
                if (shape instanceof DefineShape4Tag) {
                    DefineShape4Tag shape4 = (DefineShape4Tag) shape;
                    shape4.edgeBounds = newEdgeBounds;
                }
                shape.shapes.clearCachedOutline();
                shape.getSwf().clearShapeCache();
            }

            if (displayEditTag instanceof MorphShapeTag) {
                MorphShapeTag morphShape = (MorphShapeTag) displayEditTag;

                if (morphDisplayMode == MORPH_START) {
                    RECT newShapeBounds = transformRECT(matrix, morphShape.startBounds);
                    if (checkRectLarge(newShapeBounds)) {
                        return;
                    }
                    RECT newEdgeBounds = null;
                    if (morphShape instanceof DefineMorphShape2Tag) {
                        DefineMorphShape2Tag morphShape2 = (DefineMorphShape2Tag) morphShape;
                        newEdgeBounds = transformRECT(matrix, morphShape2.startEdgeBounds);
                    }

                    oldShapeRecords = Helper.deepCopy(morphShape.startEdges.shapeRecords);
                    transformSHAPE(matrix, morphShape.startEdges, morphShape.getShapeNum() == 1 ? 3 : 4);
                    if (checkShapeLarge(morphShape.startEdges.shapeRecords)) {
                        morphShape.startEdges.shapeRecords = oldShapeRecords;
                        return;
                    }
                    oldShapeRecords = null;
                    morphShape.startBounds = newShapeBounds;
                    if (morphShape instanceof DefineMorphShape2Tag) {
                        DefineMorphShape2Tag morphShape2 = (DefineMorphShape2Tag) morphShape;
                        morphShape2.startEdgeBounds = newEdgeBounds;
                    }
                    transformMorphStyles(matrix, morphShape.morphFillStyles, morphShape.morphLineStyles, morphShape.getShapeNum(), true, false);
                }

                if (morphDisplayMode == MORPH_END) {
                    RECT newShapeBounds = transformRECT(matrix, morphShape.endBounds);
                    if (checkRectLarge(newShapeBounds)) {
                        return;
                    }
                    RECT newEdgeBounds = null;
                    if (morphShape instanceof DefineMorphShape2Tag) {
                        DefineMorphShape2Tag morphShape2 = (DefineMorphShape2Tag) morphShape;
                        newEdgeBounds = transformRECT(matrix, morphShape2.endEdgeBounds);
                    }

                    oldShapeRecords = Helper.deepCopy(morphShape.endEdges.shapeRecords);
                    transformSHAPE(matrix, morphShape.endEdges, morphShape.getShapeNum() == 1 ? 3 : 4);
                    if (checkShapeLarge(morphShape.endEdges.shapeRecords)) {
                        morphShape.endEdges.shapeRecords = oldShapeRecords;
                        return;
                    }
                    oldShapeRecords = null;
                    morphShape.endBounds = newShapeBounds;
                    if (morphShape instanceof DefineMorphShape2Tag) {
                        DefineMorphShape2Tag morphShape2 = (DefineMorphShape2Tag) morphShape;
                        morphShape2.endEdgeBounds = newEdgeBounds;
                    }
                    transformMorphStyles(matrix, morphShape.morphFillStyles, morphShape.morphLineStyles, morphShape.getShapeNum(), false, true);
                }
                morphShape.getSwf().clearShapeCache();
            }
            displayEditTag.setModified(true);
            if (displayEditTag instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag placeTag = (PlaceObjectTypeTag) displayEditTag;
                displayEditImagePanel.selectDepth(placeTag.getDepth());
            }
            displayEditImagePanel.freeTransformDepth(-1);
            displayEditTag.getTimelined().resetTimeline();
            displayEditTransformScrollPane.setVisible(false);
            displayEditGenericPanel.setVisible(true);
        }
        if (displayEditMode == EDIT_POINTS) {
            List<SHAPERECORD> shapeRecords = null;
            if (displayEditTag instanceof ShapeTag) {
                ShapeTag shape = (ShapeTag) displayEditTag;
                if (checkShapeLarge(shape.shapes.shapeRecords)) {
                    return;
                }
            }
            if (displayEditTag instanceof MorphShapeTag) {
                MorphShapeTag morphShape = (MorphShapeTag) displayEditTag;
                if (morphDisplayMode == MORPH_START) {
                    if (checkShapeLarge(morphShape.getStartEdges().shapeRecords)) {
                        return;
                    }
                }
                if (morphDisplayMode == MORPH_END) {
                    if (checkShapeLarge(morphShape.getEndEdges().shapeRecords)) {
                        return;
                    }
                }
            }
            displayEditImagePanel.setHilightedPoints(null);
            displayEditTag.setModified(true);
            if (displayEditTag instanceof ShapeTag) {
                ShapeTag shape = (ShapeTag) displayEditTag;
                shape.updateBounds();
                if (checkRectLarge(shape.shapeBounds)) {
                    return;
                }
                if (shape instanceof DefineShape4Tag) {
                    DefineShape4Tag shape4 = (DefineShape4Tag) shape;
                    if (checkRectLarge(shape4.edgeBounds)) {
                        return;
                    }
                }
            }
            if (displayEditTag instanceof MorphShapeTag) {
                MorphShapeTag morphShape = (MorphShapeTag) displayEditTag;
                if (morphDisplayMode == MORPH_START) {
                    morphShape.updateStartBounds();
                    if (checkRectLarge(morphShape.endBounds)) {
                        return;
                    }
                    if (morphShape instanceof DefineMorphShape2Tag) {
                        DefineMorphShape2Tag morphShape2 = (DefineMorphShape2Tag) morphShape;
                        if (checkRectLarge(morphShape2.endEdgeBounds)) {
                            return;
                        }
                    }
                }
                if (morphDisplayMode == MORPH_END) {
                    morphShape.updateEndBounds();
                    if (checkRectLarge(morphShape.startBounds)) {
                        return;
                    }
                    if (morphShape instanceof DefineMorphShape2Tag) {
                        DefineMorphShape2Tag morphShape2 = (DefineMorphShape2Tag) morphShape;
                        if (checkRectLarge(morphShape2.startEdgeBounds)) {
                            return;
                        }
                    }
                }
            }
            oldShapeRecords = null;
            oldShapeBounds = null;
            oldShapeEdgeBounds = null;
            oldEndShapeRecords = null;
            oldEndShapeBounds = null;
            oldEndShapeEdgeBounds = null;
        }
        Tag hilightTag = null;
        SWF swf = null;
        if (displayEditMode == EDIT_RAW) {
            if (displayEditGenericPanel.save()) {
                Tag tag = displayEditGenericPanel.getTag();
                swf = tag.getSwf();
                tag.getTimelined().resetTimeline();
                hilightTag = tag;
            }
            displayEditGenericPanel.setEditMode(false, null);
        }

        if (displayEditTag instanceof ShapeTag) {
            replaceShapeButton.setVisible(true);
            replaceShapeUpdateBoundsButton.setVisible(true);
            displayEditEditPointsButton.setVisible(true);
        }

        if (displayEditTag instanceof MorphShapeTag) {
            replaceMorphShapeButton.setVisible(true);
            replaceMorphShapeUpdateBoundsButton.setVisible(true);
            displayEditEditPointsButton.setVisible(true);
        }

        if (displayEditTag instanceof DefineSpriteTag) {
            replaceSpriteButton.setVisible(true);
        }

        if (displayEditTag instanceof MorphShapeTag) {
            morphShowPanel.setVisible(true);
            displayEditEditPointsButton.setVisible(true);
        }

        displayEditTransformButton.setVisible(true);
        if (Configuration.editorMode.get()) {
            displayEditEditButton.setVisible(false);
            displayEditSaveButton.setVisible(true);
            displayEditSaveButton.setEnabled(false);
            displayEditCancelButton.setVisible(true);
            displayEditCancelButton.setEnabled(false);
            displayEditTransformButton.setVisible(true);
        } else {
            displayEditEditButton.setVisible(true);
            displayEditSaveButton.setVisible(false);
            displayEditCancelButton.setVisible(false);
        }

        if (displayEditMode == EDIT_RAW && refreshTree && swf != null) {
            mainPanel.refreshTree(swf);
        }
        mainPanel.clearEditingStatus();
        mainPanel.repaintTree();
        if (hilightTag != null) {
            mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), hilightTag);
        }
        if (displayEditMode == EDIT_TRANSFORM) {
            displayEditMode = EDIT_RAW;
        }
    }

    private void saveDisplayEditTagButtonActionPerformed(ActionEvent evt) {
        saveDisplayEditTag(true);
    }

    private void editDisplayEditTagButtonActionPerformed(ActionEvent evt) {
        displayEditMode = EDIT_RAW;
        displayEditGenericPanel.setEditMode(true, displayEditTag);
        displayEditEditButton.setVisible(false);
        displayEditTransformButton.setVisible(false);
        displayEditSaveButton.setVisible(true);
        displayEditCancelButton.setVisible(true);
        replaceShapeButton.setVisible(false);
        replaceMorphShapeButton.setVisible(false);
        replaceShapeUpdateBoundsButton.setVisible(false);
        replaceMorphShapeUpdateBoundsButton.setVisible(false);
        displayEditEditPointsButton.setVisible(false);
        mainPanel.setEditingStatus();
    }

    private void showAnimationDisplayEditTagButtonActionPerformed(ActionEvent evt) {
        morphDisplayMode = MORPH_ANIMATE;
        Timelined tim = TimelinedMaker.makeTimelined(displayEditTag);
        displayEditImagePanel.setTimelined(tim, displayEditTag.getSwf(), -1, true, Configuration.autoPlayPreviews.get(), !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), false, true, true, true);
        displayEditImagePanel.setGuidesCharacter(displayEditTag.getSwf(), ((CharacterTag) displayEditTag).getCharacterId());
    }

    private void showStartDisplayEditTagButtonActionPerformed(ActionEvent evt) {
        morphDisplayMode = MORPH_START;
        Timelined tim = TimelinedMaker.makeTimelined(displayEditTag);
        displayEditImagePanel.setTimelined(tim, displayEditTag.getSwf(), 0, true, Configuration.autoPlayPreviews.get(), !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), false, true, true, true);
        displayEditImagePanel.setGuidesCharacter(displayEditTag.getSwf(), ((CharacterTag) displayEditTag).getCharacterId());
    }

    private void showEndDisplayEditTagButtonActionPerformed(ActionEvent evt) {
        morphDisplayMode = MORPH_END;
        Timelined tim = TimelinedMaker.makeTimelined(displayEditTag);
        displayEditImagePanel.setTimelined(tim, displayEditTag.getSwf(), tim.getFrameCount() - 1, true, Configuration.autoPlayPreviews.get(), !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), false, true, true, true);
        displayEditImagePanel.setGuidesCharacter(displayEditTag.getSwf(), ((CharacterTag) displayEditTag).getCharacterId());
    }

    private void editPointsDisplayEditTagButtonActionPerformed(ActionEvent evt) {
        displayEditMode = EDIT_POINTS;
        displayEditGenericPanel.setVisible(false);
        displayEditEditButton.setVisible(false);
        displayEditTransformButton.setVisible(false);
        displayEditSaveButton.setVisible(true);
        displayEditCancelButton.setVisible(true);
        replaceShapeButton.setVisible(false);
        replaceMorphShapeButton.setVisible(false);
        replaceShapeUpdateBoundsButton.setVisible(false);
        replaceMorphShapeUpdateBoundsButton.setVisible(false);
        displayEditEditPointsButton.setVisible(false);

        displayEditSaveButton.setEnabled(true);
        displayEditCancelButton.setEnabled(true);

        if ((displayEditTag instanceof MorphShapeTag) && (morphDisplayMode == MORPH_ANIMATE)) {
            displayEditShowStartButton.setSelected(true);
            showStartDisplayEditTagButtonActionPerformed(null);
        }

        morphShowPanel.setVisible(false);

        if (displayEditTag instanceof ShapeTag) {
            ShapeTag shape = (ShapeTag) displayEditTag;

            oldShapeRecords = Helper.deepCopy(shape.shapes.shapeRecords);
            oldShapeBounds = shape.shapeBounds;
            if (shape instanceof DefineShape4Tag) {
                DefineShape4Tag shape4 = (DefineShape4Tag) shape;
                oldShapeEdgeBounds = shape4.edgeBounds;
            }
        }
        if (displayEditTag instanceof MorphShapeTag) {
            MorphShapeTag morphShape = (MorphShapeTag) displayEditTag;
            oldShapeRecords = Helper.deepCopy(morphShape.startEdges.shapeRecords);
            oldEndShapeRecords = Helper.deepCopy(morphShape.endEdges.shapeRecords);
            oldShapeBounds = morphShape.startBounds;
            oldEndShapeBounds = morphShape.endBounds;
            if (morphShape instanceof DefineMorphShape2Tag) {
                DefineMorphShape2Tag morphShape2 = (DefineMorphShape2Tag) morphShape;
                oldShapeEdgeBounds = morphShape2.startEdgeBounds;
                oldEndShapeEdgeBounds = morphShape2.endEdgeBounds;
            }
        }
        refreshHilightedPoints();

        mainPanel.setEditingStatus();
    }

    private void refreshHilightedPoints() {
        List<SHAPERECORD> selectedRecords = new ArrayList<>();
        if (displayEditTag instanceof ShapeTag) {
            ShapeTag shape = (ShapeTag) displayEditTag;
            selectedRecords = shape.shapes.shapeRecords;
        }
        if (displayEditTag instanceof MorphShapeTag) {
            MorphShapeTag morphShape = (MorphShapeTag) displayEditTag;
            if (morphDisplayMode == MORPH_START) {
                selectedRecords = morphShape.startEdges.shapeRecords;
            }
            if (morphDisplayMode == MORPH_END) {
                selectedRecords = morphShape.endEdges.shapeRecords;
            }
        }
        int x = 0;
        int y = 0;

        List<DisplayPoint> points = new ArrayList<>();
        boolean wasMoveTo = false;
        StyleChangeRecord lastStyleChangeRecord = null;
        for (SHAPERECORD rec : selectedRecords) {
            if (((rec instanceof StraightEdgeRecord) || (rec instanceof CurvedEdgeRecord)) && !wasMoveTo) {
                if (lastStyleChangeRecord != null) {
                    DisplayPoint point = new DisplayPoint(0, 0);
                    points.add(point);
                    wasMoveTo = true;
                }
            }

            if (rec instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                DisplayPoint point = new DisplayPoint(x + ser.deltaX, y + ser.deltaY);
                points.add(point);
            }
            if (rec instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                DisplayPoint controlPoint = new DisplayPoint(x + cer.controlDeltaX, y + cer.controlDeltaY, false);
                DisplayPoint anchorPoint = new DisplayPoint(x + cer.controlDeltaX + cer.anchorDeltaX, y + cer.controlDeltaY + cer.anchorDeltaY);
                points.add(controlPoint);
                points.add(anchorPoint);
            }

            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                lastStyleChangeRecord = scr;
                if (scr.stateMoveTo) {
                    DisplayPoint point = new DisplayPoint(scr.moveDeltaX, scr.moveDeltaY);
                    points.add(point);
                    wasMoveTo = true;
                }
            }

            x = rec.changeX(x);
            y = rec.changeY(y);
        }
        displayEditImagePanel.setHilightedPoints(points);
    }

    private void transformDisplayEditTagButtonActionPerformed(ActionEvent evt) {
        TreeItem item = mainPanel.getCurrentTree().getCurrentTreeItem();
        if (item == null) {
            return;
        }
        displayEditMode = EDIT_TRANSFORM;
        displayEditGenericPanel.setVisible(false);
        displayEditImagePanel.selectDepth(-1);

        displayEditTransformScrollPane.setVisible(true);

        displayEditEditButton.setVisible(false);
        displayEditTransformButton.setVisible(false);
        displayEditSaveButton.setVisible(true);
        displayEditCancelButton.setVisible(true);

        replaceShapeButton.setVisible(false);
        replaceMorphShapeButton.setVisible(false);
        replaceShapeUpdateBoundsButton.setVisible(false);
        replaceMorphShapeUpdateBoundsButton.setVisible(false);
        displayEditEditPointsButton.setVisible(false);

        morphShowPanel.setVisible(false);

        if ((displayEditTag instanceof MorphShapeTag) && (morphDisplayMode == MORPH_ANIMATE)) {
            displayEditShowStartButton.setSelected(true);
            showStartDisplayEditTagButtonActionPerformed(null);
        }

        if (Configuration.editorMode.get()) {
            displayEditSaveButton.setEnabled(false);
        } else {
            displayEditSaveButton.setEnabled(true);
        }
        displayEditCancelButton.setEnabled(true);
        mainPanel.setEditingStatus();

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                displayEditTransformSplitPane.setDividerLocation(getWidth() - 450);
            }
        }, 20);
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (displayEditTag instanceof PlaceObjectTypeTag) {
                    PlaceObjectTypeTag place = (PlaceObjectTypeTag) displayEditTag;
                    displayEditImagePanel.freeTransformDepth(place.getDepth());
                } else {
                    displayEditImagePanel.freeTransformDepth(1);
                }
                displayEditTransformPanel.load();
            }
        }, 40); //add some delay before controls are hidden
    }

    private void saveImageTransform(boolean refreshTree) {
        Matrix matrix = imagePanel.getNewMatrix();

        imageTransformScrollPane.setVisible(false);
        imagePanel.freeTransformDepth(-1);
        imageTransformButton.setVisible(true);
        imageTransformCancelButton.setVisible(false);
        imageTransformSaveButton.setVisible(false);

        TreeItem item = mainPanel.getCurrentTree().getCurrentTreeItem();
        if (item instanceof TagScript) {
            item = ((TagScript) item).getTag();
        }

        CharacterTag character = (CharacterTag) item;
        if (character instanceof ButtonTag) {
            ButtonTag button = (ButtonTag) character;
            for (BUTTONRECORD rec : button.getRecords()) {
                MATRIX placeMatrix = rec.placeMatrix;
                rec.placeMatrix = new Matrix(placeMatrix).preConcatenate(matrix).toMATRIX();
                rec.setModified(true);
            }
            button.setModified(true);
            button.resetTimeline();
        } else if (character instanceof DefineSpriteTag) {
            DefineSpriteTag sprite = (DefineSpriteTag) character;
            for (Tag t : sprite.getTags()) {
                if (t instanceof PlaceObjectTypeTag) {
                    PlaceObjectTypeTag pt = (PlaceObjectTypeTag) t;
                    MATRIX placeMatrix = pt.getMatrix();
                    if (placeMatrix != null) {
                        pt.setMatrix(new Matrix(placeMatrix).preConcatenate(matrix).toMATRIX());
                        pt.setModified(true);
                    }
                }
            }
            sprite.resetTimeline();
            replaceSpriteButton.setVisible(true);
        }
        mainPanel.clearEditingStatus();
        if (refreshTree) {
            mainPanel.refreshTree(item.getOpenable());
            mainPanel.reload(true);
        }
    }

    private void saveImageTransformButtonActionPerformed(ActionEvent evt) {
        saveImageTransform(true);
    }

    private void cancelImageTransformButtonActionPerformed(ActionEvent evt) {
        imageTransformScrollPane.setVisible(false);
        imagePanel.freeTransformDepth(-1);
        imageTransformButton.setVisible(true);
        imageTransformCancelButton.setVisible(false);
        imageTransformSaveButton.setVisible(false);
        mainPanel.clearEditingStatus();
        mainPanel.reload(true);

        TreeItem item = mainPanel.getCurrentTree().getCurrentTreeItem();
        if (item instanceof TagScript) {
            item = ((TagScript) item).getTag();
        }
        if (item instanceof DefineSpriteTag) {
            replaceSpriteButton.setVisible(true);
        }
    }

    private void transformImageButtonActionPerformed(ActionEvent evt) {
        TreeItem item = mainPanel.getCurrentTree().getCurrentTreeItem();
        if (item == null) {
            return;
        }
        if (item instanceof TagScript) {
            item = ((TagScript) item).getTag();
        }

        CharacterTag displayedCharacter = (CharacterTag) item;

        CharacterTag placedCharacter = displayedCharacter;
        SWF origSwf = placedCharacter.getSwf();
        RECT rect = origSwf.getRect();
        if (displayedCharacter instanceof BoundedTag) {
            rect = ((BoundedTag) displayedCharacter).getRect();
        }
        final RECT frect = rect;
        Timelined tim = new Timelined() {
            ReadOnlyTagList cachedTags = null;

            @Override
            public SWF getSwf() {
                return origSwf;
            }

            @Override
            public Timeline getTimeline() {
                return new Timeline(origSwf, this, Integer.MAX_VALUE, frect);
            }

            @Override
            public void resetTimeline() {

            }

            @Override
            public void setModified(boolean value) {

            }

            @Override
            public boolean isModified() {
                return false;
            }

            @Override
            public ReadOnlyTagList getTags() {
                if (cachedTags == null) {
                    List<Tag> tags = new ArrayList<>();
                    PlaceObject3Tag placeTag = new PlaceObject3Tag(origSwf);
                    placeTag.depth = 1;
                    placeTag.characterId = placedCharacter.getCharacterId();
                    placeTag.placeFlagHasCharacter = true;

                    placeTag.matrix = new MATRIX();
                    placeTag.setTimelined(this);
                    tags.add(placeTag);
                    ShowFrameTag showFrameTag = new ShowFrameTag(origSwf);
                    showFrameTag.setTimelined(this);
                    tags.add(showFrameTag);
                    cachedTags = new ReadOnlyTagList(tags);
                }
                return cachedTags;
            }

            @Override
            public void removeTag(int index) {
            }

            @Override
            public void removeTag(Tag tag) {
            }

            @Override
            public void addTag(Tag tag) {
            }

            @Override
            public void addTag(int index, Tag tag) {
            }

            @Override
            public void replaceTag(int index, Tag newTag) {
            }

            @Override
            public void replaceTag(Tag oldTag, Tag newTag) {
            }

            @Override
            public int indexOfTag(Tag tag) {
                return getTags().indexOf(tag);
            }

            @Override
            public void setFrameCount(int frameCount) {
            }

            @Override
            public int getFrameCount() {
                return 1;
            }

            @Override
            public RECT getRect() {
                return frect;
            }

            @Override
            public RECT getRect(Set<BoundedTag> added) {
                return getRect();
            }

            @Override
            public RECT getRectWithStrokes() {
                return getRect();
            }
        };

        imagePanel.setTimelined(tim, origSwf, 0, true, true, true, true, true, false, true, true, true);
        imagePanel.setGuidesCharacter(displayedCharacter.getSwf(), ((CharacterTag) displayedCharacter).getCharacterId());
        imagePanel.selectDepth(-1);

        replaceSpriteButton.setVisible(false);
        imageTransformButton.setVisible(false);
        imageTransformSaveButton.setVisible(true);
        imageTransformCancelButton.setVisible(true);
        imageTransformScrollPane.setVisible(true);
        mainPanel.setEditingStatus();

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                imageTransformSplitPane.setDividerLocation(getWidth() - 450);
            }
        }, 20);
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                imagePanel.freeTransformDepth(1);
                imageTransformPanel.load();
            }
        }, 40); //add some delay before controls are hidden

    }

    private void cancelDisplayEditTagButtonActionPerformed(ActionEvent evt) {
        if (displayEditMode == EDIT_TRANSFORM) {
            if (displayEditTag instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) displayEditTag;
                displayEditImagePanel.selectDepth(place.getDepth());
            }
            displayEditImagePanel.freeTransformDepth(-1);
            displayEditTag.getTimelined().resetTimeline();
            displayEditTransformScrollPane.setVisible(false);
            displayEditGenericPanel.setVisible(true);
        }

        if (displayEditMode == EDIT_POINTS) {
            displayEditImagePanel.setHilightedPoints(null);
            if (displayEditTag instanceof ShapeTag) {
                ShapeTag shape = (ShapeTag) displayEditTag;
                shape.shapes.shapeRecords = oldShapeRecords;
                shape.shapeBounds = oldShapeBounds;
                if (shape instanceof DefineShape4Tag) {
                    DefineShape4Tag shape4 = (DefineShape4Tag) shape;
                    shape4.edgeBounds = oldShapeEdgeBounds;
                }
                shape.shapes.clearCachedOutline();
            }
            if (displayEditTag instanceof MorphShapeTag) {
                MorphShapeTag morphShape = (MorphShapeTag) displayEditTag;
                morphShape.startEdges.shapeRecords = oldShapeRecords;
                morphShape.endEdges.shapeRecords = oldEndShapeRecords;
                morphShape.startBounds = oldShapeBounds;
                morphShape.endBounds = oldEndShapeBounds;
                if (morphShape instanceof DefineMorphShape2Tag) {
                    DefineMorphShape2Tag morphShape2 = (DefineMorphShape2Tag) morphShape;
                    morphShape2.startEdgeBounds = oldShapeEdgeBounds;
                    morphShape2.endEdgeBounds = oldEndShapeEdgeBounds;
                }
            }
            displayEditTag.getSwf().clearShapeCache();
            displayEditImagePanel.repaint();
            displayEditGenericPanel.setVisible(true);
        }

        if (Configuration.editorMode.get()) {
            if (displayEditMode == EDIT_RAW) {
                displayEditGenericPanel.setEditMode(true, null);
            }
            displayEditEditButton.setVisible(false);
            displayEditSaveButton.setVisible(true);
            displayEditSaveButton.setEnabled(false);
            displayEditCancelButton.setVisible(true);
            displayEditCancelButton.setEnabled(false);
        } else {
            if (displayEditMode == EDIT_RAW) {
                displayEditGenericPanel.setEditMode(false, null);
            }
            displayEditEditButton.setVisible(true);
            displayEditSaveButton.setVisible(false);
            displayEditCancelButton.setVisible(false);
        }

        if (displayEditTag instanceof ShapeTag) {
            replaceShapeButton.setVisible(true);
            replaceShapeUpdateBoundsButton.setVisible(true);
            displayEditEditPointsButton.setVisible(true);
        }

        if (displayEditTag instanceof MorphShapeTag) {
            morphShowPanel.setVisible(true);
            replaceMorphShapeButton.setVisible(true);
            replaceMorphShapeUpdateBoundsButton.setVisible(true);
            displayEditEditPointsButton.setVisible(true);
        }

        mainPanel.clearEditingStatus();
        displayEditTransformButton.setVisible(true);

        if (displayEditMode == EDIT_TRANSFORM) {
            displayEditMode = EDIT_RAW;
        }
    }

    private void prevFontsButtonActionPerformed(ActionEvent evt) {
        FontTag fontTag = fontPanel.getFontTag();
        int pageCount = getFontPageCount(fontTag);
        fontPageNum = (fontPageNum + pageCount - 1) % pageCount;
        imagePanel.setTimelined(TimelinedMaker.makeTimelined(fontTag, fontPageNum), fontTag.getSwf(), 0, true, true, true, true, true, false, false, true, false);
    }

    private void nextFontsButtonActionPerformed(ActionEvent evt) {
        FontTag fontTag = fontPanel.getFontTag();
        int pageCount = getFontPageCount(fontTag);
        fontPageNum = (fontPageNum + 1) % pageCount;
        imagePanel.setTimelined(TimelinedMaker.makeTimelined(fontTag, fontPageNum), fontTag.getSwf(), 0, true, true, true, true, true, false, false, true, false);
    }

    @Override
    public boolean tryAutoSave() {
        boolean ok = true;

        if (imageTransformSaveButton.isVisible() && imageTransformSaveButton.isEnabled() && Configuration.autoSaveTagModifications.get()) {
            saveImageTransform(false);
            ok = ok && !(imageTransformSaveButton.isVisible() && imageTransformSaveButton.isEnabled());
        }

        if (displayEditSaveButton.isVisible() && displayEditSaveButton.isEnabled() && Configuration.autoSaveTagModifications.get()) {
            saveDisplayEditTag(false);
            ok = ok && !(displayEditSaveButton.isVisible() && displayEditSaveButton.isEnabled());
        }
        if (genericSaveButton.isVisible() && genericSaveButton.isEnabled()) {
            saveGenericTag(false);
            ok = ok && !(genericSaveButton.isVisible() && genericSaveButton.isEnabled());
        }
        if (metadataSaveButton.isVisible() && metadataSaveButton.isEnabled() && Configuration.autoSaveTagModifications.get()) {
            saveMetadataButtonActionPerformed(null);
            ok = ok && !(metadataSaveButton.isVisible() && metadataSaveButton.isEnabled());
        }
        if (fontPanel.isEditing() && Configuration.autoSaveTagModifications.get()) {
            ok = ok && fontPanel.tryAutoSave();
        }
        ok = ok && textPanel.tryAutoSave();

        return ok;
    }

    @Override
    public boolean isEditing() {
        return textPanel.isEditing()
                || (genericSaveButton.isVisible() && genericSaveButton.isEnabled())
                || (metadataSaveButton.isVisible() && metadataSaveButton.isEnabled())
                || (displayEditSaveButton.isVisible() && displayEditSaveButton.isEnabled())
                || (cookieSaveButton.isVisible() && cookieSaveButton.isEnabled())
                || fontPanel.isEditing()
                || imageTransformSaveButton.isVisible();
    }

    public void selectImageDepth(int depth) {
        imagePanel.selectDepth(depth);
    }

    public void startEditPlaceTag() {
        if (!displayEditEditButton.isVisible()) {
            return;
        }
        editDisplayEditTagButtonActionPerformed(null);
    }

    public void startEditMetaDataTag() {
        if (!metadataEditButton.isVisible()) {
            return;
        }
        editMetadataButtonActionPerformed(null);
    }

    public void startEditGenericTag() {
        if (!genericEditButton.isVisible()) {
            return;
        }
        editGenericTagButtonActionPerformed(null);
    }

    public void startEditFontTag() {
        fontPanel.startEdit();
    }

    public void startEditTextTag() {
        textPanel.startEdit();
    }

    public void pauseImage() {
        imagePanel.pause();
    }
}
