/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.decompiler.flash.gui.player;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 *
 * @author JPEXS
 */
public class PlayerControls extends JPanel implements MediaDisplayListener {

    private final JButton pauseButton;

    private final JButton loopButton;

    private MediaDisplay display;

    private JProgressBar progress;

    private final JLabel timeLabel;

    private final JLabel frameLabel;

    private final JLabel totalTimeLabel;

    private final JLabel totalFrameLabel;

    private static final Icon pauseIcon = View.getIcon("pause16");

    private static final Icon playIcon = View.getIcon("play16");

    private static final Icon loopIcon = View.getIcon("loopon16");

    private static final Icon noLoopIcon = View.getIcon("loopoff16");

    private final JLabel percentLabel = new JLabel("100%");

    private final JPanel zoomPanel;

    private final JPanel graphicControls;

    private final JPanel playbackControls;

    private final JPanel frameControls;

    private boolean zoomToFit = false;

    private double realZoom = 1.0;

    private final JButton zoomFitButton;

    private final JButton snapshotButton;

    public static final int ZOOM_DECADE_STEPS = 10;

    public static final double ZOOM_MULTIPLIER = Math.pow(10, 1.0 / ZOOM_DECADE_STEPS);

    private static String underline(String s) {
        return "<html><font color=\"#000099\"><u>" + s + "</u></font></html>";
    }

    private static Font underlinedFont = null;

    private static Font notUnderlinedFont = null;

    private final int zeroCharacterWidth;

    static {
        Font font = new JLabel().getFont();
        notUnderlinedFont = font;
        Map<TextAttribute, Integer> fontAttributes = new HashMap<>();
        fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        underlinedFont = font.deriveFont(fontAttributes);
    }

    public PlayerControls(final MainPanel mainPanel, MediaDisplay display) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        graphicControls = new JPanel(new BorderLayout());
        JPanel graphicButtonsPanel = new JPanel(new FlowLayout());
        JButton selectColorButton = new JButton(View.getIcon("color16"));
        selectColorButton.addActionListener(this::selectBkColorButtonActionPerformed);
        selectColorButton.setToolTipText(AppStrings.translate("button.selectbkcolor.hint"));

        JButton zoomInButton = new JButton(View.getIcon("zoomin16"));
        zoomInButton.addActionListener(this::zoomInButtonActionPerformed);
        zoomInButton.setToolTipText(AppStrings.translate("button.zoomin.hint"));

        JButton zoomOutButton = new JButton(View.getIcon("zoomout16"));
        zoomOutButton.addActionListener(this::zoomOutButtonActionPerformed);
        zoomOutButton.setToolTipText(AppStrings.translate("button.zoomout.hint"));

        zoomFitButton = new JButton(View.getIcon("zoomfit16"));
        zoomFitButton.addActionListener(this::zoomFitButtonActionPerformed);
        zoomFitButton.setToolTipText(AppStrings.translate("button.zoomfit.hint"));

        JButton zoomNoneButton = new JButton(View.getIcon("zoomnone16"));
        zoomNoneButton.addActionListener(this::zoomNoneButtonActionPerformed);
        zoomNoneButton.setToolTipText(AppStrings.translate("button.zoomnone.hint"));

        snapshotButton = new JButton(View.getIcon("snapshot16"));
        snapshotButton.addActionListener(this::snapShotButtonActionPerformed);
        snapshotButton.setToolTipText(AppStrings.translate("button.snapshot.hint"));
        snapshotButton.setVisible(false);

        zoomPanel = new JPanel(new FlowLayout());
        zoomPanel.add(percentLabel);
        zoomPanel.add(zoomInButton);
        zoomPanel.add(zoomOutButton);
        zoomPanel.add(zoomNoneButton);
        zoomPanel.add(zoomFitButton);
        zoomPanel.add(selectColorButton);
        zoomPanel.setVisible(false);

        graphicButtonsPanel.add(zoomPanel);
        graphicButtonsPanel.add(snapshotButton);
        graphicControls.add(graphicButtonsPanel, BorderLayout.EAST);
        graphicControls.setVisible(false);

        add(graphicControls);
        graphicControls.setVisible(display.screenAvailable());

