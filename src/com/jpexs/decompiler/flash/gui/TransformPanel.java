/*
 * Copyright (C) 2022 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.helpers.Reference;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.BevelBorder;

/**
 *
 * @author JPEXS
 */
public class TransformPanel extends JPanel {

    private JTextField moveHorizontalTextField = new JTextField(8);
    private JTextField moveVerticalTextField = new JTextField(8);
    private JComboBox<Unit> moveUnitComboBox = new JComboBox<>();
    private JCheckBox moveRelativeCheckBox = new JCheckBox("Relative move");

    private JTextField scaleWidthTextField = new JTextField(formatDouble(100), 8);
    private JTextField scaleHeightTextField = new JTextField(formatDouble(100), 8);
    private JComboBox<Unit> scaleUnitComboBox = new JComboBox<>();
    private JCheckBox scaleProportionallyCheckBox = new JCheckBox("Scale proportionally");

    private JTextField rotateTextField = new JTextField(formatDouble(0), 8);
    private JComboBox<Unit> rotateUnitComboBox = new JComboBox<>();
    private JToggleButton rotateAntiClockwiseToggleButton = new JToggleButton(View.getIcon("rotateanticlockwise16"));
    private JToggleButton rotateClockwiseToggleButton = new JToggleButton(View.getIcon("rotateclockwise16"));

    private JTextField skewHorizontalTextField = new JTextField(formatDouble(0), 8);
    private JTextField skewVerticalTextField = new JTextField(formatDouble(0), 8);
    private JComboBox<Unit> skewUnitComboBox = new JComboBox<>();

    private JTextField matrixATextField = new JTextField(formatDouble(1), 8);
    private JTextField matrixBTextField = new JTextField(formatDouble(0), 8);
    private JTextField matrixCTextField = new JTextField(formatDouble(0), 8);
    private JTextField matrixDTextField = new JTextField(formatDouble(1), 8);
    private JTextField matrixETextField = new JTextField(formatDouble(0), 8);
    private JTextField matrixFTextField = new JTextField(formatDouble(0), 8);
    private JCheckBox matrixEditCurrentCheckBox = new JCheckBox("Edit current matrix");

    private ImagePanel imagePanel;

    private Rectangle2D bounds = new Rectangle2D.Double(0, 0, 1, 1);

    public static enum UnitKind {
        LENGTH,
        ANGLE
    }

    public static enum Unit {
        PX("px", 1/20.0, UnitKind.LENGTH),
        TWIP("twip", 1.0, UnitKind.LENGTH),
        PERCENT("%", 0.0, UnitKind.LENGTH),
        TURN("turn", 1 / 360.0, UnitKind.ANGLE),
        DEG("°", 1, UnitKind.ANGLE),
        RAD("rad", Math.PI / 180, UnitKind.ANGLE),
        GRAD("grad", 1 / 0.9, UnitKind.ANGLE);

        private Unit(String name, double value, UnitKind kind) {
            this.name = name;
            this.value = value;
            this.kind = kind;
        }

        private final String name;
        private final double value;
        private final UnitKind kind;

        @Override
        public String toString() {
            return name;
        }

        public double getValue() {
            return value;
        }

        public UnitKind getKind() {
            return kind;
        }
    }

    private class UnitComboItem {

        Unit unit;
        String title;
    }

