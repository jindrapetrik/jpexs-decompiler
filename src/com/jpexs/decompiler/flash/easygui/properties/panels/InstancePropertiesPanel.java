/*
 * Copyright (C) 2024 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.easygui.properties.panels;

import com.jpexs.decompiler.flash.easygui.EasyStrings;
import com.jpexs.decompiler.flash.easygui.EasySwfPanel;
import com.jpexs.decompiler.flash.easygui.EasyTagNameResolver;
import com.jpexs.decompiler.flash.easygui.UndoManager;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.IntegerPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.PropertyValidationInterface;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.BoundsChangeListener;
import com.jpexs.decompiler.flash.gui.RegistrationPointPosition;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.converters.PlaceObjectTypeConverter;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.jpexs.decompiler.flash.easygui.properties.JTriStateCheckBox;
import com.jpexs.decompiler.flash.easygui.properties.PropertyChangeDoableOperation;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;

/**
 *
 * @author JPEXS
 */
public class InstancePropertiesPanel extends AbstractPropertiesPanel {

    private final FloatPropertyField xPropertyField = new FloatPropertyField(0, -8192, 8192);
    private final FloatPropertyField yPropertyField = new FloatPropertyField(0, -8192, 8192);
    private final FloatPropertyField wPropertyField = new FloatPropertyField(0, -8192, 8192);
    private final FloatPropertyField hPropertyField = new FloatPropertyField(0, -8192, 8192);
    
    private final IntegerPropertyField alphaPercentPropertyField = new IntegerPropertyField(100, -100, 100);
    private final IntegerPropertyField redPercentPropertyField = new IntegerPropertyField(100, -100, 100);
    private final IntegerPropertyField greenPercentPropertyField = new IntegerPropertyField(100, -100, 100);
    private final IntegerPropertyField bluePercentPropertyField = new IntegerPropertyField(100, -100, 100);
    private final IntegerPropertyField alphaAddPropertyField = new IntegerPropertyField(0, -255, 255);
    private final IntegerPropertyField redAddPropertyField = new IntegerPropertyField(0, -255, 255);
    private final IntegerPropertyField greenAddPropertyField = new IntegerPropertyField(0, -255, 255);
    private final IntegerPropertyField blueAddPropertyField = new IntegerPropertyField(0, -255, 255);
    private final EasySwfPanel swfPanel;

    private final JPanel propertiesPanel;
    
    private Rectangle2D lastBounds = null;
    
    private JLabel instanceLabel;
    
    private final JTriStateCheckBox visibleCheckBox = new JTriStateCheckBox();
    private final JComboBox<String> blendingComboBox = new JComboBox<>();
    private final JTriStateCheckBox cacheAsBitmapCheckBox = new JTriStateCheckBox();
    private final JComboBox<String> backgroundComboBox = new JComboBox<>();
    private final JPanel backgroundColorPanel = new JPanel();
    private final JLabel backgroundColorLabel = new JLabel();
    
