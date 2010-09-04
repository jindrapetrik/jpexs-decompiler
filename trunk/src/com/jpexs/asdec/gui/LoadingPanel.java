package com.jpexs.asdec.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel with loading animation
 *
 * @author JPEXS
 */
public class LoadingPanel extends JPanel {
    private int pos = 0;
    private Image animationImage;
    private int iconWidth;
    private int iconHeight;

    /**
     * Constructor
     *
     * @param iconWidth  Width of displayed icon
     * @param iconHeight Height of displayed icon
     */
    public LoadingPanel(int iconWidth, int iconHeight) {
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        ImageIcon icon = (new ImageIcon(this.getClass().getResource("/com/jpexs/asdec/gui/graphics/loading.png")));
        animationImage = icon.getImage();
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new java.util.TimerTask() {
            public void run() {
                pos = (pos + 1) % 12;
                repaint();
            }
        }, 100, 100);
    }

    /**
     * Paints component
     *
     * @param g Graphics to paint on
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(animationImage, getWidth() / 2 - iconWidth / 2, getHeight() / 2 - iconHeight / 2, getWidth() / 2 + iconWidth / 2, getHeight() / 2 + iconHeight / 2, pos * 100, 0, (pos + 1) * 100, 100, this);
    }
}