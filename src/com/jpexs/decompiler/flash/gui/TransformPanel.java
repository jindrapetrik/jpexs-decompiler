/*
 *  Copyright (C) 2022-2024 JPEXS
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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.helpers.Reference;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

/**
 * @author JPEXS
 */
public class TransformPanel extends JPanel {

    private static final int NUMBER_COLS = 7;

    private JTextField moveHorizontalTextField = new JTextField(NUMBER_COLS);
    private JTextField moveVerticalTextField = new JTextField(NUMBER_COLS);
    private JComboBox<Unit> moveUnitComboBox = new JComboBox<>();
    private JCheckBox moveRelativeCheckBox = new JCheckBox(AppStrings.translate("transform.move.relative"));

    private JTextField scaleWidthTextField = new JTextField(formatDouble(100), NUMBER_COLS);
    private JTextField scaleHeightTextField = new JTextField(formatDouble(100), NUMBER_COLS);
    private JComboBox<Unit> scaleUnitComboBox = new JComboBox<>();
    private JCheckBox scaleProportionallyCheckBox = new JCheckBox(AppStrings.translate("transform.scale.proportionally"));

    private JTextField rotateTextField = new JTextField(formatDouble(0), NUMBER_COLS);
    private JComboBox<Unit> rotateUnitComboBox = new JComboBox<>();
    private JToggleButton rotateAntiClockwiseToggleButton = new JToggleButton(View.getIcon("rotateanticlockwise16"));
    private JToggleButton rotateClockwiseToggleButton = new JToggleButton(View.getIcon("rotateclockwise16"));

    private JTextField skewHorizontalTextField = new JTextField(formatDouble(0), NUMBER_COLS);
    private JTextField skewVerticalTextField = new JTextField(formatDouble(0), NUMBER_COLS);
    private JComboBox<Unit> skewUnitComboBox = new JComboBox<>();

    private JTextField matrixATextField = new JTextField(formatDouble(1), NUMBER_COLS);
    private JTextField matrixBTextField = new JTextField(formatDouble(0), NUMBER_COLS);
    private JTextField matrixCTextField = new JTextField(formatDouble(0), NUMBER_COLS);
    private JTextField matrixDTextField = new JTextField(formatDouble(1), NUMBER_COLS);
    private JTextField matrixETextField = new JTextField(formatDouble(0), NUMBER_COLS);
    private JTextField matrixFTextField = new JTextField(formatDouble(0), NUMBER_COLS);
    private JCheckBox matrixEditCurrentCheckBox = new JCheckBox(AppStrings.translate("transform.matrix.editCurrent"));

    private JButton pasteClipboardButton;

    private static final String doublePatternString = "[-+]?([0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?|[0-9]+)";
    private static final Pattern matrixPattern = Pattern.compile("^MATRIX\\[(?<scaleX>" + doublePatternString + "),(?<rotateSkew0>" + doublePatternString + "),(?<rotateSkew1>" + doublePatternString + "),(?<scaleY>" + doublePatternString + "),(?<translateX>" + doublePatternString + "),(?<translateY>" + doublePatternString + ")\\]$");

    private ImagePanel imagePanel;

    private Rectangle2D bounds = new Rectangle2D.Double(0, 0, 1, 1);
    private Point2D registrationPoint = new Point2D.Double(0, 0);

    private RegistrationPointPanel registrationPointPanel;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.000");

    private Map<String, JPanel> cardContents = new LinkedHashMap<>();
    private Map<String, JLabel> cardPlusMinusLabels = new LinkedHashMap<>();

    private static final char PLUS_CHAR = '\u2BC8';
    private static final char MINUS_CHAR = '\u2BC6';

    public static enum UnitKind {
        LENGTH,
        ANGLE
    }

    public static enum Unit {
        PX("px", 1 / 20.0, UnitKind.LENGTH),
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
        this(imagePanel, true);
    }

    public TransformPanel(ImagePanel imagePanel, boolean headerLabel) {

        imagePanel.addBoundsChangeListener(new BoundsChangeListener() {
            @Override
            public void boundsChanged(Rectangle2D newBounds, Point2D registrationPoint, RegistrationPointPosition registrationPointPosition) {
                update(newBounds, registrationPoint, registrationPointPosition);
            }
        });
        //setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        this.imagePanel = imagePanel;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        if (headerLabel) {
            JLabel transformLabel = new JLabel(AppStrings.translate("transform"));
            transformLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            transformLabel.setFont(transformLabel.getFont().deriveFont(Font.BOLD));
            add(transformLabel);
        }

