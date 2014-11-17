/*
 *  Copyright (C) 2010-2014 JPEXS
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
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

/**
 *
 * @author JPEXS
 */
public class PlayerControls extends JPanel implements ActionListener {

    static final String ACTION_PAUSE = "PAUSE";
    static final String ACTION_STOP = "STOP";
    static final String ACTION_SELECT_BKCOLOR = "SELECTCOLOR";
    static final String ACTION_ZOOMIN = "ZOOMIN";
    static final String ACTION_ZOOMOUT = "ZOOMOUT";
    static final String ACTION_ZOOMFIT = "ZOOMFIT";
    static final String ACTION_ZOOMNONE = "ZOOMNONE";
    static final String ACTION_SNAPSHOT = "SNAPSHOT";

    private final JButton pauseButton;
    private boolean paused = false;
    private MediaDisplay display;
    private JProgressBar progress;
    private final Timer timer;
    private final JLabel timeLabel;
    private final JLabel frameLabel;
    private final JLabel totalTimeLabel;
    private final JLabel totalFrameLabel;
    private static final Icon pauseIcon = View.getIcon("pause16");
    private static final Icon playIcon = View.getIcon("play16");

    private final JLabel percentLabel = new JLabel("100%");
    private final JPanel zoomPanel;
    private final JPanel graphicControls;
    private final JPanel playbackControls;
    private double realZoom = 1.0;

    private final JButton zoomFitButton;

    public static final int ZOOM_DECADE_STEPS = 10;
    public static final double ZOOM_MULTIPLIER = Math.pow(10, 1.0 / ZOOM_DECADE_STEPS);

    private static String underline(String s) {
        return "<html><font color=\"#000099\"><u>" + s + "</u></font></html>";
    }

    private static Font underlinedFont = null;
    private static Font notUnderlinedFont = null;

    static {
        notUnderlinedFont = new JLabel().getFont();
        Map<TextAttribute, Integer> fontAttributes = new HashMap<>();
        fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        underlinedFont = notUnderlinedFont.deriveFont(fontAttributes);
    }

    public PlayerControls(final MainPanel mainPanel, MediaDisplay display) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        graphicControls = new JPanel(new BorderLayout());
        JPanel graphicButtonsPanel = new JPanel(new FlowLayout());
        JButton selectColorButton = new JButton(View.getIcon("color16"));
        selectColorButton.addActionListener(this);
        selectColorButton.setActionCommand(ACTION_SELECT_BKCOLOR);
        selectColorButton.setToolTipText(AppStrings.translate("button.selectbkcolor.hint"));

        JButton zoomInButton = new JButton(View.getIcon("zoomin16"));
        zoomInButton.addActionListener(this);
        zoomInButton.setActionCommand(ACTION_ZOOMIN);
        zoomInButton.setToolTipText(AppStrings.translate("button.zoomin.hint"));

        JButton zoomOutButton = new JButton(View.getIcon("zoomout16"));
        zoomOutButton.addActionListener(this);
        zoomOutButton.setActionCommand(ACTION_ZOOMOUT);
        zoomOutButton.setToolTipText(AppStrings.translate("button.zoomout.hint"));

        zoomFitButton = new JButton(View.getIcon("zoomfit16"));
        zoomFitButton.addActionListener(this);
        zoomFitButton.setActionCommand(ACTION_ZOOMFIT);
        zoomFitButton.setToolTipText(AppStrings.translate("button.zoomfit.hint"));

        JButton zoomNoneButton = new JButton(View.getIcon("zoomnone16"));
        zoomNoneButton.addActionListener(this);
        zoomNoneButton.setActionCommand(ACTION_ZOOMNONE);
        zoomNoneButton.setToolTipText(AppStrings.translate("button.zoomnone.hint"));

        JButton snapshotButton = new JButton(View.getIcon("snapshot16"));
        snapshotButton.addActionListener(this);
        snapshotButton.setActionCommand(ACTION_SNAPSHOT);
        snapshotButton.setToolTipText(AppStrings.translate("button.snapshot.hint"));

        zoomPanel = new JPanel(new FlowLayout());
        //updateZoom();
        zoomPanel.add(percentLabel);
        zoomPanel.add(zoomInButton);
        zoomPanel.add(zoomOutButton);
        zoomPanel.add(zoomNoneButton);
        zoomPanel.add(zoomFitButton);
        zoomPanel.add(selectColorButton);
        graphicButtonsPanel.add(zoomPanel);
        graphicButtonsPanel.add(snapshotButton);
        graphicControls.add(graphicButtonsPanel, BorderLayout.EAST);

        add(graphicControls);
        graphicControls.setVisible(display.screenAvailable());

