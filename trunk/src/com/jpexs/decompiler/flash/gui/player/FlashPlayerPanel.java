package com.jpexs.decompiler.flash.gui.player;

import com.jpexs.decompiler.flash.gui.FlashUnsupportedException;
import com.jpexs.decompiler.flash.gui.Main;
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
public class FlashPlayerPanel extends Panel {

    private boolean executed = false;
    private String flash;
    private HANDLE pipe;
    private static List<HANDLE> processes = new ArrayList<>();
    private static List<HANDLE> pipes = new ArrayList<>();
    private JFrame frame;

    private synchronized void resize() {
        if (pipe != null) {
            IntByReference ibr = new IntByReference();
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{2}, 1, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{
                (byte) (getWidth() / 256), (byte) (getWidth() % 256),
                (byte) (getHeight() / 256), (byte) (getHeight() % 256),}, 4, ibr, null);
        }
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


        pipe = Kernel32.INSTANCE.CreateNamedPipe("\\\\.\\pipe\\ffdec_flashplayer_" + hwnd.getPointer().hashCode(), Kernel32.PIPE_ACCESS_OUTBOUND, Kernel32.PIPE_TYPE_BYTE, 1, 0, 0, 0, null);



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

    public synchronized void displaySWF(String flash) {
        this.flash = flash;
        repaint();
        if (!executed) {
            execute();
        }
        if (pipe != null) {
            IntByReference ibr = new IntByReference();
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{1}, 1, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, new byte[]{(byte) flash.getBytes().length}, 1, ibr, null);
            Kernel32.INSTANCE.WriteFile(pipe, flash.getBytes(), flash.getBytes().length, ibr, null);
        }
        resize();
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
}
