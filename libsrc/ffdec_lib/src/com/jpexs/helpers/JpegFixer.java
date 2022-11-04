package com.jpexs.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Fixes probles in some JPEGs to be readable by standard viewers.
 *
 * It removes EOI markers followed by SOI markers. It does it also on the
 * beginning of the file.
 *
 * @author JPEXS
 */
public class JpegFixer {

    public static final int SOI = 0xD8;
    public static final int EOI = 0xD9;

    public void fixJpeg(InputStream is, OutputStream os) throws IOException {
        boolean prevEoi = false;

        int val = is.read();
        if (val == -1) {
            return;
        }
        if (val == 0xFF) {
            val = is.read();
            if (val == -1) {
                os.write(0xFF);
                return;
            }
            if (val != SOI && val != EOI) {
                //not a JPEG file, nor invalid header, proceed as is
                os.write(0xFF);
                os.write(val);
                while ((val = is.read()) > -1) {
                    os.write(val);
                }
                return;
            }
            //Check for errorneous header at the beginning, before first SOI marker
            if (val == EOI) {
                val = is.read();
                int val2 = is.read();
                if (val == 0xFF && val2 == SOI) {
                    val = is.read();
                    val2 = is.read();
                    if (val != 0xFF || val2 != SOI) {
                        //not a JPEG file, proceed as is                       
                        os.write(0xFF);
                        os.write(EOI);
                        os.write(0xFF);
                        os.write(SOI);
                        if (val != -1) {
                            os.write(val);
                        }
                        if (val2 != -1) {
                            os.write(val2);
                        }
                        while ((val = is.read()) > -1) {
                            os.write(val);
                        }
                        return;
                    }
                } else {
                    //not a JPEG file, proceed as is
                    os.write(0xFF);
                    os.write(EOI);
                    if (val != -1) {
                        os.write(val);
                    }
                    if (val2 != -1) {
                        os.write(val2);
                    }
                    while ((val = is.read()) > -1) {
                        os.write(val);
                    }
                    return;
                }
            }
            os.write(0xFF);
            os.write(SOI);
        } else {
            //not a JPEG file, proceed as is
            os.write(val);
            while ((val = is.read()) > -1) {
                os.write(val);
            }
            return;
        }

        //main removing EOI+SOI
        while ((val = is.read()) > -1) {
            if (val == 0xFF) {
                val = is.read();
                if (val == 0) {
                    os.write(0xFF);
                    os.write(val);
                    prevEoi = false;
                    continue;
                }

                if (val == SOI && prevEoi) {
                    //ignore, effectively removing EOI and SOI
                } else if (prevEoi) {
                    os.write(0xFF);
                    os.write(EOI);
                    os.write(0xFF);
                    os.write(val);
                } else if (val != EOI) {
                    os.write(0xFF);
                    os.write(val);
                }

                prevEoi = val == EOI;
            } else {
                os.write(val);
                prevEoi = false;
            }
        }
        if (prevEoi) {
            os.write(0xFF);
            os.write(EOI);
        }
    }
}
