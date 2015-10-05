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
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionLocalData;
import com.jpexs.decompiler.flash.action.deobfuscation.FixItemCounterTranslateStack;
import com.jpexs.decompiler.flash.action.swf4.ActionAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionAnd;
import com.jpexs.decompiler.flash.action.swf4.ActionAsciiToChar;
import com.jpexs.decompiler.flash.action.swf4.ActionCharToAscii;
import com.jpexs.decompiler.flash.action.swf4.ActionDivide;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionLess;
import com.jpexs.decompiler.flash.action.swf4.ActionMBAsciiToChar;
import com.jpexs.decompiler.flash.action.swf4.ActionMBStringExtract;
import com.jpexs.decompiler.flash.action.swf4.ActionMBStringLength;
import com.jpexs.decompiler.flash.action.swf4.ActionMultiply;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionOr;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionStringAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionStringExtract;
import com.jpexs.decompiler.flash.action.swf4.ActionStringLength;
import com.jpexs.decompiler.flash.action.swf4.ActionStringLess;
import com.jpexs.decompiler.flash.action.swf4.ActionSubtract;
import com.jpexs.decompiler.flash.action.swf4.ActionToInteger;
import com.jpexs.decompiler.flash.action.swf5.ActionAdd2;
import com.jpexs.decompiler.flash.action.swf5.ActionBitAnd;
import com.jpexs.decompiler.flash.action.swf5.ActionBitLShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitOr;
import com.jpexs.decompiler.flash.action.swf5.ActionBitRShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitURShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitXor;
import com.jpexs.decompiler.flash.action.swf5.ActionDecrement;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionIncrement;
import com.jpexs.decompiler.flash.action.swf5.ActionLess2;
import com.jpexs.decompiler.flash.action.swf5.ActionModulo;
import com.jpexs.decompiler.flash.action.swf5.ActionPushDuplicate;
import com.jpexs.decompiler.flash.action.swf5.ActionStackSwap;
import com.jpexs.decompiler.flash.action.swf5.ActionToNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionToString;
import com.jpexs.decompiler.flash.action.swf5.ActionTypeOf;
import com.jpexs.decompiler.flash.action.swf6.ActionGreater;
import com.jpexs.decompiler.flash.action.swf6.ActionStrictEquals;
import com.jpexs.decompiler.flash.action.swf6.ActionStringGreater;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.javactivex.ActiveX;
import com.jpexs.javactivex.ActiveXEvent;
import com.jpexs.javactivex.ActiveXEventListener;
import com.jpexs.javactivex.Reference;
import com.jpexs.javactivex.example.controls.flash.ShockwaveFlash;
import java.awt.Panel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.testng.Assert;

/**
 *
 * @author JPEXS
 */
public class FlashPlayerTest {

    private Object lockObj = new Object();

    private Random random = new Random();

    //@Test
    public void test1() throws IOException, InterruptedException {
        final Reference<String> resultRef = new Reference<>(null);

        ShockwaveFlash flash = ActiveX.createObject(ShockwaveFlash.class, new Panel());
        flash.setAllowScriptAccess("always");
        flash.setAllowNetworking("all");
        flash.addFSCommandListener(new ActiveXEventListener() {

            @Override
            public void onEvent(ActiveXEvent axe) {
                resultRef.setVal((String) axe.args.get("args"));
                synchronized (lockObj) {
                    lockObj.notify();
                }
            }
        });

        File f = new File("libsrc/ffdec_lib/testdata/run_as3/run.swf");

        int i = 1;
        int j = 1;
        File f2 = new File("run_test_" + new Date().getTime() + "_" + i + "_" + j + ".swf");
        f2.deleteOnExit();

        SWF swf = new SWF(new BufferedInputStream(new FileInputStream(f)), false);
        DoABC2Tag abcTag = null;
        for (Tag t : swf.tags) {
            if (t instanceof DoABC2Tag) {
                abcTag = ((DoABC2Tag) t);
                break;
            }
        }

        ABC abc = abcTag.getABC();
        MethodBody body = abc.findBodyByClassAndName("Run", "run");
        body.max_stack = 10;
        AVM2Code ccode = new AVM2Code();
        ccode.code = new ArrayList<>();
        List<AVM2Instruction> code = ccode.code;
        code.add(new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
        code.add(new AVM2Instruction(0, AVM2Instructions.PushScope, null));
        code.add(new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{1}));
        code.add(new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{2}));
        code.add(new AVM2Instruction(0, AVM2Instructions.Add, null));
        code.add(new AVM2Instruction(0, AVM2Instructions.Dup, null));
        code.add(new AVM2Instruction(0, AVM2Instructions.TypeOf, null));
        code.add(new AVM2Instruction(0, AVM2Instructions.Add, null));
        code.add(new AVM2Instruction(0, AVM2Instructions.ReturnValue, null));

