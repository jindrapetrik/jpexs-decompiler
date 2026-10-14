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
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.iggy.conversion.*;
import com.jpexs.decompiler.flash.iggy.streams.*;
import com.jpexs.decompiler.flash.profiles.IggySwfProfile;
import com.jpexs.decompiler.flash.tags.*;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.shaperecords.*;
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.types.*;
import com.jpexs.helpers.ByteArrayRange;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class IggyFontConversionTest {
    @DataProvider(name="layouts") public Object[][] layouts() { return new Object[][]{{0},{1},{2}}; }
    private static final class Fixture {
        final MemoryDataStream data = new MemoryDataStream();
        final List<IggyFixup.Field> fields = new ArrayList<>();
        final boolean wide;
        final int layout;
        long font, sprite, text, records, binary, payload;

        Fixture(int layout) {
            this.layout = layout;
            wide = layout == 2;
        }

        void put(long at, long value, int width, int operation) throws Exception {
            long end = data.position();
            data.seek(at, SeekMode.SET);
            if (width == 8) { data.writeUI64(value); }
            else if (width == 4) { data.writeUI32(value); }
            else if (width == 2) { data.writeUI16((int) value); }
            else { data.writeUI8((int) value); }
            data.seek(end, SeekMode.SET);
            if (operation != 0) { fields.add(new IggyFixup.Field(at, operation)); }
        }

        void u8(long at, int value) throws Exception { put(at, value, 1, 0); }
        void u16(long at, int value) throws Exception { put(at, value, 2, 4); }
        void u32(long at, long value) throws Exception { put(at, value, 4, 5); }
        void f32(long at, float value) throws Exception { u32(at, Float.floatToIntBits(value)); }
        void ref(long at, long target) throws Exception { put(at, target == 0 ? 1 : target - at, wide ? 8 : 4, 2); }
        long allocate(int size) throws Exception {
            data.pad8bytes();
            long at = data.position();
            data.writeBytes(new byte[size]);
            return at;
        }

        byte[] file() throws Exception {
            byte[] main = data.getAllBytes();
            IggyIndexBuilder index = new IggyIndexBuilder();
            IggyFixup.writeFields(index, fields, main.length);
            byte[] program = index.getIndexBytes();
            MemoryDataStream out = new MemoryDataStream();
            new IggyHeader(0x900, 1, wide ? 64 : 32, 1, 0, 0, new byte[12], 2).writeToDataStream(out);
            new IggySubFileEntry(1, main.length, 0, 64).writeToDataStream(out);
            new IggySubFileEntry(0, program.length, 0, 64 + main.length).writeToDataStream(out);
            out.writeBytes(main);
            out.writeBytes(program);
            return out.getAllBytes();
        }
    }


    private static Fixture fixture(int layout, int type, boolean hasLayout) throws Exception {
        Fixture f = new Fixture(layout); boolean w = f.wide; int p = w ? 8 : 4;
        f.allocate(w ? 184 : layout == 1 ? 128 : 64);
        for (int at : w ? new int[]{0,8,16,24,32,40,112,120,136,144,152,160}
                : layout == 1 ? new int[]{0,4,8,12,16,20,80,84,96,100,104,108} : new int[]{0,8,12,44,56,60}) f.ref(at,0);
        f.u32(w?64:layout==1?40:4,2); f.u32(w?68:layout==1?44:32,1);
        if (layout != 0) f.f32(w?88:64,24);
        long dict = f.allocate(2*p); f.ref(0,dict); f.ref(dict,0);
        f.font = f.allocate(w?352:layout==1?216:208); f.u8(f.font,22); f.u16(f.font+2,1); f.ref(dict+p,f.font);
        for (int off : w ? new int[]{16,48,56,64,96,128} : new int[]{4,8,12,28,32,36,60,68,76,84,104}) f.ref(f.font+off,0);
        int h = w?32:16; f.u16(f.font+h,2); f.u16(f.font+h+2,19001); f.u16(f.font+h+4,4097); f.u16(f.font+h+6,-27);
        f.put(f.font+h+8,0x07000000L | (hasLayout?0x10000:0) | 0x400 | type, w?8:4, w?6:5);
        f.u8(f.font+(w?152:88),1);f.u8(f.font+(w?153:89),1);f.u32(f.font+(w?164:100),2);
        long name=f.allocate(10);for(int i=0;i<5;i++)f.u16(name+2*i,i<4?"Test".charAt(i):0);f.ref(f.font+(w?128:76),name);
        long desc=f.allocate(w?64:32);f.ref(f.font+(w?48:28),desc);f.u16(desc+(w?8:4),1);f.ref(desc+(w?24:12),0);f.ref(desc+(w?56:28),0);
        long codes=f.allocate(4);f.u16(codes,65);f.u16(codes+2,66);f.ref(f.font+(w?56:32),codes);
        if (hasLayout) {
            long advance=f.allocate(8);int units=type==3?20480:1024;f.f32(advance,10241f/units);f.f32(advance+4,5120f/units);f.ref(f.font+(w?64:36),advance);
            long kern=f.allocate(6);f.u16(kern,65);f.u16(kern+2,66);f.u16(kern+4,-37);f.ref(f.font+(w?96:60),kern);f.u32(f.font+(w?72:40),1);
        }
        long glyph=f.allocate(w?64:40);f.ref(desc+(w?24:12),glyph);
        for(int i=0;i<4;i++)f.f32(glyph+4*i,i<2?0:1);
        f.put(glyph+(w?24:20),3,w?8:4,w?6:5);
        if(w){f.u32(glyph+56,1);f.u32(glyph+60,2);}
        for(int off:w?new int[]{32,40,48}:new int[]{24,28,32})f.ref(glyph+off,0);
        long nodes=f.allocate(72);if(w)f.put(glyph+16,0,8,0);else f.ref(glyph+16,nodes);
        int units=type==3?20480:1024;
        int[][] positions={{17,-23,0,0,1},{11643,4979,0,0,2},{11849,5021,11717,4997,3}};
        for(int i=0;i<3;i++){long n=nodes+24*i;for(int j=0;j<4;j++)f.f32(n+4*j,positions[i][j]/(float)units);f.u8(n+16,positions[i][4]);}
        long show=f.allocate(w?16:8);f.ref(show,0);f.u8(show+p,1);f.ref(w?16:8,show);f.ref(w?24:12,show);
        return f;
    }

    private static IggyFile load(byte[] bytes) throws Exception {
        Path path=Files.createTempFile("iggy-font-", ".iggy");
        try {Files.write(path,bytes);return new IggyFile(path.toFile());} finally {Files.delete(path);}
    }
    private static IggyFile reload(IggyFile file) throws Exception {
        file.updateFlashEntry();MemoryDataStream out=new MemoryDataStream();file.writeToDataStream(out);return load(out.getAllBytes());
    }
    private static SWF parse(SWF swf) throws Exception {
        ByteArrayOutputStream out=new ByteArrayOutputStream();swf.saveTo(out);return new SWF(new ByteArrayInputStream(out.toByteArray()),false,false,"UTF-8",new IggySwfProfile());
    }
    private static FontTag font(SWF swf) {for(Tag tag:swf.getTags())if(tag instanceof FontTag)return (FontTag)tag;throw new AssertionError();}
    private static SWF roundTrip(IggyFile file, SWF swf) throws Exception {
        SwfToIggyConvertor.updateIggy(file.getSwf(),parse(swf));return parse(IggyToSwfConvertor.getSwf(reload(file)));
    }
    private static void replace(SWF swf, FontTag old, FontTag value) {swf.removeTag(old);swf.addTag(1,value);}

    @Test(dataProvider="layouts") public void readsNativeFont3WithoutLosingPrecision(int layout) throws Exception {
        IggyFile file=load(fixture(layout,3,true).file());SWF swf=parse(IggyToSwfConvertor.getSwf(file));
        DefineFont3Tag font=(DefineFont3Tag)font(swf);Assert.assertTrue(font.fontFlagsBold);Assert.assertTrue(font.fontFlagsItalic);
        Assert.assertTrue(font.fontFlagsSmallText);Assert.assertTrue(font.fontFlagsANSI);Assert.assertTrue(font.fontFlagsShiftJIS);Assert.assertEquals(font.languageCode.languageCode,4);
        Assert.assertEquals(font.fontAscent,19001);Assert.assertEquals(font.fontDescent,4097);Assert.assertEquals(font.fontLeading,-27);
        Assert.assertEquals(font.fontAdvanceTable,Arrays.asList(10241,5120));Assert.assertEquals(font.fontKerningTable.get(0).fontKerningAdjustment,-37);
        List<SHAPERECORD> shape=font.glyphShapeTable.get(0).shapeRecords;StyleChangeRecord move=(StyleChangeRecord)shape.get(0);
        Assert.assertEquals(move.moveDeltaX,17);Assert.assertEquals(move.moveDeltaY,-23);StraightEdgeRecord line=(StraightEdgeRecord)shape.get(1);Assert.assertEquals(line.deltaX,11626);Assert.assertEquals(line.deltaY,5002);
        CurvedEdgeRecord curve=(CurvedEdgeRecord)shape.get(2);Assert.assertEquals(curve.controlDeltaX,74);Assert.assertEquals(curve.anchorDeltaX,132);
    }
    @Test(dataProvider="layouts") public void font2UsesItsOwnMetricUnits(int layout) throws Exception {
        IggyFile file=load(fixture(layout,2,true).file());SWF swf=parse(IggyToSwfConvertor.getSwf(file));DefineFont2Tag font=(DefineFont2Tag)font(swf);
        Assert.assertEquals(font.fontAscent,19001);Assert.assertEquals(font.fontLeading,-27);Assert.assertEquals(font.fontKerningTable.get(0).fontKerningAdjustment,-37);
        font.fontAscent=18123;font.fontLeading=-31;font.fontAdvanceTable.set(0,10713);font.fontKerningTable.add(new KERNINGRECORD(66,65,-43));font.setModified(true);
        DefineFont2Tag actual=(DefineFont2Tag)font(roundTrip(file,swf));Assert.assertEquals(actual.fontAscent,18123);Assert.assertEquals(actual.fontLeading,-31);Assert.assertEquals(actual.fontAdvanceTable.get(0).intValue(),10713);Assert.assertEquals(actual.fontKerningTable.size(),2);
    }
    @Test(dataProvider="layouts") public void unchangedFontsRetainNativeObjects(int layout) throws Exception {
        IggyFile file=load(fixture(layout,3,true).file());IggyFont original=file.getSwf().getFonts().get(0);IggyShape glyph=original.getChars().get(0);IggyCharOffset descriptor=original.getCharOffsets().get(0);
        SWF swf=parse(IggyToSwfConvertor.getSwf(file));byte[] before=font(swf).getData();SwfToIggyConvertor.updateIggy(file.getSwf(),swf);
        Assert.assertSame(original.getChars().get(0),glyph);Assert.assertSame(original.getCharOffsets().get(0),descriptor);Assert.assertNull(original.getChars().get(1));
        for(int i=0;i<3;i++){file=reload(file);swf=parse(IggyToSwfConvertor.getSwf(file));Assert.assertEquals(font(swf).getData(),before);SwfToIggyConvertor.updateIggy(file.getSwf(),swf);}
    }
    @Test(dataProvider="layouts") public void editsFont3AndRetainsUnchangedGlyphs(int layout) throws Exception {
        IggyFile file=load(fixture(layout,3,true).file());IggyFont original=file.getSwf().getFonts().get(0);IggyShape glyph=original.getChars().get(0);IggyCharOffset descriptor=original.getCharOffsets().get(0);
        SWF swf=parse(IggyToSwfConvertor.getSwf(file));DefineFont3Tag tag=(DefineFont3Tag)font(swf);tag.fontName="Changed";tag.fontFlagsBold=false;tag.fontFlagsItalic=false;tag.fontFlagsANSI=false;tag.fontFlagsSmallText=false;tag.fontFlagsShiftJIS=false;tag.languageCode.languageCode=2;tag.fontAscent=18731;tag.fontLeading=-59;tag.fontAdvanceTable.set(1,5123);tag.fontKerningTable.clear();tag.setModified(true);
        SwfToIggyConvertor.updateIggy(file.getSwf(),parse(swf));Assert.assertSame(original.getChars().get(0),glyph);Assert.assertSame(original.getCharOffsets().get(0),descriptor);
        IggyFile actual=reload(file);IggyFont nativeFont=actual.getSwf().getFonts().get(0);Assert.assertFalse(nativeFont.isBold());Assert.assertFalse(nativeFont.isItalic());Assert.assertEquals(nativeFont.getLeading(),-59);
        DefineFont3Tag result=(DefineFont3Tag)font(parse(IggyToSwfConvertor.getSwf(actual)));Assert.assertEquals(result.getData(),tag.getData());Assert.assertTrue(result.fontKerningTable.isEmpty());
    }
    @Test(dataProvider="layouts") public void editsGlyphCoordinatesWithoutRoundingViaFont2(int layout) throws Exception {
        IggyFile file=load(fixture(layout,3,true).file());SWF swf=parse(IggyToSwfConvertor.getSwf(file));DefineFont3Tag tag=(DefineFont3Tag)font(swf);((StyleChangeRecord)tag.glyphShapeTable.get(0).shapeRecords.get(0)).moveDeltaX=19;tag.glyphShapeTable.get(0).clearCachedOutline();tag.setModified(true);
        DefineFont3Tag result=(DefineFont3Tag)font(roundTrip(file,swf));Assert.assertEquals(((StyleChangeRecord)result.glyphShapeTable.get(0).shapeRecords.get(0)).moveDeltaX,19);Assert.assertEquals(result.fontLeading,-27);
    }
    @Test(dataProvider="layouts") public void supportsFontsWithoutLayout(int layout) throws Exception {
        IggyFile file=load(fixture(layout,3,false).file());SWF swf=parse(IggyToSwfConvertor.getSwf(file));DefineFont3Tag tag=(DefineFont3Tag)font(swf);Assert.assertFalse(tag.fontFlagsHasLayout);tag.fontName="No layout";tag.setModified(true);
        DefineFont3Tag result=(DefineFont3Tag)font(roundTrip(file,swf));Assert.assertEquals(result.fontName,"No layout");Assert.assertFalse(result.fontFlagsHasLayout);Assert.assertNull(file.getSwf().getFonts().get(0).getCharAdvances());
    }
    @Test(dataProvider="layouts") public void importsLegacyFontAndFontInfo2(int layout) throws Exception {
        IggyFile file=load(fixture(layout,2,true).file());SWF swf=parse(IggyToSwfConvertor.getSwf(file));FontTag old=font(swf);DefineFontTag legacy=new DefineFontTag(swf);legacy.fontId=1;legacy.glyphShapeTable=old.getGlyphShapeTable();replace(swf,old,legacy);
        DefineFontInfo2Tag info=new DefineFontInfo2Tag(swf);info.fontID=1;info.fontName="Legacy";info.fontFlagsBold=true;info.fontFlagsItalic=true;info.fontFlagsSmallText=true;info.fontFlagsANSI=true;info.languageCode.languageCode=3;info.fontFlagsWideCodes=true;info.codeTable=Arrays.asList(67,68);swf.addTag(2,info);
        DefineFont2Tag result=(DefineFont2Tag)font(roundTrip(file,swf));Assert.assertFalse(result.fontFlagsHasLayout);Assert.assertEquals(result.fontName,"Legacy");Assert.assertEquals(result.codeTable,Arrays.asList(67,68));Assert.assertTrue(result.fontFlagsBold);Assert.assertTrue(result.fontFlagsItalic);Assert.assertTrue(result.fontFlagsSmallText);Assert.assertTrue(result.fontFlagsANSI);Assert.assertEquals(result.languageCode.languageCode,3);
        Assert.assertEquals(file.getSwf().getFonts().get(0).getFontType(),1);
    }
    @Test(dataProvider="layouts") public void importsLegacyFontInfo(int layout) throws Exception {
        IggyFile file=load(fixture(layout,2,true).file());SWF swf=parse(IggyToSwfConvertor.getSwf(file));FontTag old=font(swf);DefineFontTag legacy=new DefineFontTag(swf);legacy.fontId=1;legacy.glyphShapeTable=old.getGlyphShapeTable();replace(swf,old,legacy);
        DefineFontInfoTag info=new DefineFontInfoTag(swf);info.fontID=1;info.fontName="Legacy1";info.fontFlagsShiftJIS=true;info.fontFlagsSmallText=true;info.codeTable=Arrays.asList(70,71);swf.addTag(2,info);
        DefineFont2Tag result=(DefineFont2Tag)font(roundTrip(file,swf));Assert.assertEquals(result.codeTable,Arrays.asList(70,71));Assert.assertTrue(result.fontFlagsShiftJIS);Assert.assertTrue(result.fontFlagsSmallText);Assert.assertEquals(result.fontName,"Legacy1");
    }
    @Test(dataProvider="layouts") public void importsBareLegacyFont(int layout) throws Exception {
        IggyFile file=load(fixture(layout,2,true).file());SWF swf=parse(IggyToSwfConvertor.getSwf(file));FontTag old=font(swf);DefineFontTag legacy=new DefineFontTag(swf);legacy.fontId=1;legacy.glyphShapeTable=old.getGlyphShapeTable();replace(swf,old,legacy);
        Assert.assertTrue(font(roundTrip(file,swf)) instanceof DefineFontTag);Assert.assertNull(file.getSwf().getFonts().get(0).getCharIndices());
    }
    @Test(dataProvider="layouts") public void exportsEmptyNativeFontWithoutCodeTable(int layout) throws Exception {
        Fixture f=fixture(layout,1,false);int h=f.wide?32:16;f.u16(f.font+h,0);f.u32(f.font+(f.wide?164:100),0);f.ref(f.font+(f.wide?56:32),0);
        IggyFile file=load(f.file());SWF swf=parse(IggyToSwfConvertor.getSwf(file));DefineFont2Tag tag=(DefineFont2Tag)font(swf);Assert.assertEquals(tag.fontName,"Test");Assert.assertTrue(tag.glyphShapeTable.isEmpty());Assert.assertTrue(tag.codeTable.isEmpty());
        Assert.assertTrue(font(roundTrip(file,swf)) instanceof DefineFont2Tag);Assert.assertNull(file.getSwf().getFonts().get(0).getCharIndices());
    }
    @Test(dataProvider="layouts") public void updatesGlyphCountAndKerningCountTogether(int layout) throws Exception {
        IggyFile file=load(fixture(layout,3,true).file());SWF swf=parse(IggyToSwfConvertor.getSwf(file));DefineFont3Tag tag=(DefineFont3Tag)font(swf);
        tag.glyphShapeTable.add(tag.glyphShapeTable.get(0));tag.codeTable.add(67);tag.fontAdvanceTable.add(7043);tag.fontBoundsTable.add(tag.fontBoundsTable.get(0));tag.fontKerningTable.add(new KERNINGRECORD(66,67,-97));tag.setModified(true);
        DefineFont3Tag result=(DefineFont3Tag)font(roundTrip(file,swf));Assert.assertEquals(result.codeTable,Arrays.asList(65,66,67));Assert.assertEquals(result.fontAdvanceTable,Arrays.asList(10241,5120,7043));Assert.assertEquals(result.fontKerningTable.size(),2);Assert.assertEquals(result.fontKerningTable.get(1).fontKerningAdjustment,-97);
    }
    @Test public void fontFactoryUsesTheSameConversionAndNullClassReference() throws Exception {
        IggyFile file=load(fixture(2,2,true).file());DefineFont2Tag tag=(DefineFont2Tag)font(parse(IggyToSwfConvertor.getSwf(file)));IggyFont created=SwfToIggyConvertor.createIggyFont(tag);
        Assert.assertEquals(IggyFontConvertor.toSwf(tag.getSwf(),created).getData(),tag.getData());MemoryDataStream out=new MemoryDataStream();out.setIndexing(new IggyIndexBuilder());created.writeToDataStream(out);out.seek(16,SeekMode.SET);Assert.assertEquals(out.readUI64(),1L);
    }
    @Test(dataProvider="layouts") public void rejectsInvalidLayoutWithoutChangingFont(int layout) throws Exception {
        IggyFile file=load(fixture(layout,3,true).file());IggyFont original=file.getSwf().getFonts().get(0);SWF swf=parse(IggyToSwfConvertor.getSwf(file));DefineFont3Tag tag=(DefineFont3Tag)font(swf);tag.fontLeading=-32769;
        try{IggyFontConvertor.updateFont(original,tag);Assert.fail();}catch(IOException expected){Assert.assertTrue(expected.getMessage().contains("16-bit"));}Assert.assertEquals(original.getLeading(),-27);
        tag.fontLeading=-27;tag.fontAdvanceTable.clear();try{IggyFontConvertor.updateFont(original,tag);Assert.fail();}catch(IOException expected){Assert.assertTrue(expected.getMessage().contains("table size"));}Assert.assertEquals(original.getCharacterCount(),2L);
    }
}
