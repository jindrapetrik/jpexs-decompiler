/*
 *  Copyright (C) 2024-2025 JPEXS
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
package com.jpexs.decompiler.flash.easygui.properties.panels;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.easygui.ChangeDoableOperation;
import com.jpexs.decompiler.flash.easygui.EasyStrings;
import com.jpexs.decompiler.flash.easygui.UndoManager;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.IntegerPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.PropertyChangeDoableOperation;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ColorSelectionButton;
import com.jpexs.decompiler.flash.gui.ComboBoxItem;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.types.RGB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author JPEXS
 */
public class DocumentPropertiesPanel extends AbstractPropertiesPanel {

    private final JPanel compressionEditorPanel = new JPanel();

    private final JComboBox<ComboBoxItem<SWFCompression>> compressionComboBox = new JComboBox<>();

    private final JPanel versionEditorPanel = new JPanel();

    private final IntegerPropertyField versionEditor = new IntegerPropertyField(SWF.DEFAULT_VERSION, 1, 255);

    private final JCheckBox gfxCheckBox = new JCheckBox();

    private final JCheckBox encryptedCheckBox = new JCheckBox();

    private final JPanel frameRateEditorPanel = new JPanel();

    private final FloatPropertyField frameRateEditor = new FloatPropertyField(24f, 0.01f, 120f);

    private final JPanel propertiesPanel = new JPanel();

    private final JPanel displayRectEditorPanel = new JPanel();

    private final IntegerPropertyField widthEditor = new IntegerPropertyField(550, 1, 8192);

    private final IntegerPropertyField heightEditor = new IntegerPropertyField(400, 1, 8192);

    private final ColorSelectionButton colorSelectionButton;
    
    private final JPanel warningPanel = new JPanel();

    private final JLabel warningLabel = new JLabel();

    private SWF swf;
    
    private boolean modifying = false;

