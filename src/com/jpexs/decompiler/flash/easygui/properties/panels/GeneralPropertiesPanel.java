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

import com.jpexs.decompiler.flash.easygui.ChangeDoableOperation;
import com.jpexs.decompiler.flash.easygui.EasyStrings;
import com.jpexs.decompiler.flash.easygui.EasySwfPanel;
import com.jpexs.decompiler.flash.easygui.UndoManager;
import com.jpexs.decompiler.flash.easygui.properties.IntegerPropertyField;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.converters.PlaceObjectTypeConverter;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author JPEXS
 */
public class GeneralPropertiesPanel extends AbstractPropertiesPanel {

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
    
    private boolean updating = false;
    
    public GeneralPropertiesPanel(EasySwfPanel swfPanel, UndoManager undoManager) {
        super("general");

        JPanel colorEffectPanel = new JPanel();

        GridBagLayout gridBag = new GridBagLayout();
        colorEffectPanel.setLayout(gridBag);

        GridBagConstraints gbc = new GridBagConstraints();
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

        
        propertiesPanel = new JPanel();
        propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));
        
        propertiesPanel.add(makeCard("colorEffect", null, colorEffectPanel));
        this.swfPanel = swfPanel;

        alphaPercentPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (updating) {
                    return;
                }
                undoManager.doOperation(new ColorEffectChangeDoableOperation() {
                    int value = alphaPercentPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.alphaMultTerm = value * 256 / 100;
                        colorTransform.hasMultTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });
        alphaAddPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (updating) {
                    return;
                }
                undoManager.doOperation(new ColorEffectChangeDoableOperation() {                    
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
                if (updating) {
                    return;
                }
                undoManager.doOperation(new ColorEffectChangeDoableOperation() {
                    int value = redPercentPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.redMultTerm = value * 256 / 100;
                        colorTransform.hasMultTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });
        redAddPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (updating) {
                    return;
                }
                undoManager.doOperation(new ColorEffectChangeDoableOperation() {
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
                if (updating) {
                    return;
                }
                undoManager.doOperation(new ColorEffectChangeDoableOperation() {
                    int value = greenPercentPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.greenMultTerm = value * 256 / 100;
                        colorTransform.hasMultTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });
        greenAddPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (updating) {
                    return;
                }
                undoManager.doOperation(new ColorEffectChangeDoableOperation() {
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
                if (updating) {
                    return;
                }
                undoManager.doOperation(new ColorEffectChangeDoableOperation() {
                    int value = bluePercentPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.blueMultTerm = value * 256 / 100;
                        colorTransform.hasMultTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });
        blueAddPropertyField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (updating) {
                    return;
                }
                undoManager.doOperation(new ColorEffectChangeDoableOperation() {
                    int value = blueAddPropertyField.getValue();
                    @Override
                    public void doColorEffectOperation(CXFORMWITHALPHA colorTransform) {
                        colorTransform.blueAddTerm = value;
                        colorTransform.hasAddTerms = true;
                    }
                }, swfPanel.getSwf());
            }
        });
        
        add(propertiesPanel, BorderLayout.CENTER);;

    }

    public void update() {
        updating = true;        
        DepthState ds = swfPanel.getSelectedDepthState();
        if (ds == null || ds.placeObjectTag == null) {
            propertiesPanel.setVisible(false);
            return;
        }
        propertiesPanel.setVisible(true);
        ColorTransform colorTransform = ds.colorTransForm;
        if (colorTransform == null) {
            alphaPercentPropertyField.setValue(100);
            alphaAddPropertyField.setValue(0);
            redPercentPropertyField.setValue(100);
            redAddPropertyField.setValue(0);
            greenPercentPropertyField.setValue(100);
            greenAddPropertyField.setValue(0);
            bluePercentPropertyField.setValue(100);
            blueAddPropertyField.setValue(0);            
        } else {
            alphaPercentPropertyField.setValue(colorTransform.getAlphaMulti() * 100 / 256);
            alphaAddPropertyField.setValue(colorTransform.getAlphaAdd());
            redPercentPropertyField.setValue(colorTransform.getRedMulti() * 100 / 256);
            redAddPropertyField.setValue(colorTransform.getRedAdd());
            greenPercentPropertyField.setValue(colorTransform.getGreenMulti() * 100 / 256);
            greenAddPropertyField.setValue(colorTransform.getGreenAdd());
            bluePercentPropertyField.setValue(colorTransform.getBlueMulti() * 100 / 256);
            blueAddPropertyField.setValue(colorTransform.getBlueAdd());
        }
        updating = false;
    }

    abstract class PlaceChangeDoableOperation extends ChangeDoableOperation {

        int fdepth = swfPanel.getDepth();
        int fframe = swfPanel.getFrame();

        DepthState depthStateBefore = swfPanel.getSelectedDepthState();
        PlaceObjectTypeTag placeObjectBefore = depthStateBefore.placeObjectTag;
        PlaceObjectTypeTag placeObjectAfter = null;
        private final int minPlace;
        
        private final boolean timelinedModifiedBefore = placeObjectBefore.getTimelined().isModified();
        
        public PlaceChangeDoableOperation(String itemIdentifier, int minPlace) {
            super(itemIdentifier);
            this.minPlace = minPlace;
        }

        @Override
        public final void doOperation() {
            swfPanel.getStagePanel().gotoFrame(fframe + 1);
            swfPanel.getStagePanel().selectDepth(fdepth);

            int convNum = placeObjectBefore.getPlaceObjectNum() < minPlace ? minPlace : placeObjectBefore.getPlaceObjectNum();
            PlaceObjectTypeConverter conv = new PlaceObjectTypeConverter();
            placeObjectAfter = conv.convertTagType(placeObjectBefore, convNum);

            doPlaceOperation(placeObjectAfter, depthStateBefore);
            placeObjectAfter.getTimelined().resetTimeline();            
            update();
        }

        @Override
        public final void undoOperation() {
            swfPanel.getStagePanel().gotoFrame(fframe + 1);
            swfPanel.getStagePanel().selectDepth(fdepth);
            int index = placeObjectAfter.getTimelined().indexOfTag(placeObjectAfter);
            Timelined timelined = placeObjectAfter.getTimelined();
            timelined.removeTag(index);
            timelined.addTag(index, placeObjectBefore);
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

        public ColorEffectChangeDoableOperation() {
            super("colorEffect", 2);
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
