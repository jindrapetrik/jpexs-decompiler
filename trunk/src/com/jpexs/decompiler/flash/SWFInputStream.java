/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.special.ActionContainer;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.flash.action.special.ActionNop;
import com.jpexs.decompiler.flash.action.swf3.*;
import com.jpexs.decompiler.flash.action.swf4.*;
import com.jpexs.decompiler.flash.action.swf5.*;
import com.jpexs.decompiler.flash.action.swf6.*;
import com.jpexs.decompiler.flash.action.swf7.*;
import com.jpexs.decompiler.flash.action.treemodel.ConstantPool;
import com.jpexs.decompiler.flash.action.treemodel.DirectValueTreeItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItemPos;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.flash.tags.*;
import com.jpexs.decompiler.flash.types.*;
import com.jpexs.decompiler.flash.types.filters.BEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.BLURFILTER;
import com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER;
import com.jpexs.decompiler.flash.types.filters.CONVOLUTIONFILTER;
import com.jpexs.decompiler.flash.types.filters.DROPSHADOWFILTER;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.decompiler.flash.types.filters.GLOWFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTBEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTGLOWFILTER;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for reading data from SWF file
 *
 * @author JPEXS
 */
public class SWFInputStream extends InputStream {

    private InputStream is;
    private Stack<Integer> margedPos = new Stack<Integer>();
    private long pos;
    private int version;
    private static final Logger log = Logger.getLogger(SWFInputStream.class.getName());
    private List<PercentListener> listeners = new ArrayList<PercentListener>();
    private long percentMax;
    private List<byte[]> buffered = new ArrayList<byte[]>();
    private ByteArrayOutputStream buffer;
    private static boolean DEOBFUSCATION_ALL_CODE_IN_PREVIOUS_TAG = true;

    public int getBufferLength() {
        return buffer.size();
    }

    public void startBuffer() {
        stopBuffer();
        buffer = new ByteArrayOutputStream();
    }

    public byte[] getBuffer() {
        return buffer.toByteArray();
    }

    public byte[] stopBuffer() {
        if (buffer != null) {
            byte[] ret = buffer.toByteArray();
            buffered.add(ret);
            buffer = null;
            return ret;
        }
        return null;
    }

    public void addPercentListener(PercentListener listener) {
        listeners.add(listener);
    }

    public void removePercentListener(PercentListener listener) {
        int index = listeners.indexOf(listener);
        if (index > -1) {
            listeners.remove(index);
        }
    }

    public void setPercentMax(long percentMax) {
        this.percentMax = percentMax;
    }

    /**
     * Constructor
     *
     * @param is Existing inputstream
     * @param version Version of SWF to read
     */
    public SWFInputStream(InputStream is, int version, long startingPos) {
        this.version = version;
        this.is = is;
        pos = startingPos;
    }

    /**
     * Constructor
     *
     * @param is Existing inputstream
     * @param version Version of SWF to read
     */
    public SWFInputStream(InputStream is, int version) {
        this(is, version, 0L);
    }

    /**
     * Gets position in bytes in the stream
     *
     * @return Number of bytes
     */
    public long getPos() {
        return pos;
    }

    /**
     * Reads one byte from the stream
     *
     * @return byte or -1 on error
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        bitPos = 0;
        return readNoBitReset();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead = super.read(b, off, len);
        bitPos = 0;
        pos += bytesRead;
        return bytesRead;
    }

    private void alignByte() {
        bitPos = 0;
    }
    private int lastPercent = -1;

    private int readNoBitReset() throws IOException {
        pos++;
        if (percentMax > 0) {
            int percent = (int) (pos * 100 / percentMax);
            if (lastPercent != percent) {
                for (PercentListener pl : listeners) {
                    pl.percent(percent);
                }
                lastPercent = percent;
            }
        }
        return is.read();
    }

    /**
     * Reads one UI8 (Unsigned 8bit integer) value from the stream
     *
     * @return UI8 value or -1 on error
     * @throws IOException
     */
    public int readUI8() throws IOException {
        return read();
    }

