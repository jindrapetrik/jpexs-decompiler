/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jna.platform.win32;

/**
 *
 * @author petrik
 */
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.win32.StdCallLibrary;
import java.util.Arrays;
import java.util.List;

/**
 * Ported from WinGDI.h. Microsoft Windows SDK 6.0A.
 *
 * @author dblock[at]dblock.org
 */
public interface WinGDI extends StdCallLibrary {

    public int RDH_RECTANGLES = 1;

    public class RGNDATAHEADER extends Structure {

        public int dwSize = size();
        public int iType = RDH_RECTANGLES; // required
        public int nCount;
        public int nRgnSize;
        public RECT rcBound;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"dwSize", "iType", "nCount", "nRgnSize", "rcBound"});
        }
    }

    public class RGNDATA extends Structure {

        public RGNDATAHEADER rdh;
        public byte[] Buffer;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"rdh", "Buffer"});
        }

        public RGNDATA() {
            this(1);
        }

        public RGNDATA(int bufferSize) {
            Buffer = new byte[bufferSize];
            allocateMemory();
        }
    }
    public int RGN_AND = 1;
    public int RGN_OR = 2;
    public int RGN_XOR = 3;
    public int RGN_DIFF = 4;
    public int RGN_COPY = 5;
    public int ERROR = 0;
    public int NULLREGION = 1;
    public int SIMPLEREGION = 2;
    public int COMPLEXREGION = 3;
    public int ALTERNATE = 1;
    public int WINDING = 2;
    public int BI_RGB = 0;
    public int BI_RLE8 = 1;
    public int BI_RLE4 = 2;
    public int BI_BITFIELDS = 3;
    public int BI_JPEG = 4;
    public int BI_PNG = 5;

    public class BITMAPINFOHEADER extends Structure {

        public int biSize = size();
        public int biWidth;
        public int biHeight;
        public short biPlanes;
        public short biBitCount;
        public int biCompression;
        public int biSizeImage;
        public int biXPelsPerMeter;
        public int biYPelsPerMeter;
        public int biClrUsed;
        public int biClrImportant;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"biSize", "biWidth", "biHeight", "biPlanes", "biBitCount", "biCompression", "biSizeImage", "biXPelsPerMeter", "biYPelsPerMeter", "biClrUsed", "biClrImportant"});
        }
    }

    public class RGBQUAD extends Structure {

        public byte rgbBlue;
        public byte rgbGreen;
        public byte rgbRed;
        public byte rgbReserved = 0;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"rgbBlue", "rgbGreen", "rgbRed", "rgbReserved"});
        }
    }

    public class BITMAPINFO extends Structure {

        public BITMAPINFOHEADER bmiHeader = new BITMAPINFOHEADER();
        public RGBQUAD[] bmiColors = new RGBQUAD[1];

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"bmiHeader", "bmiColors"});
        }

        public BITMAPINFO() {
            this(1);
        }

        public BITMAPINFO(int size) {
            bmiColors = new RGBQUAD[size];
        }
    }
    public int DIB_RGB_COLORS = 0;
    public int DIB_PAL_COLORS = 1;
}
