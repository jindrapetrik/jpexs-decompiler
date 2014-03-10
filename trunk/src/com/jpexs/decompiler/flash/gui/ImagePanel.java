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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.AppStrings;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class ImagePanel extends JPanel implements ActionListener, MediaDisplay {

    static final String ACTION_SELECT_BKCOLOR = "SELECTCOLOR";

    private JLabel label = new JLabel();
    private Timelined timelined;
    private boolean stillFrame = false;
    private Timer timer;
    private int frame = -1;
    private SWF swf;
    private boolean loaded;
    private int mouseButton;
    private JLabel debugLabel = new JLabel("-");
    private DepthState stateUnderCursor = null;
    private MouseEvent lastMouseEvent = null;
    private List<SoundTagPlayer> soundPlayers = new ArrayList<>();

    @Override
    public void setBackground(Color bg) {
        if (label != null) {
            label.setBackground(bg);
        }
        super.setBackground(bg);
    }

    @Override
    public synchronized void addMouseListener(MouseListener l) {
        label.addMouseListener(l);
    }

    @Override
    public synchronized void removeMouseListener(MouseListener l) {
        label.removeMouseListener(l);
    }

    @Override
    public synchronized void addMouseMotionListener(MouseMotionListener l) {
        label.addMouseMotionListener(l);
    }

    @Override
    public synchronized void removeMouseMotionListener(MouseMotionListener l) {
        label.removeMouseMotionListener(l);
    }

    private void updatePos(MouseEvent e) {
        if (e == null) {
            return;
        }
        lastMouseEvent = e;
        boolean handCursor = false;
        DepthState newStateUnderCursor = null;
        if (timelined != null) {
            Timeline tim = ((Timelined) timelined).getTimeline();
            BoundedTag bounded = (BoundedTag) timelined;
            RECT rect = bounded.getRect();
            int width = rect.getWidth();
            double scale = 1.0;
            if (width > swf.displayRect.getWidth()) {
                scale = (double) swf.displayRect.getWidth() / (double) width;
            }
            Matrix m = new Matrix();
            m.translate(-rect.Xmin, -rect.Ymin);
            m.scale(scale);
            List<DepthState> objs = tim.frames.get(frame).getObjectsUnderCursor(e.getPoint(), mouseButton, m);
            String ret = "";
            ret += " [" + e.getX() + "," + e.getY() + "] : ";

            boolean first = true;
            for (DepthState ds : objs) {
                if (!first) {
                    ret += ", ";
                }
                first = false;
                CharacterTag c = tim.swf.characters.get(ds.characterId);
                if (c instanceof ButtonTag) {
                    newStateUnderCursor = ds;
                    handCursor = true;
                }
                ret += c.toString();
                if (timelined instanceof ButtonTag) {
                    handCursor = true;
                }
            }
            if (objs.isEmpty()) {
                ret += " - ";
            }
            debugLabel.setText(ret);

            if (handCursor) {
                label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                label.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            if (newStateUnderCursor != stateUnderCursor) {
                stateUnderCursor = newStateUnderCursor;
                drawFrame();
            }
        }
    }

    public ImagePanel() {
        super(new BorderLayout());
        label.setHorizontalAlignment(JLabel.CENTER);
        setOpaque(true);
        setBackground(View.DEFAULT_BACKGROUND_COLOR);

        JPanel labelPan = new JPanel(new GridBagLayout()) {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(View.transparentPaint);
                g2d.fill(new Rectangle(0, 0, getWidth(), getHeight()));
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setPaint(View.swfBackgroundColor);
                g2d.fill(new Rectangle(0, 0, getWidth(), getHeight()));
            }

        };
        labelPan.add(label, new GridBagConstraints());
        add(labelPan, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton selectColorButton = new JButton(View.getIcon("color16"));
        selectColorButton.addActionListener(this);
        selectColorButton.setActionCommand(ACTION_SELECT_BKCOLOR);
        selectColorButton.setToolTipText(AppStrings.translate("button.selectbkcolor.hint"));
        buttonsPanel.add(selectColorButton);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
        add(debugLabel, BorderLayout.NORTH);
        label.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                drawFrame();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stateUnderCursor = null;
                drawFrame();
                debugLabel.setText(" - ");
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseButton = e.getButton();
                updatePos(e);
                drawFrame();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseButton = 0;
                updatePos(e);
                drawFrame();
            }

        });
        label.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {

                updatePos(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updatePos(e);
            }

        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ACTION_SELECT_BKCOLOR)) {
            View.execInEventDispatch(new Runnable() {
                @Override
                public void run() {
                    Color newColor = JColorChooser.showDialog(null, AppStrings.translate("dialog.selectbkcolor.title"), View.swfBackgroundColor);
                    if (newColor != null) {
                        View.swfBackgroundColor = newColor;
                        setBackground(newColor);
                        repaint();
                    }
                }
            });

        }
    }

    public void setImage(byte[] data) {
        setBackground(View.swfBackgroundColor);
        if (timer != null) {
            timer.cancel();
        }
        timelined = null;
        loaded = true;
        ImageIcon icon = new ImageIcon(data);
        label.setIcon(icon);
    }

    public void setTimelined(final Timelined drawable, final SWF swf, int frame) {
        pause();
        if (drawable instanceof ButtonTag) {
            frame = ButtonTag.FRAME_UP;
        }
        this.timelined = drawable;
        this.swf = swf;
        if (frame > -1) {
            this.frame = frame;
            this.stillFrame = true;
        } else {
            this.frame = 0;
            this.stillFrame = false;
        }
        loaded = true;

        if (drawable.getTimeline().frames.isEmpty()) {
            label.setIcon(null);
            return;
        }
        frame = 0;
        play();
    }

    public void setImage(SerializableImage image) {
        setBackground(View.swfBackgroundColor);
        if (timer != null) {
            timer.cancel();
        }
        timelined = null;
        loaded = true;
        stillFrame = true;
        ImageIcon icon = new ImageIcon(image.getBufferedImage());
        label.setIcon(icon);
    }

    @Override
    public int getCurrentFrame() {
        return frame;
    }

    @Override
    public int getTotalFrames() {
        if (timelined == null) {
            return 0;
        }
        if (stillFrame) {
            return 0;
        }
        return timelined.getTimeline().frames.size();
    }

    @Override
    public void pause() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        stopAllSounds();
    }

    private void stopAllSounds() {
        for (int i = soundPlayers.size() - 1; i >= 0; i--) {
            SoundTagPlayer pl = soundPlayers.get(i);
            pl.pause();
        }
        soundPlayers.clear();
    }

    private void nextFrame() {
        int newframe = frame == timelined.getTimeline().frames.size() - 1 ? 0 : frame + 1;
        if (newframe != frame) {
            if (newframe == 0) {
                stopAllSounds();
            }
            frame = newframe;
            updatePos(lastMouseEvent);
            drawFrame();
        }
    }

    private static SerializableImage getFrame(SWF swf, int frame, Timelined drawable, DepthState stateUnderCursor, int mouseButton) {
        String key = "drawable_" + frame + "_" + drawable.hashCode() + "_" + mouseButton + "_" + (stateUnderCursor == null ? "out" : stateUnderCursor.hashCode());
        SerializableImage img = SWF.getFromCache(key);
        if (img == null) {
            if (drawable instanceof BoundedTag) {
                BoundedTag bounded = (BoundedTag) drawable;
                RECT rect = bounded.getRect();
                if (rect == null) { //??? Why?
                    rect = new RECT(0, 0, 1, 1);
                }
                int width = rect.getWidth();
                int height = rect.getHeight();
                double scale = 1.0;
                if (width > swf.displayRect.getWidth()) {
                    scale = (double) swf.displayRect.getWidth() / (double) width;
                    width = swf.displayRect.getWidth();
                }
                SerializableImage image = new SerializableImage((int) (width / SWF.unitDivisor) + 1,
                        (int) (height / SWF.unitDivisor) + 1, SerializableImage.TYPE_INT_ARGB);
                image.fillTransparent();
                Matrix m = new Matrix();
                m.translate(-rect.Xmin, -rect.Ymin);
                m.scale(scale);
                drawable.getTimeline().toImage(frame, frame, stateUnderCursor, mouseButton, image, m, new ColorTransform());
                img = image;
            } else if (drawable instanceof FontTag) {
                // only DefineFont tags
                FontTag fontTag = (FontTag) drawable;
                img = fontTag.toImage(frame, frame, Matrix.getScaleInstance(1 / SWF.unitDivisor), new ColorTransform());
            }
            SWF.putToCache(key, img);
        }
        return img;
    }

    private void drawFrame() {
        if (timelined == null) {
            return;
        }
        Matrix mat = new Matrix();
        mat.translateX = swf.displayRect.Xmin;
        mat.translateY = swf.displayRect.Ymin;
        ImageIcon icon = new ImageIcon(getFrame(swf, frame, timelined, stateUnderCursor, mouseButton).getBufferedImage());
        List<Integer> sounds = timelined.getTimeline().getSounds(frame, stateUnderCursor, mouseButton);
        for (int sndId : sounds) {
            CharacterTag c = swf.characters.get(sndId);
            if (c instanceof SoundTag) {
                SoundTag st = (SoundTag) c;
                final SoundTagPlayer sp = new SoundTagPlayer(st, 1);
                synchronized (ImagePanel.class) {
                    soundPlayers.add(sp);
                }
                sp.addListener(new PlayerListener() {

                    @Override
                    public void playingFinished() {
                        synchronized (ImagePanel.class) {
                            soundPlayers.remove(sp);
                        }
                    }
                });
                sp.play();

            }
        }
        label.setIcon(icon);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void play() {
        pause();
        if (!stillFrame && timelined.getTimeline().frames.size() > 1) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    nextFrame();
                }
            }, 0, 1000 / timelined.getTimeline().frameRate);
        } else {
            drawFrame();
        }
    }

    @Override
    public void rewind() {
        frame = 0;
        drawFrame();
    }

    @Override
    public boolean isPlaying() {
        if (timelined == null) {
            return false;
        }
        if (stillFrame) {
            return false;
        }
        return (timelined.getTimeline().frames.size() <= 1) || (timer != null);
    }

    @Override
    public void gotoFrame(int frame) {
        this.frame = frame;
        drawFrame();
    }

    @Override
    public int getFrameRate() {
        if (timelined == null) {
            return 1;
        }
        if (stillFrame) {
            return 1;
        }
        return timelined.getTimeline().frameRate;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }
}