    /**
     * Reads one string value from the stream
     *
     * @return String value
     * @throws IOException
     */
    public String readString() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int r;
        while (true) {
            r = read();
            if (r <= 0) {
                return new String(baos.toByteArray(), "utf8");
            }
            baos.write(r);
        }
    }

    /**
     * Reads one UI32 (Unsigned 32bit integer) value from the stream
     *
     * @return UI32 value
     * @throws IOException
     */
    public long readUI32() throws IOException {
        return (read() + (read() << 8) + (read() << 16) + (read() << 24)) & 0xffffffff;
    }

    /**
     * Reads one UI16 (Unsigned 16bit integer) value from the stream
     *
     * @return UI16 value
     * @throws IOException
     */
    public int readUI16() throws IOException {
        return read() + (read() << 8);
    }

    /**
     * Reads one SI32 (Signed 32bit integer) value from the stream
     *
     * @return SI32 value
     * @throws IOException
     */
    public long readSI32() throws IOException {
        long uval = read() + (read() << 8) + (read() << 16) + (read() << 24);
        if (uval >= 0x80000000) {
            return -(((~uval) & 0xffffffff) + 1);
        } else {
            return uval;
        }
    }

    /**
     * Reads one SI16 (Signed 16bit integer) value from the stream
     *
     * @return SI16 value
     * @throws IOException
     */
    public int readSI16() throws IOException {
        int uval = read() + (read() << 8);
        if (uval >= 0x8000) {
            return -(((~uval) & 0xffff) + 1);
        } else {
            return uval;
        }
    }

    /**
     * Reads one SI8 (Signed 8bit integer) value from the stream
     *
     * @return SI8 value
     * @throws IOException
     */
    public int readSI8() throws IOException {
        int uval = read();
        if (uval >= 0x80) {
            return -(((~uval) & 0xff) + 1);
        } else {
            return uval;
        }
    }

    /**
     * Reads one FIXED (Fixed point 16.16) value from the stream
     *
     * @return FIXED value
     * @throws IOException
     */
    public double readFIXED() throws IOException {
        int afterPoint = readUI16();
        int beforePoint = readUI16();
        return ((double) ((beforePoint << 16) + afterPoint)) / 65536;
    }

    /**
     * Reads one FIXED8 (Fixed point 8.8) value from the stream
     *
     * @return FIXED8 value
     * @throws IOException
     */
    public float readFIXED8() throws IOException {
        int afterPoint = read();
        int beforePoint = read();
        return beforePoint + (((float) afterPoint) / 256);
    }

    private long readLong() throws IOException {
        byte readBuffer[] = readBytes(8);
        return (((long) readBuffer[3] << 56)
                + ((long) (readBuffer[2] & 255) << 48)
                + ((long) (readBuffer[1] & 255) << 40)
                + ((long) (readBuffer[0] & 255) << 32)
                + ((long) (readBuffer[7] & 255) << 24)
                + ((readBuffer[6] & 255) << 16)
                + ((readBuffer[5] & 255) << 8)
                + ((readBuffer[4] & 255)));
    }

    /**
     * Reads one DOUBLE (double precision floating point value) value from the
     * stream
     *
     * @return DOUBLE value
     * @throws IOException
     */
    public double readDOUBLE() throws IOException {
        long el = readLong();
        double ret = Double.longBitsToDouble(el);
        return ret;
    }

    /**
     * Reads one FLOAT (single precision floating point value) value from the
     * stream
     *
     * @return FLOAT value
     * @throws IOException
     */
    public float readFLOAT() throws IOException {
        int val = (int) readUI32();
        float ret = Float.intBitsToFloat(val);
        /*int sign = val >> 31;
         int mantisa = val & 0x3FFFFF;
         int exp = (val >> 22) & 0xFF;
         float ret =(sign == 1 ? -1 : 1) * (float) Math.pow(2, exp)*  (1+((mantisa)/ (float)(1<<23)));*/
        return ret;
    }

    /**
     * Reads one FLOAT16 (16bit floating point value) value from the stream
     *
     * @return FLOAT16 value
     * @throws IOException
     */
    public float readFLOAT16() throws IOException {
        int val = readUI16();
        int sign = val >> 15;
        int mantisa = val & 0x3FF;
        int exp = (val >> 10) & 0x1F;
        float ret = (sign == 1 ? -1 : 1) * (float) Math.pow(2, exp) * (1 + ((mantisa) / (float) (1 << 10)));
        return ret;
    }

    /**
     * Reads bytes from the stream
     *
     * @param count Number of bytes to read
     * @return Array of read bytes
     * @throws IOException
     */
    public byte[] readBytes(long count) throws IOException {
        if (count <= 0) {
            return new byte[0];
        }
        byte ret[] = new byte[(int) count];
        for (int i = 0; i < count; i++) {
            ret[i] = (byte) read();
        }
        return ret;
    }

    /**
     * Reads one EncodedU32 (Encoded unsigned 32bit value) value from the stream
     *
     * @return U32 value
     * @throws IOException
     */
    public long readEncodedU32() throws IOException {
        int result = read();
        if ((result & 0x00000080) == 0) {
            return result;
        }
        result = (result & 0x0000007f) | (read()) << 7;
        if ((result & 0x00004000) == 0) {
            return result;
        }
        result = (result & 0x00003fff) | (read()) << 14;
        if ((result & 0x00200000) == 0) {
            return result;
        }
        result = (result & 0x001fffff) | (read()) << 21;
        if ((result & 0x10000000) == 0) {
            return result;
        }
        result = (result & 0x0fffffff) | (read()) << 28;
        return result;
    }
    private int bitPos = 0;
    private int tempByte = 0;

    /**
     * Reads UB[nBits] (Unsigned-bit value) value from the stream
     *
     * @param nBits Number of bits which represent value
     * @return Unsigned value
     * @throws IOException
     */
    public long readUB(int nBits) throws IOException {
        if (nBits == 0) {
            return 0;
        }
        long ret = 0;
        if (bitPos == 0) {
            tempByte = readNoBitReset();
        }
        for (int bit = 0; bit < nBits; bit++) {
            int nb = (tempByte >> (7 - bitPos)) & 1;
            ret = ret + (nb << (nBits - 1 - bit));
            bitPos++;
            if (bitPos == 8) {
                bitPos = 0;
                if (bit != nBits - 1) {
                    tempByte = readNoBitReset();
                }
            }
        }
        return ret;
    }

    /**
     * Reads SB[nBits] (Signed-bit value) value from the stream
     *
     * @param nBits Number of bits which represent value
     * @return Signed value
     * @throws IOException
     */
    public long readSB(int nBits) throws IOException {
        int uval = (int) readUB(nBits);

        int shift = 32 - nBits;
        // sign extension
        uval = (uval << shift) >> shift;
        return uval;
    }

    /**
     * Reads FB[nBits] (Signed fixed-point bit value) value from the stream
     *
     * @param nBits Number of bits which represent value
     * @return Fixed-point value
     * @throws IOException
     */
    public float readFB(int nBits) throws IOException {
        if (nBits == 0) {
            return 0;
        }
        float val = readSB(nBits);
        float ret = val / 0x10000;
        return ret;
    }

    /**
     * Reads one RECT value from the stream
     *
     * @return RECT value
     * @throws IOException
     */
    public RECT readRECT() throws IOException {
        RECT ret = new RECT();
        int NBits = (int) readUB(5);
        ret.Xmin = (int) readSB(NBits);
        ret.Xmax = (int) readSB(NBits);
        ret.Ymin = (int) readSB(NBits);
        ret.Ymax = (int) readSB(NBits);
        ret.nbits = NBits;
        alignByte();
        return ret;
    }

    public List<Action> readActionList(long address, long containerSWFOffset) throws IOException {
        ReReadableInputStream rri = new ReReadableInputStream(this);
        return readActionList(address, containerSWFOffset, rri, version, 0, -1);
    }

    public List<Action> readActionList(long address, long containerSWFOffset, ReReadableInputStream rri, int maxlen) throws IOException {
        return readActionList(address, containerSWFOffset, rri, version, rri.getPos(), rri.getPos() + maxlen);
    }

    private static void getConstantPool(ConstantPool cpool, List localData, Stack<GraphTargetItem> stack, List<GraphTargetItem> output, ActionGraphSource code, int ip, int lastIp, List<ConstantPool> constantPools, List<Integer> visited, int version) {
        boolean debugMode = false;
        while ((ip > -1) && ip < code.size()) {
            if (visited.contains(ip)) {
                break;
            }

            lastIp = ip;
            GraphSourceItem ins = code.get(ip);
            if (ins.isIgnored()) {
                ip++;
                continue;
            }

            if (ins instanceof ActionPush) {
                if (cpool != null) {
                    ((ActionPush) ins).constantPool = cpool.constants;
                    cpool.count++;
                }
            }
            if (ins instanceof ActionDefineFunction) {
                if (cpool != null) {
                    ((ActionDefineFunction) ins).setConstantPool(cpool.constants);
                    cpool.count++;
                }
            }
            if (ins instanceof ActionDefineFunction2) {
                if (cpool != null) {
                    ((ActionDefineFunction2) ins).setConstantPool(cpool.constants);
                    cpool.count++;
                }
            }
            if (debugMode) {
                System.out.println("getConstantPool ip " + ip + ": " + ins + " stack:" + Highlighting.stripHilights(stack.toString()));
            }
            if (ins instanceof ActionConstantPool) {
                constantPools.add(new ConstantPool(((ActionConstantPool) ins).constantPool));
                if (cpool == null) {
                    cpool = new ConstantPool();
                }
                cpool.setNew(((ActionConstantPool) ins).constantPool);
            }

            //for..in return
            if (((ins instanceof ActionEquals) || (ins instanceof ActionEquals2)) && (stack.size() == 1) && (stack.peek() instanceof DirectValueTreeItem)) {
                stack.push(new DirectValueTreeItem(null, 0, new Null(), new ArrayList<String>()));
            }
            try {
                ins.translate(localData, stack, output);
            } catch (Exception ex) {
                Logger.getLogger(SWFInputStream.class.getName()).log(Level.SEVERE, "Error during getting constantpool", ex);
            }
            if (ins.isExit()) {
                break;
            }

            if (ins.isBranch() || ins.isJump()) {

                if (ins instanceof ActionIf && !stack.isEmpty() && (stack.peek().isCompileTime())) {
                    ActionIf aif = (ActionIf) ins;
                    if (aif.ignoreUsed && (!aif.jumpUsed)) {
                        ins.setIgnored(true);
                    }
                    if ((!aif.ignoreUsed) && aif.jumpUsed) {
                        ActionJump jmpIns = new ActionJump(aif.getJumpOffset());
                        ((ActionJump) jmpIns).setAddress(aif.getAddress(), version);
                        code.set(ip, (ActionJump) jmpIns);
                    }

                    if ((aif.ignoreUsed && (!aif.jumpUsed)) || ((!aif.ignoreUsed) && aif.jumpUsed)) {
                        List<GraphSourceItemPos> needed = stack.peek().getNeededSources();
                        for (GraphSourceItemPos ig : needed) {
                            if (ig.item instanceof ActionPush) {
                                if (!((ActionPush) ig.item).ignoredParts.contains(ig.pos)) {
                                    ((ActionPush) ig.item).ignoredParts.add(ig.pos);

                                    if (((ActionPush) ig.item).ignoredParts.size() == ((ActionPush) ig.item).values.size()) {
                                        ((Action) ig.item).setIgnored(true);
                                    }
                                }
                            } else {
                                ((Action) ig.item).setIgnored(true);
                            }
                        }
                    }
                    boolean condition = stack.peek().toBoolean();
                    if (debugMode) {
                        if (condition) {
                            System.out.println("JUMP");
                        } else {
                            System.out.println("SKIP");
                        }
                    }
                    stack.pop();
                    getConstantPool(cpool, localData, stack, output, code, condition ? (code.adr2pos(((ActionIf) ins).getAddress() + ((ActionIf) ins).getBytes(code.version).length + ((ActionIf) ins).getJumpOffset())) : ip + 1, ip, constantPools, visited, version);
                } else {
                    if (ins instanceof ActionIf) {
                        stack.pop();
                    }
                    visited.add(ip);
                    List<Integer> branches = ins.getBranches(code);
                    for (int b : branches) {
                        Stack<GraphTargetItem> brStack = (Stack<GraphTargetItem>) stack.clone();
                        if (b >= 0) {
                            getConstantPool(cpool, localData, brStack, output, code, b, ip, constantPools, visited, version);
                        } else {
                            if (debugMode) {
                                System.out.println("Negative branch:" + b);
                            }
                        }
                    }
                }
                break;
            }
            ip++;
        };
        if (ip < 0) {
            System.out.println("Visited Negative: " + ip);
        }
    }

    public static List<ConstantPool> getConstantPool(ActionGraphSource code, int addr, int version) {
        List<ConstantPool> ret = new ArrayList<ConstantPool>();
        List localData = Helper.toList(new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>());
        try {
            getConstantPool(null, localData, new Stack<GraphTargetItem>(), new ArrayList<GraphTargetItem>(), code, code.adr2pos(addr), 0, ret, new ArrayList<Integer>(), version);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error during getting constantpool", ex);
        }
        return ret;
    }

    /**
     * Reads list of actions from the stream. Reading ends with
     * ActionEndFlag(=0) or end of the stream.
     *
     * @return List of actions
     * @throws IOException
     */
    public static List<Action> readActionList(long address, long containerSWFOffset, ReReadableInputStream rri, int version, int ip, int endip) throws IOException {
        List<Action> retdups = new ArrayList<Action>();
        ConstantPool cpool = new ConstantPool();

        Stack<GraphTargetItem> stack = new Stack<GraphTargetItem>();

        List localData = Helper.toList(new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>());


        SWFInputStream sis = new SWFInputStream(rri, version);
        boolean goesPrev = false;
        int method = 1;
        /*try {
         goesPrev = readActionListAtPos(false, localData, stack, cpool, sis, rri, ip, retdups, ip);
         } catch (Exception ex) {
         method = 2;
         goesPrev = readActionListAtPos(true, localData, stack, cpool, sis, rri, ip, retdups, ip);
         }*/
        goesPrev = readActionListAtPos(address, containerSWFOffset, false, true, localData, stack, cpool, sis, rri, ip, retdups, ip, endip);

        if (goesPrev) {
        } else {
            if (!retdups.isEmpty()) {
                for (int i = 0; i < ip; i++) {
                    retdups.remove(0);
                }
            }
        }
        List<Action> ret = new ArrayList<Action>();
        Action last = null;
        for (Action a : retdups) {
            if (a != last) {
                ret.add(a);
            }
            last = a;
        }
        int len = retdups.size();
        for (int i = 0; i < retdups.size(); i++) {
            Action a = retdups.get(i);
            if (a instanceof ActionEnd) {
                if (i < retdups.size() - 1) {
                    ActionJump jmp = new ActionJump(0);
                    jmp.setJumpOffset(retdups.size() - i - jmp.getBytes(version).length);
                    a.replaceWith = jmp;
                }
            }
        }

        List<ConstantPool> pools = new ArrayList<ConstantPool>();
        pools = getConstantPool(new ActionGraphSource(ret, version, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>()), ip, version);

        if (pools.size() == 1) {
            Action.setConstantPool(ret, pools.get(0));
        }
        if (goesPrev && (!DEOBFUSCATION_ALL_CODE_IN_PREVIOUS_TAG)) {
            ActionJump aj = new ActionJump(ip);
            int skip = aj.getBytes(version).length;
            for (Action a : ret) {
                a.setAddress(a.getAddress() + skip, version);
            }
            ret.add(0, aj);
        }
        String s = null;
        //Action.setConstantPool(ret, cpool);

        /* try {
         s = Highlighting.stripHilights(Action.actionsToString(address,ret, null, version, false,containerSWFOffset));
         //System.out.println("PARSE:"+s);
         Helper.writeFile("t.txt", s.getBytes());
         ret = ASMParser.parse(address,containerSWFOffset,false, new ByteArrayInputStream(s.getBytes()), SWF.DEFAULT_VERSION);
         } catch (ParseException ex) {
         Logger.getLogger(SWFInputStream.class.getName()).log(Level.SEVERE, "parsing error", ex);
         }*/
        return ret;
    }

    private static boolean readActionListAtPos(long address, long containerSWFOffset, boolean notCompileTime, boolean enableVariables, List localData, Stack<GraphTargetItem> stack, ConstantPool cpool, SWFInputStream sis, ReReadableInputStream rri, int ip, List<Action> ret, int startIp, int endip) throws IOException {
        boolean debugMode = false;
        boolean decideBranch = false;
        boolean retv = false;
        rri.setPos(ip);
        Action a;
        List<GraphTargetItem> output = new ArrayList<GraphTargetItem>();
        long filePos = rri.getPos();
        Scanner sc = new Scanner(System.in);
        int prevIp = ip;
        while (((endip == -1) || (endip > ip)) && (a = sis.readAction(rri)) != null) {
            if ((ip < ret.size()) && (!(ret.get(ip) instanceof ActionNop))) {
                a = ret.get(ip);
            }
            a.containerSWFOffset = containerSWFOffset;
            a.setAddress(prevIp, SWF.DEFAULT_VERSION, false);
            int info = a.actionLength + 1 + ((a.actionCode > 0x80) ? 2 : 0);
            byte b[] = a.getBytes(sis.version);
            int infoCorrect = info;
            if (a instanceof ActionContainer) {
                infoCorrect += ((ActionContainer) a).getDataLength();
            }
            if (b.length != infoCorrect) {
                //throw new RuntimeException("Wrong length "+a.toString()+" info:"+infoCorrect+" actual:"+b.length+" datalen:"+(infoCorrect-info));
            }
            int actual = 0;
            if (a instanceof ActionContainer) {
                actual = ((ActionContainer) a).getHeaderBytes().length;
            } else {
                actual = a.getBytes(sis.version).length;
            }
            if (!(a instanceof ActionContainer)) {
                int change = info - (rri.getPos() - ip);
                if (change > 0) {
                    a.afterInsert = new ActionJump(change);
                }
            } else {
                info = rri.getPos() - ip;
            }
            if (ip < startIp) {
                retv = true;
            }
            if (debugMode) {
                //if(a instanceof ActionIf){                
                String atos = a.getASMSource(new ArrayList<Long>(), cpool.constants, sis.version, false);
                if (a instanceof ActionContainer) {
                    atos = a.toString();
                }
                System.err.println("readActionListAtPos ip: " + (ip - startIp) + " (0x" + Helper.formatAddress(ip - startIp) + ") " + " action(len " + a.actionLength + "): " + atos + (a.isIgnored() ? " (ignored)" : "") + " stack:" + Helper.stackToString(stack, Helper.toList(cpool)) + " " + Helper.byteArrToString(a.getBytes(SWF.DEFAULT_VERSION)));
                //}
                String add = "";
                if (a instanceof ActionIf) {
                    add = " change: " + ((ActionIf) a).getJumpOffset();
                }
                if (a instanceof ActionJump) {
                    add = " change: " + ((ActionJump) a).getJumpOffset();
                }
                System.err.println(add);
            }


            /*if(a instanceof ActionConstantPool){
             throw new IllegalArgumentException("CP found");
             }     */
            if (a instanceof ActionPush) {
                if (cpool != null) {
                    ((ActionPush) a).constantPool = cpool.constants;
                    cpool.count++;
                }
            }
            if (a instanceof ActionDefineFunction) {
                if (cpool != null) {
                    ((ActionDefineFunction) a).setConstantPool(cpool.constants);
                    cpool.count++;
                }
            }
            if (a instanceof ActionDefineFunction2) {
                if (cpool != null) {
                    ((ActionDefineFunction2) a).setConstantPool(cpool.constants);
                    cpool.count++;
                }
            }
            long newFilePos = rri.getPos();
            long actionLen = newFilePos - filePos;

            ensureCapacity(ret, ip);
            int newip = -1;
            if (!enableVariables && (!(ret.get(ip) instanceof ActionNop))) {
                break;
            }

            if (a instanceof ActionConstantPool) {
                if (cpool == null) {
                    cpool = new ConstantPool();
                }
                cpool.setNew(((ActionConstantPool) a).constantPool);
            }
            Action beforeInsert = null;
            ActionIf aif = null;
            boolean goaif = false;
            if (!a.isIgnored()) {
                try {
                    if (a instanceof ActionIf) {
                        aif = (ActionIf) a;
                        GraphTargetItem top = null;
                        top = stack.pop();

                        int nip = rri.getPos() + aif.getJumpOffset();

                        if (decideBranch) {
                            System.out.print("newip " + nip + ", ");
                            System.out.print("Action: jump(j),ignore(i),compute(c)?");
                            String next = sc.next();
                            if (next.equals("j")) {
                                newip = rri.getPos() + aif.getJumpOffset();
                                rri.setPos(newip);

                            } else if (next.equals("i")) {
                            } else if (next.equals("c")) {
                                goaif = true;
                            }
                        } else if (top.isCompileTime() && ((!top.isVariableComputed()) || (top.isVariableComputed() && enableVariables && (!notCompileTime)))) {
                            //if(top.isCompileTime()) {
                            //if(false){
                            if (enableVariables) {
                                ((ActionIf) a).compileTime = true;
                            }
                            if (debugMode) {
                                System.out.print("is compiletime -> ");
                            }
                            if (top.toBoolean()) {
                                newip = rri.getPos() + aif.getJumpOffset();
                                //rri.setPos(newip);
                                if (((!enableVariables) || (!top.isVariableComputed())) && (!aif.ignoreUsed)) {
                                    a = new ActionJump(aif.getJumpOffset());
                                    a.setAddress(aif.getAddress(), SWF.DEFAULT_VERSION);
                                }
                                aif.jumpUsed = true;
                                if (aif.ignoreUsed) {
                                    aif.compileTime = false;
                                }
                                if (debugMode) {
                                    System.out.println("jump");
                                }
                            } else {
                                aif.ignoreUsed = true;
                                if (aif.jumpUsed) {
                                    aif.compileTime = false;
                                }
                                if (debugMode) {
                                    System.out.println("ignore");
                                }
                                if (((!enableVariables) || (!top.isVariableComputed())) && (!aif.jumpUsed)) {
                                    //a = new ActionNop();
                                    aif.setIgnored(true);
                                    //a.setAddress(aif.getAddress(), SWF.DEFAULT_VERSION);
                                }
                            }
                            if (((!enableVariables) || (!top.isVariableComputed())) && (!(aif.jumpUsed && aif.ignoreUsed))) {
                                List<GraphSourceItemPos> needed = top.getNeededSources();
                                for (GraphSourceItemPos ig : needed) {
                                    if (ig.item == null) {
                                        continue;
                                    }
                                    if (ig.item instanceof ActionPush) {
                                        if (!((ActionPush) ig.item).ignoredParts.contains(ig.pos)) {
                                            ((ActionPush) ig.item).ignoredParts.add(ig.pos);

                                            if (((ActionPush) ig.item).ignoredParts.size() == ((ActionPush) ig.item).values.size()) {
                                                ((Action) ig.item).setIgnored(true);
                                            }
                                        }
                                    } else {
                                        ((Action) ig.item).setIgnored(true);
                                    }
                                }
                            }

                        } else {
                            if (debugMode) {
                                System.out.println("goaif");
                            }
                            //throw new RuntimeException("goaif");
                            goaif = true;
                        }
                    } else if (a instanceof ActionJump) {
                        newip = rri.getPos() + ((ActionJump) a).getJumpOffset();
                        //if(newip>=0){
                        //rri.setPos(newip);
                        //}
                    } else {
                        //return in for..in,   TODO:Handle this better way
                        if (((a instanceof ActionEquals) || (a instanceof ActionEquals2)) && (stack.size() == 1) && (stack.peek() instanceof DirectValueTreeItem)) {
                            stack.push(new DirectValueTreeItem(null, 0, new Null(), new ArrayList<String>()));
                        }
                        if ((a instanceof ActionStoreRegister) && stack.isEmpty()) {
                            stack.push(new DirectValueTreeItem(null, 0, new Null(), new ArrayList<String>()));
                        }
                        a.translate(localData, stack, output);
                    }
                } catch (RuntimeException ex) {
                    if (!enableVariables) {
                        throw ex;
                    }
                    log.log(Level.SEVERE, "Disassembly exception", ex);
                    break;
                }
            }
            int nopos = -1;
            for (int i = 0; i < actionLen; i++) {
                ensureCapacity(ret, ip + i);
                if (a instanceof ActionNop) {
                    int prevPos = (int) a.getAddress();
                    a = new ActionNop();
                    a.setAddress(prevPos, SWF.DEFAULT_VERSION);
                    nopos++;
                    if (nopos > 0) {
                        a.setAddress(a.getAddress() + 1, SWF.DEFAULT_VERSION);
                    }

                    if (i == 0) {
                        a.beforeInsert = beforeInsert;
                    }
                } else {
                    a.beforeInsert = beforeInsert;
                }
                ret.set(ip + i, a);
            }

            if (a instanceof ActionEnd) {
                break;
            }
            if (newip > -1) {
                ip = newip;
            } else {
                ip = ip + info; //(int) actionLen;
            }
            rri.setPos(ip);
            filePos = rri.getPos();
            if (goaif) {
                if (aif.ignoreUsed && aif.jumpUsed) {
                    break;
                }
                aif.ignoreUsed = true;
                aif.jumpUsed = true;
                int oldPos = rri.getPos();
                Stack<GraphTargetItem> substack = (Stack<GraphTargetItem>) stack.clone();
                if (readActionListAtPos(address, containerSWFOffset, true, enableVariables, localData, substack, cpool, sis, rri, rri.getPos() + aif.getJumpOffset(), ret, startIp, endip)) {
                    retv = true;
                }
                rri.setPos(oldPos);
                notCompileTime = true;
            }
            prevIp = ip;
        }
        return retv;
    }

    private static void ensureCapacity(List<Action> ret, int index) {
        int pos = ret.size();
        while (ret.size() <= index) {
            Action a = new ActionNop();
            a.setAddress(pos++, SWF.DEFAULT_VERSION);
            ret.add(a);
        }
    }

    private static void dumpTag(PrintStream out, int version, Tag tag, int level) {
        StringBuilder sb = new StringBuilder();
        sb.append(Helper.formatHex((int) tag.getPos(), 8));
        sb.append(": ");
        sb.append(Helper.indent(level, ""));
        sb.append(Helper.format(tag.toString(), 25 - 2 * level));
        sb.append(" tagId=");
        sb.append(Helper.formatInt(tag.getId(), 3));
        sb.append(" len=");
        sb.append(Helper.formatInt((int) tag.getOrigDataLength(), 8));
        sb.append("  ");
        sb.append(Helper.bytesToHexString(64, tag.getData(version), 0));
        out.println(sb.toString());
//        out.println(Utils.formatHex((int)tag.getPos(), 8) + ": " + Utils.indent(level, "") + Utils.format(tag.toString(), 25 - 2*level) + " tagId="+tag.getId()+" len="+tag.getOrigDataLength()+": "+Utils.bytesToHexString(64, tag.getData(version), 0));
        if (tag.hasSubTags()) {
            for (Tag subTag : tag.getSubTags()) {
                dumpTag(out, version, subTag, level + 1);
            }
        }
    }

    /**
     * Reads list of tags from the stream. Reading ends with End tag(=0) or end
     * of the stream.
     *
     * @return List of tags
     * @throws IOException
     */
    public List<Tag> readTagList(int level) throws IOException {
        List<Tag> tags = new ArrayList<Tag>();
        Tag tag;
        Tag previousTag = null;
        while (true) {
            long pos = getPos();
            tag = readTag(level, pos);
            if (tag == null) {
                break;
            }
            tags.add(tag);
            if (Main.dump_tags && level == 0) {
                dumpTag(System.out, version, tag, level);
            }
            tag.previousTag = previousTag;
            previousTag = tag;
        }
        return tags;
    }

    /**
     * Reads one Tag from the stream
     *
     * @return Tag or null when End tag
     * @throws IOException
     */
    public Tag readTag(int level, long pos) throws IOException {
        int tagIDTagLength = readUI16();
        int tagID = (tagIDTagLength) >> 6;
        if (tagID == 0) {
            return null;
        }
        long tagLength = (tagIDTagLength & 0x003F);
        boolean readLong = false;
        if (tagLength == 0x3f) {
            tagLength = readSI32();
            readLong = true;
        }
        byte data[] = readBytes((int) tagLength);
        Tag ret;
        try {
            switch (tagID) {
                case 0:
                    ret = new EndTag(data, version, pos);
                    break;
                case 1:
                    ret = new ShowFrameTag(pos);
                    break;
                case 2:
                    ret = new DefineShapeTag(data, version, pos);
                    break;
                //case 3
                case 4:
                    ret = new PlaceObjectTag(data, version, pos);
                    break;
                case 5:
                    ret = new RemoveObjectTag(data, version, pos);
                    break;
                case 6:
                    ret = new DefineBitsTag(data, version, pos);
                    break;
                case 7:
                    ret = new DefineButtonTag(data, version, pos);
                    break;
                case 8:
                    ret = new JPEGTablesTag(data, pos);
                    break;
                case 9:
                    ret = new SetBackgroundColorTag(data, version, pos);
                    break;
                case 10:
                    ret = new DefineFontTag(data, version, pos);
                    break;
                case 11:
                    ret = new DefineTextTag(data, version, pos);
                    break;
                case 12:
                    ret = new DoActionTag(data, version, pos);
                    break;
                case 13:
                    ret = new DefineFontInfoTag(data, version, pos);
                    break;
                case 14:
                    ret = new DefineSoundTag(data, version, pos);
                    break;
                case 15:
                    ret = new StartSoundTag(data, version, pos);
                    break;
                //case 16
                case 17:
                    ret = new DefineButtonSoundTag(data, version, pos);
                    break;
                case 18:
                    ret = new SoundStreamHeadTag(data, version, pos);
                    break;
                case 19:
                    ret = new SoundStreamBlockTag(data, version, pos);
                    break;
                case 21:
                    ret = new DefineBitsJPEG2Tag(data, version, pos);
                    break;
                case 20:
                    ret = new DefineBitsLosslessTag(data, version, pos);
                    break;
                case 22:
                    ret = new DefineShape2Tag(data, version, pos);
                    break;
                case 23:
                    ret = new DefineButtonCxformTag(data, version, pos);
                    break;
                case 24:
                    ret = new ProtectTag(data, version, pos);
                    break;
                //case 25:
                case 26:
                    ret = new PlaceObject2Tag(data, version, pos);
                    break;
                //case 27: 
                case 28:
                    ret = new RemoveObject2Tag(data, version, pos);
                    break;
                //case 29:
                //case 30:
                //case 31: 
                case 32:
                    ret = new DefineShape3Tag(data, version, pos);
                    break;
                case 33:
                    ret = new DefineText2Tag(data, version, pos);
                    break;
                case 34:
                    ret = new DefineButton2Tag(data, version, pos);
                    break;
                case 35:
                    ret = new DefineBitsJPEG3Tag(data, version, pos);
                    break;
                case 36:
                    ret = new DefineBitsLossless2Tag(data, version, pos);
                    break;
                case 37:
                    ret = new DefineEditTextTag(data, version, pos);
                    break;
                //case 38:
                case 39:
                    ret = new DefineSpriteTag(data, version, level, pos);
                    break;
                //case 40:
                case 41:
                    ret = new ProductInfoTag(data, version, pos);
                    break;
                //case 42:
                case 43:
                    ret = new FrameLabelTag(data, version, pos);
                    break;
                //case 44:
                case 45:
                    ret = new SoundStreamHead2Tag(data, version, pos);
                    break;
                case 46:
                    ret = new DefineMorphShapeTag(data, version, pos);
                    break;
                //case 47:
                case 48:
                    ret = new DefineFont2Tag(data, version, pos);
                    break;
                //case 49-55:
                case 56:
                    ret = new ExportAssetsTag(data, version, pos);
                    break;
                case 57:
                    ret = new ImportAssetsTag(data, version, pos);
                    break;
                case 58:
                    ret = new EnableDebuggerTag(data, version, pos);
                    break;
                case 59:
                    ret = new DoInitActionTag(data, version, pos);
                    break;
                case 60:
                    ret = new DefineVideoStreamTag(data, version, pos);
                    break;
                case 61:
                    ret = new VideoFrameTag(data, version, pos);
                    break;
                case 62:
                    ret = new DefineFontInfo2Tag(data, version, pos);
                    break;
                case 63:
                    ret = new DebugIDTag(data, version, pos);
                    break;
                case 64:
                    ret = new EnableDebugger2Tag(data, version, pos);
                    break;
                case 65:
                    ret = new ScriptLimitsTag(data, version, pos);
                    break;
                case 66:
                    ret = new SetTabIndexTag(data, version, pos);
                    break;
                //case 67-68:
                case 69:
                    ret = new FileAttributesTag(data, version, pos);
                    break;
                case 70:
                    ret = new PlaceObject3Tag(data, version, pos);
                    break;
                case 71:
                    ret = new ImportAssets2Tag(data, version, pos);
                    break;
                //case 72:
                case 73:
                    ret = new DefineFontAlignZonesTag(data, version, pos);
                    break;
                case 74:
                    ret = new CSMTextSettingsTag(data, version, pos);
                    break;
                case 75:
                    ret = new DefineFont3Tag(data, version, pos);
                    break;
                case 76:
                    ret = new SymbolClassTag(data, version, pos);
                    break;
                case 77:
                    ret = new MetadataTag(data, version, pos);
                    break;
                case 78:
                    ret = new DefineScalingGridTag(data, version, pos);
                    break;
                //case 79-81:
                case 82:
                    ret = new DoABCTag(data, version, pos);
                    break;
                case 83:
                    ret = new DefineShape4Tag(data, version, pos);
                    break;
                case 84:
                    ret = new DefineMorphShape2Tag(data, version, pos);
                    break;
                //case 85:
                case 86:
                    ret = new DefineSceneAndFrameLabelDataTag(data, version, pos);
                    break;
                case 87:
                    ret = new DefineBinaryDataTag(data, version, pos);
                    break;
                case 88:
                    ret = new DefineFontNameTag(data, version, pos);
                    break;
                case 89:
                    ret = new StartSound2Tag(data, version, pos);
                    break;
                case 90:
                    ret = new DefineBitsJPEG4Tag(data, version, pos);
                    break;
                case 91:
                    ret = new DefineFont4Tag(data, version, pos);
                    break;
                default:
                    ret = new Tag(tagID, "Unknown", data, pos);
            }
        } catch (Exception ex) {
            Logger.getLogger(SWFInputStream.class.getName()).log(Level.SEVERE, "Error during tag reading", ex);
            ret = new Tag(tagID, "ErrorTag", data, pos);
        }
        ret.forceWriteAsLong = readLong;
        byte dataNew[] = ret.getData(version);

        int ignoreFirst = 0;
        for (int i = 0; i < data.length; i++) {
            if (i >= dataNew.length) {
                break;
            }
            if (dataNew[i] != data[i]) {
                if (ignoreFirst > 0) {
                    ignoreFirst--;
                    continue;
                }
                String e = "";
                e = ("TAG " + ret.toString() + " WRONG, ");
                for (int j = i - 10; j <= i + 5; j++) {
                    while (j < 0) {
                        j++;
                    }
                    if (j >= data.length) {
                        break;
                    }
                    if (j >= dataNew.length) {
                        break;
                    }
                    if (j >= i) {
                        e += (Long.toHexString(data[j] & 0xff) + " ( is " + Long.toHexString(dataNew[j] & 0xff) + ") ");
                    } else {
                        e += (Long.toHexString(data[j] & 0xff) + " ");
                    }
                }
                log.fine(e);
            }
        }
        return ret;
    }

    public Action readAction() throws IOException {
        return readAction(new ReReadableInputStream(this));
    }

    /**
     * Reads one Action from the stream
     *
     * @return Action or null when ActionEndFlag or end of the stream
     * @throws IOException
     */
    public Action readAction(ReReadableInputStream rri) throws IOException {
        {
            int actionCode = readUI8();
            if (actionCode == 0) {
                return new ActionEnd();
            }
            if (actionCode == -1) {
                return null;
            }
            int actionLength = 0;
            if (actionCode >= 0x80) {
                actionLength = readUI16();
            }
            switch (actionCode) {
                //SWF3 Actions
                case 0x81:
                    return new ActionGotoFrame(this);
                case 0x83:
                    return new ActionGetURL(actionLength, this, version);
                case 0x04:
                    return new ActionNextFrame();
                case 0x05:
                    return new ActionPrevFrame();
                case 0x06:
                    return new ActionPlay();
                case 0x07:
                    return new ActionStop();
                case 0x08:
                    return new ActionToggleQuality();
                case 0x09:
                    return new ActionStopSounds();
                case 0x8A:
                    return new ActionWaitForFrame(this);
                case 0x8B:
                    return new ActionSetTarget(actionLength, this, version);
                case 0x8C:
                    return new ActionGoToLabel(actionLength, this, version);
                //SWF4 Actions
                case 0x96:
                    return new ActionPush(actionLength, this, version);
                case 0x17:
                    return new ActionPop();
                case 0x0A:
                    return new ActionAdd();
                case 0x0B:
                    return new ActionSubtract();
                case 0x0C:
                    return new ActionMultiply();
                case 0x0D:
                    return new ActionDivide();
                case 0x0E:
                    return new ActionEquals();
                case 0x0F:
                    return new ActionLess();
                case 0x10:
                    return new ActionAnd();
                case 0x11:
                    return new ActionOr();
                case 0x12:
                    return new ActionNot();
                case 0x13:
                    return new ActionStringEquals();
                case 0x14:
                    return new ActionStringLength();
                case 0x21:
                    return new ActionStringAdd();
                case 0x15:
                    return new ActionStringExtract();
                case 0x29:
                    return new ActionStringLess();
                case 0x31:
                    return new ActionMBStringLength();
                case 0x35:
                    return new ActionMBStringExtract();
                case 0x18:
                    return new ActionToInteger();
                case 0x32:
                    return new ActionCharToAscii();
                case 0x33:
                    return new ActionAsciiToChar();
                case 0x36:
                    return new ActionMBCharToAscii();
                case 0x37:
                    return new ActionMBAsciiToChar();
                case 0x99:
                    return new ActionJump(this);
                case 0x9D:
                    return new ActionIf(this);
                case 0x9E:
                    return new ActionCall();
                case 0x1C:
                    return new ActionGetVariable();
                case 0x1D:
                    return new ActionSetVariable();
                case 0x9A:
                    return new ActionGetURL2(this);
                case 0x9F:
                    return new ActionGotoFrame2(actionLength, this);
                case 0x20:
                    return new ActionSetTarget2();
                case 0x22:
                    return new ActionGetProperty();
                case 0x23:
                    return new ActionSetProperty();
                case 0x24:
                    return new ActionCloneSprite();
                case 0x25:
                    return new ActionRemoveSprite();
                case 0x27:
                    return new ActionStartDrag();
                case 0x28:
                    return new ActionEndDrag();
                case 0x8D:
                    return new ActionWaitForFrame2(this);
                case 0x26:
                    return new ActionTrace();
                case 0x34:
                    return new ActionGetTime();
                case 0x30:
                    return new ActionRandomNumber();
                //SWF5 Actions
                case 0x3D:
                    return new ActionCallFunction();
                case 0x52:
                    return new ActionCallMethod();
                case 0x88:
                    return new ActionConstantPool(actionLength, this, version);
                case 0x9B:
                    return new ActionDefineFunction(actionLength, this, rri, version);
                case 0x3C:
                    return new ActionDefineLocal();
                case 0x41:
                    return new ActionDefineLocal2();
                case 0x3A:
                    return new ActionDelete();
                case 0x3B:
                    return new ActionDelete2();
                case 0x46:
                    return new ActionEnumerate();
                case 0x49:
                    return new ActionEquals2();
                case 0x4E:
                    return new ActionGetMember();
                case 0x42:
                    return new ActionInitArray();
                case 0x43:
                    return new ActionInitObject();
                case 0x53:
                    return new ActionNewMethod();
                case 0x40:
                    return new ActionNewObject();
                case 0x4F:
                    return new ActionSetMember();
                case 0x45:
                    return new ActionTargetPath();
                case 0x94:
                    return new ActionWith(this, rri, version);
                case 0x4A:
                    return new ActionToNumber();
                case 0x4B:
                    return new ActionToString();
                case 0x44:
                    return new ActionTypeOf();
                case 0x47:
                    return new ActionAdd2();
                case 0x48:
                    return new ActionLess2();
                case 0x3F:
                    return new ActionModulo();
                case 0x60:
                    return new ActionBitAnd();
                case 0x63:
                    return new ActionBitLShift();
                case 0x61:
                    return new ActionBitOr();
                case 0x64:
                    return new ActionBitRShift();
                case 0x65:
                    return new ActionBitURShift();
                case 0x62:
                    return new ActionBitXor();
                case 0x51:
                    return new ActionDecrement();
                case 0x50:
                    return new ActionIncrement();
                case 0x4C:
                    return new ActionPushDuplicate();
                case 0x3E:
                    return new ActionReturn();
                case 0x4D:
                    return new ActionStackSwap();
                case 0x87:
                    return new ActionStoreRegister(this);
                //SWF6 Actions
                case 0x54:
                    return new ActionInstanceOf();
                case 0x55:
                    return new ActionEnumerate2();
                case 0x66:
                    return new ActionStrictEquals();
                case 0x67:
                    return new ActionGreater();
                case 0x68:
                    return new ActionStringGreater();
                //SWF7 Actions
                case 0x8E:
                    return new ActionDefineFunction2(actionLength, this, rri, version);
                case 0x69:
                    return new ActionExtends();
                case 0x2B:
                    return new ActionCastOp();
                case 0x2C:
                    return new ActionImplementsOp();
                case 0x8F:
                    return new ActionTry(actionLength, this, rri, version);
                case 0x2A:
                    return new ActionThrow();
                default:
                    if (actionLength > 0) {
                        //skip(actionLength);
                    }
                    //throw new UnknownActionException(actionCode);
                    Action r = new ActionNop();
                    r.actionCode = actionCode;
                    r.actionLength = actionLength;
                    return r;
                //return new Action(actionCode, actionLength);
            }
        }
    }

    /**
     * Reads one MATRIX value from the stream
     *
     * @return MATRIX value
     * @throws IOException
     */
    public MATRIX readMatrix() throws IOException {
        MATRIX ret = new MATRIX();
        ret.hasScale = readUB(1) == 1;
        if (ret.hasScale) {
            int NScaleBits = (int) readUB(5);
            ret.scaleX = (int) readSB(NScaleBits);
            ret.scaleY = (int) readSB(NScaleBits);
            ret.bitsScale = NScaleBits;
        }
        ret.hasRotate = readUB(1) == 1;
        if (ret.hasRotate) {
            int NRotateBits = (int) readUB(5);
            ret.rotateSkew0 = (int) readSB(NRotateBits);
            ret.rotateSkew1 = (int) readSB(NRotateBits);
            ret.bitsRotate = NRotateBits;
        }
        int NTranslateBits = (int) readUB(5);
        ret.translateX = (int) readSB(NTranslateBits);
        ret.translateY = (int) readSB(NTranslateBits);
        ret.bitsTranslate = NTranslateBits;
        alignByte();
        return ret;
    }

    /**
     * Reads one CXFORMWITHALPHA value from the stream
     *
     * @return CXFORMWITHALPHA value
     * @throws IOException
     */
    public CXFORMWITHALPHA readCXFORMWITHALPHA() throws IOException {
        CXFORMWITHALPHA ret = new CXFORMWITHALPHA();
        ret.hasAddTerms = readUB(1) == 1;
        ret.hasMultTerms = readUB(1) == 1;
        int Nbits = (int) readUB(4);
        ret.nbits = Nbits;
        if (ret.hasMultTerms) {
            ret.redMultTerm = (int) readSB(Nbits);
            ret.greenMultTerm = (int) readSB(Nbits);
            ret.blueMultTerm = (int) readSB(Nbits);
            ret.alphaMultTerm = (int) readSB(Nbits);
        }
        if (ret.hasAddTerms) {
            ret.redAddTerm = (int) readSB(Nbits);
            ret.greenAddTerm = (int) readSB(Nbits);
            ret.blueAddTerm = (int) readSB(Nbits);
            ret.alphaAddTerm = (int) readSB(Nbits);
        }
        alignByte();
        return ret;
    }

    /**
     * Reads one CXFORM value from the stream
     *
     * @return CXFORM value
     * @throws IOException
     */
    public CXFORM readCXFORM() throws IOException {
        CXFORM ret = new CXFORM();
        ret.hasAddTerms = readUB(1) == 1;
        ret.hasMultTerms = readUB(1) == 1;
        int Nbits = (int) readUB(4);
        ret.nbits = Nbits;
        if (ret.hasMultTerms) {
            ret.redMultTerm = (int) readSB(Nbits);
            ret.greenMultTerm = (int) readSB(Nbits);
            ret.blueMultTerm = (int) readSB(Nbits);
        }
        if (ret.hasAddTerms) {
            ret.redAddTerm = (int) readSB(Nbits);
            ret.greenAddTerm = (int) readSB(Nbits);
            ret.blueAddTerm = (int) readSB(Nbits);
        }
        alignByte();
        return ret;
    }

    /**
     * Reads one CLIPEVENTFLAGS value from the stream
     *
     * @return CLIPEVENTFLAGS value
     * @throws IOException
     */
    public CLIPEVENTFLAGS readCLIPEVENTFLAGS() throws IOException {
        CLIPEVENTFLAGS ret = new CLIPEVENTFLAGS();
        ret.clipEventKeyUp = readUB(1) == 1;
        ret.clipEventKeyDown = readUB(1) == 1;
        ret.clipEventMouseUp = readUB(1) == 1;
        ret.clipEventMouseDown = readUB(1) == 1;
        ret.clipEventMouseMove = readUB(1) == 1;
        ret.clipEventUnload = readUB(1) == 1;
        ret.clipEventEnterFrame = readUB(1) == 1;
        ret.clipEventLoad = readUB(1) == 1;
        ret.clipEventDragOver = readUB(1) == 1;
        ret.clipEventRollOut = readUB(1) == 1;
        ret.clipEventRollOver = readUB(1) == 1;
        ret.clipEventReleaseOutside = readUB(1) == 1;
        ret.clipEventRelease = readUB(1) == 1;
        ret.clipEventPress = readUB(1) == 1;
        ret.clipEventInitialize = readUB(1) == 1;
        ret.clipEventData = readUB(1) == 1;
        if (version >= 6) {
            readUB(5);
            ret.clipEventConstruct = readUB(1) == 1;
            ret.clipEventKeyPress = readUB(1) == 1;
            ret.clipEventDragOut = readUB(1) == 1;
            readUB(8);
        }
        return ret;
    }

    /**
     * Reads one CLIPACTIONRECORD value from the stream
     *
     * @return CLIPACTIONRECORD value
     * @throws IOException
     */
    public CLIPACTIONRECORD readCLIPACTIONRECORD() throws IOException {
        CLIPACTIONRECORD ret = new CLIPACTIONRECORD(this, version, getPos());
        if (ret.eventFlags.isClear()) {
            return null;
        }
        //ret.actions = (new SWFInputStream(new ByteArrayInputStream(readBytes(actionRecordSize)), version)).readActionList();
        return ret;
    }

    /**
     * Reads one CLIPACTIONS value from the stream
     *
     * @return CLIPACTIONS value
     * @throws IOException
     */
    public CLIPACTIONS readCLIPACTIONS() throws IOException {
        CLIPACTIONS ret = new CLIPACTIONS();
        readUI16();//reserved
        ret.allEventFlags = readCLIPEVENTFLAGS();
        CLIPACTIONRECORD cr;
        ret.clipActionRecords = new ArrayList<CLIPACTIONRECORD>();
        while ((cr = readCLIPACTIONRECORD()) != null) {
            ret.clipActionRecords.add(cr);
        }
        return ret;
    }

    /**
     * Reads one COLORMATRIXFILTER value from the stream
     *
     * @return COLORMATRIXFILTER value
     * @throws IOException
     */
    public COLORMATRIXFILTER readCOLORMATRIXFILTER() throws IOException {
        COLORMATRIXFILTER ret = new COLORMATRIXFILTER();
        ret.matrix = new float[20];
        for (int i = 0; i < 20; i++) {
            ret.matrix[i] = readFLOAT();
        }
        return ret;
    }

    /**
     * Reads one RGBA value from the stream
     *
     * @return RGBA value
     * @throws IOException
     */
    public RGBA readRGBA() throws IOException {
        RGBA ret = new RGBA();
        ret.red = readUI8();
        ret.green = readUI8();
        ret.blue = readUI8();
        ret.alpha = readUI8();
        return ret;
    }

    /**
     * Reads one ARGB value from the stream
     *
     * @return ARGB value
     * @throws IOException
     */
    public ARGB readARGB() throws IOException {
        ARGB ret = new ARGB();
        ret.alpha = readUI8();
        ret.red = readUI8();
        ret.green = readUI8();
        ret.blue = readUI8();
        return ret;
    }

    /**
     * Reads one RGB value from the stream
     *
     * @return RGB value
     * @throws IOException
     */
    public RGB readRGB() throws IOException {
        RGB ret = new RGB();
        ret.red = readUI8();
        ret.green = readUI8();
        ret.blue = readUI8();
        return ret;
    }

    /**
     * Reads one CONVOLUTIONFILTER value from the stream
     *
     * @return CONVOLUTIONFILTER value
     * @throws IOException
     */
    public CONVOLUTIONFILTER readCONVOLUTIONFILTER() throws IOException {
        CONVOLUTIONFILTER ret = new CONVOLUTIONFILTER();
        ret.matrixX = readUI8();
        ret.matrixY = readUI8();
        ret.divisor = readFLOAT();
        ret.bias = readFLOAT();
        ret.matrix = new float[ret.matrixX][ret.matrixY];
        for (int x = 0; x < ret.matrixX; x++) {
            for (int y = 0; y < ret.matrixY; y++) {
                ret.matrix[x][y] = readFLOAT();
            }
        }
        ret.defaultColor = readRGBA();
        readUB(6);//reserved
        ret.clamp = readUB(1) == 1;
        ret.preserveAlpha = readUB(1) == 1;
        return ret;
    }

    /**
     * Reads one BLURFILTER value from the stream
     *
     * @return BLURFILTER value
     * @throws IOException
     */
    public BLURFILTER readBLURFILTER() throws IOException {
        BLURFILTER ret = new BLURFILTER();
        ret.blurX = readFIXED();
        ret.blurY = readFIXED();
        ret.passes = (int) readUB(5);
        readUB(3); //reserved
        return ret;
    }

    /**
     * Reads one DROPSHADOWFILTER value from the stream
     *
     * @return DROPSHADOWFILTER value
     * @throws IOException
     */
    public DROPSHADOWFILTER readDROPSHADOWFILTER() throws IOException {
        DROPSHADOWFILTER ret = new DROPSHADOWFILTER();
        ret.dropShadowColor = readRGBA();
        ret.blurX = readFIXED();
        ret.blurY = readFIXED();
        ret.angle = readFIXED();
        ret.distance = readFIXED();
        ret.strength = readFIXED8();
        ret.innerShadow = readUB(1) == 1;
        ret.knockout = readUB(1) == 1;
        ret.compositeSource = readUB(1) == 1;
        ret.passes = (int) readUB(5);
        return ret;
    }

    /**
     * Reads one GLOWFILTER value from the stream
     *
     * @return GLOWFILTER value
     * @throws IOException
     */
    public GLOWFILTER readGLOWFILTER() throws IOException {
        GLOWFILTER ret = new GLOWFILTER();
        ret.glowColor = readRGBA();
        ret.blurX = readFIXED();
        ret.blurY = readFIXED();
        ret.strength = readFIXED8();
        ret.innerGlow = readUB(1) == 1;
        ret.knockout = readUB(1) == 1;
        ret.compositeSource = readUB(1) == 1;
        ret.passes = (int) readUB(5);
        return ret;
    }

    /**
     * Reads one BEVELFILTER value from the stream
     *
     * @return BEVELFILTER value
     * @throws IOException
     */
    public BEVELFILTER readBEVELFILTER() throws IOException {
        BEVELFILTER ret = new BEVELFILTER();
        ret.shadowColor = readRGBA();
        ret.highlightColor = readRGBA();
        ret.blurX = readFIXED();
        ret.blurY = readFIXED();
        ret.angle = readFIXED();
        ret.distance = readFIXED();
        ret.strength = readFIXED8();
        ret.innerShadow = readUB(1) == 1;
        ret.knockout = readUB(1) == 1;
        ret.compositeSource = readUB(1) == 1;
        ret.onTop = readUB(1) == 1;
        ret.passes = (int) readUB(4);
        return ret;
    }

    /**
     * Reads one GRADIENTGLOWFILTER value from the stream
     *
     * @return GRADIENTGLOWFILTER value
     * @throws IOException
     */
    public GRADIENTGLOWFILTER readGRADIENTGLOWFILTER() throws IOException {
        GRADIENTGLOWFILTER ret = new GRADIENTGLOWFILTER();
        int numColors = readUI8();
        ret.gradientColors = new RGBA[numColors];
        ret.gradientRatio = new int[numColors];
        for (int i = 0; i < numColors; i++) {
            ret.gradientColors[i] = readRGBA();
        }
        for (int i = 0; i < numColors; i++) {
            ret.gradientRatio[i] = readUI8();
        }
        ret.blurX = readFIXED();
        ret.blurY = readFIXED();
        ret.angle = readFIXED();
        ret.distance = readFIXED();
        ret.strength = readFIXED8();
        ret.innerShadow = readUB(1) == 1;
        ret.knockout = readUB(1) == 1;
        ret.compositeSource = readUB(1) == 1;
        ret.onTop = readUB(1) == 1;
        ret.passes = (int) readUB(4);
        return ret;
    }

    /**
     * Reads one GRADIENTBEVELFILTER value from the stream
     *
     * @return GRADIENTBEVELFILTER value
     * @throws IOException
     */
    public GRADIENTBEVELFILTER readGRADIENTBEVELFILTER() throws IOException {
        GRADIENTBEVELFILTER ret = new GRADIENTBEVELFILTER();
        int numColors = readUI8();
        ret.gradientColors = new RGBA[numColors];
        ret.gradientRatio = new int[numColors];
        for (int i = 0; i < numColors; i++) {
            ret.gradientColors[i] = readRGBA();
        }
        for (int i = 0; i < numColors; i++) {
            ret.gradientRatio[i] = readUI8();
        }
        ret.blurX = readFIXED();
        ret.blurY = readFIXED();
        ret.angle = readFIXED();
        ret.distance = readFIXED();
        ret.strength = readFIXED8();
        ret.innerShadow = readUB(1) == 1;
        ret.knockout = readUB(1) == 1;
        ret.compositeSource = readUB(1) == 1;
        ret.onTop = readUB(1) == 1;
        ret.passes = (int) readUB(4);
        return ret;
    }

    /**
     * Reads list of FILTER values from the stream
     *
     * @return List of FILTER values
     * @throws IOException
     */
    public List<FILTER> readFILTERLIST() throws IOException {
        List<FILTER> ret = new ArrayList<FILTER>();
        int numberOfFilters = readUI8();
        for (int i = 0; i < numberOfFilters; i++) {
            ret.add(readFILTER());
        }
        return ret;
    }

    /**
     * Reads one FILTER value from the stream
     *
     * @return FILTER value
     * @throws IOException
     */
    public FILTER readFILTER() throws IOException {
        int filterId = readUI8();
        switch (filterId) {
            case 0:
                return readDROPSHADOWFILTER();
            case 1:
                return readBLURFILTER();
            case 2:
                return readGLOWFILTER();
            case 3:
                return readBEVELFILTER();
            case 4:
                return readGRADIENTGLOWFILTER();
            case 5:
                return readCONVOLUTIONFILTER();
            case 6:
                return readCOLORMATRIXFILTER();
            case 7:
                return readGRADIENTBEVELFILTER();
            default:
                return null;
        }
    }

    /**
     * Reads list of BUTTONRECORD values from the stream
     *
     * @param inDefineButton2 Whether read from inside of DefineButton2Tag or
     * not
     * @return List of BUTTONRECORD values
     * @throws IOException
     */
    public List<BUTTONRECORD> readBUTTONRECORDList(boolean inDefineButton2) throws IOException {
        List<BUTTONRECORD> ret = new ArrayList<BUTTONRECORD>();
        BUTTONRECORD br;
        while ((br = readBUTTONRECORD(inDefineButton2)) != null) {
            ret.add(br);
        }
        return ret;
    }

    /**
     * Reads one BUTTONRECORD value from the stream
     *
     * @return BUTTONRECORD value
     * @throws IOException
     */
    public BUTTONRECORD readBUTTONRECORD(boolean inDefineButton2) throws IOException {
        BUTTONRECORD ret = new BUTTONRECORD();
        int res = (int) readUB(2); //reserved
        ret.buttonHasBlendMode = readUB(1) == 1;
        ret.buttonHasFilterList = readUB(1) == 1;
        ret.buttonStateHitTest = readUB(1) == 1;
        ret.buttonStateDown = readUB(1) == 1;
        ret.buttonStateOver = readUB(1) == 1;
        ret.buttonStateUp = readUB(1) == 1;

        if (!ret.buttonHasBlendMode) {
            if (!ret.buttonHasFilterList) {
                if (!ret.buttonStateHitTest) {
                    if (!ret.buttonStateDown) {
                        if (!ret.buttonStateOver) {
                            if (!ret.buttonStateUp) {
                                if (res == 0) {
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
        }

        ret.characterId = readUI16();
        ret.placeDepth = readUI16();
        ret.placeMatrix = readMatrix();
        if (inDefineButton2) {
            ret.colorTransform = readCXFORMWITHALPHA();
            if (ret.buttonHasFilterList) {
                ret.filterList = readFILTERLIST();
            }
            if (ret.buttonHasBlendMode) {
                ret.blendMode = readUI8();
            }
        }
        return ret;
    }

    /**
     * Reads list of BUTTONCONDACTION values from the stream
     *
     * @return List of BUTTONCONDACTION values
     * @throws IOException
     */
    public List<BUTTONCONDACTION> readBUTTONCONDACTIONList() throws IOException {
        List<BUTTONCONDACTION> ret = new ArrayList<BUTTONCONDACTION>();
        BUTTONCONDACTION bc;
        while (!(bc = readBUTTONCONDACTION()).isLast) {
            ret.add(bc);
        }
        ret.add(bc);
        return ret;
    }

    /**
     * Reads one BUTTONCONDACTION value from the stream
     *
     * @return BUTTONCONDACTION value
     * @throws IOException
     */
    public BUTTONCONDACTION readBUTTONCONDACTION() throws IOException {
        BUTTONCONDACTION ret = new BUTTONCONDACTION(this, version, getPos());
        //ret.actions = readActionList();
        return ret;
    }

    /**
     * Reads one GRADRECORD value from the stream
     *
     * @return GRADRECORD value
     * @throws IOException
     */
    public GRADRECORD readGRADRECORD(int shapeNum) throws IOException {
        GRADRECORD ret = new GRADRECORD();
        ret.ratio = readUI8();
        if (shapeNum >= 3) {
            ret.colorA = readRGBA();
        } else {
            ret.color = readRGB();
        }
        return ret;
    }

    /**
     * Reads one GRADIENT value from the stream
     *
     * @return GRADIENT value
     * @throws IOException
     */
    public GRADIENT readGRADIENT(int shapeNum) throws IOException {
        GRADIENT ret = new GRADIENT();
        ret.spreadMode = (int) readUB(2);
        ret.interPolationMode = (int) readUB(2);
        int numGradients = (int) readUB(4);
        ret.gradientRecords = new GRADRECORD[numGradients];
        for (int i = 0; i < numGradients; i++) {
            ret.gradientRecords[i] = readGRADRECORD(shapeNum);

        }
        return ret;
    }

    /**
     * Reads one FOCALGRADIENT value from the stream
     *
     * @return FOCALGRADIENT value
     * @throws IOException
     */
    public FOCALGRADIENT readFOCALGRADIENT(int shapeNum) throws IOException {
        FOCALGRADIENT ret = new FOCALGRADIENT();
        ret.spreadMode = (int) readUB(2);
        ret.interPolationMode = (int) readUB(2);
        int numGradients = (int) readUB(4);
        ret.gradientRecords = new GRADRECORD[numGradients];
        for (int i = 0; i < numGradients; i++) {
            ret.gradientRecords[i] = readGRADRECORD(shapeNum);
        }
        ret.focalPoint = readFIXED8();
        return ret;
    }

    /**
     * Reads one FILLSTYLE value from the stream
     *
     * @return FILLSTYLE value
     * @throws IOException
     */
    public FILLSTYLE readFILLSTYLE(int shapeNum) throws IOException {
        FILLSTYLE ret = new FILLSTYLE();
        ret.fillStyleType = readUI8();
        if (ret.fillStyleType == FILLSTYLE.SOLID) {
            if (shapeNum >= 3) {
                ret.colorA = readRGBA();
            } else {
                ret.color = readRGB();
            }
        }
        if ((ret.fillStyleType == FILLSTYLE.LINEAR_GRADIENT)
                || (ret.fillStyleType == FILLSTYLE.RADIAL_GRADIENT)
                || (ret.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT)) {
            ret.gradientMatrix = readMatrix();
        }
        if ((ret.fillStyleType == FILLSTYLE.LINEAR_GRADIENT)
                || (ret.fillStyleType == FILLSTYLE.RADIAL_GRADIENT)) {
            ret.gradient = readGRADIENT(shapeNum);
        }
        if (ret.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
            ret.focalGradient = readFOCALGRADIENT(shapeNum);
        }

        if ((ret.fillStyleType == FILLSTYLE.REPEATING_BITMAP)
                || (ret.fillStyleType == FILLSTYLE.CLIPPED_BITMAP)
                || (ret.fillStyleType == FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP)
                || (ret.fillStyleType == FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP)) {
            ret.bitmapId = readUI16();
            ret.bitmapMatrix = readMatrix();
        }
        return ret;
    }

    /**
     * Reads one FILLSTYLEARRAY value from the stream
     *
     * @return FILLSTYLEARRAY value
     * @throws IOException
     */
    public FILLSTYLEARRAY readFILLSTYLEARRAY(int shapeNum) throws IOException {

        FILLSTYLEARRAY ret = new FILLSTYLEARRAY();
        int fillStyleCount = readUI8();
        if (((shapeNum == 2) || (shapeNum == 3) || (shapeNum == 4/*?*/)) && (fillStyleCount == 0xff)) {
            fillStyleCount = readUI16();
        }
        ret.fillStyles = new FILLSTYLE[fillStyleCount];
        for (int i = 0; i < fillStyleCount; i++) {
            ret.fillStyles[i] = readFILLSTYLE(shapeNum);
        }
        return ret;
    }

    /**
     * Reads one LINESTYLE value from the stream
     *
     * @return LINESTYLE value
     * @throws IOException
     */
    public LINESTYLE readLINESTYLE(int shapeNum) throws IOException {
        LINESTYLE ret = new LINESTYLE();
        ret.width = readUI16();
        if ((shapeNum == 1) || (shapeNum == 2)) {
            ret.color = readRGB();
        }
        if (shapeNum == 3) {
            ret.colorA = readRGBA();
        }
        return ret;
    }

    /**
     * Reads one LINESTYLE2 value from the stream
     *
     * @return LINESTYLE2 value
     * @throws IOException
     */
    public LINESTYLE2 readLINESTYLE2(int shapeNum) throws IOException {
        LINESTYLE2 ret = new LINESTYLE2();
        ret.width = readUI16();
        ret.startCapStyle = (int) readUB(2);
        ret.joinStyle = (int) readUB(2);
        ret.hasFillFlag = (int) readUB(1) == 1;
        ret.noHScaleFlag = (int) readUB(1) == 1;
        ret.noVScaleFlag = (int) readUB(1) == 1;
        ret.pixelHintingFlag = (int) readUB(1) == 1;
        readUB(5);//reserved
        ret.noClose = (int) readUB(1) == 1;
        ret.endCapStyle = (int) readUB(2);
        if (ret.joinStyle == LINESTYLE2.MITER_JOIN) {
            ret.miterLimitFactor = readUI16();
        }
        if (!ret.hasFillFlag) {
            ret.color = readRGBA();
        } else {
            ret.fillType = readFILLSTYLE(shapeNum);
        }
        return ret;
    }

    /**
     * Reads one LINESTYLEARRAY value from the stream
     *
     * @return LINESTYLEARRAY value
     * @throws IOException
     */
    public LINESTYLEARRAY readLINESTYLEARRAY(int shapeNum) throws IOException {
        LINESTYLEARRAY ret = new LINESTYLEARRAY();
        int lineStyleCount = readUI8();
        if (lineStyleCount == 0xff) {
            lineStyleCount = readUI16();
        }
        if ((shapeNum == 1 || shapeNum == 2 || shapeNum == 3)) {
            ret.lineStyles = new LINESTYLE[lineStyleCount];
            for (int i = 0; i < lineStyleCount; i++) {
                ret.lineStyles[i] = readLINESTYLE(shapeNum);
            }
        } else if (shapeNum == 4) {
            ret.lineStyles2 = new LINESTYLE2[lineStyleCount];
            for (int i = 0; i < lineStyleCount; i++) {
                ret.lineStyles2[i] = readLINESTYLE2(shapeNum);
            }
        }
        return ret;
    }

    /**
     * Reads one SHAPERECORD value from the stream
     *
     * @return SHAPERECORD value
     * @throws IOException
     */
    public SHAPERECORD readSHAPERECORD(int fillBits, int lineBits, int shapeNum) throws IOException {
        SHAPERECORD ret;
        int typeFlag = (int) readUB(1);
        if (typeFlag == 0) {
            boolean stateNewStyles = readUB(1) == 1;
            boolean stateLineStyle = readUB(1) == 1;
            boolean stateFillStyle1 = readUB(1) == 1;
            boolean stateFillStyle0 = readUB(1) == 1;
            boolean stateMoveTo = readUB(1) == 1;
            if ((!stateNewStyles) && (!stateLineStyle) && (!stateFillStyle1) && (!stateFillStyle0) && (!stateMoveTo)) {
                ret = new EndShapeRecord();
            } else {
                StyleChangeRecord scr = new StyleChangeRecord();
                scr.stateNewStyles = stateNewStyles;
                scr.stateLineStyle = stateLineStyle;
                scr.stateFillStyle0 = stateFillStyle0;
                scr.stateFillStyle1 = stateFillStyle1;
                scr.stateMoveTo = stateMoveTo;
                if (stateMoveTo) {
                    scr.moveBits = (int) readUB(5);
                    scr.moveDeltaX = (int) readSB(scr.moveBits);
                    scr.moveDeltaY = (int) readSB(scr.moveBits);
                }
                if (stateFillStyle0) {
                    scr.fillStyle0 = (int) readUB(fillBits);
                }
                if (stateFillStyle1) {
                    scr.fillStyle1 = (int) readUB(fillBits);
                }
                if (stateLineStyle) {
                    scr.lineStyle = (int) readUB(lineBits);
                }
                if (stateNewStyles) {
                    scr.fillStyles = readFILLSTYLEARRAY(shapeNum);
                    scr.lineStyles = readLINESTYLEARRAY(shapeNum);
                    scr.numFillBits = (int) readUB(4);
                    scr.numLineBits = (int) readUB(4);
                }
                ret = scr;
            }
        } else {//typeFlag==1
            int straightFlag = (int) readUB(1);
            if (straightFlag == 1) {
                StraightEdgeRecord ser = new StraightEdgeRecord();
                ser.numBits = (int) readUB(4);
                ser.generalLineFlag = readUB(1) == 1;
                if (!ser.generalLineFlag) {
                    ser.vertLineFlag = readUB(1) == 1;
                }
                if (ser.generalLineFlag || (!ser.vertLineFlag)) {
                    ser.deltaX = (int) readSB(ser.numBits + 2);
                }
                if (ser.generalLineFlag || (ser.vertLineFlag)) {
                    ser.deltaY = (int) readSB(ser.numBits + 2);
                }
                ret = ser;
            } else {
                CurvedEdgeRecord cer = new CurvedEdgeRecord();
                cer.numBits = (int) readUB(4);
                cer.controlDeltaX = (int) readSB(cer.numBits + 2);
                cer.controlDeltaY = (int) readSB(cer.numBits + 2);
                cer.anchorDeltaX = (int) readSB(cer.numBits + 2);
                cer.anchorDeltaY = (int) readSB(cer.numBits + 2);
                ret = cer;
            }
        }
        return ret;
    }

    /**
     * Reads one SHAPE value from the stream
     *
     * @return SHAPE value
     * @throws IOException
     */
    public SHAPE readSHAPE(int shapeNum) throws IOException {
        SHAPE ret = new SHAPE();
        ret.numFillBits = (int) readUB(4);
        ret.numLineBits = (int) readUB(4);
        ret.shapeRecords = readSHAPERECORDS(shapeNum, ret.numFillBits, ret.numLineBits);
        return ret;
    }

    /**
     * Reads one SHAPEWITHSTYLE value from the stream
     *
     * @return SHAPEWITHSTYLE value
     * @throws IOException
     */
    public SHAPEWITHSTYLE readSHAPEWITHSTYLE(int shapeNum) throws IOException {
        SHAPEWITHSTYLE ret = new SHAPEWITHSTYLE();
        ret.fillStyles = readFILLSTYLEARRAY(shapeNum);
        ret.lineStyles = readLINESTYLEARRAY(shapeNum);
        ret.numFillBits = (int) readUB(4);
        ret.numLineBits = (int) readUB(4);
        ret.shapeRecords = readSHAPERECORDS(shapeNum, ret.numFillBits, ret.numLineBits);
        return ret;
    }

    /**
     * Reads list of SHAPERECORDs from the stream
     *
     * @return SHAPERECORDs array
     * @throws IOException
     */
    public List<SHAPERECORD> readSHAPERECORDS(int shapeNum, int fillBits, int lineBits) throws IOException {
        List<SHAPERECORD> ret = new ArrayList<SHAPERECORD>();
        SHAPERECORD rec;
        do {
            rec = readSHAPERECORD(fillBits, lineBits, shapeNum);
            if (rec instanceof StyleChangeRecord) {
                if (((StyleChangeRecord) rec).stateNewStyles) {
                    fillBits = ((StyleChangeRecord) rec).numFillBits;
                    lineBits = ((StyleChangeRecord) rec).numLineBits;
                }
            }
            ret.add(rec);
        } while (!(rec instanceof EndShapeRecord));
        alignByte();
        return ret;
    }

    /**
     * Reads one SOUNDINFO value from the stream
     *
     * @return SOUNDINFO value
     * @throws IOException
     */
    public SOUNDINFO readSOUNDINFO() throws IOException {
        SOUNDINFO ret = new SOUNDINFO();
        readUB(2);
        ret.syncStop = readUB(1) == 1;
        ret.syncNoMultiple = readUB(1) == 1;
        ret.hasEnvelope = readUB(1) == 1;
        ret.hasLoops = readUB(1) == 1;
        ret.hasOutPoint = readUB(1) == 1;
        ret.hasInPoint = readUB(1) == 1;
        if (ret.hasInPoint) {
            ret.inPoint = readUI32();
        }
        if (ret.hasOutPoint) {
            ret.outPoint = readUI32();
        }
        if (ret.hasLoops) {
            ret.loopCount = readUI16();
        }
        if (ret.hasEnvelope) {
            int envPoints = readUI8();
            ret.envelopeRecords = new SOUNDENVELOPE[envPoints];
            for (int i = 0; i < envPoints; i++) {
                ret.envelopeRecords[i] = readSOUNDENVELOPE();
            }
        }
        return ret;
    }

    /**
     * Reads one SOUNDENVELOPE value from the stream
     *
     * @return SOUNDENVELOPE value
     * @throws IOException
     */
    public SOUNDENVELOPE readSOUNDENVELOPE() throws IOException {
        SOUNDENVELOPE ret = new SOUNDENVELOPE();
        ret.pos44 = readUI32();
        ret.leftLevel = readUI16();
        ret.rightLevel = readUI16();
        return ret;
    }

    /**
     * Reads one GLYPHENTRY value from the stream
     *
     * @return GLYPHENTRY value
     * @throws IOException
     */
    public GLYPHENTRY readGLYPHENTRY(int glyphBits, int advanceBits) throws IOException {
        GLYPHENTRY ret = new GLYPHENTRY();
        ret.glyphIndex = (int) readUB(glyphBits);
        ret.glyphAdvance = (int) readUB(advanceBits);
        return ret;
    }

    /**
     * Reads one TEXTRECORD value from the stream
     *
     * @return TEXTRECORD value
     * @throws IOException
     */
    public TEXTRECORD readTEXTRECORD(boolean inDefineText2, int glyphBits, int advanceBits) throws IOException {
        TEXTRECORD ret = new TEXTRECORD();
        int first = (int) readUB(1); //always 1
        readUB(3); //always 0
        ret.styleFlagsHasFont = readUB(1) == 1;
        ret.styleFlagsHasColor = readUB(1) == 1;
        ret.styleFlagsHasYOffset = readUB(1) == 1;
        ret.styleFlagsHasXOffset = readUB(1) == 1;
        if ((!ret.styleFlagsHasFont) && (!ret.styleFlagsHasColor) && (!ret.styleFlagsHasYOffset) && (!ret.styleFlagsHasXOffset) && (first == 0)) { //final text record
            return null;
        }
        if (ret.styleFlagsHasFont) {
            ret.fontId = readUI16();
        }
        if (ret.styleFlagsHasColor) {
            if (inDefineText2) {
                ret.textColorA = readRGBA();
            } else {
                ret.textColor = readRGB();
            }
        }
        if (ret.styleFlagsHasXOffset) {
            ret.xOffset = readSI16();
        }
        if (ret.styleFlagsHasYOffset) {
            ret.yOffset = readSI16();
        }
        if (ret.styleFlagsHasFont) {
            ret.textHeight = readUI16();
        }
        int glyphCount = readUI8();
        ret.glyphEntries = new GLYPHENTRY[glyphCount];
        for (int i = 0; i < glyphCount; i++) {
            ret.glyphEntries[i] = readGLYPHENTRY(glyphBits, advanceBits);
        }
        alignByte();
        return ret;
    }

    /**
     * Reads one MORPHGRADRECORD value from the stream
     *
     * @return MORPHGRADRECORD value
     * @throws IOException
     */
    public MORPHGRADRECORD readMORPHGRADRECORD() throws IOException {
        MORPHGRADRECORD ret = new MORPHGRADRECORD();
        ret.startRatio = readUI8();
        ret.startColor = readRGBA();
        ret.endRatio = readUI8();
        ret.endColor = readRGBA();
        return ret;
    }

    /**
     * Reads one MORPHGRADIENT value from the stream
     *
     * @return MORPHGRADIENT value
     * @throws IOException
     */
    public MORPHGRADIENT readMORPHGRADIENT() throws IOException {
        MORPHGRADIENT ret = new MORPHGRADIENT();
        int numGradients = (int) readUI8();
        ret.gradientRecords = new MORPHGRADRECORD[numGradients];
        for (int i = 0; i < numGradients; i++) {
            ret.gradientRecords[i] = readMORPHGRADRECORD();
        }
        return ret;
    }

    /**
     * Reads one MORPHFILLSTYLE value from the stream
     *
     * @return MORPHFILLSTYLE value
     * @throws IOException
     */
    public MORPHFILLSTYLE readMORPHFILLSTYLE() throws IOException {
        MORPHFILLSTYLE ret = new MORPHFILLSTYLE();
        ret.fillStyleType = readUI8();
        if (ret.fillStyleType == MORPHFILLSTYLE.SOLID) {
            ret.startColor = readRGBA();
            ret.endColor = readRGBA();
        }
        if ((ret.fillStyleType == MORPHFILLSTYLE.LINEAR_GRADIENT)
                || (ret.fillStyleType == MORPHFILLSTYLE.RADIAL_GRADIENT)) {
            ret.startGradientMatrix = readMatrix();
            ret.endGradientMatrix = readMatrix();
        }
        if ((ret.fillStyleType == MORPHFILLSTYLE.LINEAR_GRADIENT)
                || (ret.fillStyleType == MORPHFILLSTYLE.RADIAL_GRADIENT)) {
            ret.gradient = readMORPHGRADIENT();
        }

        if ((ret.fillStyleType == MORPHFILLSTYLE.REPEATING_BITMAP)
                || (ret.fillStyleType == MORPHFILLSTYLE.CLIPPED_BITMAP)
                || (ret.fillStyleType == MORPHFILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP)
                || (ret.fillStyleType == MORPHFILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP)) {
            ret.bitmapId = readUI16();
            ret.startBitmapMatrix = readMatrix();
            ret.endBitmapMatrix = readMatrix();
        }
        return ret;
    }

    /**
     * Reads one MORPHFILLSTYLEARRAY value from the stream
     *
     * @return MORPHFILLSTYLEARRAY value
     * @throws IOException
     */
    public MORPHFILLSTYLEARRAY readMORPHFILLSTYLEARRAY() throws IOException {

        MORPHFILLSTYLEARRAY ret = new MORPHFILLSTYLEARRAY();
        int fillStyleCount = readUI8();
        if (fillStyleCount == 0xff) {
            fillStyleCount = readUI16();
        }
        ret.fillStyles = new MORPHFILLSTYLE[fillStyleCount];
        for (int i = 0; i < fillStyleCount; i++) {
            ret.fillStyles[i] = readMORPHFILLSTYLE();
        }
        return ret;
    }

    /**
     * Reads one MORPHLINESTYLE value from the stream
     *
     * @return MORPHLINESTYLE value
     * @throws IOException
     */
    public MORPHLINESTYLE readMORPHLINESTYLE() throws IOException {
        MORPHLINESTYLE ret = new MORPHLINESTYLE();
        ret.startWidth = readUI16();
        ret.endWidth = readUI16();
        ret.startColor = readRGBA();
        ret.endColor = readRGBA();
        return ret;
    }

    /**
     * Reads one MORPHLINESTYLE2 value from the stream
     *
     * @return MORPHLINESTYLE2 value
     * @throws IOException
     */
    public MORPHLINESTYLE2 readMORPHLINESTYLE2() throws IOException {
        MORPHLINESTYLE2 ret = new MORPHLINESTYLE2();
        ret.startWidth = readUI16();
        ret.endWidth = readUI16();
        ret.startCapStyle = (int) readUB(2);
        ret.joinStyle = (int) readUB(2);
        ret.hasFillFlag = (int) readUB(1) == 1;
        ret.noHScaleFlag = (int) readUB(1) == 1;
        ret.noVScaleFlag = (int) readUB(1) == 1;
        ret.pixelHintingFlag = (int) readUB(1) == 1;
        readUB(5);//reserved
        ret.noClose = (int) readUB(1) == 1;
        ret.endCapStyle = (int) readUB(2);
        if (ret.joinStyle == LINESTYLE2.MITER_JOIN) {
            ret.miterLimitFactor = readUI16();
        }
        if (!ret.hasFillFlag) {
            ret.startColor = readRGBA();
            ret.endColor = readRGBA();
        } else {
            ret.fillType = readMORPHFILLSTYLE();
        }
        return ret;
    }

    /**
     * Reads one MORPHLINESTYLEARRAY value from the stream
     *
     * @return MORPHLINESTYLEARRAY value
     * @throws IOException
     */
    public MORPHLINESTYLEARRAY readMORPHLINESTYLEARRAY(int morphShapeNum) throws IOException {
        MORPHLINESTYLEARRAY ret = new MORPHLINESTYLEARRAY();
        int lineStyleCount = readUI8();
        if (lineStyleCount == 0xff) {
            lineStyleCount = readUI16();
        }
        if (morphShapeNum == 1) {
            ret.lineStyles = new MORPHLINESTYLE[lineStyleCount];
            for (int i = 0; i < lineStyleCount; i++) {
                ret.lineStyles[i] = readMORPHLINESTYLE();
            }
        } else if (morphShapeNum == 2) {
            ret.lineStyles2 = new MORPHLINESTYLE2[lineStyleCount];
            for (int i = 0; i < lineStyleCount; i++) {
                ret.lineStyles2[i] = readMORPHLINESTYLE2();
            }
        }
        return ret;
    }

    /**
     * Reads one KERNINGRECORD value from the stream
     *
     * @return KERNINGRECORD value
     * @throws IOException
     */
    public KERNINGRECORD readKERNINGRECORD(boolean fontFlagsWideCodes) throws IOException {
        KERNINGRECORD ret = new KERNINGRECORD();
        if (fontFlagsWideCodes) {
            ret.fontKerningCode1 = readUI16();
            ret.fontKerningCode2 = readUI16();
        } else {
            ret.fontKerningCode1 = readUI8();
            ret.fontKerningCode2 = readUI8();
        }
        ret.fontKerningAdjustment = readSI16();
        return ret;
    }

    /**
     * Reads one LANGCODE value from the stream
     *
     * @return LANGCODE value
     * @throws IOException
     */
    public LANGCODE readLANGCODE() throws IOException {
        LANGCODE ret = new LANGCODE();
        ret.languageCode = readUI8();
        return ret;
    }

    /**
     * Reads one ZONERECORD value from the stream
     *
     * @return ZONERECORD value
     * @throws IOException
     */
    public ZONERECORD readZONERECORD() throws IOException {
        ZONERECORD ret = new ZONERECORD();
        int numZoneData = readUI8();
        ret.zonedata = new ZONEDATA[numZoneData];
        for (int i = 0; i < numZoneData; i++) {
            ret.zonedata[i] = readZONEDATA();
        }
        readUB(6);
        ret.zoneMaskX = readUB(1) == 1;
        ret.zoneMaskY = readUB(1) == 1;
        return ret;
    }

    /**
     * Reads one ZONEDATA value from the stream
     *
     * @return ZONEDATA value
     * @throws IOException
     */
    public ZONEDATA readZONEDATA() throws IOException {
        ZONEDATA ret = new ZONEDATA();
        ret.alignmentCoordinate = readUI16();
        ret.range = readUI16();
        return ret;
    }

    /**
     * Reads one PIX15 value from the stream
     *
     * @return PIX15 value
     * @throws IOException
     */
    public PIX15 readPIX15() throws IOException {
        PIX15 ret = new PIX15();
        readUB(1);
        ret.red = (int) readUB(5);
        ret.green = (int) readUB(5);
        ret.blue = (int) readUB(5);
        return ret;
    }

    /**
     * Reads one PIX24 value from the stream
     *
     * @return PIX24 value
     * @throws IOException
     */
    public PIX24 readPIX24() throws IOException {
        PIX24 ret = new PIX24();
        readUI8();
        ret.red = readUI8();
        ret.green = readUI8();
        ret.blue = readUI8();
        return ret;
    }

    /**
     * Reads one COLORMAPDATA value from the stream
     *
     * @return COLORMAPDATA value
     * @throws IOException
     */
    public COLORMAPDATA readCOLORMAPDATA(int colorTableSize, int bitmapWidth, int bitmapHeight) throws IOException {
        COLORMAPDATA ret = new COLORMAPDATA();
        ret.colorTableRGB = new RGB[colorTableSize + 1];
        for (int i = 0; i < colorTableSize + 1; i++) {
            ret.colorTableRGB[i] = readRGB();
        }
        int dataLen = 0;
        for (int y = 0; y < bitmapHeight; y++) {
            int x = 0;
            for (; x < bitmapWidth; x++) {
                dataLen++;
            }
            while ((x % 4) != 0) {
                dataLen++;
                x++;
            }
        }
        ret.colorMapPixelData = readBytes(dataLen);
        return ret;
    }

    /**
     * Reads one BITMAPDATA value from the stream
     *
     * @return COLORMAPDATA value
     * @throws IOException
     */
    public BITMAPDATA readBITMAPDATA(int bitmapFormat, int bitmapWidth, int bitmapHeight) throws IOException {
        BITMAPDATA ret = new BITMAPDATA();
        List<PIX15> pix15 = new ArrayList<PIX15>();
        List<PIX24> pix24 = new ArrayList<PIX24>();
        int dataLen = 0;
        for (int y = 0; y < bitmapHeight; y++) {
            int x = 0;
            for (; x < bitmapWidth; x++) {
                if (bitmapFormat == DefineBitsLosslessTag.FORMAT_15BIT_RGB) {
                    dataLen += 2;
                    pix15.add(readPIX15());
                }
                if (bitmapFormat == DefineBitsLosslessTag.FORMAT_24BIT_RGB) {
                    dataLen += 4;
                    pix24.add(readPIX24());
                }
            }
            while ((dataLen % 4) != 0) {
                dataLen++;
                readUI8();
            }
        }
        if (bitmapFormat == DefineBitsLosslessTag.FORMAT_15BIT_RGB) {
            ret.bitmapPixelDataPix15 = pix15.toArray(new PIX15[pix15.size()]);
        } else if (bitmapFormat == DefineBitsLosslessTag.FORMAT_24BIT_RGB) {
            ret.bitmapPixelDataPix24 = pix24.toArray(new PIX24[pix24.size()]);
        }
        return ret;
    }

    /**
     * Reads one BITMAPDATA value from the stream
     *
     * @return COLORMAPDATA value
     * @throws IOException
     */
    public ALPHABITMAPDATA readALPHABITMAPDATA(int bitmapFormat, int bitmapWidth, int bitmapHeight) throws IOException {
        ALPHABITMAPDATA ret = new ALPHABITMAPDATA();
        ret.bitmapPixelData = new ARGB[bitmapWidth * bitmapHeight];
        for (int y = 0; y < bitmapHeight; y++) {
            for (int x = 0; x < bitmapWidth; x++) {
                ret.bitmapPixelData[y * bitmapWidth + x] = readARGB();
            }
        }
        return ret;
    }

    /**
     * Reads one ALPHACOLORMAPDATA value from the stream
     *
     * @return ALPHACOLORMAPDATA value
     * @throws IOException
     */
    public ALPHACOLORMAPDATA readALPHACOLORMAPDATA(int colorTableSize, int bitmapWidth, int bitmapHeight) throws IOException {
        ALPHACOLORMAPDATA ret = new ALPHACOLORMAPDATA();
        ret.colorTableRGB = new RGBA[colorTableSize + 1];
        for (int i = 0; i < colorTableSize + 1; i++) {
            ret.colorTableRGB[i] = readRGBA();
        }
        int dataLen = 0;
        for (int y = 0; y < bitmapHeight; y++) {
            int x = 0;
            for (; x < bitmapWidth; x++) {
                dataLen++;
            }
            while ((x % 4) != 0) {
                dataLen++;
                x++;
            }
        }
        ret.colorMapPixelData = readBytes(dataLen);
        return ret;
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }
}
