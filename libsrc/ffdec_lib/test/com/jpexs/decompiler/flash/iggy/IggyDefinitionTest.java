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
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.shaperecords.*;
import com.jpexs.decompiler.flash.types.*;
import com.jpexs.helpers.ByteArrayRange;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import org.testng.Assert;
import org.testng.annotations.*;

public class IggyDefinitionTest {
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


    private static Fixture fixture(int layout) throws Exception {
        Fixture f = new Fixture(layout); boolean w=f.wide; int p=w?8:4;
        f.allocate(w?184:layout==1?128:64);
        for (int at:w?new int[]{0,8,16,24,32,40,112,120,136,144,152,160}:layout==1?new int[]{0,4,8,12,16,20,80,84,96,100,104,108}:new int[]{0,8,12,44,56,60}) f.ref(at,0);
        f.u32(w?64:layout==1?40:4,5); f.u32(w?68:layout==1?44:32,1);
        if(layout!=0) f.f32(w?88:64,24);
        long dictionary=f.allocate(5*p); f.ref(0,dictionary); for(int i=0;i<5;i++)f.ref(dictionary+(long)i*p,0);
        f.font=f.allocate(w?352:layout==1?216:208);
        f.u8(f.font,22); f.u16(f.font+2,1);
        for(int off:w?new int[]{16,48,56,64,96,128}:new int[]{4,8,12,28,32,36,60,68,76,84,104}) f.ref(f.font+off,0);
        f.u16(f.font+(w?32:16),1); if(w)f.u32(f.font+164,1);
        long descriptor=f.allocate(w?32:16); f.ref(descriptor+(w?24:12),0); f.ref(f.font+(w?48:28),descriptor);
        long codes=f.allocate(2); f.u16(codes,65); f.ref(f.font+(w?56:32),codes);
        long advances=f.allocate(4); f.f32(advances,0.5f); f.ref(f.font+(w?64:36),advances);
        long name=f.allocate(4); f.u16(name,'F');f.u16(name+2,0);f.ref(f.font+(w?128:76),name);
        f.ref(dictionary+p,f.font);
        f.sprite=f.allocate(w?72:36); f.u8(f.sprite,3); f.u16(f.sprite+2,2); f.ref(f.sprite+(w?16:8),0);
        f.u16(f.sprite+(w?40:20),1);f.u16(f.sprite+(w?42:22),1);
        long show=f.allocate(w?16:8);f.ref(show,0);f.u8(show+p,1);
        f.ref(f.sprite+(w?56:28),show);f.ref(f.sprite+(w?64:32),show);f.ref(dictionary+2*p,f.sprite);
        long grid=f.allocate(16);float[] rect={1,2,30,40};for(int i=0;i<4;i++)f.f32(grid+4*i,rect[i]);f.ref(f.sprite+(w?32:16),grid);
        f.text=f.allocate(w?80:60);f.u8(f.text,5);f.u16(f.text+2,3);f.ref(f.text+(w?16:8),0);
        int h=w?32:16;f.f32(f.text+h,-2);f.f32(f.text+h+4,-1);f.f32(f.text+h+8,30);f.f32(f.text+h+12,40);
        int m=w?48:32;f.f32(f.text+m,1.25f);f.f32(f.text+m+4,0.25f);f.f32(f.text+m+8,-0.5f);f.f32(f.text+m+12,2);f.f32(f.text+m+16,-3.5f);f.f32(f.text+m+20,4.25f);
        f.records=f.allocate((w?32:20)+8);f.ref(f.text+(w?72:56),f.records);f.u16(f.records,240);f.u16(f.records+2,-10);f.u16(f.records+4,40);f.u8(f.records+6,2);f.ref(f.records+8,f.font);
        int color=w?16:12;for(int i=0;i<4;i++)f.u8(f.records+color+i,17+10*i);f.ref(f.records+(w?24:16),0);
        long glyphs=f.records+(w?32:20);f.u16(glyphs,0);f.u16(glyphs+2,-12);f.u16(glyphs+4,0);f.u16(glyphs+6,100);f.ref(dictionary+3*p,f.text);
        f.binary=f.allocate(w?48:24);f.u8(f.binary,20);f.u16(f.binary+2,4);f.ref(f.binary+(w?16:8),0);
        f.put(f.binary+(w?32:16),5,w?8:4,w?6:5);f.payload=f.allocate(5);byte[] bytes={0,1,(byte)255,0,42};for(int i=0;i<5;i++)f.u8(f.payload+i,bytes[i]&255);f.ref(f.binary+(w?40:20),f.payload);f.ref(dictionary+4*p,f.binary);
        f.ref(w?16:8,show);f.ref(w?24:12,show);
        return f;
    }
    private static IggyFile load(byte[] bytes) throws Exception {
        Path file = Files.createTempFile("iggy-buttons-", ".iggy");
        try { Files.write(file, bytes); return new IggyFile(file.toFile()); }
        finally { Files.delete(file); }
    }

