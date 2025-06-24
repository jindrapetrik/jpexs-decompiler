/*
 *  Copyright (C) 2022-2025 JPEXS
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
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.painter.border.StandardBorderPainter;
import org.pushingpixels.substance.internal.painter.SimplisticSoftBorderPainter;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceSizeUtils;

/**
 * @author JPEXS
 */
public class ClipboardPanel extends JPanel {

    private JLabel label;
    private JLabel clearButton;

    private MainPanel mainPanel;

    private Timer timer = null;

    public ClipboardPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        label = new JLabel("", View.getIcon("clipboard16"), JLabel.CENTER);
        label.setBorder(new EmptyBorder(0, 0, 0, 10));
        int scrollBarSize = ((Integer) UIManager.get("ScrollBar.width")).intValue();
        setBorder(new EmptyBorder(0, 0, 0, scrollBarSize));
        setLayout(new FlowLayout(FlowLayout.RIGHT));

        clearButton = new JLabel(View.getIcon("cancel16"));
        clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainPanel.emptyClipboard();
            }
        });
        add(label);
        add(clearButton);
        setBackground(mainPanel.tagTree.getBackground());
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        JComponent c = mainPanel.tagTreeScrollPanel;

        if (Configuration.useRibbonInterface.get()) {
            StandardBorderPainter painter = new SimplisticSoftBorderPainter();

            SubstanceColorScheme scheme = SubstanceColorSchemeUtilities
                    .getColorScheme(c, ColorSchemeAssociationKind.BORDER, c
                            .isEnabled() ? ComponentState.ENABLED
                                    : ComponentState.DISABLED_UNSELECTED);

            float borderStrokeWidth = SubstanceSizeUtils
                    .getBorderStrokeWidth(SubstanceSizeUtils
                            .getComponentFontSize(c));
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setStroke(new BasicStroke(borderStrokeWidth,
                    BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            int x = 0;
            int y = 0;

            //top
            g2d.setColor(painter.getTopBorderColor(scheme));
            g2d.drawLine(x, y, x + width, y);

            // left portion
            g2d.setColor(painter.getBottomBorderColor(scheme));
            g2d.drawLine(x, y, x, y + height - 1);

            // right portion
            g2d.setColor(painter.getTopBorderColor(scheme));
            g2d.drawLine(x + width - 1, y, x + width - 1, y + height);
        }

    }

    public void update() {
        int clipboardSize = mainPanel.getClipboardSize();
        if (clipboardSize == 1) {
            label.setText(AppStrings.translate("clipboard.item"));
        } else {
            label.setText(AppStrings.translate("clipboard.items").replace("%count%", "" + clipboardSize));
        }
        if (mainPanel.getClipboardType() == ClipboardType.FRAME) {
            label.setToolTipText(AppStrings.translate("clipboard.hint.frame"));
            clearButton.setToolTipText(AppStrings.translate("clipboard.clear.frame"));
        } else {
            label.setToolTipText(AppStrings.translate("clipboard.hint"));
            clearButton.setToolTipText(AppStrings.translate("clipboard.clear"));
        }
        setVisible(clipboardSize > 0);
    }

    public void flash() {
        if (timer != null) {
            timer.cancel();
        }
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        repaint();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            }
        }, 1000);
    }
}
