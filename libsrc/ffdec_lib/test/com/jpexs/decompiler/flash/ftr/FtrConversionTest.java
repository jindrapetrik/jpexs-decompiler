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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.ftr;

import com.jpexs.decompiler.flash.Bundle;
import com.jpexs.decompiler.flash.OpenableSourceInfo;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SearchMode;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTag;
import com.jpexs.decompiler.flash.types.RGB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FtrConversionTest {

    @Test
    public void readsAndWritesLittleEndianFrameRate() throws Exception {
        SWF swf = FtrConverter.read(new ByteArrayInputStream(movie()));
        Assert.assertEquals(swf.frameRate, 24.0f);
        Assert.assertEquals(swf.getFrameCount(), 1);
        Assert.assertEquals(swf.displayRect.Xmax, 8000);
        Assert.assertEquals(((SetBackgroundColorTag) swf.getTags().get(0)).backgroundColor.red, 16);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FtrConverter.write(swf, out);
        ByteBuffer result = ByteBuffer.wrap(out.toByteArray()).order(ByteOrder.LITTLE_ENDIAN);
        Assert.assertEquals(result.getDouble(16), 24.0);
        Assert.assertEquals(result.getInt(24), 2);
    }

    @Test
    public void savesAsFtrAndLeavesOriginalIntactOnConversionError() throws Exception {
        Path file = Files.createTempFile("ffdec-ftr-", ".FTR");
        try {
            byte[] original = movie();
            Files.write(file, original);
            Bundle bundle = new OpenableSourceInfo(null, file.toString(), null).getBundle(false, SearchMode.ALL);
            Assert.assertTrue(bundle instanceof FtrSwfBundle);
            String key = bundle.getKeys().iterator().next();
            SWF swf = new SWF(bundle.getOpenable(key), false);
            bundle.putOpenable(key, swfBytes(swf));
            Assert.assertTrue(Arrays.equals(Files.readAllBytes(file), original));

            SetBackgroundColorTag background = (SetBackgroundColorTag) swf.getTags().get(0);
            background.backgroundColor = new RGB(48, 32, 16);
            background.setModified(true);
            bundle.putOpenable(key, swfBytes(swf));
            byte[] changed = Files.readAllBytes(file);
            SWF reopened = FtrConverter.read(new ByteArrayInputStream(changed));
            Assert.assertEquals(((SetBackgroundColorTag) reopened.getTags().get(0)).backgroundColor.red, 48);

            swf.addTag(new SoundStreamHeadTag(swf));
            try {
                bundle.putOpenable(key, swfBytes(swf));
                Assert.fail("FTR cannot store sound streams");
            } catch (IOException expected) {
                Assert.assertTrue(Arrays.equals(Files.readAllBytes(file), changed));
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static ByteArrayInputStream swfBytes(SWF swf) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        swf.saveTo(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private static byte[] movie() {
        ByteBuffer out = ByteBuffer.allocate(40).order(ByteOrder.LITTLE_ENDIAN);
        out.putInt(0).putInt(0).putInt(8000).putInt(800).putDouble(24.0);
        out.putInt(2);
        out.putInt(9).putInt(0xff302010);
        out.putInt(1);
        return out.array();
    }
}

