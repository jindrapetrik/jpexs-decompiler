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
import com.jpexs.decompiler.flash.types.*;
import com.jpexs.helpers.ByteArrayRange;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class IggyAdditionalTagsTest {
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
    @DataProvider(name = "symbolLayouts")
    public Object[][] symbolLayouts() {
        return new Object[][]{{1}, {2}};
    }

    private static Fixture documentOnlyFixture(int layout, int id) throws Exception {
        Fixture f = fixture(layout);
        f.ref(0, 0);
        f.u32(f.wide ? 64 : 40, 0);
        long table = f.allocate(f.wide ? 24 : 12);
        long name = f.allocate(28);
        String className = "DocumentClass";
        for (int i = 0; i < className.length(); i++) {
            f.u16(name + 2 * i, className.charAt(i));
        }
        f.ref(table, 0);
        f.ref(table + (f.wide ? 8 : 4), name);
        f.u32(table + (f.wide ? 16 : 8), id);
        f.ref(f.wide ? 160 : 108, table);
        f.u32(f.wide ? 176 : 120, 1);
        return f;
    }

    @Test(dataProvider = "symbolLayouts")
    public void documentClassWithoutDictionary(int layout) throws Exception {
        byte[] original = documentOnlyFixture(layout, 0).file();
        IggyFile file = load(original);
        Assert.assertTrue(file.getSwf().getDictionary().isEmpty());
        SymbolClassTag symbols = find(reparse(IggyToSwfConvertor.getSwf(file)), SymbolClassTag.class);
        Assert.assertEquals(symbols.tags, Arrays.asList(0));
        Assert.assertEquals(symbols.names, Arrays.asList("DocumentClass"));
        SWF swf = reparse(IggyToSwfConvertor.getSwf(file));
        SwfToIggyConvertor.updateIggy(file.getSwf(), swf);
        Assert.assertEquals(save(file), original, "An unchanged document-only movie must retain its bytes");
        IggyFile loaded = load(save(file));
        Assert.assertTrue(loaded.getSwf().getDictionary().isEmpty());
        Assert.assertEquals(loaded.getSwf().getSymbolClasses().get(0).getCharacterId(), 0L);
    }

    @Test(dataProvider = "symbolLayouts")
    public void editsDocumentClassWithoutDictionary(int layout) throws Exception {
        IggyFile file = load(documentOnlyFixture(layout, 0).file());
        SWF swf = reparse(IggyToSwfConvertor.getSwf(file));
        SymbolClassTag symbols = find(swf, SymbolClassTag.class);
        symbols.names.set(0, "RenamedDocument");
        symbols.setModified(true);
        SWF actual = roundTrip(file, swf);
        Assert.assertEquals(find(actual, SymbolClassTag.class).names, Arrays.asList("RenamedDocument"));
        IggyFile loaded = load(save(file));
        Assert.assertTrue(loaded.getSwf().getDictionary().isEmpty());
        remove(actual, SymbolClassTag.class);
        Assert.assertFalse(has(roundTrip(loaded, actual), SymbolClassTag.class));
    }

    @Test(dataProvider = "symbolLayouts")
    public void rejectsNonzeroSymbolWithoutDictionary(int layout) throws Exception {
        try {
            load(documentOnlyFixture(layout, 1).file());
            Assert.fail("A nonzero ID must still reference a dictionary slot");
        } catch (IOException ex) {
            Assert.assertEquals(ex.getMessage(), "Invalid Iggy symbol dictionary ID");
        }
    }

    @Test(dataProvider="layouts") public void readsIndependentNativeAssets(int layout)throws Exception{
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));
        DefineText2Tag text=find(swf,DefineText2Tag.class);Assert.assertEquals(text.characterID,3);Assert.assertEquals(text.textBounds.Xmin,-40);Assert.assertEquals(text.textBounds.Ymin,-20);Assert.assertEquals(text.textBounds.Xmax,600);Assert.assertEquals(text.textMatrix.translateX,-70);
        TEXTRECORD r=text.textRecords.get(0);Assert.assertEquals(r.fontId,1);Assert.assertEquals(r.textHeight,240);Assert.assertEquals(r.xOffset,-10);Assert.assertEquals(r.yOffset,40);Assert.assertEquals(r.textColorA.alpha,47);Assert.assertEquals(r.glyphEntries.get(0).glyphAdvance,-12);Assert.assertEquals(r.glyphEntries.get(1).glyphAdvance,100);
        Assert.assertEquals(find(swf,DefineBinaryDataTag.class).binaryData.getRangeData(),new byte[]{0,1,(byte)255,0,42});
        DefineScalingGridTag grid=find(swf,DefineScalingGridTag.class);Assert.assertEquals(grid.characterId,2);Assert.assertEquals(grid.splitter.Xmin,20);Assert.assertEquals(grid.splitter.Ymax,800);
    }
    @Test(dataProvider="layouts") public void editsAssetsAndScalingGrid(int layout)throws Exception{
        IggyFile file=load(fixture(layout).file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));DefineText2Tag text=find(swf,DefineText2Tag.class);
        text.textRecords.get(0).glyphEntries.get(1).glyphAdvance=-200;text.textRecords.get(0).textColorA.alpha=123;text.textMatrix.translateY=-255;text.setModified(true);
        DefineBinaryDataTag binary=find(swf,DefineBinaryDataTag.class);binary.binaryData=new ByteArrayRange(new byte[]{5,4,3,2,1,0});binary.setModified(true);
        DefineScalingGridTag grid=find(swf,DefineScalingGridTag.class);grid.splitter.Xmax=1000;grid.setModified(true);
        SWF result=roundTrip(file,swf);Assert.assertEquals(find(result,DefineText2Tag.class).textRecords.get(0).glyphEntries.get(1).glyphAdvance,-200);Assert.assertEquals(find(result,DefineText2Tag.class).textRecords.get(0).textColorA.alpha,123);Assert.assertEquals(find(result,DefineText2Tag.class).textMatrix.translateY,-255);Assert.assertEquals(find(result,DefineBinaryDataTag.class).binaryData.getRangeData(),new byte[]{5,4,3,2,1,0});Assert.assertEquals(find(result,DefineScalingGridTag.class).splitter.Xmax,1000);
    }
    @Test(dataProvider="layouts") public void removesAndReaddsScalingGrid(int layout)throws Exception{
        IggyFile file=load(fixture(layout).file());SWF swf=IggyToSwfConvertor.getSwf(file);remove(swf,DefineScalingGridTag.class);Assert.assertFalse(has(roundTrip(file,swf),DefineScalingGridTag.class));
        IggyFile loaded=load(save(file));SWF changed=IggyToSwfConvertor.getSwf(loaded);DefineScalingGridTag grid=new DefineScalingGridTag(changed);grid.characterId=2;grid.splitter=new RECT(20,200,40,400);changed.addTag(1,grid);Assert.assertEquals(find(roundTrip(loaded,changed),DefineScalingGridTag.class).splitter.Ymin,40);
    }
    @Test(dataProvider="layouts") public void writesAndRemovesSceneMetadata(int layout)throws Exception{
        IggyFile file=load(fixture(layout).file());SWF swf=IggyToSwfConvertor.getSwf(file);DefineSceneAndFrameLabelDataTag metadata=new DefineSceneAndFrameLabelDataTag(swf);metadata.sceneOffsets=new long[]{0,25};metadata.sceneNames=new String[]{"First","Příští"};metadata.frameNums=new long[]{7};metadata.frameNames=new String[]{"Label"};swf.addTag(1,metadata);
        DefineSceneAndFrameLabelDataTag result=find(roundTrip(file,swf),DefineSceneAndFrameLabelDataTag.class);Assert.assertEquals(result.sceneNames,metadata.sceneNames);Assert.assertEquals(result.sceneOffsets,metadata.sceneOffsets);Assert.assertEquals(result.frameNums,metadata.frameNums);Assert.assertEquals(result.frameNames,metadata.frameNames);
        IggyFile loaded=load(save(file));SWF changed=IggyToSwfConvertor.getSwf(loaded);remove(changed,DefineSceneAndFrameLabelDataTag.class);Assert.assertFalse(has(roundTrip(loaded,changed),DefineSceneAndFrameLabelDataTag.class));
    }
    @Test(dataProvider="layouts") public void addsRenamesRemovesSymbolClasses(int layout)throws Exception{
        if(layout==0)return;IggyFile file=load(fixture(layout).file());SWF swf=IggyToSwfConvertor.getSwf(file);SymbolClassTag symbols=new SymbolClassTag(swf);symbols.tags.add(0);symbols.names.add("DocumentClass");symbols.tags.add(3);symbols.names.add("StaticLabel");symbols.tags.add(4);symbols.names.add("Blob");swf.addTag(1,symbols);
        SymbolClassTag actual=find(roundTrip(file,swf),SymbolClassTag.class);Assert.assertEquals(actual.tags,symbols.tags);Assert.assertEquals(actual.names,symbols.names);
        Assert.assertEquals(className(load(save(file)),3),"StaticLabel");
        IggyFile loaded=load(save(file));SWF changed=IggyToSwfConvertor.getSwf(loaded);SymbolClassTag edit=find(changed,SymbolClassTag.class);edit.names.set(1,"RenamedLabel");edit.tags.remove(2);edit.names.remove(2);edit.setModified(true);actual=find(roundTrip(loaded,changed),SymbolClassTag.class);Assert.assertEquals(actual.names,Arrays.asList("DocumentClass","RenamedLabel"));
        IggyFile again=load(save(loaded));SWF removed=IggyToSwfConvertor.getSwf(again);remove(removed,SymbolClassTag.class);Assert.assertFalse(has(roundTrip(again,removed),SymbolClassTag.class));
        Assert.assertEquals(className(again,3),"RenamedLabel");
        IggyFile noClasses=load(save(again));long object=noClasses.getSwf().getDictionary().get(3).getAddress();Assert.assertEquals(noClasses.getSwf().getTimeline().reference(object+(layout==2?16:8)),0L,"Removed class must also leave the shared object header");
    }
    @Test(dataProvider="layouts") public void importsLegacyAbcTag(int layout)throws Exception{
        if(layout==0)return;IggyFile file=load(fixture(layout).file());SWF swf=IggyToSwfConvertor.getSwf(file);DoABCTag abc=new DoABCTag(swf);swf.addTag(1,abc);ByteArrayOutputStream expected=new ByteArrayOutputStream();abc.getABC().saveTo(expected);
        DoABC2Tag result=find(roundTrip(file,swf),DoABC2Tag.class);ByteArrayOutputStream actual=new ByteArrayOutputStream();result.getABC().saveTo(actual);Assert.assertEquals(actual.toByteArray(),expected.toByteArray());
        Assert.assertTrue(load(save(file)).getSwf().getHdr().hasAS3(),"The runtime AS3 flag must enable the newly added ABC");
    }
    @Test(dataProvider="layouts") public void detectsStaticTextCyclesAndTruncation(int layout)throws Exception{
        Fixture f=fixture(layout);f.ref(f.records+(f.wide?24:16),f.records);IggyFile file=load(f.file());
        try{IggyStaticTextConvertor.toSwf(new SWF(),file.getSwf(),file.getSwf().getTimeline(),f.text,3);Assert.fail();}catch(IOException ex){Assert.assertTrue(ex.getMessage().contains("Cycle"));}
        Fixture binary=fixture(layout);binary.put(binary.binary+(binary.wide?32:16),Integer.MAX_VALUE,binary.wide?8:4,0);IggyFile bad=load(binary.file());
        try{IggyBinaryDataConvertor.toSwf(new SWF(),bad.getSwf().getTimeline(),binary.binary,4);Assert.fail();}catch(IOException ex){Assert.assertTrue(ex.getMessage().contains("outside"));}
    }

    @Test(dataProvider="layouts") public void importsLegacyTextAndInheritedStyles(int layout)throws Exception{
        IggyFile file=load(fixture(layout).file());SWF swf=IggyToSwfConvertor.getSwf(file);
        DefineTextTag text=new DefineTextTag(swf);text.characterID=10;text.textBounds=new RECT(0,1000,0,400);
        TEXTRECORD first=new TEXTRECORD();first.styleFlagsHasFont=true;first.fontId=1;first.textHeight=240;first.styleFlagsHasColor=true;first.textColor=new RGB(12,34,56);first.styleFlagsHasXOffset=true;first.xOffset=100;first.styleFlagsHasYOffset=true;first.yOffset=-50;first.glyphEntries=new ArrayList<>();
        GLYPHENTRY glyph=new GLYPHENTRY();glyph.glyphIndex=0;glyph.glyphAdvance=70;first.glyphEntries.add(glyph);text.textRecords.add(first);
        TEXTRECORD second=new TEXTRECORD();second.glyphEntries=new ArrayList<>();second.glyphEntries.add(glyph);text.textRecords.add(second);swf.addTag(1,text);
        SWF result=roundTrip(file,swf);DefineText2Tag actual=null;for(Tag t:result.getTags())if(t instanceof DefineText2Tag&&((DefineText2Tag)t).characterID==10)actual=(DefineText2Tag)t;
        Assert.assertNotNull(actual);TEXTRECORD r=actual.textRecords.get(1);Assert.assertEquals(r.fontId,1);Assert.assertEquals(r.textHeight,240);Assert.assertEquals(r.xOffset,170);Assert.assertEquals(r.yOffset,-50);Assert.assertEquals(r.textColorA.red,12);Assert.assertEquals(r.textColorA.alpha,255);
    }

    @Test(dataProvider="layouts") public void supportsEmptyAndNewBinaryPayloads(int layout)throws Exception{
        IggyFile file=load(fixture(layout).file());SWF swf=IggyToSwfConvertor.getSwf(file);
        DefineBinaryDataTag original=find(swf,DefineBinaryDataTag.class);original.binaryData=ByteArrayRange.EMPTY;original.setModified(true);
        DefineBinaryDataTag added=new DefineBinaryDataTag(swf);added.tag=12;added.binaryData=new ByteArrayRange(new byte[]{8,9,10});swf.addTag(1,added);
        SWF result=roundTrip(file,swf);for(Tag t:result.getTags())if(t instanceof DefineBinaryDataTag){DefineBinaryDataTag b=(DefineBinaryDataTag)t;if(b.tag==4)Assert.assertEquals(b.binaryData.getLength(),0L);else if(b.tag==12)Assert.assertEquals(b.binaryData.getRangeData(),new byte[]{8,9,10});}
    }

    @Test(dataProvider="layouts") public void metadataChangesCoexistWithTimelineEdits(int layout)throws Exception{
        IggyFile file=load(fixture(layout).file());SWF swf=IggyToSwfConvertor.getSwf(file);FrameLabelTag frame=new FrameLabelTag(swf);frame.name="TimelineLabel";swf.addTag(swf.getTags().size()-2,frame);
        DefineSceneAndFrameLabelDataTag metadata=new DefineSceneAndFrameLabelDataTag(swf);metadata.sceneNames=new String[]{"Scene"};metadata.sceneOffsets=new long[]{0};metadata.frameNames=new String[]{"MetadataLabel"};metadata.frameNums=new long[]{12};swf.addTag(1,metadata);
        SWF result=roundTrip(file,swf);Assert.assertEquals(find(result,FrameLabelTag.class).name,"TimelineLabel");Assert.assertEquals(find(result,DefineSceneAndFrameLabelDataTag.class).frameNames,new String[]{"MetadataLabel"});
    }

    @Test(dataProvider="layouts") public void unchangedAssetsAndGridKeepOriginalAddresses(int layout)throws Exception{
        Fixture f=fixture(layout);IggyFile file=load(f.file());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));SwfToIggyConvertor.updateIggy(file.getSwf(),swf);IggyFile loaded=load(save(file));Assert.assertEquals(loaded.getSwf().getDictionary().get(3).getAddress(),f.text);Assert.assertEquals(loaded.getSwf().getDictionary().get(4).getAddress(),f.binary);
        byte[] before=file.getSwf().getOriginalData(),after=loaded.getSwf().getOriginalData();int field=(int)f.sprite+(layout==2?32:16);Assert.assertEquals(Arrays.copyOfRange(after,field,field+(layout==2?8:4)),Arrays.copyOfRange(before,field,field+(layout==2?8:4)));
    }

    @Test(dataProvider="layouts") public void resolvesImportedStaticFont(int layout)throws Exception{
        if(layout==0)return;IggyFile file=load(fixture(layout).file());SWF swf=IggyToSwfConvertor.getSwf(file);ImportAssets2Tag imported=new ImportAssets2Tag(swf);imported.url="fonts.swf";imported.tags.add(500);imported.names.add("Font");swf.addTag(1,imported);DefineText2Tag text=find(swf,DefineText2Tag.class);text.textRecords.get(0).fontId=500;text.setModified(true);Assert.assertEquals(find(roundTrip(file,swf),DefineText2Tag.class).textRecords.get(0).fontId,500);
    }

    @Test(dataProvider="layouts") public void rejectsUnrepresentableInputs(int layout)throws Exception{
        IggyFile file=load(fixture(layout).file());SWF swf=IggyToSwfConvertor.getSwf(file);find(swf,DefineText2Tag.class).textRecords.get(0).fontId=2;
        try{SwfToIggyConvertor.updateIggy(file.getSwf(),swf);Assert.fail();}catch(IOException ex){Assert.assertTrue(ex.getMessage().contains("font"));}
        DefineText2Tag text=find(swf,DefineText2Tag.class);text.textRecords.get(0).fontId=1;text.textRecords.get(0).glyphEntries.get(0).glyphAdvance=32768;
        try{IggyStaticTextConvertor.fromSwf(layout==2,text);Assert.fail();}catch(IOException ex){Assert.assertTrue(ex.getMessage().contains("16-bit"));}
        DefineBinaryDataTag binary=find(swf,DefineBinaryDataTag.class);binary.reserved=1;try{IggyBinaryDataConvertor.fromSwf(layout==2,binary);Assert.fail();}catch(IOException ex){Assert.assertTrue(ex.getMessage().contains("reserved"));}
    }

    @Test(dataProvider="layouts") public void canRevertPendingMetadataChanges(int layout)throws Exception{
        IggyFile file=load(fixture(layout).file());byte[] original=file.getSwf().getOriginalData();SWF before=reparse(IggyToSwfConvertor.getSwf(file));SWF edit=reparse(before);find(edit,DefineScalingGridTag.class).splitter.Xmax=2000;
        DefineSceneAndFrameLabelDataTag scene=new DefineSceneAndFrameLabelDataTag(edit);scene.sceneNames=new String[]{"temporary"};scene.sceneOffsets=new long[]{0};edit.addTag(1,scene);
        if(layout!=0){SymbolClassTag symbol=new SymbolClassTag(edit);symbol.tags.add(4);symbol.names.add("TemporaryBlob");edit.addTag(1,symbol);}
        IggyMovieTagConvertor.update(file.getSwf(),edit);IggyMovieTagConvertor.update(file.getSwf(),before);Assert.assertEquals(load(save(file)).getSwf().getOriginalData(),original);
    }

    @Test(dataProvider="layouts") public void canRemoveAndRestoreIntrinsicTextClass(int layout)throws Exception{
        Fixture f=fixture(layout);boolean w=layout==2;int p=w?8:4;byte[] before=f.data.getAllBytes();ByteBuffer source=ByteBuffer.wrap(before).order(ByteOrder.LITTLE_ENDIAN);long oldDictionary=source.getInt(0)&0xffffffffL;
        long table=f.allocate(6*p);f.ref(0,table);f.u32(w?64:layout==1?40:4,6);for(int i=0;i<5;i++){long field=oldDictionary+(long)i*p;long value=w?source.getLong((int)field):source.getInt((int)field);f.ref(table+(long)i*p,value==1?0:field+value);}
        long text=f.allocate(w?104:68);f.u8(text,6);f.u16(text+2,5);for(int off:w?new int[]{16,56,96}:new int[]{8,36,64})f.ref(text+off,0);String name="IntrinsicLabel";long string=f.allocate(2*(name.length()+1));for(int i=0;i<=name.length();i++)f.u16(string+2L*i,i==name.length()?0:name.charAt(i));f.ref(text+(w?16:8),string);f.ref(table+5*p,text);
        IggyFile file=load(f.file());SWF original=reparse(IggyToSwfConvertor.getSwf(file));SWF removed=reparse(original);remove(removed,SymbolClassTag.class);IggyMovieTagConvertor.update(file.getSwf(),removed);IggyMovieTagConvertor.update(file.getSwf(),original);
        IggyFile result=load(save(file));Assert.assertEquals(result.getSwf().getTexts().get(0).getSymbolClassName(),name);Assert.assertTrue(result.getSwf().getOriginalSymbolClasses().isEmpty(),"Intrinsic class must not migrate to a new root table");
    }
}