    public DocumentPropertiesPanel(UndoManager undoManager) {
        super("document");
        setLayout(new BorderLayout());

        JLabel documentLabel = new JLabel(EasyStrings.translate("properties.document"));
        documentLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        add(documentLabel, BorderLayout.NORTH);
        
        FlowLayout layout = new FlowLayout(SwingConstants.WEST);
        layout.setHgap(0);
        layout.setVgap(0);

        compressionEditorPanel.setLayout(layout);
        compressionComboBox.addItem(new ComboBoxItem<>(AppStrings.translate("header.uncompressed"), SWFCompression.NONE));
        compressionComboBox.addItem(new ComboBoxItem<>("Zlib", SWFCompression.ZLIB));
        compressionComboBox.addItem(new ComboBoxItem<>("LZMA", SWFCompression.LZMA));
        compressionComboBox.addActionListener((ActionEvent e) -> {
            validateHeader();
        });
        compressionEditorPanel.add(compressionComboBox);

        versionEditorPanel.setLayout(layout);
        versionEditor.setPreferredSize(new Dimension(80, versionEditor.getPreferredSize().height));
        versionEditor.addChangeListener((ChangeEvent e) -> {
            validateHeader();
        });
        versionEditorPanel.add(versionEditor);

        encryptedCheckBox.addChangeListener((ChangeEvent e) -> {
            validateHeader();
        });

        gfxCheckBox.addChangeListener((ChangeEvent e) -> {
            validateHeader();
        });

        frameRateEditorPanel.setLayout(layout);
        frameRateEditor.setPreferredSize(new Dimension(80, frameRateEditor.getPreferredSize().height));
        frameRateEditorPanel.add(frameRateEditor);

        displayRectEditorPanel.setLayout(layout);
        //displayRectEditorPanel.setMinimumSize(new Dimension(10, displayRectEditorPanel.getMinimumSize().height));
        //widthEditor.setPreferredSize(new Dimension(80, widthEditor.getPreferredSize().height));
        //heightEditor.setPreferredSize(new Dimension(80, heightEditor.getPreferredSize().height));
        displayRectEditorPanel.add(widthEditor);
        displayRectEditorPanel.add(new JLabel("\u00D7    "));
        displayRectEditorPanel.add(heightEditor);
        displayRectEditorPanel.add(new JLabel(" px"));
        
        colorSelectionButton = new ColorSelectionButton(Color.white, null);        

        warningLabel.setIcon(View.getIcon("warning16"));
        warningPanel.setLayout(layout);
        warningPanel.setBackground(new Color(255, 213, 29));
        warningPanel.add(warningLabel);

        GridBagLayout gridBag = new GridBagLayout();
        propertiesPanel.setLayout(gridBag);

        int y = 0;
        y++;
        addToGrid(gridBag, propertiesPanel, new JLabel(AppStrings.translate("header.compression")), 0, y);
        addToGrid(gridBag, propertiesPanel, compressionEditorPanel, 1, y);
        y++;
        addToGrid(gridBag, propertiesPanel, new JLabel(AppStrings.translate("header.version")), 0, y);
        addToGrid(gridBag, propertiesPanel, versionEditorPanel, 1, y);
        y++;
        addToGrid(gridBag, propertiesPanel, new JLabel(AppStrings.translate("header.encrypted")), 0, y);
        addToGrid(gridBag, propertiesPanel, encryptedCheckBox, 1, y);
        y++;
        addToGrid(gridBag, propertiesPanel, new JLabel(AppStrings.translate("header.gfx")), 0, y);
        addToGrid(gridBag, propertiesPanel, gfxCheckBox, 1, y);
        y++;
        addToGrid(gridBag, propertiesPanel, new JLabel(AppStrings.translate("header.framerate")), 0, y);
        addToGrid(gridBag, propertiesPanel, frameRateEditorPanel, 1, y);
        y++;
        addToGrid(gridBag, propertiesPanel, new JLabel(AppStrings.translate("header.displayrect")), 0, y);
        addToGrid(gridBag, propertiesPanel, displayRectEditorPanel, 1, y);
        y++;
        addToGrid(gridBag, propertiesPanel, new JLabel(EasyStrings.translate("property.label").replace("%item%", EasyStrings.translate("property.document.backgroundColor"))), 0, y);
        addToGrid(gridBag, propertiesPanel, colorSelectionButton, 1, y);
        y++;
        addToGrid(gridBag, propertiesPanel, warningPanel, 0, y, 2, 1);

        y++;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 3;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        propertiesPanel.add(new JPanel(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = y;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        propertiesPanel.add(new JPanel(), gbc);

        add(propertiesPanel, BorderLayout.CENTER);

        warningPanel.setVisible(false);

        compressionComboBox.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (modifying) {
                    return;
                }
                
                undoManager.doOperation(new HeaderChangeDoableOperation("compression") {
                    SWFCompression itemBefore = swf.compression;
                    SWFCompression itemAfter = getCompression();
                    
                    @Override
                    public void doHeaderOperation() {
                        swf.compression = itemAfter;
                    }

                    @Override
                    public void undoHeaderOperation() {
                        swf.compression = itemBefore;
                    }
                    
                }, swf);
            }
        });
        versionEditor.addChangeListener((ChangeEvent e) -> {
            if (modifying) {
                return;
            }
            undoManager.doOperation(new HeaderChangeDoableOperation("swfVersion") {
                int itemBefore = swf.version;
                int itemAfter = versionEditor.getValue();

                @Override
                public void doHeaderOperation() {
                    swf.version = itemAfter;
                }

                @Override
                public void undoHeaderOperation() {
                    swf.version = itemBefore;
                }
            }, swf);
        });
        encryptedCheckBox.addChangeListener((ChangeEvent e) -> {
            if (modifying) {
                return;
            }
            if (encryptedCheckBox.isSelected() == swf.encrypted) {
                return;
            }
            undoManager.doOperation(new HeaderChangeDoableOperation("encrypted") {
                boolean itemBefore = swf.encrypted;
                boolean itemAfter = encryptedCheckBox.isSelected();
                @Override
                public void doHeaderOperation() {
                    swf.encrypted = itemAfter;                    
                }

                @Override
                public void undoHeaderOperation() {
                    swf.encrypted = itemBefore;                    
                }
            }, swf);
        });
        gfxCheckBox.addChangeListener((ChangeEvent e) -> {
            if (modifying) {
                return;
            }
            if (gfxCheckBox.isSelected() == swf.gfx) {
                return;
            }
            
            undoManager.doOperation(new HeaderChangeDoableOperation("gfx") {
                boolean itemBefore = swf.gfx;
                boolean itemAfter = gfxCheckBox.isSelected();

                @Override
                public void doHeaderOperation() {
                    swf.gfx = itemAfter;
                }

                @Override
                public void undoHeaderOperation() {
                    swf.gfx = itemBefore;                    
                }
            }, swf);
        });
        frameRateEditor.addChangeListener((ChangeEvent e) -> {
            if (modifying) {
                return;
            }
            undoManager.doOperation(new HeaderChangeDoableOperation("frameRate") {
                float itemBefore = swf.frameRate;
                float itemAfter = frameRateEditor.getValue();

                @Override
                public void doHeaderOperation() {
                    swf.frameRate = itemAfter;
                }

                @Override
                public void undoHeaderOperation() {
                    swf.frameRate = itemBefore;                    
                }
            }, swf);
        });
        widthEditor.addChangeListener((ChangeEvent e) -> {
            if (modifying) {
                return;
            }
            undoManager.doOperation(new HeaderChangeDoableOperation("width") {
                int xMinBefore = swf.displayRect.Xmin;
                int xMaxBefore = swf.displayRect.Xmax;
                int xMaxAfter = widthEditor.getValue() * 20;

                @Override
                public void doHeaderOperation() {
                    swf.displayRect.Xmin = 0;
                    swf.displayRect.Xmax = xMaxAfter;                    
                }

                @Override
                public void undoHeaderOperation() {
                    swf.displayRect.Xmin = xMinBefore;
                    swf.displayRect.Xmax = xMaxBefore;
                }
            }, swf);
        });
        heightEditor.addChangeListener((ChangeEvent e) -> {
            if (modifying) {
                return;
            }
            undoManager.doOperation(new HeaderChangeDoableOperation("height") {
                int yMinBefore = swf.displayRect.Ymin;
                int yMaxBefore = swf.displayRect.Ymax;
                int yMaxAfter = heightEditor.getValue() * 20;

                @Override
                public void doHeaderOperation() {
                    swf.displayRect.Ymin = 0;
                    swf.displayRect.Ymax = yMaxAfter;
                }

                @Override
                public void undoHeaderOperation() {
                    swf.displayRect.Ymin = yMinBefore;
                    swf.displayRect.Ymax = yMaxBefore;
                }
            }, swf);
        });
        
        colorSelectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SetBackgroundColorTag backgroundColorTag = swf.getBackgroundColor();
                if (backgroundColorTag == null) {
                    return; //???
                }
                
                undoManager.doOperation(new PropertyChangeDoableOperation("document.backgroundColor") {
                    final RGB prevColor = backgroundColorTag.backgroundColor;
                    final RGB newColor = new RGB(colorSelectionButton.getValue());
                    final boolean prevModified = backgroundColorTag.isModified();
                    
                    @Override
                    public void doOperation() {
                        modifying = true;
                        SetBackgroundColorTag backgroundColorTag = swf.getBackgroundColor();
                        if (backgroundColorTag == null) {
                            return;
                        }
                        backgroundColorTag.backgroundColor = newColor;
                        backgroundColorTag.setModified(true);
                        swf.resetTimeline();
                        refresh();
                        modifying = false;
                    }

                    @Override
                    public void undoOperation() {
                        SetBackgroundColorTag backgroundColorTag = swf.getBackgroundColor();
                        if (backgroundColorTag == null) {
                            return;
                        }
                        backgroundColorTag.backgroundColor = prevColor;
                        backgroundColorTag.setModified(prevModified);
                        swf.resetTimeline();
                        refresh();
                    }                    
                }, swf);
            }
        });
    }

    

    private boolean validateHeader() {
        int version = getVersionNumber();
        boolean gfx = gfxCheckBox.isSelected();
        boolean encrypted = encryptedCheckBox.isSelected();
        SWFCompression compression = getCompression();
        List<String> results = new ArrayList<>();
        if (gfx && !(compression == SWFCompression.NONE || compression == SWFCompression.ZLIB)) {
            results.add(AppStrings.translate("header.warning.unsupportedGfxCompression"));
        }

        if (gfx && encrypted) {
            results.add(AppStrings.translate("header.warning.unsupportedGfxEncryption"));
        }

        if (compression == SWFCompression.ZLIB && version < 6) {
            results.add(AppStrings.translate("header.warning.minimumZlibVersion"));
        }

        if (compression == SWFCompression.LZMA && version < 13) {
            results.add(AppStrings.translate("header.warning.minimumLzmaVersion"));
        }

        warningPanel.setVisible(!results.isEmpty());
        if (!results.isEmpty()) {
            warningLabel.setText("<html>" + String.join("<br>", results) + "</html>");
        }

        return results.isEmpty();
    }

    private int getVersionNumber() {
        return versionEditor.getValue();
    }

    private SWFCompression getCompression() {
        @SuppressWarnings("unchecked")
        ComboBoxItem<SWFCompression> item = (ComboBoxItem<SWFCompression>) compressionComboBox.getSelectedItem();
        return item.getValue();
    }

    private void refresh() {
        modifying = true;
        propertiesPanel.setVisible(swf != null);
        if (swf == null) {
            modifying = false;
            return;
        }
        switch (swf.compression) {
            case LZMA:
                compressionComboBox.setSelectedIndex(2);
                break;
            case ZLIB:
                compressionComboBox.setSelectedIndex(1);
                break;
            case NONE:
                compressionComboBox.setSelectedIndex(0);
                break;
        }

        versionEditor.setValue(swf.version);

        encryptedCheckBox.setSelected(swf.encrypted);

        gfxCheckBox.setSelected(swf.gfx);

        frameRateEditor.setValue(swf.frameRate);

        widthEditor.setValue(swf.displayRect.getWidth() / 20);
        heightEditor.setValue(swf.displayRect.getHeight() / 20);
        
        SetBackgroundColorTag backgroundColorTag = swf.getBackgroundColor();
        
        if (backgroundColorTag != null) {
            colorSelectionButton.setValue(backgroundColorTag.backgroundColor.toColor());
        }
        
        modifying = false;
    }

    public void setSwf(SWF swf) {

        this.swf = swf;       
        refresh();        
    }

    abstract class HeaderChangeDoableOperation extends ChangeDoableOperation {

        private boolean modifiedBefore = swf.isHeaderModified();

        public HeaderChangeDoableOperation(String itemIdentifier) {
            super(itemIdentifier);
        }

        @Override
        public final void doOperation() {
            modifying = true;
            doHeaderOperation();
            swf.setHeaderModified(true);
            refresh();
            validateHeader();
            modifying = false;
        }

        @Override
        public final void undoOperation() {
            modifying = true;
            undoHeaderOperation();
            swf.setHeaderModified(modifiedBefore);
            refresh();
            validateHeader();
            modifying = false;
        }

        public abstract void doHeaderOperation();

        public abstract void undoHeaderOperation();
    }
}
