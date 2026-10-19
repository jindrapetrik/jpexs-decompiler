/*
 *  Copyright (C) 2010-2026 JPEXS
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

import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.gui.player.MediaDisplayListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Displays a compact peak waveform for a {@link SoundTagPlayer}.
 */
public class SoundWaveformPanel extends JPanel implements MediaDisplayListener {

    private static final int WAVEFORM_BUCKETS = 2048;

    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0);
    
    private static final Color WAVEFORM_COLOR = new Color(0, 255, 0);

    private static final Color CENTER_LINE_COLOR = new Color(45, 100, 58);

    private static final Color POSITION_COLOR = new Color(235, 255, 238);

    private volatile SoundTagPlayer player;

    private volatile WaveformData waveformData;

    private boolean waveformLoadAttempted;

    private final Timer positionTimer;

    public SoundWaveformPanel() {
        setBackground(Color.BLACK);
        setOpaque(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        positionTimer = new Timer(33, event -> {
            SoundTagPlayer currentPlayer = player;
            if (currentPlayer != null && currentPlayer.isPlaying()) {
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                seekTo(event.getX());
            }
        });
    }

    private void seekTo(int x) {
        SoundTagPlayer currentPlayer = player;
        if (currentPlayer == null) {
            return;
        }
        currentPlayer.gotoFrame(frameForX(x, getWidth(), currentPlayer.getTotalFrames()));
    }

    static int frameForX(int x, int width, int totalFrames) {
        if (width <= 1 || totalFrames <= 0) {
            return 0;
        }
        double position = Math.max(0, Math.min(1, x / (double) (width - 1)));
        return (int) Math.round(position * totalFrames);
    }

    public synchronized void setPlayer(SoundTagPlayer player) {
        if (this.player != null) {
            this.player.removeEventListener(this);
        }
        this.player = player;
        waveformData = null;
        waveformLoadAttempted = false;
        if (player != null) {
            player.addEventListener(this);
            loadWaveformWhenAvailable(player);
            positionTimer.start();
        } else {
            positionTimer.stop();
        }
        repaint();
    }

    private synchronized void loadWaveformWhenAvailable(final SoundTagPlayer source) {
        if (waveformLoadAttempted || source != player) {
            return;
        }
        final byte[] wavData = source.getWavData();
        if (wavData == null) {
            return;
        }
        waveformLoadAttempted = true;
        Thread loader = new Thread("Sound waveform loader") {
            @Override
            public void run() {
                WaveformData loaded = null;
                try {
                    loaded = WaveformData.create(wavData, WAVEFORM_BUCKETS);
                } catch (IOException | UnsupportedAudioFileException | IllegalArgumentException ex) {
                    // The sound can still be played when a waveform cannot be decoded.
                }
                synchronized (SoundWaveformPanel.this) {
                    if (source == player) {
                        waveformData = loaded;
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        repaint();
                    }
                });
            }
        };
        loader.setDaemon(true);
        loader.start();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        
        
        WaveformData data = waveformData;
        int width = getWidth();
        int height = getHeight();
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, width, height);

        if (data != null && width > 0 && height > 0) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setColor(CENTER_LINE_COLOR);
                for (int channel = 0; channel < data.getChannelCount(); channel++) {
                    int bandTop = channel * height / data.getChannelCount();
                    int bandBottom = (channel + 1) * height / data.getChannelCount();
                    int centerY = (bandTop + bandBottom) / 2;
                    g.drawLine(0, centerY, width, centerY);
                }
                if (data.getChannelCount() > 1) {
                    g.drawLine(0, height / 2, width, height / 2);
                }

                g.setColor(WAVEFORM_COLOR);
                for (int x = 0; x < width; x++) {
                    int bucket = Math.min(data.getBucketCount() - 1,
                            (int) ((long) x * data.getBucketCount() / width));
                    for (int channel = 0; channel < data.getChannelCount(); channel++) {
                        int bandTop = channel * height / data.getChannelCount();
                        int bandBottom = (channel + 1) * height / data.getChannelCount();
                        int centerY = (bandTop + bandBottom) / 2;
                        int amplitudeHeight = Math.max(1, (bandBottom - bandTop - 6) / 2);
                        int y1 = centerY - Math.round(data.maximum[channel][bucket] * amplitudeHeight);
                        int y2 = centerY - Math.round(data.minimum[channel][bucket] * amplitudeHeight);
                        g.drawLine(x, y1, x, y2);
                    }
                }
            } finally {
                g.dispose();
            }
        }

        SoundTagPlayer currentPlayer = player;
        if (currentPlayer != null && !currentPlayer.isPlaybackFinished() && width > 0) {
            int totalFrames = currentPlayer.getTotalFrames();
            if (totalFrames > 0) {
                double position = Math.max(0, Math.min(1,
                        currentPlayer.getCurrentFrame() / (double) totalFrames));
                int x = Math.min(width - 1, (int) Math.round(position * (width - 1)));
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setColor(POSITION_COLOR);
                    g.setStroke(new BasicStroke(2));
                    g.drawLine(x, 0, x, height);
                } finally {
                    g.dispose();
                }
            }
        }
    }

    @Override
    public void mediaDisplayStateChanged(MediaDisplay source) {
        SoundTagPlayer currentPlayer = player;
        if (source != currentPlayer || currentPlayer == null) {
            return;
        }
        if (waveformData == null) {
            loadWaveformWhenAvailable(currentPlayer);
        }
        repaintOnEventDispatchThread();
    }

    @Override
    public void playingFinished(MediaDisplay source) {
        if (source == player) {
            repaintOnEventDispatchThread();
        }
    }

    @Override
    public void statusChanged(String status) {
    }

    private void repaintOnEventDispatchThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            repaint();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    repaint();
                }
            });
        }
    }

    static class WaveformData {

        private final float[][] minimum;

        private final float[][] maximum;

        private WaveformData(int channelCount, int bucketCount) {
            minimum = new float[channelCount][bucketCount];
            maximum = new float[channelCount][bucketCount];
        }

        public int getChannelCount() {
            return minimum.length;
        }

        public int getBucketCount() {
            return minimum[0].length;
        }

        float getMinimum(int channel, int bucket) {
            return minimum[channel][bucket];
        }

        float getMaximum(int channel, int bucket) {
            return maximum[channel][bucket];
        }

        static WaveformData create(byte[] wavData, int bucketCount)
                throws IOException, UnsupportedAudioFileException {
            AudioInputStream original = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wavData));
            AudioInputStream decoded = original;
            try {
                AudioFormat originalFormat = original.getFormat();
                int channelCount = Math.max(1, Math.min(2, originalFormat.getChannels()));
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        originalFormat.getSampleRate(), 16, originalFormat.getChannels(),
                        originalFormat.getChannels() * 2, originalFormat.getSampleRate(), false);
                if (!formatsMatch(originalFormat, decodedFormat)) {
                    decoded = AudioSystem.getAudioInputStream(decodedFormat, original);
                } else {
                    decodedFormat = originalFormat;
                }

                long frameLength = decoded.getFrameLength();
                if (frameLength <= 0) {
                    frameLength = Math.max(1, wavData.length / decodedFormat.getFrameSize());
                }
                WaveformData result = new WaveformData(channelCount, bucketCount);
                byte[] buffer = new byte[Math.max(decodedFormat.getFrameSize(), decodedFormat.getFrameSize() * 2048)];
                long frameIndex = 0;
                int bytesRead;
                while ((bytesRead = decoded.read(buffer)) != -1) {
                    int frameCount = bytesRead / decodedFormat.getFrameSize();
                    for (int frame = 0; frame < frameCount; frame++) {
                        int bucket = Math.min(bucketCount - 1,
                                (int) (frameIndex * bucketCount / frameLength));
                        int frameOffset = frame * decodedFormat.getFrameSize();
                        for (int channel = 0; channel < channelCount; channel++) {
                            int sampleOffset = frameOffset + channel * 2;
                            int sample = (buffer[sampleOffset] & 0xff)
                                    | (buffer[sampleOffset + 1] << 8);
                            float value = sample / 32768f;
                            result.minimum[channel][bucket] = Math.min(result.minimum[channel][bucket], value);
                            result.maximum[channel][bucket] = Math.max(result.maximum[channel][bucket], value);
                        }
                        frameIndex++;
                    }
                }
                return result;
            } finally {
                if (decoded != original) {
                    decoded.close();
                }
                original.close();
            }
        }

        private static boolean formatsMatch(AudioFormat first, AudioFormat second) {
            return first.getEncoding().equals(second.getEncoding())
                    && first.getSampleRate() == second.getSampleRate()
                    && first.getSampleSizeInBits() == second.getSampleSizeInBits()
                    && first.getChannels() == second.getChannels()
                    && first.getFrameSize() == second.getFrameSize()
                    && first.isBigEndian() == second.isBigEndian();
        }
    }
}