        playbackControls = new JPanel();
        this.display = display;
        JPanel controlPanel = new JPanel(new BorderLayout());
        frameLabel = new JLabel("0", SwingConstants.CENTER);
        frameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        frameLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });

        frameLabel.setVisible(false);

        zeroCharacterWidth = frameLabel.getFontMetrics(notUnderlinedFont).stringWidth("0");

        frameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                frameLabel.setFont(underlinedFont);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                frameLabel.setFont(notUnderlinedFont);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int gotoFrame = PlayerControls.this.display.getCurrentFrame();
                if (gotoFrame > 0) {
                    mainPanel.gotoFrame(gotoFrame);
                }
            }
        });
        timeLabel = new JLabel("(00:00.00)");
        totalTimeLabel = new JLabel("(00:00.00)");
        totalFrameLabel = new JLabel("0");
        totalFrameLabel.setVisible(false);

        frameControls = new JPanel(new FlowLayout());

        JButton prevFrameButton = new JButton(View.getIcon("prevframe16"));
        prevFrameButton.setToolTipText(AppStrings.translate("preview.prevframe"));
        prevFrameButton.setMargin(new Insets(4, 2, 2, 2));
        prevFrameButton.addActionListener(this::prevFrameButtonActionPerformed);
        frameControls.add(prevFrameButton);
        frameControls.setVisible(display.screenAvailable());

        frameControls.add(frameLabel);

        JButton nextFrameButton = new JButton(View.getIcon("nextframe16"));
        nextFrameButton.setToolTipText(AppStrings.translate("preview.nextframe"));
        nextFrameButton.setMargin(new Insets(4, 2, 2, 2));
        nextFrameButton.addActionListener(this::nextFrameButtonActionPerformed);
        frameControls.add(nextFrameButton);

        JButton gotoFrameButton = new JButton(View.getIcon("gotoframe16"));
        gotoFrameButton.setToolTipText(AppStrings.translate("preview.gotoframe"));
        gotoFrameButton.setMargin(new Insets(4, 2, 2, 2));
        gotoFrameButton.addActionListener(this::gotoFrameButtonActionPerformed);
        frameControls.add(gotoFrameButton);
        frameControls.setVisible(false);

        JPanel currentPanel = new JPanel(new FlowLayout());
        currentPanel.add(frameControls);
        currentPanel.add(timeLabel);
        JPanel totalPanel = new JPanel(new FlowLayout());
        totalPanel.add(totalFrameLabel);
        totalPanel.add(totalTimeLabel);

        controlPanel.add(currentPanel, BorderLayout.WEST);
        controlPanel.add(totalPanel, BorderLayout.EAST);
        playbackControls.setLayout(new BoxLayout(playbackControls, BoxLayout.Y_AXIS));
        JPanel buttonsPanel = new JPanel(new FlowLayout());

        pauseButton = new JButton(pauseIcon);
        pauseButton.setToolTipText(AppStrings.translate("preview.pause"));
        pauseButton.setMargin(new Insets(4, 2, 2, 2));
        pauseButton.addActionListener(this::pauseButtonActionPerformed);
        JButton stopButton = new JButton(View.getIcon("stop16"));
        stopButton.setToolTipText(AppStrings.translate("preview.stop"));
        stopButton.setMargin(new Insets(4, 2, 2, 2));
        stopButton.addActionListener(this::stopButtonActionPerformed);
        loopButton = new JButton(pauseIcon);
        loopButton.setToolTipText(AppStrings.translate("preview.loop"));
        loopButton.setMargin(new Insets(4, 2, 2, 2));
        loopButton.addActionListener(this::loopButtonActionPerformed);
        boolean loop = Configuration.loopMedia.get();
        loopButton.setIcon(loop ? loopIcon : noLoopIcon);
        buttonsPanel.add(pauseButton);
        buttonsPanel.add(stopButton);
        buttonsPanel.add(loopButton);
        controlPanel.add(buttonsPanel, BorderLayout.CENTER);

        progress = new JProgressBar();
        Dimension pref = progress.getPreferredSize();
        pref.height = 20;
        progress.setPreferredSize(pref);
        final PlayerControls t = this;
        progress.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int frame = (int) Math.floor(e.getX() * t.display.getTotalFrames() / (double) progress.getWidth());
                boolean p = t.display.isPlaying();
                t.display.gotoFrame(frame);
                if (p) {
                    t.display.play();
                }
            }
        });
        playbackControls.add(progress);
        playbackControls.add(controlPanel);

        add(playbackControls);
        this.display.addEventListener(this);
    }

    private String formatMs(long ms) {
        long s = ms / 1000;
        ms %= 1000;
        long m = s / 60;
        s %= 60;
        long h = m / 60;
        m %= 60;
        return (h > 0 ? h + ":" : "") + pad(m) + ":" + pad(s) + "." + pad(ms / 10);
    }

    private String pad(long t) {
        String ret = Long.toString(t);
        while (ret.length() < 2) {
            ret = "0" + ret;
        }
        return ret;
    }

    public void setMedia(MediaDisplay media) {
        if (this.display != null) {
            this.display.removeEventListener(this);
        }

        this.display = media;
        this.display.addEventListener(this);

        update();
    }

    private void update() {
        if (!display.isLoaded()) {
            return;
        }

        View.execInEventDispatchLater(() -> {
            updateZoom();
            int totalFrames = display.getTotalFrames();
            int currentFrame = display.getCurrentFrame();
            if (currentFrame >= totalFrames) {
                currentFrame = totalFrames - 1;
            }
            float frameRate = display.getFrameRate();
            Zoom zoom = display.getZoom();
            zoomFitButton.setVisible(zoom != null);
            percentLabel.setVisible(zoom != null);
            zoomPanel.setVisible(display.zoomAvailable());
            boolean screenAvailable = display.screenAvailable();
            snapshotButton.setVisible(screenAvailable);
            graphicControls.setVisible(screenAvailable);
            totalFrameLabel.setVisible(screenAvailable);
            frameLabel.setVisible(screenAvailable);
            if (screenAvailable) {
                int charCount = Math.max(Integer.toString(totalFrames).length(), 3);
                Dimension min = new Dimension(zeroCharacterWidth * charCount, frameLabel.getPreferredSize().height);
                frameLabel.setMinimumSize(min);
                frameLabel.setPreferredSize(min);
            }

            frameControls.setVisible(screenAvailable);
            if (totalFrames == 0) {
                progress.setIndeterminate(true);
            } else {
                progress.setMaximum(totalFrames - 1);
                progress.setMinimum(0);
                progress.setValue(currentFrame);
                progress.setIndeterminate(false);
            }
            frameLabel.setText(Integer.toString(currentFrame + 1));
            totalFrameLabel.setText(Integer.toString(totalFrames));
            if (frameRate != 0) {
                timeLabel.setText("(" + formatMs((int) (currentFrame * 1000.0 / frameRate)) + ")");
                totalTimeLabel.setText("(" + formatMs((int) (totalFrames * 1000.0 / frameRate)) + ")");
            }
            if (totalFrames <= 1 && playbackControls.isVisible()) {
                playbackControls.setVisible(false);
            }
            if (totalFrames > 1 && !playbackControls.isVisible()) {
                playbackControls.setVisible(true);
            }
            boolean paused1 = !display.isPlaying();
            if (paused1) {
                pauseButton.setToolTipText(AppStrings.translate("preview.play"));
                pauseButton.setIcon(playIcon);
            } else {
                pauseButton.setToolTipText(AppStrings.translate("preview.pause"));
                pauseButton.setIcon(pauseIcon);
            }
        });

    }

    private static double roundZoom(double realZoom, int mantisa) {
        double l10 = Math.log10(realZoom);
        int lg = (int) (-Math.floor(l10) + mantisa - 1);
        if (lg < 0) {
            lg = 0;
        }
        BigDecimal bd = new BigDecimal(String.valueOf(realZoom)).setScale(lg, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    private void updateZoom() {
        double pctzoom = roundZoom(getRealZoom() * 100, 3);
        String r = Double.toString(pctzoom);
        double zoom = pctzoom / 100.0;
        if (r.endsWith(".0")) {
            r = r.substring(0, r.length() - 2);
        }

        r += "%";

        if (zoomToFit) {
            percentLabel.setText(AppStrings.translate("fit") + " (" + r + ")");
        } else {
            percentLabel.setText(r);
        }

        Zoom zoomObj = new Zoom();
        zoomObj.value = zoom;
        zoomObj.fit = zoomToFit;
        display.zoom(zoomObj);
    }

    private void pauseButtonActionPerformed(ActionEvent evt) {
        if (display.isPlaying()) {
            display.pause();
        } else {
            display.play();
        }
    }

    private void loopButtonActionPerformed(ActionEvent evt) {
        boolean loop = !Configuration.loopMedia.get();
        Configuration.loopMedia.set(loop);
        loopButton.setIcon(loop ? loopIcon : noLoopIcon);
        display.setLoop(loop);
    }

    private void gotoFrameButtonActionPerformed(ActionEvent evt) {
        final JPanel gotoPanel = new JPanel(new BorderLayout());
        final JTextField frameField = new JTextField("" + display.getCurrentFrame());
        gotoPanel.add(new JLabel(AppStrings.translate("preview.gotoframe.dialog.message").replace("%min%", "1").replace("%max%", "" + display.getTotalFrames())), BorderLayout.NORTH);
        gotoPanel.add(frameField, BorderLayout.CENTER);
        gotoPanel.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                final AncestorListener al = this;
                View.execInEventDispatch(() -> {
                    frameField.selectAll();
                    frameField.requestFocusInWindow();
                    gotoPanel.removeAncestorListener(al);
                });

            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {

            }

            @Override
            public void ancestorMoved(AncestorEvent event) {

            }
        });
        if (View.showConfirmDialog(this, gotoPanel, AppStrings.translate("preview.gotoframe.dialog.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            int frame = -1;
            try {
                frame = Integer.parseInt(frameField.getText());
            } catch (NumberFormatException nfe) {
                //handled as -1
            }
            if (frame <= 0 || frame > display.getTotalFrames()) {
                View.showMessageDialog(this, AppStrings.translate("preview.gotoframe.dialog.frame.error").replace("%min%", "1").replace("%max%", "" + display.getTotalFrames()), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            display.gotoFrame(frame - 1);
        }
    }

    private void nextFrameButtonActionPerformed(ActionEvent evt) {
        display.gotoFrame(display.getCurrentFrame() + 1);
    }

    private void prevFrameButtonActionPerformed(ActionEvent evt) {
        display.gotoFrame(display.getCurrentFrame() - 1);
    }

    private void stopButtonActionPerformed(ActionEvent evt) {
        display.stop();
    }

    private void selectBkColorButtonActionPerformed(ActionEvent evt) {
        Color newColor = JColorChooser.showDialog(null, AppStrings.translate("dialog.selectbkcolor.title"), View.getSwfBackgroundColor());
        if (newColor != null) {
            View.setSwfBackgroundColor(newColor);
            display.setBackground(newColor);
        }
    }

    private void zoomInButtonActionPerformed(ActionEvent evt) {
        realZoom = getRealZoom() * ZOOM_MULTIPLIER;
        zoomToFit = false;
        updateZoom();
    }

    private void zoomOutButtonActionPerformed(ActionEvent evt) {
        realZoom = getRealZoom() / ZOOM_MULTIPLIER;
        zoomToFit = false;
        updateZoom();
    }

    private void zoomNoneButtonActionPerformed(ActionEvent evt) {
        realZoom = 1.0;
        zoomToFit = false;
        updateZoom();
    }

    private void zoomFitButtonActionPerformed(ActionEvent evt) {
        realZoom = 1.0;
        zoomToFit = true;
        updateZoom();
    }

    private void snapShotButtonActionPerformed(ActionEvent evt) {
        putImageToClipBoard(display.printScreen());
    }

    private double getRealZoom() {
        if (zoomToFit) {
            return display.getZoomToFit();
        }

        return realZoom;
    }

    @Override
    public void mediaDisplayStateChanged(MediaDisplay source) {
        if (display != source) {
            return;
        }

        update();
    }

    @Override
    public void playingFinished(MediaDisplay source) {
    }

    private class TransferableImage implements Transferable {

        Image img;

        public TransferableImage(Image img) {
            this.img = img;
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (flavor.equals(DataFlavor.imageFlavor) && img != null) {
                return img;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[1];
            flavors[0] = DataFlavor.imageFlavor;
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavor.equals(flavors[i])) {
                    return true;
                }
            }

            return false;
        }
    }

    private void putImageToClipBoard(BufferedImage img) {
        if (img == null) {
            return;
        }
        TransferableImage trans = new TransferableImage(img);
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        c.setContents(trans, new ClipboardOwner() {
            @Override
            public void lostOwnership(Clipboard clipboard, Transferable contents) {
            }
        });
    }
}
