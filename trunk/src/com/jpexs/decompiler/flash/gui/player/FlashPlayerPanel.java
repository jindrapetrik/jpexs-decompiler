/*
 *  Copyright (C) 2010-2013 JPEXS
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class FlashPlayerPanel extends Panel implements FlashDisplay {

    private boolean executed = false;
    private String flash;
    private HANDLE pipe;
    private static final List<HANDLE> processes = new ArrayList<>();
    private static final List<HANDLE> pipes = new ArrayList<>();
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
    private int frameRate;
    public boolean specialPlayback = false;
    private boolean specialPlaying = false;

    public synchronized String getVariable(String name) {
        if (pipe != null) {
            IntByReference ibr = new IntByReference();
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_GETVARIABLE}, 1, ibr, null);
            int nameLen = name.getBytes().length;
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{(byte) ((nameLen >> 8) & 0xff), (byte) (nameLen & 0xff)}, 2, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, name.getBytes(), nameLen, ibr, null);
            byte res[] = new byte[2];
            if (Kernel32.INSTANCE.ReadFile(pipe, res, 2, ibr, null)) {
                int retLen = ((res[0] & 0xff) << 8) + (res[1] & 0xff);
                res = new byte[retLen];
                if (Kernel32.INSTANCE.ReadFile(pipe, res, retLen, ibr, null)) {
                    String ret = new String(res, 0, retLen);
                    return ret;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public synchronized void setVariable(String name, String value) {
        if (pipe != null) {
            IntByReference ibr = new IntByReference();
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_SETVARIABLE}, 1, ibr, null);
            int nameLen = name.getBytes().length;
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{(byte) ((nameLen >> 8) & 0xff), (byte) (nameLen & 0xff)}, 2, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, name.getBytes(), nameLen, ibr, null);

            int valLen = value.getBytes().length;
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{(byte) ((valLen >> 8) & 0xff), (byte) (valLen & 0xff)}, 2, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, value.getBytes(), valLen, ibr, null);
        }
    }

    public synchronized String call(String callString) {
        if (pipe != null) {
            IntByReference ibr = new IntByReference();
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_CALL}, 1, ibr, null);
            int callLen = callString.getBytes().length;
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{(byte) ((callLen >> 8) & 0xff), (byte) (callLen & 0xff)}, 2, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, callString.getBytes(), callLen, ibr, null);

            byte res[] = new byte[2];
            if (Kernel32.INSTANCE.ReadFile(pipe, res, 2, ibr, null)) {
                int retLen = ((res[0] & 0xff) << 8) + (res[1] & 0xff);
                res = new byte[retLen];
                if (Kernel32.INSTANCE.ReadFile(pipe, res, retLen, ibr, null)) {
                    String ret = new String(res, 0, retLen);
                    return ret;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    private synchronized void resize() {
        if (pipe != null) {
            IntByReference ibr = new IntByReference();
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_RESIZE}, 1, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{
                (byte) (getWidth() / 256), (byte) (getWidth() % 256),
                (byte) (getHeight() / 256), (byte) (getHeight() % 256),}, 4, ibr, null);
        }
    }

    private int __getCurrentFrame() {
        byte[] res = new byte[2];
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_CURRENT_FRAME}, 1, ibr, null);
        if (Kernel32.INSTANCE.ReadFile(pipe, res, res.length, ibr, null)) {
            return ((res[0] & 0xff) << 8) + (res[1] & 0xff);
        } else {
            return 0;
        }
    }

    @Override
    public synchronized int getCurrentFrame() {
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
    }

    @Override
    public synchronized int getTotalFrames() {
        if (specialPlayback) {
            String durStr = getVariable("_root.my_sound.duration");
            if (durStr != null) {
                return Integer.parseInt(durStr);
            }
        }
        byte[] res = new byte[2];
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_TOTAL_FRAMES}, 1, ibr, null);
        if (Kernel32.INSTANCE.ReadFile(pipe, res, res.length, ibr, null)) {
            return ((res[0] & 0xff) << 8) + (res[1] & 0xff);
        } else {
            return 0;
        }
    }

    @Override
    public synchronized void setBackground(Color color) {
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_BGCOLOR}, 1, ibr, null);
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()}, 3, ibr, null);
    }

    public FlashPlayerPanel(Component frame) {
        if (!Platform.isWindows()) {
            throw new FlashUnsupportedException();
        }
        this.frame = frame;
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                resize();
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
    private WinDef.HWND hwndFrame;

    private void execute() {
        String path = Utf8Helper.urlDecode(FlashPlayerPanel.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String appDir = new File(path).getParentFile().getAbsolutePath();
        if (!appDir.endsWith("\\")) {
            appDir += "\\";
        }
        String exePath = appDir + "lib\\FlashPlayer.exe";
        File f = new File(exePath);
        if (!f.exists()) {
            return;
        }

        WinDef.HWND hwnd = new WinDef.HWND();
        hwnd.setPointer(Native.getComponentPointer(this));

        hwndFrame = new WinDef.HWND();
        hwndFrame.setPointer(Native.getComponentPointer(frame));

        pipe = Kernel32.INSTANCE.CreateNamedPipe("\\\\.\\pipe\\ffdec_flashplayer_" + hwnd.getPointer().hashCode(), Kernel32.PIPE_ACCESS_DUPLEX, Kernel32.PIPE_TYPE_BYTE, 1, 0, 0, 0, null);

        SHELLEXECUTEINFO sei = new SHELLEXECUTEINFO();
        sei.fMask = 0x00000040;
        sei.lpFile = new WString(exePath);
        sei.lpParameters = new WString(hwnd.getPointer().hashCode() + " " + hwndFrame.getPointer().hashCode());
        sei.nShow = WinUser.SW_NORMAL;
        Shell32.INSTANCE.ShellExecuteEx(sei);
        processes.add(sei.hProcess);

        Kernel32.INSTANCE.ConnectNamedPipe(pipe, null);
        pipes.add(pipe);
        executed = true;
    }

    public synchronized void stopSWF() {
        displaySWF("-", null, 1);
        stopped = true;
    }

    public synchronized boolean isStopped() {
        return stopped;
    }

    public synchronized void displaySWF(String flash, Color bgColor, int frameRate) {
        this.flash = flash;
        repaint();
        this.frameRate = frameRate;
        if (!executed) {
            execute();
            /*
             //How about commenting this out? Will it work? Let's try...
             try {
             Thread.sleep(1000);
             } catch (InterruptedException ex) {
             Logger.getLogger(FlashPlayerPanel.class.getName()).log(Level.SEVERE, null, ex);
             }*/
        }
        if (bgColor != null) {
            setBackground(bgColor);
        }
        resize();
        if (pipe != null) {
            IntByReference ibr = new IntByReference();
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_PLAY}, 1, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{(byte) flash.getBytes().length}, 1, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, flash.getBytes(), flash.getBytes().length, ibr, null);
        }
        stopped = false;
        specialPlaying = false;
        specialPosition = 0;
        if (specialPlayback) {
            play();
        }
    }

    public static void unload() {
        if (Platform.isWindows()) {
            for (int i = 0; i < processes.size(); i++) {
                Kernel32.INSTANCE.CloseHandle(pipes.get(i));
                Kernel32.INSTANCE.TerminateProcess(processes.get(i), 0);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        if ((!executed) && flash != null) {
            execute();
        }
        super.paint(g);
    }
    private int specialPosition = 0;

    private synchronized void __pause() {
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_PAUSE}, 1, ibr, null);
    }

    @Override
    public void pause() {
        if (specialPlayback) {
            specialPosition = getCurrentFrame();
            __gotoFrame(3);
            __play();
            specialPlaying = false;
            return;
        }
        __pause();
    }

    @Override
    public void rewind() {
        if (specialPlayback) {
            boolean plays = specialPlaying;
            pause();
            specialPosition = 0;
            if (plays) {
                play();
            }

            return;
        }
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_REWIND}, 1, ibr, null);
    }

    private synchronized void __play() {
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_RESUME}, 1, ibr, null);
    }

    @Override
    public void play() {
        if (specialPlayback) {
            double p = (((double) specialPosition) / 1000.0);
            setVariable("_root.execParam", "" + p);
            __gotoFrame(1);
            __play();
            specialPlaying = true;
            return;
        }
        __play();
    }

    @Override
    public boolean isPlaying() {
        if (specialPlayback) {
            return specialPlaying;
        }
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_PLAYING}, 1, ibr, null);
        byte[] res = new byte[1];
        if (Kernel32.INSTANCE.ReadFile(pipe, res, res.length, ibr, null)) {
            return res[0] == 1;
        } else {
            return false;
        }
    }

    private synchronized void __gotoFrame(int frame) {
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_GOTO}, 1, ibr, null);
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{(byte) ((frame >> 8) & 0xff), (byte) (frame & 0xff)}, 2, ibr, null);
    }

    @Override
    public void gotoFrame(int frame) {
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
}
