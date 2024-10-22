/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Fixes probles in some JPEGs to be readable by standard viewers.
 * <p>
 * It removes: 1) EOI markers followed by SOI markers 2) EOI SOI on the
 * beginning of the file 3) Second or more SOI markers in the file
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
            //Check for erroneous header at the beginning, before first SOI marker
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
        loopread:
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
                } else if (val == SOI) {
                    //second or more SOI in the file, remove that too
                } else if (prevEoi) {
                    os.write(0xFF);
                    os.write(EOI);
                    os.write(0xFF);
                    if (val != -1) {
                        os.write(val);
                    }
                } else if (val != EOI) {
                    os.write(0xFF);
                    if (val != -1) {
                        os.write(val);
                    }
                }

                if (val != -1 && JpegMarker.markerHasLength(val)) {
                    int len1 = is.read();
                    if (len1 == -1) {
                        break;
                    }
                    int len2 = is.read();
                    if (len2 == -1) {
                        os.write(len1);
                        break;
                    }
                    os.write(len1);
                    os.write(len2);
                    int len = (len1 << 8) + len2;
                    for (int i = 0; i < len - 2; i++) {
                        int val2 = is.read();
                        if (val2 == -1) {
                            break loopread;
                        }
                        os.write(val2);
                    }
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