    private static byte[] save(IggyFile file) throws Exception {
        file.updateFlashEntry();
        MemoryDataStream bytes = new MemoryDataStream();
        file.writeToDataStream(bytes);
        return bytes.getAllBytes();
    }

    private static SWF reparse(SWF swf) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        return new SWF(new ByteArrayInputStream(bytes.toByteArray()), false, false, "UTF-8", new IggySwfProfile());
    }


    private static <T> T find(SWF swf,Class<T> type){for(Tag t:swf.getTags())if(type.isInstance(t))return type.cast(t);throw new AssertionError(type);}
    private static boolean has(SWF swf,Class<?> type){for(Tag t:swf.getTags())if(type.isInstance(t))return true;return false;}
    private static void remove(SWF swf,Class<?> type){for(Tag t:new ArrayList<Tag>(swf.getTags().toArrayList()))if(type.isInstance(t))swf.removeTag(t);}
    private static SWF roundTrip(IggyFile file,SWF swf)throws Exception{SwfToIggyConvertor.updateIggy(file.getSwf(),reparse(swf));return reparse(IggyToSwfConvertor.getSwf(load(save(file))));}
    private static String className(IggyFile file,int id)throws Exception{IggySwf movie=file.getSwf();long at=movie.getDictionary().get(id).getAddress();return movie.getTimeline().string(movie.getTimeline().reference(at+(movie.is64()?16:8)));}
    private static final class AssetFixture {
        final boolean wide;final int layout;final MemoryDataStream data=new MemoryDataStream();final List<IggyFixup.Field> fields=new ArrayList<>();
        AssetFixture(int layout){this.layout=layout;wide=layout==2;}
        void put(long at,long value,int width,int op)throws Exception{long end=data.position();data.seek(at,SeekMode.SET);if(width==8)data.writeUI64(value);else if(width==4)data.writeUI32(value);else if(width==2)data.writeUI16((int)value);else data.writeUI8((int)value);data.seek(end,SeekMode.SET);if(op!=0)fields.add(new IggyFixup.Field(at,op));}
        void u8(long at,int value)throws Exception{put(at,value,1,0);}void u16(long at,int value)throws Exception{put(at,value,2,4);}void u32(long at,long value)throws Exception{put(at,value,4,5);}
        void f32(long at,float value)throws Exception{u32(at,Float.floatToIntBits(value));}void ref(long at,long target)throws Exception{put(at,target==0?1:target-at,wide?8:4,2);}
        long allocate(int count)throws Exception{data.pad8bytes();long at=data.position();data.writeBytes(new byte[count]);return at;}
        long body(int type,int id,int size)throws Exception{long at=allocate(size);u8(at,type);put(at+1,255,1,12);u16(at+2,id);ref(at+(wide?16:8),0);return at;}
        void rect(long at,float max)throws Exception{f32(at,0);f32(at+4,0);f32(at+8,max);f32(at+12,max);}
        void rgba(long at,int red,int green,int blue,int alpha)throws Exception{u8(at,red);u8(at+1,green);u8(at+2,blue);u8(at+3,alpha);}
        void geometry(long at,boolean morph,boolean end)throws Exception{
            int count=end?0:wide?8:4;u16(at+count,1);u8(at+count+2,1);if(!end){ref(at,0);put(at+(wide?16:8),0,wide?8:4,8);}
            long group=allocate(wide?64:40);ref(at+(end?wide?8:4:wide?24:12),group);rect(group,end?20:10);
            long nodes=allocate(5*24);ref(group+16,nodes);u32(group+(wide?24:20),5);
            int[][] points={{0,0,1},{10,0,2},{10,10,3},{0,10,2},{0,0,2}};
            for(int i=0;i<5;i++){long n=nodes+24L*i;int scale=end?2:1;f32(n,points[i][0]*scale);f32(n+4,points[i][1]*scale);f32(n+8,i==2?12*scale:0);f32(n+12,i==2?5*scale:0);u8(n+16,points[i][2]);u16(n+18,!end&&i!=0?(morph?0:1):0);u16(n+20,!end&&morph&&i!=0?1:0);u16(n+22,!end&&i!=0?1:0);}
            if(end){for(int off:wide?new int[]{32,40,48}:new int[]{24,28,32})ref(group+off,0);return;}
            int prefix=wide?8:4;long fills=allocate(prefix+(wide?72:60));u32(fills,1);ref(group+(wide?32:24),fills);long fill=fills+prefix;u8(fill,0);ref(fill+(wide?8:4),0);ref(fill+(wide?64:56),0);rgba(fill+(wide?16:8),12,34,56,78);if(morph)rgba(fill+(wide?20:12),98,76,54,32);
            long lines=allocate(prefix+(wide?88:76));u32(lines,1);ref(group+(wide?40:28),lines);long line=lines+prefix;u8(line,0);ref(line+(wide?8:4),0);ref(line+(wide?64:56),0);rgba(line+(wide?16:8),1,2,3,255);if(morph)rgba(line+(wide?20:12),3,2,1,255);f32(line+(wide?76:64),1.5f);if(morph)f32(line+(wide?80:68),2.5f);
            long bounds=allocate(16);rect(bounds,12);ref(group+(wide?48:32),bounds);
        }
    }
    private static byte[] assetFixture(int layout,boolean external)throws Exception{
        AssetFixture f=new AssetFixture(layout);boolean w=f.wide;int p=w?8:4;f.allocate(w?184:layout==1?128:64);
        for(int at:w?new int[]{0,8,16,24,32,40,112,120,136,144,152,160}:layout==1?new int[]{0,4,8,12,16,20,80,84,96,100,104,108}:new int[]{0,8,12,44,56,60})f.ref(at,0);
        f.u32(w?64:layout==1?40:4,5);f.u32(w?68:layout==1?44:32,1);if(layout!=0)f.f32(w?88:64,24);
        long dictionary=f.allocate(5*p);f.ref(0,dictionary);for(int i=0;i<5;i++)f.ref(dictionary+(long)p*i,0);
        long shape=f.body(1,1,w?80:48);f.rect(shape+(w?32:16),12);f.geometry(shape+(w?48:32),false,false);f.ref(dictionary+p,shape);
        long morph=f.body(2,2,w?128:88);f.rect(morph+(w?32:16),12);f.rect(morph+(w?112:72),24);f.rect(morph+(w?96:56),24);f.geometry(morph+(w?48:32),true,false);f.geometry(morph+(w?80:48),true,true);f.ref(dictionary+2*p,morph);
        long bitmap=f.body(4,3,w?88:56);int h=w?32:16;f.u16(bitmap+h,2);f.u16(bitmap+h+2,1);f.u8(bitmap+h+4,4);f.u8(bitmap+h+5,36);f.u8(bitmap+h+6,5);f.f32(bitmap+(w?48:28),1);f.f32(bitmap+(w?52:32),1);f.ref(bitmap+(w?72:48),0);
        byte[] compressed=zlib(new byte[]{(byte)255,(byte)255,0,0,(byte)128,0,64,0});f.u32(bitmap+(w?80:52),compressed.length);
        if(!external){long pixels=f.allocate(compressed.length);for(int i=0;i<compressed.length;i++)f.u8(pixels+i,compressed[i]&255);f.ref(bitmap+(w?72:48),pixels);}f.ref(dictionary+3*p,bitmap);
        long sound=f.body(21,4,w?64:40);f.u32(sound+(w?32:16),11);f.u32(sound+(w?36:20),987);f.u32(sound+(w?48:28),4);f.u16(sound+(w?52:32),22050);f.u8(sound+(w?54:34),2);f.u8(sound+(w?55:35),16);f.u8(sound+(w?56:36),2);long audio=f.allocate(4);f.u8(audio,254);f.u8(audio+1,255);f.u8(audio+2,17);f.u8(audio+3,34);f.ref(sound+(w?40:24),audio);f.ref(dictionary+4*p,sound);
        long place=f.allocate(w?64:48);f.ref(place,0);f.u8(place+p,2);f.u8(place+p+1,0);f.u8(place+p+2,127);f.u8(place+p+3,15);f.u16(place+(w?40:36),1);f.ref(place+(w?48:40),shape);
        long show=f.allocate(w?16:8);f.ref(show,0);f.u8(show+p,1);f.ref(place,show);f.ref(w?16:8,place);f.ref(w?24:12,show);
        byte[] main=f.data.getAllBytes();IggyIndexBuilder index=new IggyIndexBuilder();IggyFixup.writeFields(index,f.fields,main.length);List<byte[]>blocks=new ArrayList<>();blocks.add(main);blocks.add(index.getIndexBytes());
        if(external&&layout!=0){byte[] metadata=new byte[(w?16:12)+12];metadata[0]=1;int record=w?16:12;metadata[record+4]=0;metadata[record+5]=1;metadata[record+6]=2;metadata[record+8]=1;metadata[record+10]=3;blocks.add(metadata);}
        MemoryDataStream out=new MemoryDataStream();new IggyHeader(0x900,1,w?64:32,1,0,0,new byte[12],blocks.size()).writeToDataStream(out);int offset=32+16*blocks.size();for(int i=0;i<blocks.size();i++){byte[] b=blocks.get(i);new IggySubFileEntry(i==0?1:i==1?0:8,b.length,0,offset).writeToDataStream(out);offset+=b.length;}for(byte[] b:blocks)out.writeBytes(b);return out.getAllBytes();
    }
    private static byte[] zlib(byte[] data)throws Exception{ByteArrayOutputStream b=new ByteArrayOutputStream();try(DeflaterOutputStream out=new DeflaterOutputStream(b)){out.write(data);}return b.toByteArray();}
    private static void beforeFrame(SWF swf, Tag tag) {
        int index=0;
        for(Tag old:swf.getTags()) {if(old instanceof ShowFrameTag) {swf.addTag(index,tag);return;}index++;}
        swf.addTag(tag);
    }
    private static void removeId(SWF swf,int id) {
        for(Tag tag:new ArrayList<Tag>(swf.getTags().toArrayList())) {
            if(tag instanceof CharacterTag && ((CharacterTag)tag).getCharacterId()==id)swf.removeTag(tag);
            if(tag instanceof DefineScalingGridTag && ((DefineScalingGridTag)tag).characterId==id)swf.removeTag(tag);
        }
    }
    private static DefineFont3Tag newFont(SWF swf,int id) {
        DefineFont3Tag font=new DefineFont3Tag(swf);font.fontID=id;font.fontName="Added font";font.fontFlagsHasLayout=true;font.fontFlagsWideCodes=true;font.fontFlagsWideOffsets=true;font.fontFlagsBold=true;font.fontAscent=18321;font.fontDescent=4127;font.fontLeading=-53;font.languageCode.languageCode=2;
        SHAPE glyph=new SHAPE();glyph.numFillBits=1;glyph.shapeRecords=new ArrayList<>();StyleChangeRecord move=new StyleChangeRecord();move.stateMoveTo=true;move.moveDeltaX=17;move.moveDeltaY=-23;move.stateFillStyle0=true;move.fillStyle0=1;move.calculateBits();glyph.shapeRecords.add(move);
        StraightEdgeRecord line=new StraightEdgeRecord();line.deltaX=2048;line.deltaY=-4096;line.generalLineFlag=true;line.calculateBits();glyph.shapeRecords.add(line);
        CurvedEdgeRecord curve=new CurvedEdgeRecord();curve.controlDeltaX=137;curve.controlDeltaY=97;curve.anchorDeltaX=53;curve.anchorDeltaY=47;curve.calculateBits();glyph.shapeRecords.add(curve);glyph.shapeRecords.add(new EndShapeRecord());
        font.glyphShapeTable=Arrays.asList(glyph);font.codeTable=Arrays.asList(65);font.fontAdvanceTable=Arrays.asList(10243);font.fontBoundsTable=Arrays.asList(glyph.getBounds(1));font.fontKerningTable=new ArrayList<>();font.fontKerningTable.add(new KERNINGRECORD(65,65,-37));return font;
    }
    private static DefineEditTextTag newText(SWF swf,int id,int fontId) {
        DefineEditTextTag text=new DefineEditTextTag(swf);text.characterID=id;text.bounds=new RECT(-20,1000,-40,600);text.hasFont=true;text.fontId=fontId;text.fontHeight=320;text.hasText=true;text.initialText="New text čřž";text.hasTextColor=true;text.textColor=new RGBA(12,34,56,78);text.hasLayout=true;text.leading=-7;text.indent=13;text.align=2;text.multiline=true;text.wordWrap=true;text.useOutlines=true;return text;
    }
    private static PlaceObject2Tag place(SWF swf,int id,int depth) {
        PlaceObject2Tag tag=new PlaceObject2Tag(swf);tag.placeFlagHasCharacter=true;tag.characterId=id;tag.depth=depth;tag.placeFlagHasMatrix=true;tag.matrix=new MATRIX();tag.matrix.translateX=137;tag.matrix.translateY=-43;return tag;
    }
    private static DefineSpriteTag addDefinitions(SWF swf) {
        beforeFrame(swf,newFont(swf,10));beforeFrame(swf,newText(swf,11,10));DefineSpriteTag sprite=new DefineSpriteTag(swf);sprite.spriteId=12;sprite.frameCount=2;sprite.addTag(place(swf,11,1));sprite.addTag(new ShowFrameTag(swf));sprite.addTag(new ShowFrameTag(swf));sprite.addTag(new EndTag(swf));beforeFrame(swf,sprite);beforeFrame(swf,place(swf,12,4));return sprite;
    }
    private static IggyFile updateReload(IggyFile file,SWF swf)throws Exception {
        SwfToIggyConvertor.updateIggy(file.getSwf(),reparse(swf));return load(save(file));
    }
    private static Tag character(SWF swf,int id) {
        for(Tag tag:swf.getTags())if(tag instanceof CharacterTag && ((CharacterTag)tag).getCharacterId()==id)return tag;throw new AssertionError("Missing "+id);
    }

    @Test(dataProvider="layouts") public void addsLinkedFontsTextsAndSprites(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));addDefinitions(swf);IggyFile result=updateReload(file,swf);
        Assert.assertEquals(result.getSwf().getDictionary().get(10).getType(),22);Assert.assertEquals(result.getSwf().getDictionary().get(11).getType(),6);Assert.assertEquals(result.getSwf().getDictionary().get(12).getType(),3);
        SWF actual=reparse(IggyToSwfConvertor.getSwf(result));DefineFont3Tag font=(DefineFont3Tag)character(actual,10);Assert.assertEquals(font.fontAscent,18321);Assert.assertEquals(font.fontLeading,-53);Assert.assertEquals(font.fontAdvanceTable,Arrays.asList(10243));Assert.assertEquals(font.fontKerningTable.get(0).fontKerningAdjustment,-37);Assert.assertTrue(font.fontFlagsBold);
        StyleChangeRecord move=(StyleChangeRecord)font.glyphShapeTable.get(0).shapeRecords.get(0);Assert.assertEquals(move.moveDeltaX,17);Assert.assertEquals(move.moveDeltaY,-23);
        DefineEditTextTag text=(DefineEditTextTag)character(actual,11);Assert.assertEquals(text.fontId,10);Assert.assertEquals(text.initialText,"New text čřž");Assert.assertEquals(text.leading,-7);Assert.assertEquals(text.textColor.alpha,78);
        DefineSpriteTag sprite=(DefineSpriteTag)character(actual,12);Assert.assertEquals(sprite.frameCount,2);PlaceObjectTypeTag placement=(PlaceObjectTypeTag)sprite.getTags().get(0);Assert.assertEquals(placement.getCharacterId(),11);Assert.assertEquals(placement.getMatrix().translateX,137);
        IggyFont nativeFont=result.getSwf().getFonts().stream().filter(f->f.getFontId()==10).findFirst().get();Assert.assertTrue(nativeFont.getCharOffsets().get(0).isChar2());
        if(layout==2){IggySwf movie=result.getSwf();long at=movie.getDictionary().get(10).getAddress();long descriptors=movie.getTimeline().reference(at+48);long glyph=movie.getTimeline().reference(descriptors+24);long nodes=movie.getTimeline().reference(glyph+16);Assert.assertEquals(nodes,glyph+64);}
        if(layout!=2){IggySwf movie=result.getSwf();long at=movie.getDictionary().get(10).getAddress();long descriptors=movie.getTimeline().reference(at+28);long glyph=movie.getTimeline().reference(descriptors+12);long nodes=movie.getTimeline().reference(glyph+16);Assert.assertEquals(movie.getTimeline().bytes(nodes+24+20,2).getShort(),(short)1);}
        byte[] stable=save(result);result=updateReload(result,actual);Assert.assertEquals(save(result),stable);
    }
    @Test(dataProvider="layouts") public void addsNestedDefinitionsAndForwardSpriteReferences(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));DefineSpriteTag outer=new DefineSpriteTag(swf);outer.spriteId=12;outer.frameCount=1;outer.addTag(newFont(swf,10));outer.addTag(newText(swf,11,10));outer.addTag(place(swf,13,1));outer.addTag(new ShowFrameTag(swf));beforeFrame(swf,outer);
        DefineSpriteTag inner=new DefineSpriteTag(swf);inner.spriteId=13;inner.frameCount=1;inner.addTag(place(swf,11,1));inner.addTag(new ShowFrameTag(swf));outer.addTag(inner);
        DefineSpriteTag original=(DefineSpriteTag)character(swf,2);original.addTag(0,place(swf,12,3));SWF actual=reparse(IggyToSwfConvertor.getSwf(updateReload(file,swf)));Assert.assertTrue(character(actual,10) instanceof DefineFont3Tag);Assert.assertTrue(character(actual,11) instanceof DefineEditTextTag);Assert.assertEquals(((DefineSpriteTag)character(actual,13)).frameCount,1);Assert.assertEquals(((PlaceObjectTypeTag)((DefineSpriteTag)character(actual,2)).getTags().get(0)).getCharacterId(),12);
    }
    @Test(dataProvider="layouts") public void addsScalingGridToNewSprite(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));addDefinitions(swf);DefineScalingGridTag grid=new DefineScalingGridTag(swf);grid.characterId=12;grid.splitter=new RECT(20,200,40,400);beforeFrame(swf,grid);IggyFile result=updateReload(file,swf);Assert.assertEquals(result.getSwf().getScalingGrid(12),new float[]{1,2,10,20});
    }
    @Test(dataProvider="layouts") public void removesAllOriginalDefinitions(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));for(int id=1;id<=4;id++)removeId(swf,id);IggyFile result=updateReload(file,swf);
        for(int id=1;id<=4;id++)Assert.assertTrue(result.getSwf().getDictionary().get(id).isNull());Assert.assertTrue(result.getSwf().getFonts().isEmpty());Assert.assertTrue(result.getSwf().getTexts().isEmpty());Assert.assertTrue(result.getSwf().getSpriteTimelines().isEmpty());Assert.assertTrue(result.getSwf().getSymbolClasses().isEmpty());
        SWF actual=reparse(IggyToSwfConvertor.getSwf(result));IggyFile stable=updateReload(result,actual);Assert.assertEquals(save(stable),save(result));
    }
    @Test(dataProvider="layouts") public void removesNewFontTextAndSpriteAfterReload(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));addDefinitions(swf);file=updateReload(file,swf);swf=reparse(IggyToSwfConvertor.getSwf(file));for(int id=10;id<=12;id++)removeId(swf,id);
        for(Tag tag:new ArrayList<Tag>(swf.getTags().toArrayList()))if(tag instanceof PlaceObjectTypeTag && ((PlaceObjectTypeTag)tag).getCharacterId()==12)swf.removeTag(tag);
        IggyFile result=updateReload(file,swf);for(int id=10;id<=12;id++)Assert.assertTrue(result.getSwf().getDictionary().get(id).isNull());Assert.assertEquals(result.getSwf().getFonts().size(),1);Assert.assertTrue(result.getSwf().getTexts().isEmpty());
    }
    @Test(dataProvider="layouts") public void rejectsDeletingReferencedFont(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));removeId(swf,1);try{SwfToIggyConvertor.updateIggy(file.getSwf(),swf);Assert.fail();}catch(IOException expected){Assert.assertTrue(expected.getMessage().contains("referenced"));}
    }
    @Test(dataProvider="layouts") public void rejectsDeletingReferencedSprite(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));beforeFrame(swf,place(swf,2,1));removeId(swf,2);try{SwfToIggyConvertor.updateIggy(file.getSwf(),swf);Assert.fail();}catch(IOException expected){Assert.assertTrue(expected.getMessage().contains("referenced"));}
    }
    @Test(dataProvider="layouts") public void rejectsDuplicateAndOccupiedIds(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));beforeFrame(swf,newFont(swf,1));try{SwfToIggyConvertor.updateIggy(file.getSwf(),swf);Assert.fail();}catch(IOException expected){Assert.assertTrue(expected.getMessage().contains("duplicate"));}
        swf=reparse(IggyToSwfConvertor.getSwf(file));removeId(swf,4);beforeFrame(swf,newFont(swf,4));try{SwfToIggyConvertor.updateIggy(file.getSwf(),swf);Assert.fail();}catch(IOException expected){Assert.assertTrue(expected.getMessage().contains("occupied"));}
    }
    @Test(dataProvider="layouts") public void revertsPendingAdditionsAndRemovalsBeforeSave(int layout)throws Exception {
        byte[] source=fixture(layout).file();IggyFile file=load(source);SWF original=reparse(IggyToSwfConvertor.getSwf(file));SWF added=reparse(IggyToSwfConvertor.getSwf(file));addDefinitions(added);SwfToIggyConvertor.updateIggy(file.getSwf(),reparse(added));SwfToIggyConvertor.updateIggy(file.getSwf(),original);Assert.assertEquals(save(file),source);
        SWF deleted=reparse(IggyToSwfConvertor.getSwf(file));for(int id=1;id<=4;id++)removeId(deleted,id);SwfToIggyConvertor.updateIggy(file.getSwf(),deleted);SwfToIggyConvertor.updateIggy(file.getSwf(),original);Assert.assertEquals(save(file),source);
    }
    @Test(dataProvider="layouts") public void addsFontInfoLegacyFontAndText(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));DefineFontTag legacy=new DefineFontTag(swf);legacy.fontId=10;legacy.glyphShapeTable=newFont(swf,10).glyphShapeTable;beforeFrame(swf,legacy);DefineFontInfo2Tag info=new DefineFontInfo2Tag(swf);info.fontID=10;info.fontName="Legacy added";info.fontFlagsWideCodes=true;info.fontFlagsItalic=true;info.codeTable=Arrays.asList(65);beforeFrame(swf,info);beforeFrame(swf,newText(swf,11,10));SWF actual=reparse(IggyToSwfConvertor.getSwf(updateReload(file,swf)));Assert.assertEquals(((DefineFont2Tag)character(actual,10)).fontName,"Legacy added");Assert.assertTrue(((DefineFont2Tag)character(actual,10)).fontFlagsItalic);
    }
    @Test public void bindsNewSymbolsAndClassOnlyPlacements()throws Exception {
        for(int layout:new int[]{1,2}){
            IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));addDefinitions(swf);for(Tag tag:new ArrayList<Tag>(swf.getTags().toArrayList()))if(tag instanceof PlaceObject2Tag)swf.removeTag(tag);
            SymbolClassTag symbols=new SymbolClassTag(swf);symbols.tags=Arrays.asList(10,11,12);symbols.names=Arrays.asList("AddedFont","AddedText","AddedSprite");beforeFrame(swf,symbols);
            PlaceObject3Tag place=new PlaceObject3Tag(swf);place.depth=4;place.placeFlagHasClassName=true;place.className="AddedSprite";beforeFrame(swf,place);
            IggyFile result=updateReload(file,swf);Assert.assertEquals(result.getSwf().getSymbolClasses().size(),3);Assert.assertEquals(className(result,12),"AddedSprite");Assert.assertEquals(className(result,10),"AddedFont");Assert.assertEquals(className(result,11),"AddedText");
            IggyTimeline.Command command=result.getSwf().getTimeline().getCommands().get(0);long field=command.getAddress()+(layout==2?48:40);Assert.assertEquals(result.getSwf().getDictionaryId(field,result.getSwf().getTimeline().reference(field)),12);
        }
    }
    @Test(dataProvider="layouts") public void removesGraphicsSoundsAndExternalTextureRecords(int layout)throws Exception {
        IggyFile file=load(assetFixture(layout,true));SWF swf=reparse(IggyToSwfConvertor.getSwf(file));for(int id=1;id<=4;id++)removeId(swf,id);
        for(Tag tag:new ArrayList<Tag>(swf.getTags().toArrayList()))if(tag instanceof PlaceObjectTypeTag)swf.removeTag(tag);
        IggyFile result=updateReload(file,swf);for(int id=1;id<=4;id++)Assert.assertTrue(result.getSwf().getDictionary().get(id).isNull());if(layout!=0)Assert.assertTrue(result.getTextureMetadata().get(0).getRecords().isEmpty());
    }
    @Test(dataProvider="layouts") public void addsAndRemovesButtonDefinition(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));DefineButton2Tag button=new DefineButton2Tag(swf);button.buttonId=10;button.actions=new ArrayList<>();beforeFrame(swf,button);file=updateReload(file,swf);Assert.assertEquals(file.getSwf().getDictionary().get(10).getType(),7);swf=reparse(IggyToSwfConvertor.getSwf(file));removeId(swf,10);file=updateReload(file,swf);Assert.assertTrue(file.getSwf().getDictionary().get(10).isNull());
    }
    @Test(dataProvider="layouts") public void addsClassicFont2Definition(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));DefineFont2Tag font=new DefineFont2Tag(swf);font.fontID=10;font.fontName="Font2 added";font.fontFlagsHasLayout=false;font.glyphShapeTable=newFont(swf,10).glyphShapeTable;font.codeTable=Arrays.asList(65);beforeFrame(swf,font);DefineFont2Tag actual=(DefineFont2Tag)character(reparse(IggyToSwfConvertor.getSwf(updateReload(file,swf))),10);Assert.assertEquals(actual.codeTable,Arrays.asList(65));Assert.assertEquals(actual.fontName,"Font2 added");Assert.assertEquals(((StyleChangeRecord)actual.glyphShapeTable.get(0).shapeRecords.get(0)).moveDeltaX,17);
    }
    @Test public void clearsImportFixupsAfterRemovingTheirSprite()throws Exception {
        for(int layout:new int[]{1,2}){
            IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));ImportAssets2Tag imports=new ImportAssets2Tag(swf);imports.url="lib.iggy";imports.tags.add(30);imports.names.add("ImportedAsset");beforeFrame(swf,imports);DefineSpriteTag sprite=new DefineSpriteTag(swf);sprite.spriteId=10;sprite.frameCount=1;sprite.addTag(place(swf,30,1));sprite.addTag(new ShowFrameTag(swf));beforeFrame(swf,sprite);file=updateReload(file,swf);Assert.assertEquals(file.getSwf().getImports().size(),1);
            swf=reparse(IggyToSwfConvertor.getSwf(file));removeId(swf,10);remove(swf,ImportAssets2Tag.class);IggyFile result=updateReload(file,swf);Assert.assertTrue(result.getSwf().getImports().isEmpty());Assert.assertTrue(result.getSwf().getDictionary().get(10).isNull());long table=result.getSwf().getHdr().getDictionaryFixupsAddress();Assert.assertEquals(result.getSwf().getTimeline().bytes(table+(layout==2?8:4),4).getInt(),0);
        }
    }
    @Test public void removesSymbolBindingsTogetherWithDefinitions()throws Exception {
        for(int layout:new int[]{1,2}){
            IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));addDefinitions(swf);SymbolClassTag symbols=new SymbolClassTag(swf);symbols.tags=Arrays.asList(10,11,12);symbols.names=Arrays.asList("AddedFont","AddedText","AddedSprite");beforeFrame(swf,symbols);file=updateReload(file,swf);swf=reparse(IggyToSwfConvertor.getSwf(file));for(int id=10;id<=12;id++)removeId(swf,id);remove(swf,SymbolClassTag.class);for(Tag tag:new ArrayList<Tag>(swf.getTags().toArrayList()))if(tag instanceof PlaceObjectTypeTag)swf.removeTag(tag);IggyFile result=updateReload(file,swf);Assert.assertTrue(result.getSwf().getSymbolClasses().isEmpty());
        }
    }
    @Test(dataProvider="layouts") public void emitsNativeLibraryAndPlayerFixupsForNewDefinitions(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));addDefinitions(swf);file=updateReload(file,swf);byte[] main=file.getEntryData(0);List<IggyFixup.Field> fields=IggyFixup.parse(layout==2,file.getEntryData(1),main.length).getFields();Map<Long,Integer> operations=new HashMap<>();for(IggyFixup.Field f:fields)operations.put(f.offset,f.operation);
        for(int id=10;id<=12;id++){long at=file.getSwf().getDictionary().get(id).getAddress();Assert.assertEquals(operations.get(at+1).intValue(),12);}
        long at=file.getSwf().getDictionary().get(10).getAddress();long descriptors=file.getSwf().getTimeline().reference(at+(layout==2?48:28));Assert.assertEquals(operations.get(descriptors+(layout==2?16:8)).intValue(),8);
    }

    @Test public void rejectsRemovingClassOnlyPlacementTarget()throws Exception {
        for(int layout:new int[]{1,2}){
            IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));addDefinitions(swf);for(Tag tag:new ArrayList<Tag>(swf.getTags().toArrayList()))if(tag instanceof PlaceObjectTypeTag)swf.removeTag(tag);
            SymbolClassTag symbols=new SymbolClassTag(swf);symbols.tags.add(12);symbols.names.add("AddedSprite");beforeFrame(swf,symbols);PlaceObject3Tag place=new PlaceObject3Tag(swf);place.depth=4;place.placeFlagHasClassName=true;place.className="AddedSprite";beforeFrame(swf,place);file=updateReload(file,swf);swf=reparse(IggyToSwfConvertor.getSwf(file));removeId(swf,12);remove(swf,SymbolClassTag.class);
            try{SwfToIggyConvertor.updateIggy(file.getSwf(),swf);Assert.fail();}catch(IOException expected){Assert.assertTrue(expected.getMessage().contains("class is still referenced"));}
        }
    }

    @Test(dataProvider="layouts") public void addsLegacyFontWithFontInfo1(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));DefineFontTag font=new DefineFontTag(swf);font.fontId=10;font.glyphShapeTable=newFont(swf,10).glyphShapeTable;beforeFrame(swf,font);DefineFontInfoTag info=new DefineFontInfoTag(swf);info.fontID=10;info.fontName="Added old info";info.fontFlagsSmallText=true;info.fontFlagsWideCodes=true;info.codeTable=Arrays.asList(65);beforeFrame(swf,info);
        DefineFont2Tag actual=(DefineFont2Tag)character(reparse(IggyToSwfConvertor.getSwf(updateReload(file,swf))),10);Assert.assertEquals(actual.fontName,"Added old info");Assert.assertTrue(actual.fontFlagsSmallText);Assert.assertEquals(actual.codeTable,Arrays.asList(65));
    }
    @Test(dataProvider="layouts") public void addsBareLegacyFontAndTextByFontClass(int layout)throws Exception {
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));DefineFontTag font=new DefineFontTag(swf);font.fontId=10;font.glyphShapeTable=newFont(swf,10).glyphShapeTable;beforeFrame(swf,font);DefineEditTextTag text=newText(swf,11,10);text.hasFont=false;text.hasFontClass=true;text.fontClass="ExternalFontClass";beforeFrame(swf,text);
        SWF actual=reparse(IggyToSwfConvertor.getSwf(updateReload(file,swf)));Assert.assertTrue(character(actual,10) instanceof DefineFontTag);DefineEditTextTag actualText=(DefineEditTextTag)character(actual,11);Assert.assertEquals(actualText.fontClass,"ExternalFontClass");Assert.assertEquals(actualText.initialText,"New text čřž");
    }

    @Test public void bindsImportedClassOnlyPlacementInNewSprite()throws Exception {
        for(int layout:new int[]{1,2}){
            IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));ImportAssets2Tag imports=new ImportAssets2Tag(swf);imports.url="lib.iggy";imports.tags.add(30);imports.names.add("ImportedSprite");beforeFrame(swf,imports);
            DefineSpriteTag sprite=new DefineSpriteTag(swf);sprite.spriteId=10;sprite.frameCount=1;PlaceObject3Tag place=new PlaceObject3Tag(swf);place.depth=1;place.placeFlagHasClassName=true;place.className="ImportedSprite";sprite.addTag(place);sprite.addTag(new ShowFrameTag(swf));beforeFrame(swf,sprite);IggyFile result=updateReload(file,swf);IggyTimeline timeline=result.getSwf().getSpriteTimelines().get(10);IggyTimeline.Command command=timeline.getCommands().get(0);long field=command.getAddress()+(layout==2?48:40);Assert.assertEquals(result.getSwf().getDictionaryId(field,timeline.reference(field)),30);
            SWF actual=reparse(IggyToSwfConvertor.getSwf(result));PlaceObject3Tag actualPlace=(PlaceObject3Tag)((DefineSpriteTag)character(actual,10)).getTags().get(0);Assert.assertEquals(actualPlace.className,"ImportedSprite");Assert.assertEquals(actualPlace.getCharacterId(),-1);
        }
    }

}
