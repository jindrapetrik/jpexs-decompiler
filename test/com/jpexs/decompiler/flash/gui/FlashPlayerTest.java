/*
 *  Copyright (C) 2010-2015 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.javactivex.ActiveX;
import com.jpexs.javactivex.example.controls.flash.ShockwaveFlash;
import java.awt.Panel;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import org.testng.Assert;

/**
 *
 * @author JPEXS
 */
public class FlashPlayerTest {

    //@Test
    public void test1() throws IOException, InterruptedException {
        ShockwaveFlash flash = ActiveX.createObject(ShockwaveFlash.class, new Panel());
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream("libsrc/ffdec_lib/testdata/run_as3/run.swf")), false);
        DoABC2Tag abcTag = null;
        for (Tag t : swf.tags) {
            if (t instanceof DoABC2Tag) {
                abcTag = ((DoABC2Tag) t);
                break;
            }
        }

        ABC abc = abcTag.getABC();
        MethodBody body = abc.findBodyByClassAndName("Run", "run");
        flash.setMovie("libsrc/ffdec_lib/testdata/run_as3/run2.swf");

        int cnt = 0;
        while (flash.getReadyState() != 4) {
            Thread.sleep(50);
            if (cnt > 100) {
                Assert.fail("Flash init timeout");
            }

            cnt++;
        }

        flash.setAllowScriptAccess("always");
        try {
            String res = flash.CallFunction("testFunc");
            throw new Error(res + " " + body.getCode().toString() + "");
        } catch (Exception ex) {
            int a = 1;
        }
    }
}