    public TransformPanel(ImagePanel imagePanel) {

        imagePanel.addBoundsChangeListener(new BoundsChangeListener() {
            @Override
            public void boundsChanged(Rectangle2D newBounds) {
                update(newBounds);
            }
        });
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        this.imagePanel = imagePanel;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        add(makeHeader("Move", "transformmove16"));
        JPanel movePanel = new JPanel(new GridBagLayout());
        addRow(movePanel, 0, new JLabel("Horizontal:"), moveHorizontalTextField, moveUnitComboBox);
        addRow(movePanel, 1, new JLabel("Vertical:"), moveVerticalTextField);
        addJoinedRow(movePanel, 2, moveRelativeCheckBox, 3);
        addJoinedRow(movePanel, 3, makeClearApplyPanel(this::applyMoveActionPerformed, this::clearMoveActionPerformed), 3);
        add(movePanel);

        moveUnitComboBox.addItem(Unit.PX);
        moveUnitComboBox.addItem(Unit.TWIP);

        moveUnitComboBox.setSelectedItem(Unit.PX);

        addUnitChangeListener(moveUnitComboBox, new UnitChangedListener() {
            @Override
            public void unitChanged(Unit prevUnit, Unit newUnit) {
                try {
                    double moveHorizontal = Double.parseDouble(moveHorizontalTextField.getText());
                    double moveVertical = Double.parseDouble(moveVerticalTextField.getText());
                    moveHorizontalTextField.setText(formatDouble(convertUnit(moveHorizontal, prevUnit, newUnit)));
                    moveVerticalTextField.setText(formatDouble(convertUnit(moveVertical, prevUnit, newUnit)));
                } catch (NumberFormatException nfe) {
                    return;
                }
            }
        });

        add(makeHeader("Scale", "transformscale16"));

        JPanel scalePanel = new JPanel(new GridBagLayout());
        addRow(scalePanel, 0, new JLabel("Width:"), scaleWidthTextField, scaleUnitComboBox);
        addRow(scalePanel, 1, new JLabel("Height:"), scaleHeightTextField);
        addJoinedRow(scalePanel, 2, scaleProportionallyCheckBox, 3);
        addJoinedRow(scalePanel, 3, makeClearApplyPanel(this::applyScaleActionPerformed, this::clearScaleActionPerformed), 3);
        add(scalePanel);
        scaleUnitComboBox.addItem(Unit.PERCENT);
        scaleUnitComboBox.addItem(Unit.PX);
        scaleUnitComboBox.addItem(Unit.TWIP);

        scaleUnitComboBox.setSelectedItem(Unit.PERCENT);

        addUnitChangeListener(scaleUnitComboBox, new UnitChangedListener() {
            @Override
            public void unitChanged(Unit prevUnit, Unit newUnit) {
                try {
                    double scaleWidth = Double.parseDouble(scaleWidthTextField.getText());
                    double scaleHeight = Double.parseDouble(scaleHeightTextField.getText());

                    if (prevUnit == Unit.PERCENT) {
                        scaleWidthTextField.setText(formatDouble(convertUnit(bounds.getWidth() * scaleWidth / 100, Unit.TWIP, newUnit)));
                        scaleHeightTextField.setText(formatDouble(convertUnit(bounds.getHeight() * scaleHeight / 100, Unit.TWIP, newUnit)));
                        return;
                    }
                    if (newUnit == Unit.PERCENT) {
                        double scaleWidthTwip = convertUnit(scaleWidth, prevUnit, Unit.TWIP);
                        double scaleHeightTwip = convertUnit(scaleHeight, prevUnit, Unit.TWIP);
                        scaleWidthTextField.setText(formatDouble((scaleWidthTwip * 100 / bounds.getWidth())));
                        scaleHeightTextField.setText(formatDouble((scaleHeightTwip * 100 / bounds.getHeight())));
                        return;
                    }

                    scaleWidthTextField.setText(formatDouble(convertUnit(scaleWidth, prevUnit, newUnit)));
                    scaleHeightTextField.setText(formatDouble(convertUnit(scaleHeight, prevUnit, newUnit)));

                } catch (NumberFormatException nfe) {
                    return;
                }
            }
        });

        scaleWidthTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (scaleProportionallyCheckBox.isSelected()) {                    
                    try {
                        double scaleWidth = Double.parseDouble(scaleWidthTextField.getText());
                        double scaleHeight;
                        if (scaleUnitComboBox.getSelectedItem() == Unit.PERCENT) {
                            scaleHeight = scaleWidth;
                        } else {
                            double ratio = bounds.getHeight() / bounds.getWidth();
                            scaleHeight = ratio * scaleWidth;
                        }
                        scaleWidthTextField.setText(formatDouble(scaleWidth));
                        scaleHeightTextField.setText(formatDouble(scaleHeight));
                    } catch (NumberFormatException nfe) {

                    }
                }
            }
        });

        scaleHeightTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (scaleProportionallyCheckBox.isSelected()) {
                    try {
                        double scaleHeight = Double.parseDouble(scaleHeightTextField.getText());
                        double scaleWidth;
                        if (scaleUnitComboBox.getSelectedItem() == Unit.PERCENT) {
                            scaleWidth = scaleHeight;
                        } else {
                            double ratio = bounds.getWidth() / bounds.getHeight();
                            scaleWidth = ratio * scaleHeight;                            
                        }                        
                        scaleWidthTextField.setText(formatDouble(scaleWidth));
                        scaleHeightTextField.setText(formatDouble(scaleHeight));
                    } catch (NumberFormatException nfe) {

                    }
                }
            }
        });

        add(makeHeader("Rotate", "transformrotate16"));

        ButtonGroup clockGroup = new ButtonGroup();
        clockGroup.add(rotateClockwiseToggleButton);
        clockGroup.add(rotateAntiClockwiseToggleButton);
        JPanel rotatePanel = new JPanel(new GridBagLayout());
        addRow(rotatePanel, 0, new JLabel("Angle:"), rotateTextField, rotateUnitComboBox, rotateAntiClockwiseToggleButton, rotateClockwiseToggleButton);
        addJoinedRow(rotatePanel, 1, makeClearApplyPanel(this::applyRotateActionPerformed, this::clearRotateActionPerformed), 5);

        add(rotatePanel);

        rotateUnitComboBox.addItem(Unit.TURN);
        rotateUnitComboBox.addItem(Unit.DEG);
        rotateUnitComboBox.addItem(Unit.RAD);
        rotateUnitComboBox.addItem(Unit.GRAD);

        rotateUnitComboBox.setSelectedItem(Unit.TURN);

        rotateClockwiseToggleButton.setSelected(true);

        addUnitChangeListener(rotateUnitComboBox, new UnitChangedListener() {
            @Override
            public void unitChanged(Unit prevUnit, Unit newUnit) {
                try {
                    double rotate = Double.parseDouble(rotateTextField.getText());
                    rotateTextField.setText(formatDouble(convertUnit(rotate, prevUnit, newUnit)));

                } catch (NumberFormatException nfe) {
                    return;
                }
            }
        });

        add(makeHeader("Skew", "transformskew16"));

        JPanel skewPanel = new JPanel(new GridBagLayout());
        addRow(skewPanel, 0, new JLabel("Horizontal:"), skewHorizontalTextField, skewUnitComboBox);
        addRow(skewPanel, 1, new JLabel("Vertical:"), skewVerticalTextField);
        addJoinedRow(skewPanel, 2, makeClearApplyPanel(this::applySkewActionPerformed, this::clearSkewActionPerformed), 3);

        add(skewPanel);

        skewUnitComboBox.addItem(Unit.PX);
        skewUnitComboBox.addItem(Unit.TWIP);
        skewUnitComboBox.addItem(Unit.TURN);
        skewUnitComboBox.addItem(Unit.DEG);
        skewUnitComboBox.addItem(Unit.RAD);
        skewUnitComboBox.addItem(Unit.GRAD);

        skewUnitComboBox.setSelectedItem(Unit.DEG);

        addUnitChangeListener(skewUnitComboBox, new UnitChangedListener() {
            @Override
            public void unitChanged(Unit prevUnit, Unit newUnit) {
                try {
                    double skewHorizontal = Double.parseDouble(skewHorizontalTextField.getText());
                    double skewVertical = Double.parseDouble(skewVerticalTextField.getText());
                    skewHorizontalTextField.setText(formatDouble(convertUnit(skewHorizontal, prevUnit, newUnit)));
                    skewVerticalTextField.setText(formatDouble(convertUnit(skewVertical, prevUnit, newUnit)));

                } catch (NumberFormatException nfe) {
                    return;
                }
            }
        });

        add(makeHeader("Matrix", null));

        JPanel matrixPanel = new JPanel(new GridBagLayout());
        addRow(matrixPanel, 0, new JLabel("A"), matrixATextField, new JLabel("C"), matrixCTextField, new JLabel("E"), matrixETextField);
        addRow(matrixPanel, 1, new JLabel("B"), matrixBTextField, new JLabel("D"), matrixDTextField, new JLabel("F"), matrixFTextField);
        addJoinedRow(matrixPanel, 2, matrixEditCurrentCheckBox, 6);
        addJoinedRow(matrixPanel, 3, makeClearApplyPanel(this::applyMatrixActionPerformed, this::clearMatrixActionPerformed), 6);

        add(matrixPanel);

        setVisible(false);
    }

    public void load() {
        clearMoveActionPerformed(null);
        clearScaleActionPerformed(null);
        clearRotateActionPerformed(null);
        clearSkewActionPerformed(null);
        clearMatrixActionPerformed(null);
    }

    private void update(Rectangle2D bounds) {
        this.bounds = bounds;
        if (!moveRelativeCheckBox.isSelected()) {
            moveHorizontalTextField.setText(formatDouble(convertUnit(bounds.getX(), Unit.TWIP, (Unit) moveUnitComboBox.getSelectedItem())));
            moveVerticalTextField.setText(formatDouble(convertUnit(bounds.getY(), Unit.TWIP, (Unit) moveUnitComboBox.getSelectedItem())));
        }
        if (scaleProportionallyCheckBox.isSelected() && scaleUnitComboBox.getSelectedItem() != Unit.PERCENT) {
            try {
                double ratio = bounds.getHeight() / bounds.getWidth();

                double scaleWidth = Double.parseDouble(scaleWidthTextField.getText());
                double scaleHeight = ratio * scaleWidth;
                scaleHeightTextField.setText(formatDouble(scaleHeight));
            } catch (NumberFormatException nfe) {

            }
        }
    }

    private void clearMoveActionPerformed(ActionEvent e) {
        moveHorizontalTextField.setText(formatDouble(convertUnit(bounds.getX(), Unit.TWIP, Unit.PX)));
        moveVerticalTextField.setText(formatDouble(convertUnit(bounds.getY(), Unit.TWIP, Unit.PX)));
        moveUnitComboBox.setSelectedItem(Unit.PX);
        moveRelativeCheckBox.setSelected(false);
    }

    private void applyMoveActionPerformed(ActionEvent e) {
        //TODO
    }

    private void clearScaleActionPerformed(ActionEvent e) {
        scaleUnitComboBox.setSelectedItem(Unit.PERCENT);
        scaleWidthTextField.setText(formatDouble(100));
        scaleHeightTextField.setText(formatDouble(100));
        scaleProportionallyCheckBox.setSelected(true);
    }

    private void applyScaleActionPerformed(ActionEvent e) {

    }

    private void clearRotateActionPerformed(ActionEvent e) {
        rotateTextField.setText(formatDouble(0));
        rotateUnitComboBox.setSelectedItem(Unit.TURN);
        rotateClockwiseToggleButton.setSelected(true);
    }

    private void applyRotateActionPerformed(ActionEvent e) {

    }

    private void clearSkewActionPerformed(ActionEvent e) {
        skewHorizontalTextField.setText(formatDouble(0));
        skewVerticalTextField.setText(formatDouble(0));
        skewUnitComboBox.setSelectedItem(Unit.DEG);
    }

    private void applySkewActionPerformed(ActionEvent e) {

    }

    private void clearMatrixActionPerformed(ActionEvent e) {
        matrixATextField.setText(formatDouble(1));
        matrixBTextField.setText(formatDouble(0));
        matrixCTextField.setText(formatDouble(0));
        matrixDTextField.setText(formatDouble(1));
        matrixETextField.setText(formatDouble(0));
        matrixFTextField.setText(formatDouble(0));
        matrixEditCurrentCheckBox.setSelected(false);
    }

    private void applyMatrixActionPerformed(ActionEvent e) {

    }

    private void addJoinedRow(JPanel panel, int rownum, Component comp, int numCols) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 2, 2, 2);
        c.gridx = 0;
        c.gridy = rownum;
        c.gridwidth = numCols;
        panel.add(comp, c);
    }

    private void addRow(JPanel panel, int rownum, Component... comp) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 2, 2, 2);
        for (int i = 0; i < comp.length; i++) {
            c.gridx = i;
            c.gridy = rownum;
            panel.add(comp[i], c);
        }
    }

    private JPanel makeHeader(String title, String icon) {
        JPanel headerPanel = new JPanel(new FlowLayout());
        JLabel label = new JLabel(title);
        if (icon != null) {
            label.setIcon(View.getIcon(icon));
        }
        headerPanel.add(label);
        headerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        return headerPanel;
    }

    private JPanel makeClearApplyPanel(ActionListener onApply, ActionListener onClear) {
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(onClear);
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(onApply);
        buttonsPanel.add(clearButton);
        buttonsPanel.add(applyButton);
        return buttonsPanel;
    }

    private void addUnitChangeListener(JComboBox<Unit> unitComboBox, UnitChangedListener listener) {
        final Reference<Unit> previousValue = new Reference<>((Unit) unitComboBox.getSelectedItem());
        unitComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Unit newValue = (Unit) e.getItem();
                    listener.unitChanged(previousValue.getVal(), newValue);
                    previousValue.setVal(newValue);
                }
            }
        });
    }

    interface UnitChangedListener {

        public void unitChanged(Unit prevUnit, Unit newUnit);
    }

    private static double convertUnit(double value, Unit sourceUnit, Unit targetUnit) {
        if (sourceUnit == targetUnit) {
            return value;
        }
        if (sourceUnit.kind != targetUnit.kind) {
            return value; //no conversion
        }
        //to px
        return (value / sourceUnit.value) * targetUnit.value;
    }

    private static String formatDouble(double value) {
        DecimalFormat df = new DecimalFormat("0.000");
        return df.format(value);
    }

    public static void main(String[] args) {
        System.out.println("20 twip to px =" + convertUnit(20, Unit.TWIP, Unit.PX));
        System.out.println("1 turn to deg =" + convertUnit(1, Unit.TURN, Unit.DEG));
        System.out.println("1 deg to rad =" + convertUnit(1, Unit.DEG, Unit.RAD));
        System.out.println("1 deg to grad =" + convertUnit(1, Unit.DEG, Unit.GRAD));
    }
}