        JPanel registrationPointPanel = new JPanel(new FlowLayout());
        this.registrationPointPanel = new RegistrationPointPanel(this::registrationPointChangedActionPerformed);
        registrationPointPanel.add(this.registrationPointPanel);
        add(makeCard("transformPoint", "transformpoint16", registrationPointPanel));

        JPanel basicPanel = new JPanel();
        basicPanel.setLayout(new BoxLayout(basicPanel, BoxLayout.Y_AXIS));
        JButton flipHorizontallyButton = new JButton(AppStrings.translate("transform.basic.flip.horizontally"), View.getIcon("transformflipx16"));
        flipHorizontallyButton.setHorizontalAlignment(SwingConstants.LEFT);
        flipHorizontallyButton.addActionListener(this::flipHorizontallyActionPerformed);
        JButton flipVerticallyButton = new JButton(AppStrings.translate("transform.basic.flip.vertically"), View.getIcon("transformflipy16"));
        flipVerticallyButton.setHorizontalAlignment(SwingConstants.LEFT);
        flipVerticallyButton.addActionListener(this::flipVerticallyActionPerformed);
        JButton rotate90ClockwiseButton = new JButton(AppStrings.translate("transform.basic.rotate90.clockwise"), View.getIcon("transformrotate90clock16"));
        rotate90ClockwiseButton.setHorizontalAlignment(SwingConstants.LEFT);
        rotate90ClockwiseButton.addActionListener(this::rotate90ClockwiseActionPerformed);
        JButton rotate90AntiClockwiseButton = new JButton(AppStrings.translate("transform.basic.rotate90.anticlockwise"), View.getIcon("transformrotate90anticlock16"));
        rotate90AntiClockwiseButton.setHorizontalAlignment(SwingConstants.LEFT);
        rotate90AntiClockwiseButton.addActionListener(this::rotate90AnticlockwiseActionPerformed);
        JButton rotate180Button = new JButton(AppStrings.translate("transform.basic.rotate180"), View.getIcon("transformrotate18016"));
        rotate180Button.setHorizontalAlignment(SwingConstants.LEFT);
        rotate180Button.addActionListener(this::rotate180ActionPerformed);
        //addRow(basicPanel, 0, flipHorizontallyButton, rotate90ClockwiseButton);
        //addRow(basicPanel, 1, flipVerticallyButton, rotate90AntiClockwiseButton, rotate180Button);
        JPanel basicPanel1 = new JPanel(new FlowLayout());
        basicPanel1.add(flipHorizontallyButton);
        basicPanel1.add(flipVerticallyButton);
        JPanel basicPanel2 = new JPanel(new FlowLayout());
        basicPanel2.add(rotate90ClockwiseButton);
        basicPanel2.add(rotate90AntiClockwiseButton);
        JPanel basicPanel3 = new JPanel(new FlowLayout());
        basicPanel3.add(rotate180Button);
        basicPanel.add(basicPanel1);
        basicPanel.add(basicPanel2);
        basicPanel.add(basicPanel3);
        basicPanel.add(Box.createVerticalGlue());
        add(makeCard("basic", "transformbasic16", basicPanel));

        JPanel movePanel = new JPanel(new GridBagLayout());
        addRow(movePanel, 0, new JLabel(AppStrings.translate("transform.move.horizontal")), moveHorizontalTextField, moveUnitComboBox);
        addRow(movePanel, 1, new JLabel(AppStrings.translate("transform.move.vertical")), moveVerticalTextField);
        addJoinedRow(movePanel, 2, moveRelativeCheckBox, 3);
        addJoinedRow(movePanel, 3, makeClearApplyPanel(this::applyMoveActionPerformed, this::clearMoveActionPerformed), 3);
        finishRow(movePanel, 4);
        add(makeCard("move", "transformmove16", movePanel));

        moveUnitComboBox.addItem(Unit.PX);
        moveUnitComboBox.addItem(Unit.TWIP);

        moveUnitComboBox.setSelectedItem(Unit.PX);

