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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.SOUNDENVELOPE;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class SoundTagPlayerTest {

    @Test
    public void playbackStartsAtExactPositionAndCloseReleasesTheLineAndWorker() throws Exception {
        DefineSoundTag tag = new DefineSoundTag(new SWF());
        tag.soundFormat = 3;
        tag.soundRate = 3;
        tag.soundSize = true;
        byte[] pcm = new byte[20000];
        for (int i = 0; i < pcm.length; i++) {
            pcm[i] = (byte) i;
        }
        tag.soundData = new ByteArrayRange(pcm);
        AtomicInteger openLines = new AtomicInteger();
        CompletableFuture<byte[]> firstWrite = new CompletableFuture<>();
        CompletableFuture<Thread> worker = new CompletableFuture<>();
        SoundTagPlayer player = new SoundTagPlayer(null, tag, 1, true, 100000, format -> {
            AtomicBoolean open = new AtomicBoolean();
            CountDownLatch released = new CountDownLatch(1);
            return (SourceDataLine) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{SourceDataLine.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "open":
                        if (open.compareAndSet(false, true)) {
                            openLines.incrementAndGet();
                        }
                        return null;
                    case "close":
                        if (open.compareAndSet(true, false)) {
                            openLines.decrementAndGet();
                        }
                        released.countDown();
                        return null;
                    case "write":
                        firstWrite.complete(Arrays.copyOfRange((byte[]) args[0], (int) args[1], (int) args[1] + (int) args[2]));
                        worker.complete(Thread.currentThread());
                        released.await(10, TimeUnit.SECONDS);
                        return (int) args[2];
                    default:
                        if (method.getReturnType() == boolean.class) {
                            return false;
                        }
                        if (method.getReturnType() == long.class) {
                            return 0L;
                        }
                        return null;
                }
            });
        });
        try {
            byte[] actual = firstWrite.get(5, TimeUnit.SECONDS);
            assertEquals(actual, Arrays.copyOfRange(pcm, 8820, 8820 + actual.length));
        } finally {
            player.close();
        }
        Thread playbackThread = worker.get(5, TimeUnit.SECONDS);
        playbackThread.join(3000);
        assertFalse(playbackThread.isAlive());
        assertEquals(openLines.get(), 0);
    }

    @Test
    public void closingWhileDecodingDoesNotOpenAnAudioDevice() throws Exception {
        CountDownLatch decoding = new CountDownLatch(1);
        CountDownLatch finishDecoding = new CountDownLatch(1);
        AtomicReference<Thread> worker = new AtomicReference<>();
        DefineSoundTag tag = new DefineSoundTag(new SWF()) {
            @Override
            public List<ByteArrayRange> getRawSoundData() {
                worker.set(Thread.currentThread());
                decoding.countDown();
                try {
                    finishDecoding.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    throw new AssertionError(ex);
                }
                return Collections.singletonList(new ByteArrayRange(new byte[100]));
            }
        };
        tag.soundFormat = 3;
        tag.soundRate = 3;
        tag.soundSize = true;
        AtomicInteger opened = new AtomicInteger();
        SoundTagPlayer player = new SoundTagPlayer(null, tag, 1, true, 0, format -> {
            opened.incrementAndGet();
            throw new AssertionError("Closed playback must not open a line");
        });
        try {
            assertTrue(decoding.await(5, TimeUnit.SECONDS));
        } finally {
            player.close();
            finishDecoding.countDown();
        }
        worker.get().join(3000);
        assertFalse(worker.get().isAlive());
        assertEquals(opened.get(), 0);
    }

    @Test
    public void unsignedPcm8PlaybackPreservesAmplitudeChannelsAndDuration() throws Exception {
        byte[] input = {0, 125, (byte) 128, (byte) 131, (byte) 255, (byte) 128};
        for (int codec : new int[]{0, 3}) {
            for (boolean stereo : new boolean[]{false, true}) {
                SoundFormat format = new SoundFormat(codec, 11025, stereo, false);
                byte[] wav = wave(format, input, null, 0, false);
                try (AudioInputStream source = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav))) {
                    assertEquals(source.getFormat().getEncoding(), AudioFormat.Encoding.PCM_UNSIGNED);
                    assertEquals(source.getFormat().getSampleSizeInBits(), 8);
                    assertEquals(read(source), input);
                }
                try (AudioInputStream playback = SoundTagPlayer.createPlaybackStream(wave(format, input, null, 0, true))) {
                    assertEquals(playback.getFormat().getEncoding(), AudioFormat.Encoding.PCM_SIGNED);
                    assertEquals(playback.getFormat().getSampleSizeInBits(), 16);
                    assertEquals(playback.getFormat().getChannels(), stereo ? 2 : 1);
                    assertEquals(playback.getFrameLength(), (long) input.length / (stereo ? 2 : 1));
                    assertEquals(read(playback), pcm16(new int[]{-32768, -768, 0, 768, 32512, 0}));
                }
                try (AudioInputStream skipped = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wave(format, input, null, 1, false)))) {
                    assertEquals(read(skipped), Arrays.copyOfRange(input, stereo ? 2 : 1, input.length));
                }
            }
        }
    }

    @Test
    public void pcm8SoundInfoTrimAndEnvelopeUseWholeSampleFrames() throws Exception {
        for (boolean stereo : new boolean[]{false, true}) {
            byte[] input = stereo ? new byte[]{(byte) 128, (byte) 128, 0, (byte) 255, (byte) 255, 0, (byte) 128, (byte) 128}
                    : new byte[]{(byte) 128, 0, (byte) 255, (byte) 128};
            SOUNDINFO info = new SOUNDINFO();
            info.hasInPoint = true;
            info.inPoint = 1;
            info.hasOutPoint = true;
            info.outPoint = 3;
            info.hasEnvelope = true;
            info.envelopeRecords = new SOUNDENVELOPE[2];
            for (int i = 0; i < 2; i++) {
                SOUNDENVELOPE envelope = new SOUNDENVELOPE();
                envelope.pos44 = i * 10;
                envelope.leftLevel = 16384;
                envelope.rightLevel = 8192;
                info.envelopeRecords[i] = envelope;
            }
            byte[] wav = wave(new SoundFormat(3, 44100, stereo, false), input, info, 0);
            try (AudioInputStream playback = SoundTagPlayer.createPlaybackStream(wav)) {
                assertEquals(playback.getFormat().getSampleSizeInBits(), 16);
                assertEquals(playback.getFormat().getChannels(), 2);
                assertEquals(playback.getFrameLength(), 2L);
                assertEquals(read(playback), pcm16(stereo ? new int[]{-16384, 8128, 16256, -8192}
                        : new int[]{-16384, -8192, 16256, 8128}));
            }
            try (AudioInputStream exported = AudioSystem.getAudioInputStream(new ByteArrayInputStream(
                    wave(new SoundFormat(3, 44100, stereo, false), input, info, 0, false)))) {
                assertEquals(exported.getFormat().getSampleSizeInBits(), 8);
                assertEquals(exported.getFrameLength(), 2L);
                assertEquals(read(exported), stereo ? new byte[]{64, (byte) 159, (byte) 191, 96}
                        : new byte[]{64, 96, (byte) 191, (byte) 159});
            }
        }
    }

    @Test
    public void soundExporterPreservesUnsignedPcm8() throws Exception {
        com.jpexs.decompiler.flash.tags.DefineSoundTag tag = new com.jpexs.decompiler.flash.tags.DefineSoundTag(new com.jpexs.decompiler.flash.SWF());
        tag.soundFormat = 3;
        tag.soundSize = false;
        tag.soundRate = 1;
        tag.soundType = true;
        byte[] input = {0, (byte) 255, (byte) 128, (byte) 128};
        tag.soundData = new ByteArrayRange(input);
        byte[] wav = new com.jpexs.decompiler.flash.exporters.SoundExporter().exportSound(tag,
                com.jpexs.decompiler.flash.exporters.modes.SoundExportMode.MP3_WAV);
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav))) {
            assertEquals(stream.getFormat().getSampleSizeInBits(), 8);
            assertEquals(stream.getFormat().getEncoding(), AudioFormat.Encoding.PCM_UNSIGNED);
            assertEquals(stream.getFormat().getChannels(), 2);
            assertEquals(stream.getFrameLength(), 2L);
            assertEquals(read(stream), input);
        }
    }

    @Test
    public void pcm16AndCompressedDecoderOutputRemain16Bit() throws Exception {
        for (SoundFormat format : new SoundFormat[]{new SoundFormat(3, 22050, true, true), new SoundFormat(1, 22050, false, false)}) {
            byte[] input = {(byte) 128, 0, 0, 0};
            byte[] decoded = format.decode(null, Collections.singletonList(new ByteArrayRange(input)), 0);
            try (AudioInputStream playback = SoundTagPlayer.createPlaybackStream(wave(format, input, null, 0))) {
                assertEquals(playback.getFormat().getSampleSizeInBits(), 16);
                assertEquals(read(playback), decoded);
            }
            try (AudioInputStream skipped = SoundTagPlayer.createPlaybackStream(wave(format, input, null, 1))) {
                int frameBytes = 2 * (format.stereo ? 2 : 1);
                assertEquals(read(skipped), Arrays.copyOfRange(decoded, frameBytes, decoded.length));
            }
        }
    }

    @Test
    public void pcm8PlaybackCrossesBufferBoundariesAndSeeksByWholeFrames() throws Exception {
        byte[] input = new byte[20000];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) i;
        }
        byte[] wav = wave(new SoundFormat(3, 44100, true, false), input, null, 0);
        try (AudioInputStream playback = SoundTagPlayer.createPlaybackStream(wav)) {
            assertEquals(playback.skip(16380), 16380L);
            byte[] actual = read(playback);
            int[] expected = new int[input.length - 8190];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = ((input[8190 + i] & 255) - 128) * 256;
            }
            assertEquals(actual, pcm16(expected));
            assertEquals(playback.read(new byte[4]), -1);
            assertEquals(playback.read(new byte[4]), -1);
        }
    }

    @Test
    public void seekPositionsRespectBitDepthChannelsAndFrameAlignment() {
        for (int bits : new int[]{8, 16}) {
            for (int channels : new int[]{1, 2}) {
                AudioFormat format = new AudioFormat(44100, bits, channels, bits == 16, false);
                assertEquals(SoundTagPlayer.positionToByteOffset(100000, format), 4410L * format.getFrameSize());
                assertEquals(SoundTagPlayer.positionToByteOffset(13, format), 0L);
                assertEquals(SoundTagPlayer.positionToByteOffset(45, format), (long) format.getFrameSize());
            }
        }
    }

    private static byte[] wave(SoundFormat format, byte[] pcm, SOUNDINFO info, int skip) throws Exception {
        return wave(format, pcm, info, skip, true);
    }

    private static byte[] wave(SoundFormat format, byte[] pcm, SOUNDINFO info, int skip, boolean convert8bitTo16bit) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(format.createWav(info, Collections.singletonList(new ByteArrayRange(pcm)), out, skip, convert8bitTo16bit));
        return out.toByteArray();
    }

    private static byte[] read(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        for (int n; (n = in.read(data)) != -1;) {
            out.write(data, 0, n);
        }
        return out.toByteArray();
    }

    private static byte[] pcm16(int[] samples) {
        byte[] data = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            data[i * 2] = (byte) samples[i];
            data[i * 2 + 1] = (byte) (samples[i] >> 8);
        }
        return data;
    }
}
