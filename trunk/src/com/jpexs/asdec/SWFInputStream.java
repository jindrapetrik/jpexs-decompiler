/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.swf3.*;
import com.jpexs.asdec.action.swf4.*;
import com.jpexs.asdec.action.swf5.*;
import com.jpexs.asdec.action.swf6.*;
import com.jpexs.asdec.action.swf7.*;
import com.jpexs.asdec.tags.*;
import com.jpexs.asdec.types.*;
import com.jpexs.asdec.types.filters.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Class for reading data from SWF file
 *
 * @author JPEXS
 */
public class SWFInputStream extends InputStream {
    private InputStream is;
    private Stack<Integer> margedPos = new Stack<Integer>();
    private long pos = 0;
    private int version;

    /**
     * Constructor
     *
     * @param is      Existing inputstream
     * @param version Version of SWF to read
     */
    public SWFInputStream(InputStream is, int version) {
        this.version = version;
        this.is = is;
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
        pos++;
        bitPos = 0;
        return is.read();
    }

    private void alignByte() {
        bitPos = 0;
    }

    private int readNoBitReset() throws IOException {
        pos++;
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
        int r = 0;
        while (true) {
            r = read();
            if (r <= 0) return new String(baos.toByteArray(), "utf8");
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
        return (((long) readBuffer[3] << 56) +
                ((long) (readBuffer[2] & 255) << 48) +
                ((long) (readBuffer[1] & 255) << 40) +
                ((long) (readBuffer[0] & 255) << 32) +
                ((long) (readBuffer[7] & 255) << 24) +
                ((readBuffer[6] & 255) << 16) +
                ((readBuffer[5] & 255) << 8) +
                ((readBuffer[4] & 255) << 0));
    }

    /**
     * Reads one DOUBLE (double precision floating point value) value from the stream
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
     * Reads one FLOAT (single precision floating point value) value from the stream
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
        if(count<=0) return new byte[0];
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
        if (nBits == 0) return 0;
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
        int uval = (int)readUB(nBits);

        int shift = 32-nBits;
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
    public double readFB(int nBits) throws IOException {

        double val = readSB(nBits);
        double ret = val / (1 << 16);
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
        return ret;
    }

    /**
     * Reads list of actions from the stream. Reading ends with ActionEndFlag(=0) or end of the stream.
     *
     * @return List of actions
     * @throws IOException
     */
    public List<Action> readActionList() throws IOException {
        List<Action> ret = new ArrayList<Action>();
        Action a;
        while ((a = readAction()) != null) {
            ret.add(a);
        }
        return ret;
    }

    /**
     * Reads list of tags from the stream. Reading ends with End tag(=0) or end of the stream.
     *
     * @return List of tags
     * @throws IOException
     */
    public List<Tag> readTagList() throws IOException {
        List<Tag> tags = new ArrayList<Tag>();
        Tag tag;
        while ((tag = readTag()) != null) {
            tags.add(tag);
        }
        return tags;
    }

    /**
     * Reads one Tag from the stream
     *
     * @return Tag or null when End tag
     * @throws IOException
     */
    public Tag readTag() throws IOException {
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
        switch (tagID) {
            case 82:
                ret = new DoABCTag(data, version);
                break;
            case 12:
                ret = new DoActionTag(data, version);
                break;
            case 59:
                ret = new DoInitActionTag(data, version);
                break;
            case 39:
                ret = new DefineSpriteTag(data, version);
                break;
            case 1:
                ret = new ShowFrameTag();
                break;
            case 26:
                ret = new PlaceObject2Tag(data, version);
                break;
            case 56:
                ret = new ExportAssetsTag(data, version);
                break;
            case 70:
                ret = new PlaceObject3Tag(data, version);
                break;
            case 7:
                ret = new DefineButtonTag(data, version);
                break;
            case 34:
                ret = new DefineButton2Tag(data, version);
                break;
            case 69:
            	ret = new FileAttributes(data);
            	break;
            case 77:
            	ret = new Metadata(data);
            	break;
            case 65:
            	ret = new ScriptLimits(data, version);
            	break;
            case 9:
            	ret = new SetBackgroundColor(data);
            	break;
            case 41:
            	ret = new ProductInfo(data, version);
            	break;
            case 43:
            	ret = new FrameLabel(data, version);
            	break;
            case 36:
            	ret = new DefineBitsLossless2(data, version);
            	break;
            case 76:
            	ret = new SymbolClass(data, version);
            	break;
            case 32:
            	ret = new DefineShape3(data, version);
            	break;
            case 28:
            	ret = new RemoveObject2(data, version);
            	break;
            case 78:
            	ret = new DefineScalingGrid(data, version);
            	break;
            case 2:
            	ret = new DefineShape(data, version);
            	break;
            case 22:
            	ret = new DefineShape2(data, version);
            	break;
            case 83:
            	ret = new DefineShape4(data, version);
            	break;
            case 20:
            	ret = new DefineBitsLossless(data, version);
            	break;
            case 35:
            	ret = new DefineBitsJPEG3(data, version);
            	break;
            case 87:
            	ret = new DefineBinaryData(data, version);
            	break;
            case 8:
            	ret = new JPEGTables(data);
            	break;
            case 6:
            	ret = new DefineBits(data, version);
            	break;
            case 21:
            	ret = new DefineBitsJPEG2(data, version);
            	break;
            case 75:
            	ret = new DefineFont3(data, version);
            	break;
            case 73:
            	ret = new DefineFontAlignZones(data, version);
            	break;
            case 88:
            	ret = new DefineFontName(data, version);
            	break;            	
            case 91:
            	ret = new DefineFont4(data, version);
            	break;
            default:
                ret = new Tag(tagID, data);
        }
        ret.forceWriteAsLong = readLong;
        return ret;
    }

    /**
     * Reads one Action from the stream
     *
     * @return Action or null when ActionEndFlag or end of the stream
     * @throws IOException
     */
    public Action readAction() throws IOException {
        {
            int actionCode = readUI8();
            if (actionCode == 0) return null;
            if (actionCode == -1) return null;
            int actionLength = 0;
            if (actionCode >= 0x80) {
                actionLength = readUI16();
            }
            switch (actionCode) {
                //SWF3 Actions
                case 0x81:
                    return new ActionGotoFrame(this);
                case 0x83:
                    return new ActionGetURL(actionLength, this,version);
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
                    return new ActionSetTarget(actionLength, this,version);
                case 0x8C:
                    return new ActionGoToLabel(actionLength, this,version);
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
                    return new ActionConstantPool(actionLength, this,version);
                case 0x9B:
                    return new ActionDefineFunction(actionLength, this, version);
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
                    return new ActionWith(this, version);
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
                    return new ActionDefineFunction2(actionLength, this, version);
                case 0x69:
                    return new ActionExtends();
                case 0x2B:
                    return new ActionCastOp();
                case 0x2C:
                    return new ActionImplementsOp();
                case 0x8F:
                    return new ActionTry(actionLength, this, version);
                case 0x2A:
                    return new ActionThrow();
                default:
                    if (actionLength > 0) skip(actionLength);
                    return new Action(actionCode, actionLength);
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
            ret.scaleNBits = NScaleBits;

            ret.scaleX = readFB(NScaleBits);
            ret.scaleY = readFB(NScaleBits);
        }
        ret.hasRotate = readUB(1) == 1;
        if (ret.hasRotate) {
            int NRotateBits = (int) readUB(5);
            ret.rotateNBits = NRotateBits;
            ret.rotateSkew0 = readFB(NRotateBits);
            ret.rotateSkew1 = readFB(NRotateBits);
        }
        int NTranslateBits = (int) readUB(5);
        ret.translateNBits = NTranslateBits;
        ret.translateX = (int)readSB(NTranslateBits);
        ret.translateY = (int)readSB(NTranslateBits);
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
        CLIPACTIONRECORD ret = new CLIPACTIONRECORD();
        ret.eventFlags = readCLIPEVENTFLAGS();
        if (ret.eventFlags.isClear()) return null;
        long actionRecordSize = readUI32();
        if (ret.eventFlags.clipEventKeyPress) {
            ret.keyCode = readUI8();
            actionRecordSize--;
        }
        ret.actionBytes=readBytes(actionRecordSize);
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
     * @param inDefineButton2 Whether read from inside of DefineButton2Tag or not
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

        if (!ret.buttonHasBlendMode)
            if (!ret.buttonHasFilterList)
                if (!ret.buttonStateHitTest)
                    if (!ret.buttonStateDown)
                        if (!ret.buttonStateOver)
                            if (!ret.buttonStateUp)
                                if (res == 0)
                                    return null;

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
        BUTTONCONDACTION ret = new BUTTONCONDACTION();
        int condActionSize = readUI16();
        ret.isLast = condActionSize <= 0;
        ret.condIdleToOverDown = readUB(1) == 1;
        ret.condOutDownToIdle = readUB(1) == 1;
        ret.condOutDownToOverDown = readUB(1) == 1;
        ret.condOverDownToOutDown = readUB(1) == 1;
        ret.condOverDownToOverUp = readUB(1) == 1;
        ret.condOverUpToOverDown = readUB(1) == 1;
        ret.condOverUpToIddle = readUB(1) == 1;
        ret.condIdleToOverUp = readUB(1) == 1;
        ret.condKeyPress = (int) readUB(7);
        ret.condOverDownToIddle = readUB(1) == 1;
        if(condActionSize<=0){
            ret.actionBytes=readBytes(available());
        }else{
            ret.actionBytes=readBytes(condActionSize-4);
        }
        //ret.actions = readActionList();
        return ret;
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }



}