        addUnitChangeListener(moveUnitComboBox, new UnitChangedListener() {
            @Override
            public void unitChanged(Unit prevUnit, Unit newUnit) {
                try {
                    double moveHorizontal = parseDouble(moveHorizontalTextField.getText());
                    double moveVertical = parseDouble(moveVerticalTextField.getText());
                    moveHorizontalTextField.setText(formatDouble(convertUnit(moveHorizontal, prevUnit, newUnit)));
                    moveVerticalTextField.setText(formatDouble(convertUnit(moveVertical, prevUnit, newUnit)));
                } catch (NumberFormatException nfe) {
                    return;
                }
            }
        });

        JPanel scalePanel = new JPanel(new GridBagLayout());
        addRow(scalePanel, 0, new JLabel(AppStrings.translate("transform.scale.width")), scaleWidthTextField, scaleUnitComboBox);
        addRow(scalePanel, 1, new JLabel(AppStrings.translate("transform.scale.height")), scaleHeightTextField);
        addJoinedRow(scalePanel, 2, scaleProportionallyCheckBox, 3);
        addJoinedRow(scalePanel, 3, makeClearApplyPanel(this::applyScaleActionPerformed, this::clearScaleActionPerformed), 3);
        finishRow(scalePanel, 4);
        add(makeCard("scale", "transformscale16", scalePanel));
        scaleUnitComboBox.addItem(Unit.PERCENT);
        scaleUnitComboBox.addItem(Unit.PX);
        scaleUnitComboBox.addItem(Unit.TWIP);

        scaleUnitComboBox.setSelectedItem(Unit.PERCENT);