        playbackControls = new JPanel();
        this.display = display;
        JPanel controlPanel = new JPanel(new BorderLayout());
        frameLabel = new JLabel("0", SwingConstants.RIGHT);
        frameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        frameLabel.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {

            }

        });

        Dimension min = new Dimension(frameLabel.getFontMetrics(notUnderlinedFont).stringWidth("000"), frameLabel.getPreferredSize().height);
        frameLabel.setMinimumSize(min);
        frameLabel.setPreferredSize(min);

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

        JPanel currentPanel = new JPanel(new FlowLayout());
        currentPanel.add(frameLabel);
        currentPanel.add(timeLabel);
        JPanel totalPanel = new JPanel(new FlowLayout());
        totalPanel.add(totalFrameLabel);
        totalPanel.add(totalTimeLabel);

        controlPanel.add(currentPanel, BorderLayout.WEST);
        controlPanel.add(totalPanel, BorderLayout.EAST);
        playbackControls.setLayout(new BoxLayout(playbackControls, BoxLayout.Y_AXIS));
        JPanel buttonsPanel = new JPanel(new FlowLayout());

        pauseButton = new JButton(AppStrings.translate("preview.pause"), pauseIcon);
        pauseButton.setMargin(new Insets(0, 0, 0, 0));
        pauseButton.setActionCommand(ACTION_PAUSE);
        pauseButton.addActionListener(this);
        JButton stopButton = new JButton(AppStrings.translate("preview.stop"), View.getIcon("stop16"));
        stopButton.setMargin(new Insets(0, 0, 0, 0));
        stopButton.setActionCommand(ACTION_STOP);
        stopButton.addActionListener(this);
        buttonsPanel.add(pauseButton);
        buttonsPanel.add(stopButton);
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
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 100, 100);
        add(playbackControls);
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
        this.display = media;
    }

    private void update() {
        if (!display.isLoaded()) {
            return;
        }

        View.execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                double zoom = display.getZoom();
                zoomFitButton.setVisible(zoom != 0.0);
                percentLabel.setVisible(zoom != 0.0);
                zoomPanel.setVisible(display.zoomAvailable());
                graphicControls.setVisible(display.screenAvailable());
                int totalFrames = display.getTotalFrames();
                int currentFrame = display.getCurrentFrame();
                int frameRate = display.getFrameRate();
                if (totalFrames == 0) {
                    progress.setIndeterminate(true);
                } else {
                    progress.setMaximum(totalFrames - 1);
                    progress.setMinimum(0);
                    progress.setValue(currentFrame);
                    progress.setIndeterminate(false);
                }
                frameLabel.setText(("" + (currentFrame + 1)));
                totalFrameLabel.setText("" + totalFrames);
                if (frameRate != 0) {
                    timeLabel.setText("(" + formatMs((currentFrame * 1000) / frameRate) + ")");
                    totalTimeLabel.setText("(" + formatMs(((totalFrames - 1) * 1000) / frameRate) + ")");
                }
                if (totalFrames <= 1 && playbackControls.isVisible()) {
                    playbackControls.setVisible(false);
                }
                if (totalFrames > 1 && !playbackControls.isVisible()) {
                    playbackControls.setVisible(true);
                }
                if (display.isPlaying() == paused) {
                    paused = !paused;

                    if (paused) {
                        pauseButton.setText(AppStrings.translate("preview.play"));
                        pauseButton.setIcon(playIcon);
                    } else {
                        pauseButton.setText(AppStrings.translate("preview.pause"));
                        pauseButton.setIcon(pauseIcon);
                    }
                }
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
        double pctzoom = roundZoom(realZoom * 100, 3);
        String r = Double.toString(pctzoom);
        double zoom = pctzoom / 100.0;
        if (r.endsWith(".0")) {
            r = r.substring(0, r.length() - 2);
        }
        percentLabel.setText(r + "%");
        display.zoom(zoom);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_PAUSE:
                if (paused) {
                    display.play();
                } else {
                    display.pause();
                }
                break;
            case ACTION_STOP:
                display.pause();
                display.rewind();
                break;
            case ACTION_SELECT_BKCOLOR:
                View.execInEventDispatch(new Runnable() {
                    @Override
                    public void run() {
                        Color newColor = JColorChooser.showDialog(null, AppStrings.translate("dialog.selectbkcolor.title"), View.swfBackgroundColor);
                        if (newColor != null) {
                            View.swfBackgroundColor = newColor;
                            display.setBackground(newColor);
                        }
                    }
                });
                break;
            case ACTION_ZOOMIN:
                realZoom *= ZOOM_MULTIPLIER;
                updateZoom();
                break;
            case ACTION_ZOOMOUT:
                realZoom /= ZOOM_MULTIPLIER;
                updateZoom();
                break;
            case ACTION_ZOOMNONE:
                realZoom = 1.0;
                updateZoom();
                break;
            case ACTION_ZOOMFIT:
                realZoom = display.getZoomToFit();
                updateZoom();
                break;
            case ACTION_SNAPSHOT:
                putImageToClipBoard(display.printScreen());
                break;
        }
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
