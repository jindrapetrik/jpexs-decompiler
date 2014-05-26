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

import com.jpexs.decompiler.flash.gui.FlashUnsupportedException;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.utf8.Utf8Helper;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.SHELLEXECUTEINFO;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class FlashPlayerPanel extends Panel implements Closeable, MediaDisplay {

    private boolean executed = false;
    private String flash;
    private HANDLE pipe;
    private HANDLE process;
    private WinDef.HWND hwnd;
    private WinDef.HWND hwndFrame;
    private Component frame;
    private boolean stopped = true;
    private static final int CMD_PLAY = 1;
    private static final int CMD_RESIZE = 2;
    private static final int CMD_BGCOLOR = 3;
    private static final int CMD_CURRENT_FRAME = 4;
    private static final int CMD_TOTAL_FRAMES = 5;
    private static final int CMD_PAUSE = 6;
    private static final int CMD_RESUME = 7;
    private static final int CMD_PLAYING = 8;
    private static final int CMD_REWIND = 9;
    private static final int CMD_GOTO = 10;
    private static final int CMD_CALL = 11;
    private static final int CMD_GETVARIABLE = 12;
    private static final int CMD_SETVARIABLE = 13;
    private static final int PIPE_TIMEOUT_MS = 1000;
    private int frameRate;
    public boolean specialPlayback = false;
    private boolean specialPlaying = false;

    public synchronized String getVariable(String name) throws IOException {
        if (pipe != null) {
            writeToPipe(new byte[]{CMD_GETVARIABLE});
            int nameLen = name.getBytes().length;
            writeToPipe(new byte[]{(byte) ((nameLen >> 8) & 0xff), (byte) (nameLen & 0xff)});
            writeToPipe(name.getBytes());
            byte res[] = new byte[2];
            readFromPipe(res);
            int retLen = ((res[0] & 0xff) << 8) + (res[1] & 0xff);
            res = new byte[retLen];
            readFromPipe(res);
            String ret = new String(res, 0, retLen);
            return ret;
        }
        return null;
    }

    public synchronized void setVariable(String name, String value) throws IOException {
        if (pipe != null) {
            writeToPipe(new byte[]{CMD_SETVARIABLE});
            int nameLen = name.getBytes().length;
            writeToPipe(new byte[]{(byte) ((nameLen >> 8) & 0xff), (byte) (nameLen & 0xff)});
            writeToPipe(name.getBytes());

            int valLen = value.getBytes().length;
            writeToPipe(new byte[]{(byte) ((valLen >> 8) & 0xff), (byte) (valLen & 0xff)});
            writeToPipe(value.getBytes());
        }
    }

    public synchronized String call(String callString) throws IOException {
        if (pipe != null) {
            writeToPipe(new byte[]{CMD_CALL});
            int callLen = callString.getBytes().length;
            writeToPipe(new byte[]{(byte) ((callLen >> 8) & 0xff), (byte) (callLen & 0xff)});
            writeToPipe(callString.getBytes());

            byte res[] = new byte[2];
            readFromPipe(res);
            int retLen = ((res[0] & 0xff) << 8) + (res[1] & 0xff);
            res = new byte[retLen];
            readFromPipe(res);
            String ret = new String(res, 0, retLen);
            return ret;
        }
        return null;
    }

    private synchronized void resize() throws IOException {
        if (pipe != null) {
            writeToPipe(new byte[]{CMD_RESIZE});
            writeToPipe(new byte[]{
                (byte) (getWidth() / 256), (byte) (getWidth() % 256),
                (byte) (getHeight() / 256), (byte) (getHeight() % 256),});
        }
    }

    private int __getCurrentFrame() throws IOException {
        byte[] res = new byte[2];
        writeToPipe(new byte[]{CMD_CURRENT_FRAME});
        readFromPipe(res);
        return ((res[0] & 0xff) << 8) + (res[1] & 0xff);
    }

    @Override
    public synchronized int getCurrentFrame() {
        try {
            if (specialPlayback) {
                if (!specialPlaying) {
                    return specialPosition;
                }
                String posStr = getVariable("_root.my_sound.position");
                if (posStr != null) {
                    return Integer.parseInt(posStr);
                }
            }
            return __getCurrentFrame();
        } catch (IOException ex) {
            return 0;
        }
    }

    @Override
    public synchronized int getTotalFrames() {
        try {
            if (specialPlayback) {
                String durStr = getVariable("_root.my_sound.duration");
                if (durStr != null) {
                    return Integer.parseInt(durStr);
                }
            }
            byte[] res = new byte[2];
            writeToPipe(new byte[]{CMD_TOTAL_FRAMES});
            readFromPipe(res);
            return ((res[0] & 0xff) << 8) + (res[1] & 0xff);
        } catch (IOException ex) {
            return 0;
        }
    }

    @Override
    public synchronized void setBackground(Color color) {
        try {
            writeToPipe(new byte[]{CMD_BGCOLOR});
            writeToPipe(new byte[]{(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()});
        } catch (IOException ex) {
        }
    }

    public FlashPlayerPanel(Component frame) {
        if (!Platform.isWindows()) {
            throw new FlashUnsupportedException();
        }
        this.frame = frame;
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                try {
                    resize();
                } catch (IOException ex) {
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
                componentResized(e);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
    }

    private synchronized void execute() {
        if (!executed) {
            hwnd = new WinDef.HWND();
            hwnd.setPointer(Native.getComponentPointer(this));

            hwndFrame = new WinDef.HWND();
            hwndFrame.setPointer(Native.getComponentPointer(frame));

            startFlashPlayer();

            executed = true;
        }
    }

    private void restartFlashPlayer() {
        Kernel32.INSTANCE.TerminateProcess(process, 0);
        Kernel32.INSTANCE.CloseHandle(pipe);
        startFlashPlayer();
    }

    private void startFlashPlayer() {
        String path = Utf8Helper.urlDecode(FlashPlayerPanel.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String appDir = new File(path).getParentFile().getAbsolutePath();
        if (!appDir.endsWith("\\")) {
            appDir += "\\";
        }
        String exePath = appDir + "lib\\FlashPlayer.exe";
        File f = new File(exePath);
        if (!f.exists()) {
            Logger.getLogger(FlashPlayerPanel.class.getName()).log(Level.SEVERE, "FlashPlayer.exe not found: " + f.getPath());
            return;
        }

        String pipeName = "\\\\.\\pipe\\ffdec_flashplayer_" + hwnd.getPointer().hashCode();
        pipe = Kernel32.INSTANCE.CreateNamedPipe(pipeName, Kernel32.PIPE_ACCESS_DUPLEX, Kernel32.PIPE_TYPE_BYTE, 1, 4096, 4096, 0, null);

        SHELLEXECUTEINFO sei = new SHELLEXECUTEINFO();
        sei.fMask = 0x00000040;
        sei.lpFile = new WString(exePath);
        sei.lpParameters = new WString(hwnd.getPointer().hashCode() + " " + hwndFrame.getPointer().hashCode());
        sei.nShow = WinUser.SW_NORMAL;
        Shell32.INSTANCE.ShellExecuteEx(sei);
        process = sei.hProcess;

        Kernel32.INSTANCE.ConnectNamedPipe(pipe, null);
    }

    public synchronized void stopSWF() {
        displaySWF("-", null, 1);
        stopped = true;
    }

    public synchronized boolean isStopped() {
        return stopped;
    }

    public synchronized void displaySWF(String flash, Color bgColor, int frameRate) {
        try {
            this.flash = flash;
            repaint();
            this.frameRate = frameRate;
            execute();
            if (bgColor != null) {
                setBackground(bgColor);
            }
            resize();
            if (pipe != null) {
                writeToPipe(new byte[]{CMD_PLAY});
                writeToPipe(new byte[]{(byte) flash.getBytes().length});
                writeToPipe(flash.getBytes());
            }
            stopped = false;
            specialPlaying = false;
            specialPosition = 0;
            if (specialPlayback) {
                play();
            }
        } catch (IOException ex) {
        }
    }

    @Override
    public void close() throws IOException {
        Kernel32.INSTANCE.CloseHandle(pipe);
        Kernel32.INSTANCE.TerminateProcess(process, 0);
    }

    @Override
    public void paint(Graphics g) {
        if (flash != null) {
            execute();
        }
        super.paint(g);
    }
    private int specialPosition = 0;

    private synchronized void __pause() throws IOException {
        writeToPipe(new byte[]{CMD_PAUSE});
    }

    @Override
    public void pause() {
        try {
            if (specialPlayback) {
                specialPosition = getCurrentFrame();
                __gotoFrame(3);
                __play();
                specialPlaying = false;
                return;
            }
            __pause();
        } catch (IOException ex) {
        }
    }

    @Override
    public void rewind() {
        try {
            if (specialPlayback) {
                boolean plays = specialPlaying;
                pause();
                specialPosition = 0;
                if (plays) {
                    play();
                }

                return;
            }
            writeToPipe(new byte[]{CMD_REWIND});
        } catch (IOException ex) {
        }
    }

    private synchronized void __play() throws IOException {
        writeToPipe(new byte[]{CMD_RESUME});
    }

    @Override
    public void play() {
        try {
            if (specialPlayback) {
                double p = (((double) specialPosition) / 1000.0);
                setVariable("_root.execParam", "" + p);
                __gotoFrame(1);
                __play();
                specialPlaying = true;
                return;
            }
            __play();
        } catch (IOException ex) {
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            if (specialPlayback) {
                return specialPlaying;
            }
            writeToPipe(new byte[]{CMD_PLAYING});
            byte[] res = new byte[1];
            readFromPipe(res);
            return res[0] == 1;
        } catch (IOException ex) {
            return false;
        }
    }

    private synchronized void __gotoFrame(int frame) throws IOException {
        writeToPipe(new byte[]{CMD_GOTO});
        writeToPipe(new byte[]{(byte) ((frame >> 8) & 0xff), (byte) (frame & 0xff)});
    }

    @Override
    public void gotoFrame(int frame) {
        try {
            if (specialPlayback) {
                if (specialPlaying) {
                    pause();
                    specialPosition = frame;
                    play();
                } else {
                    specialPosition = frame;
                }
                return;
            }
            __gotoFrame(frame);
        } catch (IOException ex) {
        }
    }

    @Override
    public int getFrameRate() {
        if (specialPlayback) {
            return 1000;
        }
        return frameRate;
    }

    @Override
    public boolean isLoaded() {
        return !isStopped();
    }

    private synchronized boolean writeToPipe(final byte[] data) throws IOException {
        final IntByReference ibr = new IntByReference();
        int result = -1;
        try {
            result = CancellableWorker.call(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    boolean result = Kernel32.INSTANCE.WriteFile(pipe, data, data.length, ibr, null);
                    if (!result) {
                        return Kernel32.INSTANCE.GetLastError();
                    }
                    if (ibr.getValue() != data.length) {
                        return -1;
                    }
                    return 0;
                }
            }, PIPE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            // ignore
        }
        if (result != 0) {
            if (result == Kernel32.ERROR_NO_DATA || result == -1) {
                restartFlashPlayer();
                throw new IOException("Pipe write error.");
            } else {
                // System.out.println("pipe write failed. datalength: " + data.length + " error:" + result);
            }
        }
        return result == 0;
    }

    private synchronized boolean readFromPipe(final byte[] res) throws IOException {
        final IntByReference ibr = new IntByReference();
        int result = -1;
        try {
            result = CancellableWorker.call(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    int read = 0;
                    while (read < res.length) {
                        byte[] data = new byte[res.length - read];
                        boolean result = Kernel32.INSTANCE.ReadFile(pipe, data, data.length, ibr, null);
                        if (!result) {
                            return Kernel32.INSTANCE.GetLastError();
                        }
                        int readNow = ibr.getValue();
                        System.arraycopy(data, 0, res, read, readNow);
                        read += readNow;
                    }
                    return 0;
                }
            }, PIPE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            // ignore
        }
        if (result != 0) {
            if (result == Kernel32.ERROR_BROKEN_PIPE || result == -1) {
                restartFlashPlayer();
                throw new IOException("Pipe read error.");
            } else {
                // System.out.println("pipe read failed. result: " + result + " datalength: " + res.length + " received: " + ibr.getValue() + " error: " + result);
            }
        }
        return result == 0;
    }
}
