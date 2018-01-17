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
package com.jpexs.process.win32;

import com.jpexs.helpers.ProgressListener;
import com.jpexs.process.ProcessTools;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.BITMAP;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.Gdi32;
import com.sun.jna.platform.win32.ICONINFO;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.MEMORY_BASIC_INFORMATION;
import com.sun.jna.platform.win32.PROCESSENTRY32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.SHFILEINFO;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class Win32ProcessTools extends ProcessTools {

    private static long pointerToAddress(Pointer p) {
        String s = p.toString();
        s = s.replace("native@0x", "");
        return Long.parseLong(s, 16);
    }

    public static List<MEMORY_BASIC_INFORMATION> getPageRanges(WinNT.HANDLE hOtherProcess) {
        List<MEMORY_BASIC_INFORMATION> ret = new ArrayList<>();
        MEMORY_BASIC_INFORMATION mbi;
        WinBase.SYSTEM_INFO si = new WinBase.SYSTEM_INFO();
        Kernel32.INSTANCE.GetSystemInfo(si);
        Pointer lpMem = si.lpMinimumApplicationAddress;
        while (pointerToAddress(lpMem) < pointerToAddress(si.lpMaximumApplicationAddress)) {
            mbi = new MEMORY_BASIC_INFORMATION();
            BaseTSD.SIZE_T t = Kernel32.INSTANCE.VirtualQueryEx(hOtherProcess, lpMem, mbi, new BaseTSD.SIZE_T(mbi.size()));
            if (t.longValue() == 0) {
                Logger.getLogger(Win32ProcessTools.class.getName()).log(Level.SEVERE, "Cannot get page ranges. Last error:" + Kernel32.INSTANCE.GetLastError());
                break;
            }
            ret.add(mbi);
            lpMem = new Pointer(pointerToAddress(mbi.baseAddress) + mbi.regionSize.longValue());
        }
        return ret;
    }

    public static void printMemInfo(MEMORY_BASIC_INFORMATION mbi) {
        System.out.println("Region size:" + mbi.regionSize);
        String stateStr = "";
        if ((mbi.state.intValue() & Kernel32.MEM_COMMIT) == Kernel32.MEM_COMMIT) {
            stateStr += " commit";
        }
        if ((mbi.state.intValue() & Kernel32.MEM_FREE) == Kernel32.MEM_FREE) {
            stateStr += " free";
        }
        if ((mbi.state.intValue() & Kernel32.MEM_RESERVE) == Kernel32.MEM_RESERVE) {
            stateStr += " reserve";
        }
        stateStr = stateStr.trim();

        String typeStr = "";
        if ((mbi.type.intValue() & Kernel32.MEM_IMAGE) == Kernel32.MEM_IMAGE) {
            typeStr += " image";
        }
        if ((mbi.type.intValue() & Kernel32.MEM_MAPPED) == Kernel32.MEM_MAPPED) {
            typeStr += " mapped";
        }
        if ((mbi.type.intValue() & Kernel32.MEM_PRIVATE) == Kernel32.MEM_PRIVATE) {
            typeStr += " private";
        }
        typeStr = typeStr.trim();

        String protStr = "";
        if ((mbi.allocationProtect.intValue() & WinNT.PAGE_EXECUTE) == Kernel32.PAGE_EXECUTE) {
            protStr += " execute";
        }
        if ((mbi.allocationProtect.intValue() & WinNT.PAGE_EXECUTE_READ) == Kernel32.PAGE_EXECUTE_READ) {
            protStr += " execute_read";
        }
        if ((mbi.allocationProtect.intValue() & WinNT.PAGE_EXECUTE_READWRITE) == Kernel32.PAGE_EXECUTE_READWRITE) {
            protStr += " execute_readwrite";
        }
        if ((mbi.allocationProtect.intValue() & WinNT.PAGE_READONLY) == Kernel32.PAGE_READONLY) {
            protStr += " readonly";
        }
        if ((mbi.allocationProtect.intValue() & WinNT.PAGE_READWRITE) == Kernel32.PAGE_READWRITE) {
            protStr += " readwrite";
        }
        if ((mbi.allocationProtect.intValue() & WinNT.PAGE_WRITECOPY) == Kernel32.PAGE_WRITECOPY) {
            protStr += " writecopy";
        }

        protStr = protStr.trim();
        System.out.println("State:" + stateStr);
        System.out.println("Type:" + typeStr);
        System.out.println("Protect:" + protStr);
        System.out.println("baseAddress:" + mbi.baseAddress);
        System.out.println("========================");
    }

    private static byte[] mergeArrays(byte[] one, byte[] two) {
        byte[] combined = new byte[one.length + two.length];

        System.arraycopy(one, 0, combined, 0, one.length);
        System.arraycopy(two, 0, combined, one.length, two.length);
        return combined;
    }

    public static Map<String, Character> getDriveMappings() {
        Map<String, Character> ret = new HashMap<>();
        for (char d = 'A'; d <= 'Z'; d++) {
            char[] buf = new char[1024];
            int len = Kernel32.INSTANCE.QueryDosDevice(d + ":", buf, buf.length).intValue();
            String tar = new String(buf, 0, len);
            tar = tar.trim();
            if (!"".equals(tar)) {
                ret.put(tar, d);
            }
        }
        return ret;
    }

    public static String ntPathToWin32(String path) {
        for (String dp : driveMappings.keySet()) {
            if (path.startsWith(dp)) {
                return driveMappings.get(dp) + ":" + path.substring(dp.length());
            }
        }
        return path;
    }

    private static final Map<String, Character> driveMappings = getDriveMappings();

    public static boolean drawIcon(BufferedImage ret, WinDef.HICON hIcon, int diFlags) {

        WinDef.HDC hdcScreen = User32.INSTANCE.GetDC(null);
        WinDef.HDC hdcMem = Gdi32.INSTANCE.CreateCompatibleDC(hdcScreen);

        WinDef.HBITMAP bitmap = Gdi32.INSTANCE.CreateCompatibleBitmap(hdcScreen, ret.getWidth(), ret.getHeight());

        WinNT.HANDLE hbmOld = Gdi32.INSTANCE.SelectObject(hdcMem, bitmap);

        WinNT.HANDLE hBrush = Gdi32.INSTANCE.CreateSolidBrush(new WinDef.DWORD(0xffffff));
        WinDef.RECT rect = new WinDef.RECT();
        rect.left = 0;
        rect.top = 0;
        rect.right = ret.getWidth();
        rect.bottom = ret.getHeight();
        User32.INSTANCE.FillRect(hdcMem, rect, hBrush);
        Gdi32.INSTANCE.DeleteObject(hBrush);

        boolean ok = User32.INSTANCE.DrawIconEx(hdcMem, 0, 0, hIcon, ret.getWidth(), ret.getHeight(), new WinDef.UINT(0), new WinDef.HBRUSH(Pointer.NULL), diFlags);

        if (!ok) {
            return false;
        }

        for (int x = 0; x < ret.getWidth(); x++) {
            for (int y = 0; y < ret.getHeight(); y++) {
                int rgb = Gdi32.INSTANCE.GetPixel(hdcMem, x, y).intValue();
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb) & 0xff;
                rgb = (b << 16) + (g << 8) + r;
                ret.setRGB(x, y, rgb);
            }
        }

        Gdi32.INSTANCE.SelectObject(hdcMem, hbmOld);
        Gdi32.INSTANCE.DeleteObject(bitmap);
        Gdi32.INSTANCE.DeleteDC(hdcMem);
        User32.INSTANCE.ReleaseDC(null, hdcScreen);
        return true;
    }

    private static void applyMask(BufferedImage image, BufferedImage mask) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int masked = mask.getRGB(x, y);
                int alpha = 255 - (masked & 0xff);
                if (alpha != 0 && alpha != 255) {
                    System.out.println("a=" + alpha);
                }
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb) & 0xff;
                r = r * alpha / 255;
                g = g * alpha / 255;
                b = b * alpha / 255;

                rgb = (r << 16) + (g << 8) + b;

                rgb = (alpha << 24) | rgb;
                image.setRGB(x, y, rgb);
            }
        }
    }

    public static BufferedImage iconToImage(WinDef.HICON hIcon) {
        ICONINFO info = new ICONINFO();
        boolean ok = User32.INSTANCE.GetIconInfo(hIcon, info);
        if (!ok) {
            return null;
        }
        BufferedImage ret = null;
        BITMAP bmp = new BITMAP();
        if (info.hbmColor != null) {
            int nWrittenBytes = Gdi32.INSTANCE.GetObject(info.hbmColor, bmp.size(), bmp);
            if (nWrittenBytes > 0) {
                ret = new BufferedImage(bmp.bmWidth.intValue(), bmp.bmHeight.intValue(), BufferedImage.TYPE_INT_ARGB);
            }
        } else if (info.hbmMask != null) {
            int nWrittenBytes = Gdi32.INSTANCE.GetObject(info.hbmMask, bmp.size(), bmp);
            if (nWrittenBytes > 0) {
                ret = new BufferedImage(bmp.bmWidth.intValue(), bmp.bmHeight.intValue() / 2, BufferedImage.TYPE_INT_ARGB);
            }
        }
        if (ret == null) {
            return ret;
        }

        if (info.hbmColor != null) {
            Gdi32.INSTANCE.DeleteObject(info.hbmColor);
        }
        if (info.hbmMask != null) {
            Gdi32.INSTANCE.DeleteObject(info.hbmMask);
        }

        drawIcon(ret, hIcon, User32.DI_NORMAL);
        BufferedImage mask = new BufferedImage(ret.getWidth(), ret.getHeight(), BufferedImage.TYPE_INT_RGB);
        drawIcon(mask, hIcon, User32.DI_MASK);
        applyMask(ret, mask);

        return ret;
    }

    public static BufferedImage getShellIcon(String path) {
        SHFILEINFO fi = new SHFILEINFO();
        BaseTSD.DWORD_PTR r = Shell32.INSTANCE.SHGetFileInfo(path, 0, fi, fi.size(), Shell32.SHGFI_ICON | Shell32.SHGFI_SMALLICON);
        if (r.intValue() == 0) {
            return null;
        }
        return iconToImage(fi.hIcon);
    }

    public static BufferedImage extractExeIcon(String fullExeFile, int index, boolean large) {
        PointerByReference iconsLargeRef = new PointerByReference();
        PointerByReference iconsSmallRef = new PointerByReference();
        int cnt = Shell32.INSTANCE.ExtractIconEx(fullExeFile, index, iconsLargeRef, iconsSmallRef, new WinDef.UINT(1)).intValue();
        if (cnt == 0) {
            return null;
        }
        Pointer iconsLarge = iconsLargeRef.getPointer();
        Pointer iconsSmall = iconsSmallRef.getPointer();

        WinDef.HICON icon;
        if (large) {
            icon = new WinDef.HICON(iconsLarge.getPointer(0));
        } else {
            icon = new WinDef.HICON(iconsSmall.getPointer(0));
        }

        BufferedImage ic = iconToImage(icon);

        User32.INSTANCE.DestroyIcon(icon);
        return ic;
    }

    public static List<com.jpexs.process.Process> listProcesses() {
        if (!privAdjusted) {
            adjustPrivileges();
        }
        List<com.jpexs.process.Process> ret = new ArrayList<>();
        WinNT.HANDLE hSnapShot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(new WinDef.DWORD(Kernel32.TH32CS_SNAPPROCESS), new WinDef.DWORD(0));
        PROCESSENTRY32 pe = new PROCESSENTRY32();
        if (Kernel32.INSTANCE.Process32First(hSnapShot, pe)) {
            do {
                int i;
                i = 0;
                for (; i < pe.szExeFile.length; i++) {
                    if (pe.szExeFile[i] == 0) {
                        break;
                    }
                }
                String exeFile = new String(pe.szExeFile, 0, i);
                char[] outputnames = new char[1024];
                WinNT.HANDLE tempProcess = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_QUERY_INFORMATION, false, pe.th32ProcessID);
                if (tempProcess == null) {
                    continue;
                }

                try {
                    int rn = Psapi.INSTANCE.GetProcessImageFileNameW(tempProcess, outputnames, 1024);
                    if (rn == 0) {
                        Logger.getLogger(Win32ProcessTools.class.getName()).log(Level.SEVERE, "Can't get EXE path");
                    }
                } catch (Exception | UnsatisfiedLinkError e) {
                }

                try {
                    int rn = Kernel32.INSTANCE.GetProcessImageFileNameW(tempProcess, outputnames, 1024);
                    if (rn == 0) {
                        Logger.getLogger(Win32ProcessTools.class.getName()).log(Level.SEVERE, "Can't get EXE path");
                    }
                } catch (Exception | UnsatisfiedLinkError e) {
                }
                i = 0;
                for (; i < outputnames.length; i++) {
                    if (outputnames[i] == 0) {
                        break;
                    }
                }
                String fullExeFile = new String(outputnames, 0, i);
                fullExeFile = ntPathToWin32(fullExeFile);
                ret.add(new Win32Process(fullExeFile, exeFile, getShellIcon(fullExeFile), pe.th32ProcessID));
            } while (Kernel32.INSTANCE.Process32Next(hSnapShot, pe));
        }
        return ret;
    }

    private static boolean pageReadable(MEMORY_BASIC_INFORMATION mbi) {
        int iUnReadable = 0;
        iUnReadable |= (mbi.state.intValue() == Kernel32.MEM_FREE) ? 1 : 0;
        iUnReadable |= (mbi.state.intValue() == Kernel32.MEM_RESERVE) ? 1 : 0;
        iUnReadable |= (mbi.protect.intValue() & WinNT.PAGE_WRITECOPY);
        iUnReadable |= (mbi.protect.intValue() & WinNT.PAGE_EXECUTE);
        iUnReadable |= (mbi.protect.intValue() & WinNT.PAGE_GUARD);
        iUnReadable |= (mbi.protect.intValue() & WinNT.PAGE_NOACCESS);
        return iUnReadable == 0;
    }

    public static Map<Long, InputStream> findBytesInProcessMemory(ProgressListener progListener, WinDef.DWORD dwProcessID, byte[][] findBytesAll) {
        int maxFindLen = 0;
        for (int i = 0; i < findBytesAll.length; i++) {
            if (findBytesAll[i].length > maxFindLen) {
                maxFindLen = findBytesAll[i].length;
            }
        }
        Map<Long, InputStream> ret = new HashMap<>();
        WinNT.HANDLE hOtherProcess = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_VM_WRITE | Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_OPERATION /*for VirtualProtectEx*/, false, dwProcessID);
        List<MEMORY_BASIC_INFORMATION> pages = getPageRanges(hOtherProcess);
        long totalMemLen = 0;
        int progress = 0;
        for (int pg = 0; pg < pages.size(); pg++) {
            totalMemLen += pages.get(pg).regionSize.longValue();
        }

        long actualPos = 0;
        List<Integer> guardedPages = new ArrayList<>();
        for (int pg = 0; pg < pages.size(); pg++) {
            MEMORY_BASIC_INFORMATION mbi = pages.get(pg);
            if (pageReadable(mbi)) {
                long addr = pointerToAddress(mbi.baseAddress);
                int maxsize = mbi.regionSize.intValue();
                long pos = 0;
                long bufSize = 1024 * 512;
                do {
                    NativeLongByReference bytesReadRef = new NativeLongByReference();
                    Memory buf = new Memory(bufSize);
                    boolean ok = Kernel32.INSTANCE.ReadProcessMemory(hOtherProcess, new Pointer(addr + pos), buf, new NativeLong(bufSize), bytesReadRef);
                    if (!ok) {
                        break;
                    }
                    if (bytesReadRef.getValue().longValue() == 0) {
                        break;
                    }

                    byte[] data = buf.getByteArray(0, bytesReadRef.getValue().intValue());

                    byte[] prevBytes = Arrays.copyOfRange(data, data.length - maxFindLen, data.length);
                    byte[] dataPlusPrev = mergeArrays(prevBytes, data);
                    loopi:
                    for (int i = 0; i < dataPlusPrev.length - maxFindLen; i++) {
                        loopk:
                        for (int k = 0; k < findBytesAll.length; k++) {
                            if (dataPlusPrev[i] == findBytesAll[k][0]) {
                                for (int p = 1; p < findBytesAll[k].length; p++) {
                                    if (dataPlusPrev[i + p] != findBytesAll[k][p]) {
                                        continue loopk;
                                    }
                                }
                                long dataAddr = (long) (addr + pos - prevBytes.length + i);
                                ret.put(dataAddr, new ProcessMemoryInputStream(pages, hOtherProcess, pg, pos - prevBytes.length + i));
                            }
                        }

                    }
                    pos += bytesReadRef.getValue().longValue();
                    if (progListener != null) {
                        int newprogress = Math.round((actualPos + pos) * 100 / totalMemLen);
                        if (newprogress != progress) {
                            progress = newprogress;
                            progListener.progress(progress);
                        }
                    }
                    if (pos + bufSize >= maxsize) {
                        bufSize = maxsize - pos;
                    }
                } while (bufSize > 0);
            } else if (hasGuard(mbi)) {
                if (unsetGuard(hOtherProcess, mbi)) {
                    guardedPages.add(pg);
                    pg--;
                    continue;
                }
            }
            actualPos += mbi.regionSize.longValue();
            int newprogress = Math.round(actualPos * 100 / totalMemLen);
            if (newprogress != progress) {
                progress = newprogress;
                progListener.progress(progress);
            }
        }

        //Set PAGE_GUARD back again
        for (int pg : guardedPages) {
            setGuard(hOtherProcess, pages.get(pg));
        }
        return ret;
    }

    private static boolean hasGuard(MEMORY_BASIC_INFORMATION mbi) {
        return (mbi.protect.intValue() & WinNT.PAGE_GUARD) == WinNT.PAGE_GUARD;
    }

    private static boolean unsetGuard(HANDLE hOtherProcess, MEMORY_BASIC_INFORMATION mbi) {
        if (!hasGuard(mbi)) {
            return true;
        }
        int oldProt = mbi.protect.intValue();
        int newProt = oldProt - WinNT.PAGE_GUARD;
        IntByReference oldProtRef = new IntByReference();
        boolean ok = Kernel32.INSTANCE.VirtualProtectEx(hOtherProcess, new WinDef.LPVOID(pointerToAddress(mbi.baseAddress)), mbi.regionSize, newProt, oldProtRef);
        if (ok) {
            mbi.protect = new NativeLong(newProt);
            return true;
        }
        return false;
    }

    private static boolean setGuard(HANDLE hOtherProcess, MEMORY_BASIC_INFORMATION mbi) {
        if (hasGuard(mbi)) {
            return true;
        }
        int oldProt = mbi.protect.intValue();
        int newProt = oldProt | WinNT.PAGE_GUARD;
        IntByReference oldProtRef = new IntByReference();
        boolean ok = Kernel32.INSTANCE.VirtualProtectEx(hOtherProcess, new WinDef.LPVOID(pointerToAddress(mbi.baseAddress)), mbi.regionSize, newProt, oldProtRef);
        if (ok) {
            mbi.protect = new NativeLong(newProt);
            return true;
        }
        return false;
    }

    private static boolean privAdjusted = false;

    public static boolean adjustPrivileges() {

        WinNT.TOKEN_PRIVILEGES tp = new WinNT.TOKEN_PRIVILEGES(1);
        WinNT.TOKEN_PRIVILEGES oldtp = new WinNT.TOKEN_PRIVILEGES(1);
        WinNT.LUID luid = new WinNT.LUID();
        WinNT.HANDLEByReference hTokenRef = new WinNT.HANDLEByReference();
        if (!Advapi32.INSTANCE.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(), WinNT.TOKEN_ADJUST_PRIVILEGES | WinNT.TOKEN_QUERY, hTokenRef)) {
            return false;
        }
        WinNT.HANDLE hToken = hTokenRef.getValue();
        if (!Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_DEBUG_NAME, luid)) {
            Kernel32.INSTANCE.CloseHandle(hToken);
            return false;
        }

        tp.PrivilegeCount = new WinDef.DWORD(1);
        tp.Privileges = new WinNT.LUID_AND_ATTRIBUTES[1];
        tp.Privileges[0] = new WinNT.LUID_AND_ATTRIBUTES(luid, new WinDef.DWORD(WinNT.SE_PRIVILEGE_ENABLED));

        IntByReference retSize = new IntByReference(0);
        if (!Advapi32.INSTANCE.AdjustTokenPrivileges(hToken, false, tp, tp.size(), oldtp, retSize)) {
            Kernel32.INSTANCE.CloseHandle(hToken);
            return false;
        }
        Kernel32.INSTANCE.CloseHandle(hToken);
        privAdjusted = true;
        return true;
    }

    private static class ProcessMemoryInputStream extends InputStream {

        private final List<MEMORY_BASIC_INFORMATION> pages;

        private int currentPage = 0;

        private long pagePos = 0;

        private static final int BUFFER_SIZE = 1024 * 512;

        private byte[] buf;

        private int bufPos;

        private final HANDLE hOtherProcess;

        public ProcessMemoryInputStream(List<MEMORY_BASIC_INFORMATION> pages, HANDLE hOtherProcess, int currentPage, long pagePos) {
            this.pages = pages;
            this.hOtherProcess = hOtherProcess;
            this.currentPage = currentPage;
            this.pagePos = pagePos;
        }

        private boolean readNext() throws IOException {
            MEMORY_BASIC_INFORMATION mbi = pages.get(currentPage);
            if (!pageReadable(mbi)) {
                if (hasGuard(mbi)) {
                    if (unsetGuard(hOtherProcess, mbi)) {
                        boolean ret = readNext();
                        setGuard(hOtherProcess, mbi);
                        return ret;
                    }
                }
                if (currentPage + 1 < pages.size()) {
                    pagePos = 0;
                    currentPage++;
                    return readNext();
                }
                return false;
            }
            long addr = pointerToAddress(mbi.baseAddress);
            int maxsize = mbi.regionSize.intValue();
            NativeLongByReference bytesReadRef = new NativeLongByReference();
            Memory membuf = new Memory(BUFFER_SIZE);
            NativeLong bufSize = new NativeLong(BUFFER_SIZE);
            if (pagePos + bufSize.longValue() > maxsize) {
                bufSize.setValue(maxsize - pagePos);
            }
            if (bufSize.longValue() == 0) {
                if (currentPage + 1 < pages.size()) {
                    pagePos = 0;
                    currentPage++;
                    return readNext();
                }
                return false;
            }
            boolean ok = Kernel32.INSTANCE.ReadProcessMemory(hOtherProcess, new Pointer(addr + pagePos), membuf, bufSize, bytesReadRef);
            if (!ok) {
                throw new IOException("Cannot read memory");
            }
            if (bytesReadRef.getValue().longValue() == 0) {
                return readNext();
            }
            pagePos += bytesReadRef.getValue().longValue();

            buf = membuf.getByteArray(0, bytesReadRef.getValue().intValue());
            return true;
        }

        @Override
        public int read() throws IOException {
            if ((buf == null) || (bufPos >= buf.length)) {
                if (buf != null) {
                    bufPos = 0;
                }
                if (!readNext()) {
                    return -1;
                }
            }

            return buf[bufPos++] & 0xff;
        }
    }
}
