package com.jpexs.decompiler.flash.gui.player;

import com.jpexs.decompiler.flash.gui.FlashUnsupportedException;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.View;
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
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author JPEXS
 */
public class FlashPlayerPanel extends Panel implements FlashDisplay {

    private boolean executed = false;
    private String flash;
    private HANDLE pipe;
    private static List<HANDLE> processes = new ArrayList<>();
    private static List<HANDLE> pipes = new ArrayList<>();
    private JFrame frame;
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
    private int frameRate;

    private synchronized void resize() {
        if (pipe != null) {
            IntByReference ibr = new IntByReference();
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_RESIZE}, 1, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{
                (byte) (getWidth() / 256), (byte) (getWidth() % 256),
                (byte) (getHeight() / 256), (byte) (getHeight() % 256),}, 4, ibr, null);
        }
    }

    @Override
    public synchronized int getCurrentFrame() {
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
    public synchronized int getTotalFrames() {
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

    public FlashPlayerPanel(JFrame frame) {
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
        WinDef.HWND hwnd = new WinDef.HWND();
        hwnd.setPointer(Native.getComponentPointer(this));

        hwndFrame = new WinDef.HWND();
        hwndFrame.setPointer(Native.getComponentPointer(frame));


        pipe = Kernel32.INSTANCE.CreateNamedPipe("\\\\.\\pipe\\ffdec_flashplayer_" + hwnd.getPointer().hashCode(), Kernel32.PIPE_ACCESS_DUPLEX, Kernel32.PIPE_TYPE_BYTE, 1, 0, 0, 0, null);



        SHELLEXECUTEINFO sei = new SHELLEXECUTEINFO();
        sei.fMask = 0x00000040;
        String appDir = "";
        try {
            appDir = new File(URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8")).getParentFile().getAbsolutePath();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FlashPlayerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!appDir.endsWith("\\")) {
            appDir += "\\";
        }
        sei.lpFile = new WString(appDir + "lib\\FlashPlayer.exe");
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
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FlashPlayerPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (bgColor != null) {
            setBackground(bgColor);
        }
        if (pipe != null) {
            IntByReference ibr = new IntByReference();
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_PLAY}, 1, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{(byte) flash.getBytes().length}, 1, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, flash.getBytes(), flash.getBytes().length, ibr, null);
        }
        resize();
        stopped = false;
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

    @Override
    public void pause() {
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_PAUSE}, 1, ibr, null);
    }

    @Override
    public void rewind() {
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_REWIND}, 1, ibr, null);
    }

    @Override
    public void play() {
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_RESUME}, 1, ibr, null);
    }

    @Override
    public boolean isPlaying() {
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_PLAYING}, 1, ibr, null);
        byte res[] = new byte[1];
        if (Kernel32.INSTANCE.ReadFile(pipe, res, res.length, ibr, null)) {
            return res[0] == 1;
        } else {
            return false;
        }
    }

    @Override
    public void gotoFrame(int frame) {
        IntByReference ibr = new IntByReference();
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{CMD_GOTO}, 1, ibr, null);
        Kernel32.INSTANCE.WriteFile(pipe, new byte[]{(byte) ((frame >> 8) & 0xff), (byte) (frame & 0xff)}, 2, ibr, null);
    }

    @Override
    public int getFrameRate() {
        return frameRate;
    }

    @Override
    public boolean isLoaded() {
        return !isStopped();
    }
}