        addUnitChangeListener(scaleUnitComboBox, new UnitChangedListener() {
            @Override
            public void unitChanged(Unit prevUnit, Unit newUnit) {
                try {
                    double scaleWidth = parseDouble(scaleWidthTextField.getText());
                    double scaleHeight = parseDouble(scaleHeightTextField.getText());

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
                        double scaleWidth = parseDouble(scaleWidthTextField.getText());
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
                        //ignored
                    }
                }
            }
        });

        scaleHeightTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (scaleProportionallyCheckBox.isSelected()) {
                    try {
                        double scaleHeight = parseDouble(scaleHeightTextField.getText());
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
                        //ignored
                    }
                }
            }
        });

        ButtonGroup clockGroup = new ButtonGroup();
        clockGroup.add(rotateClockwiseToggleButton);
        clockGroup.add(rotateAntiClockwiseToggleButton);
        JPanel rotatePanel = new JPanel(new GridBagLayout());
        addRow(rotatePanel, 0, new JLabel(AppStrings.translate("transform.rotate.angle")), rotateTextField, rotateUnitComboBox, rotateAntiClockwiseToggleButton, rotateClockwiseToggleButton);
        addJoinedRow(rotatePanel, 1, makeClearApplyPanel(this::applyRotateActionPerformed, this::clearRotateActionPerformed), 5);
        finishRow(rotatePanel, 2);
        add(makeCard("rotate", "transformrotate16", rotatePanel));

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
                    double rotate = parseDouble(rotateTextField.getText());
                    rotateTextField.setText(formatDouble(convertUnit(rotate, prevUnit, newUnit)));

                } catch (NumberFormatException nfe) {
                    return;
                }
            }
        });

        JPanel skewPanel = new JPanel(new GridBagLayout());
        addRow(skewPanel, 0, new JLabel(AppStrings.translate("transform.skew.horizontal")), skewHorizontalTextField, skewUnitComboBox);
        addRow(skewPanel, 1, new JLabel(AppStrings.translate("transform.skew.vertical")), skewVerticalTextField);
        addJoinedRow(skewPanel, 2, makeClearApplyPanel(this::applySkewActionPerformed, this::clearSkewActionPerformed), 3);
        finishRow(skewPanel, 3);

        add(makeCard("skew", "transformskew16", skewPanel));

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
                    double skewHorizontal = parseDouble(skewHorizontalTextField.getText());
                    double skewVertical = parseDouble(skewVerticalTextField.getText());
                    skewHorizontalTextField.setText(formatDouble(convertUnit(skewHorizontal, prevUnit, newUnit)));
                    skewVerticalTextField.setText(formatDouble(convertUnit(skewVertical, prevUnit, newUnit)));

                } catch (NumberFormatException nfe) {
                    return;
                }
            }
        });

        JPanel matrixPanel = new JPanel(new GridBagLayout());
        addRow(matrixPanel, 0, new JLabel(AppStrings.translate("transform.matrix.a")), matrixATextField,
                new JLabel(AppStrings.translate("transform.matrix.c")), matrixCTextField,
                new JLabel(AppStrings.translate("transform.matrix.e")), matrixETextField);
        addRow(matrixPanel, 1, new JLabel(AppStrings.translate("transform.matrix.b")), matrixBTextField,
                new JLabel(AppStrings.translate("transform.matrix.d")), matrixDTextField,
                new JLabel(AppStrings.translate("transform.matrix.f")), matrixFTextField);
        addJoinedRow(matrixPanel, 2, matrixEditCurrentCheckBox, 6);
        addJoinedRow(matrixPanel, 3, makeClearApplyPanel(this::applyMatrixActionPerformed, this::clearMatrixActionPerformed), 6);
        finishRow(matrixPanel, 4);
        add(makeCard("matrix", "transformmatrix16", matrixPanel));

        matrixEditCurrentCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (matrixEditCurrentCheckBox.isSelected()) {
                    Matrix matrix = imagePanel.getNewMatrix();
                    matrixATextField.setText(formatDouble(matrix.scaleX));
                    matrixBTextField.setText(formatDouble(matrix.rotateSkew0));
                    matrixCTextField.setText(formatDouble(matrix.rotateSkew1));
                    matrixDTextField.setText(formatDouble(matrix.scaleY));
                    matrixETextField.setText(formatDouble(matrix.translateX));
                    matrixFTextField.setText(formatDouble(matrix.translateY));
                } else {
                    matrixATextField.setText(formatDouble(1));
                    matrixBTextField.setText(formatDouble(0));
                    matrixCTextField.setText(formatDouble(0));
                    matrixDTextField.setText(formatDouble(1));
                    matrixETextField.setText(formatDouble(0));
                    matrixFTextField.setText(formatDouble(0));
                }
            }
        });

        JPanel clipboardPanel = new JPanel(new FlowLayout());
        JButton copyClipboardButton = new JButton(AppStrings.translate("transform.clipboard.copy"), View.getIcon("copy16"));
        copyClipboardButton.addActionListener(this::copyClipboardActionPerformed);
        pasteClipboardButton = new JButton(AppStrings.translate("transform.clipboard.paste"), View.getIcon("paste16"));
        pasteClipboardButton.addActionListener(this::pasteClipboardActionPerformed);
        clipboardPanel.add(copyClipboardButton);
        clipboardPanel.add(pasteClipboardButton);
        Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(this::clipBoardflavorsChanged);

        add(makeCard("clipboard", "clipboard16", clipboardPanel));

        add(Box.createVerticalGlue());
        /*JPanel finalPanel = new JPanel();
        //finalPanel.setPreferredSize(new Dimension(1, Integer.MAX_VALUE));
        finalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        finalPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        add(finalPanel);*/
    }

    public void load() {
        clearMoveActionPerformed(null);
        clearScaleActionPerformed(null);
        clearRotateActionPerformed(null);
        clearSkewActionPerformed(null);
        clearMatrixActionPerformed(null);
        loadOpenedCards();
    }

    private void update(Rectangle2D bounds, Point2D registrationPoint, RegistrationPointPosition registrationPointPosition) {
        this.bounds = bounds;
        this.registrationPoint = registrationPoint;
        this.registrationPointPanel.setSelectedPosition(registrationPointPosition);
        if (!moveRelativeCheckBox.isSelected()) {
            moveHorizontalTextField.setText(formatDouble(convertUnit(bounds.getX(), Unit.TWIP, (Unit) moveUnitComboBox.getSelectedItem())));
            moveVerticalTextField.setText(formatDouble(convertUnit(bounds.getY(), Unit.TWIP, (Unit) moveUnitComboBox.getSelectedItem())));
        }
        if (scaleProportionallyCheckBox.isSelected() && scaleUnitComboBox.getSelectedItem() != Unit.PERCENT) {
            try {
                double ratio = bounds.getHeight() / bounds.getWidth();

                double scaleWidth = parseDouble(scaleWidthTextField.getText());
                double scaleHeight = ratio * scaleWidth;
                scaleHeightTextField.setText(formatDouble(scaleHeight));
            } catch (NumberFormatException nfe) {
                //ignored
            }
        }
        if (matrixEditCurrentCheckBox.isSelected()) {
            Matrix matrix = imagePanel.getNewMatrix();
            matrixATextField.setText(formatDouble(matrix.scaleX));
            matrixBTextField.setText(formatDouble(matrix.rotateSkew0));
            matrixCTextField.setText(formatDouble(matrix.rotateSkew1));
            matrixDTextField.setText(formatDouble(matrix.scaleY));
            matrixETextField.setText(formatDouble(matrix.translateX));
            matrixFTextField.setText(formatDouble(matrix.translateY));
        }
    }

    public void clipBoardflavorsChanged(FlavorEvent e) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
            if (hasTransferableText) {
                String result = (String) contents.getTransferData(DataFlavor.stringFlavor);
                if (result != null) {
                    Matcher matcher = matrixPattern.matcher(result);
                    if (matcher.matches()) {
                        pasteClipboardButton.setEnabled(true);
                    } else {
                        pasteClipboardButton.setEnabled(false);
                    }
                } else {
                    pasteClipboardButton.setEnabled(false);
                }
            } else {
                pasteClipboardButton.setEnabled(false);
            }
        } catch (Exception ex) {
            pasteClipboardButton.setEnabled(false);
        }
    }

    private void copyClipboardActionPerformed(ActionEvent e) {
        Matrix matrix = imagePanel.getNewMatrix();
        String copyString = "MATRIX[" + matrix.scaleX + "," + matrix.rotateSkew0 + "," + matrix.rotateSkew1 + "," + matrix.scaleY + "," + matrix.translateX + "," + matrix.translateY + "]";
        StringSelection stringSelection = new StringSelection(copyString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private void pasteClipboardActionPerformed(ActionEvent e) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
            if (hasTransferableText) {
                String result = (String) contents.getTransferData(DataFlavor.stringFlavor);
                if (result != null) {
                    Matcher matcher = matrixPattern.matcher(result);
                    if (matcher.matches()) {
                        Matrix matrix = new Matrix();
                        matrix.scaleX = Double.parseDouble(matcher.group("scaleX"));
                        matrix.rotateSkew0 = Double.parseDouble(matcher.group("rotateSkew0"));
                        matrix.rotateSkew1 = Double.parseDouble(matcher.group("rotateSkew1"));
                        matrix.scaleY = Double.parseDouble(matcher.group("scaleY"));
                        matrix.translateX = Double.parseDouble(matcher.group("translateX"));
                        matrix.translateY = Double.parseDouble(matcher.group("translateY"));

                        matrix = imagePanel.getNewMatrix().inverse().concatenate(matrix);
                        imagePanel.applyTransformMatrix(matrix);
                    }
                }
            }
        } catch (Exception ex) {
            //ignore
        }
    }

    private void clearMoveActionPerformed(ActionEvent e) {
        moveUnitComboBox.setSelectedItem(Unit.PX);
        moveRelativeCheckBox.setSelected(false);
        moveHorizontalTextField.setText(formatDouble(convertUnit(bounds.getX(), Unit.TWIP, Unit.PX)));
        moveVerticalTextField.setText(formatDouble(convertUnit(bounds.getY(), Unit.TWIP, Unit.PX)));
    }

    private void applyMoveActionPerformed(ActionEvent e) {
        Matrix matrix = new Matrix();
        try {
            double moveHorizontal = convertUnit(parseDouble(moveHorizontalTextField.getText()), (Unit) moveUnitComboBox.getSelectedItem(), Unit.TWIP);
            double moveVertical = convertUnit(parseDouble(moveVerticalTextField.getText()), (Unit) moveUnitComboBox.getSelectedItem(), Unit.TWIP);
            if (!moveRelativeCheckBox.isSelected()) {
                matrix.translate(-bounds.getX(), -bounds.getY());
            }
            matrix.translate(moveHorizontal, moveVertical);
            imagePanel.applyTransformMatrix(matrix);
        } catch (NumberFormatException nfe) {
            //ignored
        }
    }

    private void clearScaleActionPerformed(ActionEvent e) {
        scaleUnitComboBox.setSelectedItem(Unit.PERCENT);
        scaleWidthTextField.setText(formatDouble(100));
        scaleHeightTextField.setText(formatDouble(100));
        scaleProportionallyCheckBox.setSelected(true);
    }

    private void applyScaleActionPerformed(ActionEvent e) {
        try {
            double scaleWidth = parseDouble(scaleWidthTextField.getText());
            double scaleHeight = parseDouble(scaleHeightTextField.getText());
            Unit scaleUnit = (Unit) scaleUnitComboBox.getSelectedItem();
            double scaleWidthFactor;
            double scaleHeightFactor;
            if (scaleUnit == Unit.PERCENT) {
                scaleWidthFactor = scaleWidth / 100.0;
                scaleHeightFactor = scaleHeight / 100.0;
            } else {
                scaleWidthFactor = convertUnit(scaleWidth, scaleUnit, Unit.TWIP) / bounds.getWidth();
                scaleHeightFactor = convertUnit(scaleHeight, scaleUnit, Unit.TWIP) / bounds.getHeight();
            }

            Matrix matrix = new Matrix();
            matrix.translate(registrationPoint.getX(), registrationPoint.getY());
            matrix.scale(scaleWidthFactor, scaleHeightFactor);
            matrix.translate(-registrationPoint.getX(), -registrationPoint.getY());
            imagePanel.applyTransformMatrix(matrix);
        } catch (NumberFormatException nfe) {
            //ignored
        }
    }

    private void clearRotateActionPerformed(ActionEvent e) {
        rotateTextField.setText(formatDouble(0));
        rotateUnitComboBox.setSelectedItem(Unit.TURN);
        rotateClockwiseToggleButton.setSelected(true);
    }

    private void applyRotateActionPerformed(ActionEvent e) {
        try {
            double rotate = parseDouble(rotateTextField.getText());
            double rotateRad = (rotateAntiClockwiseToggleButton.isSelected() ? -1.0 : 1.0) * convertUnit(rotate, (Unit) rotateUnitComboBox.getSelectedItem(), Unit.RAD);
            Matrix matrix = new Matrix(AffineTransform.getRotateInstance(rotateRad, registrationPoint.getX(), registrationPoint.getY()));
            imagePanel.applyTransformMatrix(matrix);
        } catch (NumberFormatException nfe) {
            //ignored
        }
    }

    private void clearSkewActionPerformed(ActionEvent e) {
        skewHorizontalTextField.setText(formatDouble(0));
        skewVerticalTextField.setText(formatDouble(0));
        skewUnitComboBox.setSelectedItem(Unit.DEG);
    }

    private void applySkewActionPerformed(ActionEvent e) {
        try {
            Unit skewUnit = (Unit) skewUnitComboBox.getSelectedItem();
            double skewHorizontal = parseDouble(skewHorizontalTextField.getText());
            double skewVertical = parseDouble(skewVerticalTextField.getText());
            double skewHorizontalTwip;
            double skewVerticalTwip;

            if (skewUnit.getKind() == UnitKind.ANGLE) {
                double skewHorizontalRad = convertUnit(skewHorizontal, skewUnit, Unit.RAD);
                skewHorizontalTwip = bounds.getHeight() * Math.tan(skewHorizontalRad);
                double skewVerticalRad = convertUnit(skewVertical, skewUnit, Unit.RAD);
                skewVerticalTwip = bounds.getWidth() * Math.tan(skewVerticalRad);
            } else {
                skewHorizontalTwip = convertUnit(skewHorizontal, skewUnit, Unit.TWIP);
                skewVerticalTwip = convertUnit(skewVertical, skewUnit, Unit.TWIP);
            }
            AffineTransform trans = new AffineTransform();
            trans.translate(registrationPoint.getX(), registrationPoint.getY());
            trans.shear(skewHorizontalTwip / bounds.getWidth(), skewVerticalTwip / bounds.getHeight());
            trans.translate(-registrationPoint.getX(), -registrationPoint.getY());

            Matrix matrix = new Matrix(trans);
            imagePanel.applyTransformMatrix(matrix);
        } catch (NumberFormatException nfe) {
            //ignored
        }
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
        try {
            Matrix matrix = new Matrix();
            matrix.scaleX = parseDouble(matrixATextField.getText());
            matrix.rotateSkew0 = parseDouble(matrixBTextField.getText());
            matrix.rotateSkew1 = parseDouble(matrixCTextField.getText());
            matrix.scaleY = parseDouble(matrixDTextField.getText());
            matrix.translateX = parseDouble(matrixETextField.getText());
            matrix.translateY = parseDouble(matrixFTextField.getText());
            if (matrixEditCurrentCheckBox.isSelected()) {
                matrix = imagePanel.getNewMatrix().inverse().concatenate(matrix);
            }
            imagePanel.applyTransformMatrix(matrix);
        } catch (NumberFormatException nfe) {
            //ignored
        }
    }

    private void applyRotate(double degree) {
        double rotateRad = convertUnit(degree, Unit.DEG, Unit.RAD);
        Matrix matrix = new Matrix(AffineTransform.getRotateInstance(rotateRad, registrationPoint.getX(), registrationPoint.getY()));
        imagePanel.applyTransformMatrix(matrix);
    }

    private void rotate90ClockwiseActionPerformed(ActionEvent e) {
        applyRotate(90);
    }

    private void rotate90AnticlockwiseActionPerformed(ActionEvent e) {
        applyRotate(-90);
    }

    private void rotate180ActionPerformed(ActionEvent e) {
        applyRotate(180);
    }

    private void flipHorizontallyActionPerformed(ActionEvent e) {
        Matrix matrix = new Matrix();
        matrix.translate(registrationPoint.getX(), registrationPoint.getY());
        matrix.scale(-1, 1);
        matrix.translate(-registrationPoint.getX(), -registrationPoint.getY());
        imagePanel.applyTransformMatrix(matrix);
    }

    private void flipVerticallyActionPerformed(ActionEvent e) {
        Matrix matrix = new Matrix();
        matrix.translate(registrationPoint.getX(), registrationPoint.getY());
        matrix.scale(1, -1);
        matrix.translate(-registrationPoint.getX(), -registrationPoint.getY());
        imagePanel.applyTransformMatrix(matrix);
    }

    private void registrationPointChangedActionPerformed(ActionEvent e) {
        imagePanel.setRegistrationPointPosition(registrationPointPanel.getSelectedPosition());
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

    private void finishRow(JPanel panel, int row) {
        /*GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel(" "), c);*/
    }

    private JPanel makeCard(String id, String icon, JPanel contents) {
        JPanel cardPanel = new JPanel();

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(AppStrings.translate("transform." + id));
        if (icon != null) {
            label.setIcon(View.getIcon(icon));
        }
        label.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(label, BorderLayout.CENTER);
        JLabel plusMinusLabel = new JLabel("" + PLUS_CHAR);
        plusMinusLabel.setFont(plusMinusLabel.getFont().deriveFont(plusMinusLabel.getFont().getSize2D() * 1.4f));
        plusMinusLabel.setHorizontalAlignment(JLabel.CENTER);
        plusMinusLabel.setPreferredSize(new Dimension(25, 20));
        headerPanel.add(plusMinusLabel, BorderLayout.WEST);
        headerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        headerPanel.setMinimumSize(new Dimension(0, 30));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    setCardOpened(id, !isCardOpened(id));
                    saveOpenedCards();
                    cardPanel.revalidate();
                    cardPanel.repaint();
                }
            }
        });
        contents.setAlignmentX(Component.LEFT_ALIGNMENT);
        contents.setVisible(false);
        cardPanel.add(headerPanel);
        cardPanel.add(contents);
        contents.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        //contents.setMaximumSize(new Dimension(getPreferredSize().width, contents.getPreferredSize().height + 10));
        cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        cardContents.put(id, contents);
        cardPlusMinusLabels.put(id, plusMinusLabel);
        return cardPanel;
    }

    private boolean isCardOpened(String id) {
        return cardContents.get(id).isVisible();
    }

    private void setCardOpened(String id, boolean opened) {
        JPanel contents = cardContents.get(id);
        contents.setVisible(opened);
        contents.setMaximumSize(new Dimension(Integer.MAX_VALUE, contents.getPreferredSize().height));
        JLabel plusMinusLabel = cardPlusMinusLabels.get(id);
        if (opened) {
            plusMinusLabel.setText("" + MINUS_CHAR);
        } else {
            plusMinusLabel.setText("" + PLUS_CHAR);
        }
    }

    private void loadOpenedCards() {
        List<String> lastOpenedCards = Arrays.asList(Configuration.guiTransformLastExpandedCards.get().split(","));
        for (String id : cardContents.keySet()) {
            setCardOpened(id, lastOpenedCards.contains(id));
        }
        revalidate();
        repaint();
    }

    private void saveOpenedCards() {
        List<String> openedCards = new ArrayList<>();
        for (String id : cardContents.keySet()) {
            if (cardContents.get(id).isVisible()) {
                openedCards.add(id);
            }
        }
        Configuration.guiTransformLastExpandedCards.set(String.join(",", openedCards));
    }

    private JPanel makeClearApplyPanel(ActionListener onApply, ActionListener onClear) {
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton clearButton = new JButton(AppStrings.translate("transform.clear"));
        clearButton.addActionListener(onClear);
        JButton applyButton = new JButton(AppStrings.translate("transform.apply"), View.getIcon("apply16"));
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

    /*@Override
    public Dimension getPreferredSize() {
        return new Dimension(400, super.getPreferredSize().height);
    }*/
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
        return DECIMAL_FORMAT.format(value);
    }

    private static double parseDouble(String value) {
        try {
            return DECIMAL_FORMAT.parse(value).doubleValue();
        } catch (ParseException ex) {
            throw new NumberFormatException();
        }
    }

    class RegistrationPointPanel extends JPanel {

        private Rectangle[][] rects = new Rectangle[3][3];
        private RegistrationPointPosition[][] positions = new RegistrationPointPosition[][]{
            /*x = LEFT */{RegistrationPointPosition.TOP_LEFT, RegistrationPointPosition.LEFT, RegistrationPointPosition.BOTTOM_LEFT},
            /*x = CENTER*/ {RegistrationPointPosition.TOP, RegistrationPointPosition.CENTER, RegistrationPointPosition.BOTTOM},
            /*x = RIGHT*/ {RegistrationPointPosition.TOP_RIGHT, RegistrationPointPosition.RIGHT, RegistrationPointPosition.BOTTOM_RIGHT}};

        private RegistrationPointPosition selectedPosition = RegistrationPointPosition.CENTER;

        final int RECT_SIZE = 10;
        final int SPACE = 4;

        private final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
        private final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

        private ActionListener listener;

        public RegistrationPointPosition getSelectedPosition() {
            return selectedPosition;
        }

        public void setSelectedPosition(RegistrationPointPosition selectedPosition) {
            this.selectedPosition = selectedPosition;
            repaint();
        }

        public RegistrationPointPanel(ActionListener listener) {
            this.listener = listener;
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    rects[x][y] = new Rectangle();
                    rects[x][y].x = x * (RECT_SIZE + SPACE);
                    rects[x][y].y = y * (RECT_SIZE + SPACE);
                    rects[x][y].width = RECT_SIZE;
                    rects[x][y].height = RECT_SIZE;
                }
            }

            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        for (int y = 0; y < 3; y++) {
                            for (int x = 0; x < 3; x++) {
                                if (rects[x][y].contains(e.getPoint())) {
                                    selectedPosition = positions[x][y];
                                    repaint();
                                    listener.actionPerformed(new ActionEvent(RegistrationPointPanel.this, 0, ""));
                                    return;
                                }
                            }
                        }
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    for (int y = 0; y < 3; y++) {
                        for (int x = 0; x < 3; x++) {
                            if (rects[x][y].contains(e.getPoint())) {
                                if (getCursor() != HAND_CURSOR) {
                                    setCursor(HAND_CURSOR);
                                }
                                return;
                            }
                        }
                    }
                    if (getCursor() != DEFAULT_CURSOR) {
                        setCursor(DEFAULT_CURSOR);
                    }
                }

            };
            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(3 * RECT_SIZE + 2 * SPACE + 1, 3 * RECT_SIZE + 2 * SPACE + 1);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setStroke(new BasicStroke(1));
            g2d.setPaint(getForeground());
            GeneralPath path = new GeneralPath();
            for (int y = 0; y < 3; y++) {
                path.moveTo(rects[0][y].getCenterX(), rects[0][y].getCenterY());
                path.lineTo(rects[2][y].getCenterX(), rects[2][y].getCenterY());
            }
            for (int x = 0; x < 3; x++) {
                path.moveTo(rects[x][0].getCenterX(), rects[x][0].getCenterY());
                path.lineTo(rects[x][2].getCenterX(), rects[x][2].getCenterY());
            }

            g2d.draw(path);
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if (positions[x][y] == selectedPosition) {
                        g2d.setPaint(getForeground());
                    } else {
                        g2d.setPaint(getBackground());
                    }
                    g2d.fill(rects[x][y]);
                    g2d.setPaint(getForeground());
                    g2d.draw(rects[x][y]);
                }
            }
        }
    }
}