        body.setCode(ccode);
        abcTag.setModified(true);
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f2))) {
            swf.saveTo(fos);
        }

        flash.setMovie(f2.getAbsolutePath());

        synchronized (lockObj) {
            lockObj.wait();
        }

        String flashResult = resultRef.getVal();

        f2.delete();

        /*int cnt = 0;
         while (flash.getReadyState() != 4) {
         Thread.sleep(50);
         if (cnt > 100) {
         Assert.fail("Flash init timeout");
         }

         cnt++;
         }*/
        /*try {
         String res = flash.CallFunction("<invoke name=\"testFunc\" returntype=\"xml\"><arguments><string>something</string></arguments></invoke>");
         //String str = flash.GetVariable("_root.myText.text");
         throw new Error(res + " " + body.getCode().toString() + "");
         } catch (Exception ex) {
         int a = 1;
         }*/
    }

    //@Test
    public void testAs2() throws IOException, InterruptedException {
        final Reference<String> resultRef = new Reference<>(null);

        ShockwaveFlash flash = ActiveX.createObject(ShockwaveFlash.class, new Panel());
        flash.setAllowScriptAccess("always");
        flash.setAllowNetworking("all");
        flash.addFSCommandListener(new ActiveXEventListener() {

            @Override
            public void onEvent(ActiveXEvent axe) {
                resultRef.setVal((String) axe.args.get("args"));
                synchronized (lockObj) {
                    lockObj.notify();
                }
            }
        });

        File f = new File("libsrc/ffdec_lib/testdata/run_as2/run_as2.swf");
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 12 + 23; j++) {

                File f2 = new File("run_test_" + new Date().getTime() + "_" + i + "_" + j + ".swf");
                f2.deleteOnExit();

                SWF swf = new SWF(new BufferedInputStream(new FileInputStream(f)), false);
                Map<String, ASMSource> asms = swf.getASMs(true);
                ASMSource asm = asms.get("\\frame_1\\DoAction");
                ActionList actions = asm.getActions();
                actions.removeAction(2, 4);

                List<Action> newActions = new ArrayList<>();
                int r1 = random.nextInt(500) - 255;
                int r2 = random.nextInt(100);

                Action opAction = getOpAction(j);

                if (j >= 12) {
                    newActions.add(new ActionPush(r1));
                }

                if (i == 0) {
                    newActions.add(new ActionPush(Undefined.INSTANCE));
                } else if (i == 1) {
                    newActions.add(new ActionPush(Null.INSTANCE));
                } else if (i == 2) {
                    newActions.add(new ActionPush(false));
                } else if (i == 3) {
                    newActions.add(new ActionPush(true));
                } else if (i == 4) {
                    newActions.add(new ActionPush("test"));
                } else if (i == 5) {
                    newActions.add(new ActionPush("0"));
                } else if (i == 6) {
                    newActions.add(new ActionPush("0.0"));
                } else if (i == 7) {
                    newActions.add(new ActionPush("1.0"));
                } else if (i == 8) {
                    newActions.add(new ActionPush("-1.0"));
                } else if (i == 9) {
                    newActions.add(new ActionPush(0));
                } else if (i == 10) {
                    newActions.add(new ActionPush(-100));
                } else if (i == 11) {
                    newActions.add(new ActionPush(100));
                } else {
                    newActions.add(new ActionPush(r2));
                }

                System.out.println(i + " " + j + " " + opAction.toString() + " r1:" + r1 + " r2:" + r2);
                newActions.add(opAction);
                newActions.add(new ActionPushDuplicate());
                newActions.add(new ActionTypeOf());
                newActions.add(new ActionStackSwap());
                newActions.add(new ActionStringAdd());
                actions.addActions(2, newActions);

                List<GraphTargetItem> output = new ArrayList<>();
                ActionLocalData localData = new ActionLocalData();
                FixItemCounterTranslateStack stack = new FixItemCounterTranslateStack("");
                for (Action a : newActions) {
                    a.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");
                }

                Object ffdecResult = stack.pop().getResult();
                System.out.println("FFDec result: " + ffdecResult);

                asm.setActions(actions);
                asm.setModified();
                try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f2))) {
                    swf.saveTo(fos);
                }

                flash.setMovie(f2.getAbsolutePath());

                synchronized (lockObj) {
                    lockObj.wait();
                }

                String str = flash.GetVariable("myText.text");
                String flashResult = resultRef.getVal();
                boolean checkOnlyStart = false;
                /*if (flashResult.length() > 10) {
                 boolean onlyNumber = true;
                 for (int k = 0; k < 10; k++) {
                 char ch = flashResult.charAt(flashResult.length() - k - 1);
                 if (ch < '0' || ch > '9') {
                 onlyNumber = false;
                 break;
                 }
                 }

                 if (onlyNumber) {
                 flashResult = flashResult.substring(0, flashResult.length() - 1);
                 checkOnlyStart = true;
                 }
                 }*/

                if (checkOnlyStart) {
                    Assert.assertTrue(((String) ffdecResult).startsWith(flashResult));
                } else {
                    Assert.assertEquals(ffdecResult, flashResult);
                }

                f2.delete();
            }
        }
    }

    private Action getOpAction(int idx) {
        Action result;
        if (idx < 12) {
            result = getUnaryOpAction(idx);
            Assert.assertEquals(1, result.getStackPopCount(null, null));
            Assert.assertEquals(1, result.getStackPushCount(null, null));
        } else {
            result = getBinaryOpAction(idx - 12);
            Assert.assertEquals(2, result.getStackPopCount(null, null));
            Assert.assertEquals(1, result.getStackPushCount(null, null));
        }

        return result;
    }

    private Action getUnaryOpAction(int idx) {
        switch (idx) {
            case 0:
                return new ActionAsciiToChar();
            case 1:
                return new ActionCharToAscii();
            case 2:
                return new ActionDecrement();
            case 3:
                return new ActionIncrement();
            case 4:
                return new ActionNot();
            case 5:
                return new ActionToInteger();
            case 6:
                return new ActionToNumber();
            case 7:
                return new ActionToString();
            case 8:
                return new ActionTypeOf();
            case 9:
                return new ActionStringLength();
            case 10:
                return new ActionMBAsciiToChar();
            case 11:
                return new ActionMBStringLength();
        }

        throw new Error("Invalid index");
    }

    private Action getBinaryOpAction(int idx) {
        switch (idx) {
            case 0:
                return new ActionAnd();
            case 1:
                return new ActionAdd();
            case 2:
                return new ActionAdd2();
            case 3:
                return new ActionBitAnd();
            case 4:
                return new ActionBitLShift();
            case 5:
                return new ActionBitOr();
            case 6:
                return new ActionBitRShift();
            case 7:
                return new ActionBitURShift();
            case 8:
                return new ActionBitXor();
            case 9:
                return new ActionDivide();
            case 10:
                return new ActionEquals();
            case 11:
                return new ActionEquals2();
            case 12:
                return new ActionGreater();
            case 13:
                return new ActionLess();
            case 14:
                return new ActionLess2();
            case 15:
                return new ActionModulo();
            case 16:
                return new ActionMultiply();
            case 17:
                return new ActionOr();
            case 18:
                return new ActionStringAdd();
            case 19:
                return new ActionStrictEquals();
            case 20:
                return new ActionStringGreater();
            case 21:
                return new ActionStringLess();
            case 22:
                return new ActionSubtract();
        }

        throw new Error("Invalid index");
    }

    private Action getTernaryOpAction(int idx) {
        switch (idx) {
            case 0:
                return new ActionStringExtract();
            case 1:
                return new ActionMBStringExtract();
        }

        throw new Error("Invalid index");
    }
}
