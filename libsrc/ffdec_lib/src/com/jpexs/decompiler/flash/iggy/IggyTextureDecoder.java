/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.helpers.Bc7Decoder;
import java.io.IOException;

/**
 * Linear IggyFileTextureRaw data, decoded to straight-alpha ARGB.
 * The resource word contains a format byte followed by a mip level count.
 */
public final class IggyTextureDecoder {

    /** RGBA bytes in R, G, B, A order. */
    public static final int FORMAT_RGBA_8888 = 0;

    /** Little endian A4R4G4B4. */
    public static final int FORMAT_RGBA_4444_LE = 1;

    /** Little endian A1R5G5B5. */
    public static final int FORMAT_RGBA_5551_LE = 2;

    /** Luminance byte followed by alpha byte. */
    public static final int FORMAT_LA_88 = 3;

    /** Alpha in the high nibble, luminance in the low nibble. */
    public static final int FORMAT_LA_44 = 4;

    /** Byte intensity used for both RGB and alpha. */
    public static final int FORMAT_I_8 = 5;

    /** Byte intensity with a four-bit GPU precision hint. */
    public static final int FORMAT_I_4 = 6;

    /** Byte luminance with opaque alpha. */
    public static final int FORMAT_L_8 = 7;

    /** Byte luminance with a four-bit GPU precision hint. */
    public static final int FORMAT_L_4 = 8;

    /** DXT1 / BC1, including its transparent mode. */
    public static final int FORMAT_DXT1 = 9;

    /** DXT3 / BC2 with explicit four-bit alpha. */
    public static final int FORMAT_DXT3 = 10;

    /** DXT5 / BC3 with interpolated alpha. */
    public static final int FORMAT_DXT5 = 11;

    /** BC7 block compression. */
    public static final int FORMAT_BC7 = 13;

    private static final long MAX_BYTES = 268435456;

    private IggyTextureDecoder() {
    }

    private static int levelLength(int width, int height, int format) throws IOException {
        long length;
        switch (format) {
            case FORMAT_RGBA_8888:
                length = (long) width * height * 4;
                break;
            case FORMAT_RGBA_4444_LE:
            case FORMAT_RGBA_5551_LE:
            case FORMAT_LA_88:
                length = (long) width * height * 2;
                break;
            case FORMAT_LA_44:
            case FORMAT_I_8:
            case FORMAT_I_4:
            case FORMAT_L_8:
            case FORMAT_L_4:
                // i_4 and l_4 are submitted as one byte per texel by GDraw.
                length = (long) width * height;
                break;
            case FORMAT_DXT1:
            case FORMAT_DXT3:
            case FORMAT_DXT5:
            case FORMAT_BC7:
                length = ((width + 3L) / 4) * ((height + 3L) / 4) * (format == FORMAT_DXT1 ? 8 : 16);
                break;
            default:
                throw new IOException("Unsupported Iggy raw texture format " + format);
        }
        if (length > MAX_BYTES) {
            throw new IOException("Iggy texture data exceeds 256 MiB");
        }
        return (int) length;
    }

    /**
     * Returns the encoded length of the complete declared mip chain.
     *
     * @param width base level width
     * @param height base level height
     * @param resourceFormat low byte format, high byte mip count (zero means one)
     * @return encoded byte count
     * @throws IOException for unsupported formats, dimensions or mip counts
     */
    public static int dataLength(int width, int height, int resourceFormat) throws IOException {
        if (width <= 0 || height <= 0 || (long) width * height > MAX_BYTES / 4
                || resourceFormat < 0 || resourceFormat > 65535) {
            throw new IOException("Invalid Iggy texture dimensions or metadata");
        }
        int levels = Math.max(1, resourceFormat >>> 8);
        int maximum = 32 - Integer.numberOfLeadingZeros(Math.max(width, height));
        if (levels > maximum) {
            throw new IOException("Invalid Iggy texture mip count " + levels);
        }
        long length = 0;
        for (int i = 0; i < levels; i++) {
            length += levelLength(width, height, resourceFormat & 255);
            width = Math.max(1, width / 2);
            height = Math.max(1, height / 2);
        }
        if (length > MAX_BYTES) {
            throw new IOException("Iggy texture mip chain exceeds 256 MiB");
        }
        return (int) length;
    }

    private static int u16(byte[] data, int at) {
        return (data[at] & 255) | (data[at + 1] & 255) << 8;
    }

    private static int rgb565(int value) {
        int r = value >>> 11;
        int g = (value >>> 5) & 63;
        int b = value & 31;
        return 0xff000000 | ((r << 3) | (r >>> 2)) << 16
                | ((g << 2) | (g >>> 4)) << 8 | (b << 3) | (b >>> 2);
    }

    private static int mix(int first, int second, int a, int b) {
        int result = 0xff000000;
        for (int shift = 0; shift < 24; shift += 8) {
            result |= ((((first >>> shift) & 255) * a + ((second >>> shift) & 255) * b) / (a + b)) << shift;
        }
        return result;
    }

