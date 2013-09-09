/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class ImageTag extends CharacterTag {

    public ImageTag(SWF swf, int id, String name, byte[] data, long pos) {
        super(swf, id, name, data, pos);
    }

    public abstract BufferedImage getImage(List<Tag> tags);

    public abstract void setImage(byte[] data) throws IOException;

    public abstract String getImageFormat();

    public boolean importSupported() {
        return true;
    }

    public static String getImageFormat(byte[] data) {
        if (SWF.hasErrorHeader(data)) {
            return "jpg";
        }
        if (data.length > 2 && ((data[0] & 0xff) == 0xff) && ((data[1] & 0xff) == 0xd8)) {
            return "jpg";
        }
        if (data.length > 6 && ((data[0] & 0xff) == 0x47) && ((data[1] & 0xff) == 0x49) && ((data[2] & 0xff) == 0x46) && ((data[3] & 0xff) == 0x38) && ((data[4] & 0xff) == 0x39) && ((data[5] & 0xff) == 0x61)) {
            return "gif";
        }

        if (data.length > 8 && ((data[0] & 0xff) == 0x89) && ((data[1] & 0xff) == 0x50) && ((data[2] & 0xff) == 0x4e) && ((data[3] & 0xff) == 0x47) && ((data[4] & 0xff) == 0x0d) && ((data[5] & 0xff) == 0x0a) && ((data[6] & 0xff) == 0x1a) && ((data[7] & 0xff) == 0x0a)) {
            return "png";
        }

        return "unk";
    }

    protected static int max255(float val) {
        if (val > 255) {
            return 255;
        }
        return (int) val;
    }

    protected static Color intToColor(int val) {
        return new Color(val & 0xff, (val >> 8) & 0xff, (val >> 16) & 0xff, (val >> 24) & 0xff);
    }

    protected static int colorToInt(Color c) {
        return (c.getAlpha() << 24) | (c.getBlue() << 16) | (c.getGreen() << 8) | c.getRed();
    }

    protected static Color multiplyAlpha(Color c) {
        float multiplier = c.getAlpha() == 0 ? 0 : 255.0f / c.getAlpha();
        return new Color(max255(c.getRed() * multiplier), max255(c.getGreen() * multiplier), max255(c.getBlue() * multiplier), c.getAlpha());
    }
}
