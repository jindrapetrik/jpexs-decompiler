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
package com.jpexs.decompiler.flash.gui.pipes;

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.View;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import java.awt.Window;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class FirstInstance {

    private static final String MUTEX_NAME = "FFDEC_MUTEX";

    private static WinNT.HANDLE mutex;

    public static final int PIPE_MAJOR = 1;

    public static final int PIPE_MINOR = 0;

    public static final String PIPE_NAME = "ffdec";

    public static final String PIPE_APP_CODE = "ffdec";

    private static boolean isRunning() {
        if (Platform.isWindows()) {
            mutex = Kernel32.INSTANCE.CreateMutex(null, false, MUTEX_NAME);
            if (mutex == null) {
                return false;
            }
            int er = Kernel32.INSTANCE.GetLastError();
            if (er == WinError.ERROR_ALREADY_EXISTS) {
                return true;
            }

            new Thread("OtherInstanceCommunicator") {
                @Override
                public void run() {
                    while (true) {
                        try (PipeInputStream pis = new PipeInputStream(PIPE_NAME, true)) {
                            ObjectInputStream ois = new ObjectInputStream(pis);
                            String app = ois.readUTF();
                            if (app.equals(PIPE_APP_CODE)) {
                                int major = ois.readInt();
                                int minor = ois.readInt();
                                int release = ois.readInt();
                                int build = ois.readInt();
                                int pipeMajor = ois.readInt();
                                int pipeMinor = ois.readInt();

                                if (pipeMajor == PIPE_MAJOR) {
                                    String command = ois.readUTF();
                                    switch (command) {
                                        case "open":
                                            int cnt = ois.readInt();
                                            String[] fileNames = new String[cnt];
                                            for (int i = 0; i < cnt; i++) {
                                                fileNames[i] = ois.readUTF();
                                            }

                                            View.execInEventDispatch(() -> {
                                                for (int i = 0; i < cnt; i++) {
                                                    Main.openFile(fileNames[i], null);
                                                }
                                            });
                                        //no break - focus too
                                        case "focus":

                                            View.execInEventDispatch(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Window wnd = Main.getMainFrame().getWindow();
                                                    wnd.setAlwaysOnTop(true);
                                                    wnd.toFront();
                                                    wnd.requestFocus();
                                                    wnd.setAlwaysOnTop(false);
                                                    wnd.repaint();
                                                }
                                            });
                                            break;
                                    }
                                }

                            }
                        } catch (IOException ex) {
                            //ignore
                        }
                    }
                }
            }.start();

        }
        return false;
    }

    private static ObjectOutputStream startCommand(String command) throws IOException {
        PipeOutputStream pos = new PipeOutputStream(PIPE_NAME, false);
        ObjectOutputStream oos = new ObjectOutputStream(pos);
        oos.writeUTF(PIPE_APP_CODE);
        oos.writeInt(ApplicationInfo.version_major);
        oos.writeInt(ApplicationInfo.version_minor);
        oos.writeInt(ApplicationInfo.version_release);
        oos.writeInt(ApplicationInfo.version_build);
        oos.writeInt(PIPE_MAJOR);
        oos.writeInt(PIPE_MINOR);
        oos.writeUTF(command);
        return oos;
    }

    public static boolean focus() {
        if (!isRunning()) {
            return false;
        }
        try {
            ObjectOutputStream oos = startCommand("focus");
            oos.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static boolean openFiles(List<String> files) {
        try {
            ObjectOutputStream oos = startCommand("open");
            oos.writeInt(files.size());
            for (String s : files) {
                oos.writeUTF(s);
            }
            oos.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