    private static void dxt(byte[] data, int width, int height, int format, int[] pixels) {
        int at = 0;
        int[] colors = new int[4];
        int[] alpha = new int[8];
        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4) {
                int colorAt = at + (format == FORMAT_DXT1 ? 0 : 8);
                int first = u16(data, colorAt);
                int second = u16(data, colorAt + 2);
                colors[0] = rgb565(first);
                colors[1] = rgb565(second);
                if (format != FORMAT_DXT1 || first > second) {
                    colors[2] = mix(colors[0], colors[1], 2, 1);
                    colors[3] = mix(colors[0], colors[1], 1, 2);
                } else {
                    colors[2] = mix(colors[0], colors[1], 1, 1);
                    colors[3] = 0;
                }
                long indices = 0;
                long alphaIndices = 0;
                for (int i = 0; i < 4; i++) {
                    indices |= (long) (data[colorAt + 4 + i] & 255) << (8 * i);
                }
                if (format == FORMAT_DXT5) {
                    alpha[0] = data[at] & 255;
                    alpha[1] = data[at + 1] & 255;
                    int denominator = alpha[0] > alpha[1] ? 7 : 5;
                    for (int i = 1; i < denominator; i++) {
                        alpha[i + 1] = ((denominator - i) * alpha[0] + i * alpha[1]) / denominator;
                    }
                    if (denominator == 5) {
                        alpha[6] = 0;
                        alpha[7] = 255;
                    }
                    for (int i = 0; i < 6; i++) {
                        alphaIndices |= (long) (data[at + 2 + i] & 255) << (8 * i);
                    }
                }
                for (int i = 0; i < 16; i++) {
                    int pixel = colors[(int) (indices >>> (2 * i)) & 3];
                    if (format == FORMAT_DXT3) {
                        pixel = (pixel & 0xffffff) | (((data[at + i / 2] & 255) >>> (4 * (i % 2))) & 15) * 17 << 24;
                    } else if (format == FORMAT_DXT5) {
                        pixel = (pixel & 0xffffff) | alpha[(int) (alphaIndices >>> (3 * i)) & 7] << 24;
                    }
                    int px = x + i % 4;
                    int py = y + i / 4;
                    if (px < width && py < height) {
                        pixels[py * width + px] = pixel;
                    }
                }
                at += format == FORMAT_DXT1 ? 8 : 16;
            }
        }
    }

    /**
     * Decodes the base level and validates the declared mip chain.
     *
     * @param width base level width
     * @param height base level height
     * @param resourceFormat low byte format, high byte mip count
     * @param data texture bytes, starting at the resource offset
     * @return straight-alpha ARGB base level pixels
     * @throws IOException for invalid or unsupported input
     */
    public static int[] decode(int width, int height, int resourceFormat, byte[] data) throws IOException {
        int length = dataLength(width, height, resourceFormat);
        if (data == null || data.length < length) {
            throw new IOException("Truncated Iggy external texture");
        }
        int format = resourceFormat & 255;
        if (format == FORMAT_BC7) {
            return Bc7Decoder.decode(width, height, data);
        }
        int[] pixels = new int[width * height];
        if (format == FORMAT_DXT1 || format == FORMAT_DXT3 || format == FORMAT_DXT5) {
            dxt(data, width, height, format, pixels);
            return pixels;
        }
        for (int i = 0; i < pixels.length; i++) {
            int r;
            int g;
            int b;
            int a;
            if (format == FORMAT_RGBA_8888) {
                r = data[4 * i] & 255;
                g = data[4 * i + 1] & 255;
                b = data[4 * i + 2] & 255;
                a = data[4 * i + 3] & 255;
            } else if (format == FORMAT_RGBA_4444_LE || format == FORMAT_RGBA_5551_LE) {
                int value = u16(data, 2 * i);
                if (format == FORMAT_RGBA_4444_LE) {
                    a = (value >>> 12) * 17;
                    r = ((value >>> 8) & 15) * 17;
                    g = ((value >>> 4) & 15) * 17;
                    b = (value & 15) * 17;
                } else {
                    a = (value >>> 15) * 255;
                    r = (value >>> 10) & 31;
                    g = (value >>> 5) & 31;
                    b = value & 31;
                    r = (r << 3) | (r >>> 2);
                    g = (g << 3) | (g >>> 2);
                    b = (b << 3) | (b >>> 2);
                }
            } else {
                int value = data[format == FORMAT_LA_88 ? 2 * i : i] & 255;
                a = format == FORMAT_LA_88 ? data[2 * i + 1] & 255 : format == FORMAT_LA_44 ? (value >>> 4) * 17 : format == FORMAT_I_8 || format == FORMAT_I_4 ? value : 255;
                r = format == FORMAT_LA_44 ? (value & 15) * 17 : value;
                g = r;
                b = r;
            }
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        return pixels;
    }
}