    private boolean updating = false;
        
    
    public InstancePropertiesPanel(EasySwfPanel swfPanel, UndoManager undoManager) {
        super("instance");
        setLayout(new BorderLayout());

        instanceLabel = new JLabel(EasyStrings.translate("properties.instance.none"));
        instanceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        add(instanceLabel, BorderLayout.NORTH);
        
        GridBagLayout gridBag;
        GridBagConstraints gbc;
        
        JPanel positionSizePanel = new JPanel();        
        gridBag = new GridBagLayout();
        positionSizePanel.setLayout(gridBag);
        gbc = new GridBagConstraints();
        gbc.weightx = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        positionSizePanel.add(new JLabel(formatPropertyName("positionSize.x")), gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        positionSizePanel.add(xPropertyField, gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.EAST;        
        positionSizePanel.add(new JLabel(formatPropertyName("positionSize.y")), gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        positionSizePanel.add(yPropertyField, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;        
        positionSizePanel.add(new JLabel(formatPropertyName("positionSize.width")), gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;        
        positionSizePanel.add(wPropertyField, gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.EAST;        
        positionSizePanel.add(new JLabel(formatPropertyName("positionSize.height")), gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        positionSizePanel.add(hPropertyField, gbc);       
        
        gbc.gridx++;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        positionSizePanel.add(new JPanel(), gbc);
        
        JPanel colorEffectPanel = new JPanel();                       
        
        gridBag = new GridBagLayout();
        colorEffectPanel.setLayout(gridBag);

        gbc = new GridBagConstraints();
        gbc.weightx = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        colorEffectPanel.add(new JLabel(formatPropertyName("colorEffect.alpha")), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx++;
        colorEffectPanel.add(alphaPercentPropertyField, gbc);
        gbc.gridx++;
        colorEffectPanel.add(new JLabel("%   \u00D7 A + "), gbc);
        gbc.gridx++;
        colorEffectPanel.add(alphaAddPropertyField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;        
        colorEffectPanel.add(new JLabel(formatPropertyName("colorEffect.red")), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx++;
        colorEffectPanel.add(redPercentPropertyField, gbc);
        gbc.gridx++;
        colorEffectPanel.add(new JLabel("%   \u00D7 R + "), gbc);
        gbc.gridx++;
        colorEffectPanel.add(redAddPropertyField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;        
        colorEffectPanel.add(new JLabel(formatPropertyName("colorEffect.green")), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx++;
        colorEffectPanel.add(greenPercentPropertyField, gbc);
        gbc.gridx++;
        colorEffectPanel.add(new JLabel("%   \u00D7 G + "), gbc);
        gbc.gridx++;
        colorEffectPanel.add(greenAddPropertyField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;        
        colorEffectPanel.add(new JLabel(formatPropertyName("colorEffect.blue")), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx++;
        colorEffectPanel.add(bluePercentPropertyField, gbc);
        gbc.gridx++;
        colorEffectPanel.add(new JLabel("%   \u00D7 B + "), gbc);
        gbc.gridx++;
        colorEffectPanel.add(blueAddPropertyField, gbc);
        
        gbc.gridx++;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        colorEffectPanel.add(new JPanel(), gbc);

        
        JPanel displayPanel = new JPanel();
        gridBag = new GridBagLayout();
        displayPanel.setLayout(gridBag);        
        gbc = new GridBagConstraints();                        
        
        gbc.insets = new Insets(3, 3, 3, 3);        
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;        
        gbc.anchor = GridBagConstraints.EAST;
        displayPanel.add(new JLabel(formatPropertyName("display.visible")), gbc);
        
        
        gbc.gridx++;
        gbc.gridwidth = 2;        
        gbc.anchor = GridBagConstraints.WEST;
        
        
        displayPanel.add(visibleCheckBox, gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;        
        gbc.anchor = GridBagConstraints.EAST;        
        displayPanel.add(new JLabel(formatPropertyName("display.blending")), gbc);
        
        
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.normal"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.layer"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.multiply"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.screen"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.lighten"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.darken"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.difference"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.add"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.subtract"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.invert"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.alpha"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.erase"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.overlay"));
        blendingComboBox.addItem(EasyStrings.translate("property.instance.display.blending.hardlight"));
        
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 2;        
        displayPanel.add(blendingComboBox, gbc);
                        
        gbc.gridy++;
        
        gbc.gridx = 0;
        gbc.gridwidth = 1;                
        gbc.anchor = GridBagConstraints.EAST;
        displayPanel.add(new JLabel(formatPropertyName("display.cacheAsBitmap")), gbc);
        
        gbc.gridx++;
        gbc.gridwidth = 2;        
        gbc.anchor = GridBagConstraints.WEST;        
        
        displayPanel.add(cacheAsBitmapCheckBox, gbc);
                        
        backgroundComboBox.addItem(EasyStrings.translate("property.instance.display.cacheAsBitmap.transparent"));
        backgroundComboBox.addItem(EasyStrings.translate("property.instance.display.cacheAsBitmap.opaque"));
        
        
        backgroundColorPanel.setLayout(new BorderLayout());
        backgroundColorPanel.add(backgroundColorLabel, BorderLayout.CENTER);
        backgroundColorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        backgroundColorPanel.setPreferredSize(new Dimension(16,16));
        
        
        gbc.gridy++;        
        gbc.gridx = 1;
        gbc.gridwidth = 1;        
        displayPanel.add(backgroundComboBox, gbc);
        gbc.gridx++;       
        displayPanel.add(backgroundColorPanel, gbc);
        
        cacheAsBitmapCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (updating) {
                    return;
                }
                backgroundComboBox.setVisible(cacheAsBitmapCheckBox.isSelected());
                backgroundColorPanel.setVisible(cacheAsBitmapCheckBox.isSelected() && backgroundComboBox.getSelectedIndex() == backgroundComboBox.getItemCount() - 1);
                revalidate();
            }            
        });
        
        backgroundComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (updating) {
                    return;
                }
                backgroundColorPanel.setVisible(backgroundComboBox.getSelectedIndex() == backgroundComboBox.getItemCount() - 1);                
                revalidate();
            }
        });
        
        gbc.gridy++;
        gbc.gridx=0;
        gbc.gridwidth=3;
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        displayPanel.add(new JPanel(), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        displayPanel.add(new JPanel(), gbc);
               
        
        
        propertiesPanel = new JPanel();
        propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));
        
        propertiesPanel.add(makeCard("positionSize", null, positionSizePanel));
        
        setCardOpened("positionSize", true);
        
        propertiesPanel.add(makeCard("colorEffect", null, colorEffectPanel));
        
        propertiesPanel.add(makeCard("display", null, displayPanel));
        
        this.swfPanel = swfPanel;

        alphaPercentPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                undoManager.doOperation(new ColorEffectChangeDoableOperation("alpha") {
                    int value = alphaPercentPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.alphaMultTerm = Math.round(value * 256 / 100f);
                        colorTransform.hasMultTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });
        alphaAddPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                undoManager.doOperation(new ColorEffectChangeDoableOperation("alpha") {                    
                    int value = alphaAddPropertyField.getValue();                    
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.alphaAddTerm = value;
                        colorTransform.hasAddTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });

        redPercentPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                undoManager.doOperation(new ColorEffectChangeDoableOperation("red") {
                    int value = redPercentPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.redMultTerm = Math.round(value * 256 / 100f);
                        colorTransform.hasMultTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });
        redAddPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                undoManager.doOperation(new ColorEffectChangeDoableOperation("red") {
                    int value = redAddPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.redAddTerm = value;
                        colorTransform.hasAddTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });

        greenPercentPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                undoManager.doOperation(new ColorEffectChangeDoableOperation("green") {
                    int value = greenPercentPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.greenMultTerm = Math.round(value * 256 / 100f);
                        colorTransform.hasMultTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });
        greenAddPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                undoManager.doOperation(new ColorEffectChangeDoableOperation("green") {
                    int value = greenAddPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.greenAddTerm = value;
                        colorTransform.hasAddTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });

        bluePercentPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                undoManager.doOperation(new ColorEffectChangeDoableOperation("blue") {
                    int value = bluePercentPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.blueMultTerm = Math.round(value * 256 / 100f);
                        colorTransform.hasMultTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });
        blueAddPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                undoManager.doOperation(new ColorEffectChangeDoableOperation("blue") {
                    int value = blueAddPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.blueAddTerm = value;
                        colorTransform.hasAddTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });
        
        xPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double xBefore = lastBounds.getX();
                double xAfter = xPropertyField.getValue() * 20;
                double xDelta = Math.round(xAfter - xBefore);
                swfPanel.getStagePanel().applyTransformMatrix(Matrix.getTranslateInstance(xDelta, 0));
            }
        });
        
        yPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double yBefore = lastBounds.getY();
                double yAfter = yPropertyField.getValue() * 20;
                double yDelta = Math.round(yAfter - yBefore);
                swfPanel.getStagePanel().applyTransformMatrix(Matrix.getTranslateInstance(0, yDelta));
            }
        });
        
        PropertyValidationInterface<Float> nonZeroFloatValidation = new PropertyValidationInterface<Float>() {
            @Override
            public boolean validate(Float value) {
                return value != 0f;
            }            
        };
        
        wPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {                
                double wBefore = lastBounds.getWidth();
                if (wBefore == 0) {
                    return;
                }
                double wAfter = wPropertyField.getValue() * 20;
                double wScale = wAfter / wBefore;
                Matrix m = new Matrix();
                m.translate(Math.round(lastBounds.getX()), Math.round(lastBounds.getY()));
                m.scale(wScale, 1);
                m.translate(-Math.round(lastBounds.getX()), -Math.round(lastBounds.getY()));
                swfPanel.getStagePanel().applyTransformMatrix(m);
            }
        });
        wPropertyField.addValidation(nonZeroFloatValidation);
        
        hPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {                
                double hBefore = lastBounds.getHeight();
                if (hBefore == 0) {
                    return;
                }
                double hAfter = hPropertyField.getValue() * 20;
                double hScale = hAfter / hBefore;
                Matrix m = new Matrix();
                m.translate(Math.round(lastBounds.getX()), Math.round(lastBounds.getY()));
                m.scale(1, hScale);
                m.translate(-Math.round(lastBounds.getX()), -Math.round(lastBounds.getY()));
                swfPanel.getStagePanel().applyTransformMatrix(m);
            }
        });
        hPropertyField.addValidation(nonZeroFloatValidation);
        
        visibleCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (updating) {
                    return;
                }
                undoManager.doOperation(new PlaceChangeDoableOperation("instance.display.visible", 3) {
                    int visible = visibleCheckBox.isSelected() ? 1 : 0;
                    @Override
                    public void doPlaceOperation(PlaceObjectTypeTag placeObject, DepthState depthState) {
                        placeObject.setVisible(visible);
                    }                    
                }, swfPanel.getSwf());
            }            
        });
        
        blendingComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (updating) {
                    return;
                }
                undoManager.doOperation(new PlaceChangeDoableOperation("instance.display.blending", 3) {
                    int blendMode = blendingComboBox.getSelectedIndex() + 1;
                    @Override
                    public void doPlaceOperation(PlaceObjectTypeTag placeObject, DepthState depthState) {
                        placeObject.setBlendMode(blendMode);
                    }
                }, swfPanel.getSwf());
            }            
        });
        
        cacheAsBitmapCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (updating) {
                    return;
                }
                undoManager.doOperation(new PlaceChangeDoableOperation("instance.display.cacheAsBitmap", 3) {
                    int bitmapCache = cacheAsBitmapCheckBox.isSelected() ? 1 : 0;
                    @Override
                    public void doPlaceOperation(PlaceObjectTypeTag placeObject, DepthState depthState) {
                        placeObject.setBitmapCache(bitmapCache);
                        placeObject.setPlaceFlagHasCacheAsBitmap(bitmapCache == 1);
                        if (bitmapCache == 0) {
                            placeObject.setBackgroundColor(null);
                            placeObject.setPlaceFlagOpaqueBackground(false);
                        }
                    }                    
                }, swfPanel.getSwf());
            }            
        });
        
        backgroundComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (updating) {
                    return;
                }
                boolean isOpaque = backgroundComboBox.getSelectedIndex() == backgroundComboBox.getItemCount() - 1;
                undoManager.doOperation(new PlaceChangeDoableOperation("instance.display.cacheAsBitmap", 3) {
                    RGBA color = isOpaque ? new RGBA(backgroundColorPanel.getBackground()) : null;
                    @Override
                    public void doPlaceOperation(PlaceObjectTypeTag placeObject, DepthState depthState) {                       
                        if (color != null) {
                            backgroundColorLabel.setText("");
                            placeObject.setBackgroundColor(color);
                        } else {
                            placeObject.setPlaceFlagOpaqueBackground(false);
                        }
                    }                    
                }, swfPanel.getSwf());
            }            
        });
        
        backgroundColorPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    Color newColor = ViewMessages.showColorDialog(InstancePropertiesPanel.this, backgroundColorPanel.getBackground(), false);
                    if (newColor != null) {
                        backgroundColorPanel.setBackground(newColor);
                        backgroundColorLabel.setText("");
                        
                        undoManager.doOperation(new PlaceChangeDoableOperation("instance.display.cacheAsBitmap", 3) {
                            RGBA color = new RGBA(newColor);
                            @Override
                            public void doPlaceOperation(PlaceObjectTypeTag placeObject, DepthState depthState) {                       
                                placeObject.setBackgroundColor(color);
                            }                    
                        }, swfPanel.getSwf());
                    }
                }
            }            
        });
        
        add(propertiesPanel, BorderLayout.CENTER);
               
        swfPanel.getStagePanel().addBoundsChangeListener(new BoundsChangeListener() {
            @Override
            public void boundsChanged(Rectangle2D newBounds, Point2D registrationPoint, RegistrationPointPosition registrationPointPosition) {
                lastBounds = newBounds;
                xPropertyField.setValue(twipToPixelValue(newBounds.getX()), true);
                yPropertyField.setValue(twipToPixelValue(newBounds.getY()), true);
                wPropertyField.setValue(twipToPixelValue(newBounds.getWidth()), true);
                hPropertyField.setValue(twipToPixelValue(newBounds.getHeight()), true);
            }            
        });

    }

    private static float twipToPixelValue(double val) {
        float ret = (float) val;
        ret = Math.round(ret);
        ret = ret / 20;
        return ret;
    }
    
    public void update() {
        updating = true;
        List<DepthState> dss = swfPanel.getSelectedDepthStates();
        if (dss == null || dss.isEmpty()) {
            instanceLabel.setText(EasyStrings.translate("properties.instance.none"));
            propertiesPanel.setVisible(false);
            return;
        }
        propertiesPanel.setVisible(true);
        
        if (dss.size() == 1) {
            EasyTagNameResolver resolver = new EasyTagNameResolver();
            instanceLabel.setText(EasyStrings.translate("properties.instance.single").replace("%item%", resolver.getTagName(dss.get(0).getCharacter())));
        } else {
            instanceLabel.setText(EasyStrings.translate("properties.instance.multiple").replace("%count%", "" + dss.size()));
        }
        
        //swfPanel.getStagePanel().get
        
        Set<Integer> alphaPercent = new HashSet<>();
        Set<Integer> alphaAdd = new HashSet<>();
        Set<Integer> redPercent = new HashSet<>();
        Set<Integer> redAdd = new HashSet<>();
        Set<Integer> greenPercent = new HashSet<>();
        Set<Integer> greenAdd = new HashSet<>();
        Set<Integer> bluePercent = new HashSet<>();
        Set<Integer> blueAdd = new HashSet<>();
        Set<Boolean> visible = new HashSet<>();
        Set<Integer> blendMode = new HashSet<>();
        Set<Boolean> cacheAsBitmap = new HashSet<>();
        Set<RGBA> backgroundColor = new HashSet<>();
        
        for (DepthState ds : dss) {
            if (ds == null) {
                continue;
            }
            ColorTransform colorTransform = ds.colorTransForm;
            if (colorTransform == null) {
                alphaPercent.add(100);
                alphaAdd.add(0);
                redPercent.add(100);
                redAdd.add(0);
                greenPercent.add(100);
                greenAdd.add(0);
                bluePercent.add(100);
                blueAdd.add(0);
            } else {
                alphaPercent.add(colorTransform.getAlphaMulti() * 100 / 256);
                alphaAdd.add(colorTransform.getAlphaAdd());
                redPercent.add(colorTransform.getRedMulti() * 100 / 256);
                redAdd.add(colorTransform.getRedAdd());
                greenPercent.add(colorTransform.getGreenMulti() * 100 / 256);
                greenAdd.add(colorTransform.getGreenAdd());
                bluePercent.add(colorTransform.getBlueMulti() * 100 / 256);
                blueAdd.add(colorTransform.getBlueAdd());
            }
            visible.add(ds.isVisible);
            int bm = ds.blendMode;
            if (bm == 0) {
                bm = 1;
            }
            blendMode.add(bm);
            cacheAsBitmap.add(ds.cacheAsBitmap);
            backgroundColor.add(ds.backGroundColor);
        }
        
        
        alphaPercentPropertyField.setValue(alphaPercent, true);
        alphaAddPropertyField.setValue(alphaAdd, true);
        redPercentPropertyField.setValue(redPercent, true);
        redAddPropertyField.setValue(redAdd, true);
        greenPercentPropertyField.setValue(greenPercent, true);
        greenAddPropertyField.setValue(greenAdd, true);
        bluePercentPropertyField.setValue(bluePercent, true);
        blueAddPropertyField.setValue(blueAdd, true);
        
        
        if (visible.size() > 1) {
            visibleCheckBox.setSelectionState(1);
        } else {
            visibleCheckBox.setSelected(visible.iterator().next());
        }       
        
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) blendingComboBox.getModel();            
        if (blendMode.size() > 1) {
            if (!model.getElementAt(0).equals("")) {
                model.addAll(0, Arrays.asList(""));
            }
            blendingComboBox.setSelectedIndex(0);
        } else {
            if (model.getElementAt(0).equals("")) {
                model.removeElementAt(0);
            }            
            int bm = blendMode.iterator().next();
            
            bm--;
            blendingComboBox.setSelectedIndex(bm);
        }
        if (cacheAsBitmap.size() > 1) {
            cacheAsBitmapCheckBox.setSelectionState(1);
        } else {
            cacheAsBitmapCheckBox.setSelected(cacheAsBitmap.iterator().next());
        }
                
        backgroundComboBox.setVisible(cacheAsBitmapCheckBox.getSelectionState() == 2);
        
        
        DefaultComboBoxModel<String> backgroundModel = (DefaultComboBoxModel<String>) backgroundComboBox.getModel();
        if (backgroundColor.size() > 1) {
            
            if (backgroundColor.contains(null)) {
                if (backgroundModel.getSize() == 2) {
                    backgroundModel.addAll(0, Arrays.asList(""));
                }
                backgroundComboBox.setSelectedIndex(0);
                backgroundColorPanel.setVisible(false);
            } else {
                if (backgroundModel.getSize() == 3) {
                    backgroundModel.removeElementAt(0);
                }
                backgroundComboBox.setSelectedIndex(1);
                
                backgroundColorPanel.setVisible(true);
                backgroundColorLabel.setText("--");
                backgroundColorPanel.setBackground(Color.WHITE);
                backgroundColorPanel.setOpaque(true);
            }                                  
        } else {
            if (backgroundModel.getSize() == 3) {
                backgroundModel.removeElementAt(0);
            }
            RGBA bgColor = backgroundColor.iterator().next();
            backgroundComboBox.setSelectedIndex(bgColor == null ? 0 : 1);
            backgroundColorLabel.setText("");
            
            if (bgColor == null) {
                backgroundColorPanel.setVisible(false);
            } else {
                backgroundColorPanel.setVisible(true);
                backgroundColorPanel.setBackground(bgColor.toColor());
                backgroundColorPanel.setOpaque(true);
            }         
            
            
        }
        updating = false;
        revalidate();        
    }

    abstract class PlaceChangeDoableOperation extends PropertyChangeDoableOperation {

        List<Integer> fdepths = swfPanel.getDepths();
        int fframe = swfPanel.getFrame();

        List<DepthState> depthStatesBefore = swfPanel.getSelectedDepthStates();
        List<PlaceObjectTypeTag> placeObjectsBefore = swfPanel.getSelectedPlaceTags();
        List<PlaceObjectTypeTag> placeObjectsAfter = new ArrayList<>();
        
        private final boolean timelinedModifiedBefore = swfPanel.getTimelined().isModified();
        
        private final Timelined timelined = swfPanel.getTimelined();
        
        
        
        public PlaceChangeDoableOperation(String propertyIdentifier, int minPlace) {
            super(propertyIdentifier);
            
            for (int i = 0; i < fdepths.size(); i++) {
                PlaceObjectTypeTag placeObjectBefore = placeObjectsBefore.get(i);
                int convNum = placeObjectBefore.getPlaceObjectNum() < minPlace ? minPlace : placeObjectBefore.getPlaceObjectNum();
                PlaceObjectTypeConverter conv = new PlaceObjectTypeConverter();
                PlaceObjectTypeTag placeObjectAfter = conv.convertTagType(placeObjectBefore, timelined.getSwf(), convNum, false);                
                placeObjectsAfter.add(placeObjectAfter);
            }
        }

        @Override
        public final void doOperation() {
            swfPanel.getStagePanel().gotoFrame(fframe + 1);
            swfPanel.getStagePanel().selectDepths(fdepths);
            
            for (int i = 0; i < fdepths.size(); i++) {
                PlaceObjectTypeTag placeObjectBefore = placeObjectsBefore.get(i);
                PlaceObjectTypeTag placeObjectAfter = placeObjectsAfter.get(i);
                
                int index = timelined.indexOfTag(placeObjectBefore);
                timelined.removeTag(index);
                timelined.addTag(index, placeObjectAfter);
                timelined.setModified(true);
                DepthState depthStateBefore = depthStatesBefore.get(i);                
                doPlaceOperation(placeObjectAfter, depthStateBefore);
            }
            
            timelined.resetTimeline();            
            update();
        }

        @Override
        public final void undoOperation() {
            swfPanel.getStagePanel().gotoFrame(fframe + 1);
            swfPanel.getStagePanel().selectDepths(fdepths);
            for (int i = 0; i < placeObjectsAfter.size(); i++) {
                PlaceObjectTypeTag placeObjectAfter = placeObjectsAfter.get(i);
                PlaceObjectTypeTag placeObjectBefore  = placeObjectsBefore.get(i);
                int index = timelined.indexOfTag(placeObjectAfter);
                timelined.removeTag(index);
                timelined.addTag(index, placeObjectBefore);
            }
            if (!timelinedModifiedBefore) {
                timelined.setModified(false);
            }
            timelined.resetTimeline();
            
            update();
        }

        public abstract void doPlaceOperation(PlaceObjectTypeTag placeObject, DepthState depthState);
    }

    abstract class ColorEffectChangeDoableOperation extends PlaceChangeDoableOperation {

        CXFORMWITHALPHA colorTransformAfter;

        public ColorEffectChangeDoableOperation(String colorProperty) {
            super("instance.colorEffect." + colorProperty, 2);
        }

        @Override
        public final void doPlaceOperation(PlaceObjectTypeTag placeObject, DepthState depthState) {            
            colorTransformAfter = depthState.colorTransForm == null ? new CXFORMWITHALPHA() : new CXFORMWITHALPHA(depthState.colorTransForm);
            doColorEffectOperation(colorTransformAfter);
            placeObject.setColorTransform(colorTransformAfter);
        }

        public abstract void doColorEffectOperation(CXFORMWITHALPHA colorTransform);
    }

}
