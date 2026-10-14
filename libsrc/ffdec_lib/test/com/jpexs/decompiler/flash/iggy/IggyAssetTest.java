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
import com.jpexs.decompiler.flash.iggy.conversion.*;
import com.jpexs.decompiler.flash.iggy.streams.*;
import com.jpexs.decompiler.flash.profiles.*;
import com.jpexs.decompiler.flash.tags.*;
import com.jpexs.decompiler.flash.tags.base.*;
import com.jpexs.decompiler.flash.tags.profiles.iggy.IggyExternalImage;
import com.jpexs.decompiler.flash.types.*;
import com.jpexs.decompiler.flash.types.shaperecords.*;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import javax.imageio.ImageIO;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyAssetTest {
    @DataProvider(name="layouts") public Object[][] layouts(){return new Object[][]{{0},{1},{2}};}
    private static final class Fixture {
        final boolean wide;final int layout;final MemoryDataStream data=new MemoryDataStream();final List<IggyFixup.Field> fields=new ArrayList<>();
        Fixture(int layout){this.layout=layout;wide=layout==2;}
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
    private static byte[] fixture(int layout,boolean external)throws Exception{
        Fixture f=new Fixture(layout);boolean w=f.wide;int p=w?8:4;f.allocate(w?184:layout==1?128:64);
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
    private static IggyFile load(byte[] bytes)throws Exception{Path path=Files.createTempFile("iggy-assets-", ".iggy");try{Files.write(path,bytes);return new IggyFile(path.toFile());}finally{Files.delete(path);}}
    private static byte[] save(IggyFile f)throws Exception{f.updateFlashEntry();MemoryDataStream out=new MemoryDataStream();f.writeToDataStream(out);return out.getAllBytes();}
    private static SWF reparse(SWF swf)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();swf.saveTo(out);return new SWF(new ByteArrayInputStream(out.toByteArray()),false,false,"UTF-8",new IggySwfProfile());}
    private static <T> T find(SWF swf,Class<T> type,int id){for(Tag tag:swf.getTags())if(type.isInstance(tag)&&(!(tag instanceof CharacterTag)||((CharacterTag)tag).getCharacterId()==id))return type.cast(tag);throw new AssertionError(type+" id="+id);}
    private static byte[] png(int argb)throws Exception{BufferedImage image=new BufferedImage(2,1,BufferedImage.TYPE_INT_ARGB);image.setRGB(0,0,argb);image.setRGB(1,0,argb);ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(image,"png",out);return out.toByteArray();}

    @Test(dataProvider="layouts") public void independentBinaryExportsShapesMorphsBitmapAndMp3(int layout)throws Exception{
        SWF swf=reparse(IggyToSwfConvertor.getSwf(load(fixture(layout,false))));Assert.assertEquals(swf.getSwfProfile().getId(),"iggy");
        DefineShape4Tag shape=find(swf,DefineShape4Tag.class,1);Assert.assertEquals(((RGBA)shape.shapes.fillStyles.fillStyles[0].color).alpha,78);Assert.assertEquals(shape.shapes.lineStyles.lineStyles2[0].width,30);
        boolean curve=false;for(SHAPERECORD r:shape.shapes.shapeRecords)if(r instanceof CurvedEdgeRecord){CurvedEdgeRecord c=(CurvedEdgeRecord)r;Assert.assertEquals(c.controlDeltaX,40);Assert.assertEquals(c.controlDeltaY,100);curve=true;}Assert.assertTrue(curve);
        DefineMorphShape2Tag morph=find(swf,DefineMorphShape2Tag.class,2);Assert.assertEquals(morph.endBounds.Xmax,480);Assert.assertEquals(morph.morphFillStyles.fillStyles[0].endColor.red,98);Assert.assertEquals(morph.morphLineStyles.lineStyles2[0].endWidth,50);
        DefineBitsLossless2Tag image=find(swf,DefineBitsLossless2Tag.class,3);Assert.assertEquals(image.getImageCached().getBufferedImage().getRGB(0,0),0xffff0000);
        DefineSoundTag sound=find(swf,DefineSoundTag.class,4);Assert.assertEquals(sound.soundFormat,2);Assert.assertEquals(sound.soundRate,2);Assert.assertEquals(sound.soundSampleCount,987L);Assert.assertEquals(sound.soundData.getRangeData(),new byte[]{(byte)254,(byte)255,17,34});
    }
    @Test(dataProvider="layouts") public void unchangedRoundTripPreservesOriginalGraphs(int layout)throws Exception{
        for(boolean external:new boolean[]{false,true}){byte[] original=fixture(layout,external);IggyFile file=load(original);SWF swf=reparse(IggyToSwfConvertor.getSwf(file));SwfToIggyConvertor.updateIggy(file.getSwf(),swf);Assert.assertEquals(save(file),original);}
    }
    @Test(dataProvider="layouts") public void editsAssetsAndAddsDictionaryEntriesWithCrossReferences(int layout)throws Exception{
        IggyFile file=load(fixture(layout,false));SWF swf=reparse(IggyToSwfConvertor.getSwf(file));
        DefineShape4Tag shape=find(swf,DefineShape4Tag.class,1);shape.shapes.fillStyles.fillStyles[0].color=new RGBA(100,110,120,130);shape.setModified(true);
        DefineMorphShape2Tag morph=find(swf,DefineMorphShape2Tag.class,2);morph.morphFillStyles.fillStyles[0].endColor=new RGBA(201,202,203,204);morph.setModified(true);
        find(swf,DefineBitsLossless2Tag.class,3).setImage(png(0xff0000ff));DefineSoundTag sound=find(swf,DefineSoundTag.class,4);sound.soundFormat=3;sound.soundData=new ByteArrayRange(new byte[]{1,2,3,4,5,6});sound.soundSampleCount=3;sound.setModified(true);
        DefineBitsLossless2Tag added=new DefineBitsLossless2Tag(swf);added.characterID=9;added.setImage(png(0xff00ff00));swf.addTag(added);
        FILLSTYLE fill=shape.shapes.fillStyles.fillStyles[0];fill.fillStyleType=64;fill.bitmapId=9;fill.bitmapMatrix=new MATRIX();fill.bitmapMatrix.hasScale=true;fill.bitmapMatrix.scaleX=fill.bitmapMatrix.scaleY=20;
        DefineShape4Tag addedShape=new DefineShape4Tag(swf);addedShape.shapeId=10;addedShape.shapes=shape.shapes;addedShape.shapeBounds=new RECT(shape.shapeBounds);addedShape.edgeBounds=new RECT(shape.edgeBounds);swf.addTag(addedShape);
        PlaceObject3Tag addedPlace=new PlaceObject3Tag(swf);addedPlace.depth=2;addedPlace.placeFlagHasCharacter=true;addedPlace.characterId=10;swf.addTag(addedPlace);
        SwfToIggyConvertor.updateIggy(file.getSwf(),reparse(swf));byte[] saved=save(file);IggyFile loaded=load(saved);SWF again=reparse(IggyToSwfConvertor.getSwf(loaded));
        Assert.assertEquals(loaded.getSwf().getDictionary().size(),11);Assert.assertEquals(find(again,DefineShape4Tag.class,1).shapes.fillStyles.fillStyles[0].bitmapId,9);Assert.assertEquals(find(again,DefineShape4Tag.class,10).shapes.fillStyles.fillStyles[0].bitmapId,9);
        Assert.assertEquals(find(again,DefineMorphShape2Tag.class,2).morphFillStyles.fillStyles[0].endColor.alpha,204);Assert.assertEquals(find(again,DefineBitsLossless2Tag.class,3).getImageCached().getBufferedImage().getRGB(0,0),0xff0000ff);Assert.assertEquals(find(again,DefineBitsLossless2Tag.class,9).getImageCached().getBufferedImage().getRGB(0,0),0xff00ff00);
        Assert.assertEquals(find(again,DefineSoundTag.class,4).soundData.getRangeData(),new byte[]{1,2,3,4,5,6});Assert.assertEquals(save(loaded),saved);
        PlaceObject3Tag place=find(again,PlaceObject3Tag.class,-1);Assert.assertEquals(place.characterId,1);
        boolean newPlacement=false;for(Tag tag:again.getTags())if(tag instanceof PlaceObject3Tag&&((PlaceObject3Tag)tag).depth==2){Assert.assertEquals(((PlaceObject3Tag)tag).characterId,10);newPlacement=true;}Assert.assertTrue(newPlacement);
    }
    @Test(dataProvider="layouts") public void allSoundCodecsPreservePayloadAndNativePcmByteOrder(int layout)throws Exception{
        for(int format:new int[]{0,1,2,3,4,5,6,11}){
            IggyFile file=load(fixture(layout,false));SWF swf=reparse(IggyToSwfConvertor.getSwf(file));DefineSoundTag sound=find(swf,DefineSoundTag.class,4);sound.soundFormat=format;sound.soundRate=3;sound.soundSize=true;sound.soundType=false;sound.soundSampleCount=2;sound.soundData=new ByteArrayRange(new byte[]{1,2,3,4});sound.setModified(true);
            SwfToIggyConvertor.updateIggy(file.getSwf(),reparse(swf));IggyFile loaded=load(save(file));DefineSoundTag result=find(IggyToSwfConvertor.getSwf(loaded),DefineSoundTag.class,4);Assert.assertEquals(result.soundFormat,format);Assert.assertEquals(result.soundData.getRangeData(),new byte[]{1,2,3,4});
            if(format==0){IggySwf movie=loaded.getSwf();long at=movie.getDictionary().get(4).getAddress();long raw=movie.getTimeline().reference(at+(layout==2?40:24));byte[] bytes=new byte[4];movie.getTimeline().bytes(raw,4).get(bytes);Assert.assertEquals(bytes,new byte[]{2,1,4,3});}
        }
    }
    @Test(dataProvider="layouts") public void externalTagIsProfileScopedAndCanBeReplacedByEmbeddedPixels(int layout)throws Exception{
        IggyFile file=load(fixture(layout,true));SWF swf=IggyToSwfConvertor.getSwf(file);IggyExternalImage image=find(swf,IggyExternalImage.class,3);Assert.assertEquals(image.width,2);Assert.assertEquals(image.resourceOffset,layout==0?-1L:0L);
        ByteArrayOutputStream out=new ByteArrayOutputStream();swf.saveTo(out);SWF standard=new SWF(new ByteArrayInputStream(out.toByteArray()),false);boolean unknown=false;for(Tag tag:standard.getTags())if(tag.getId()==IggyExternalImage.ID)unknown=tag instanceof UnknownTag;Assert.assertTrue(unknown);Assert.assertEquals(SwfProfiles.getMatchingProfiles(standard).get(0).getId(),"iggy");
        image=find(reparse(swf),IggyExternalImage.class,3);image.setImage(png(0xffffff00));swf=reparse(swf);find(swf,IggyExternalImage.class,3).setImage(png(0xffffff00));SwfToIggyConvertor.updateIggy(file.getSwf(),reparse(swf));IggyFile result=load(save(file));Assert.assertEquals(find(IggyToSwfConvertor.getSwf(result),DefineBitsLossless2Tag.class,3).getImageCached().getBufferedImage().getRGB(0,0),0xffffff00);if(layout!=0)Assert.assertTrue(result.getTextureMetadata().get(0).getRecords().isEmpty());
    }
    @Test public void externalResourcePreviewReadsRgbaAndBc7WithoutEmbeddingIt()throws Exception{
        Path dir=Files.createTempDirectory("iggy-resource-");Path path=dir.resolve("movie.iggy"),resource=dir.resolve("movie.raw.iggytex");
        try{Files.write(path,fixture(2,true));Files.write(resource,new byte[]{(byte)255,0,0,(byte)255,0,(byte)255,0,(byte)255});IggyFile file=new IggyFile(path.toFile());IggyExternalImage image=find(IggyToSwfConvertor.getSwf(file),IggyExternalImage.class,3);Assert.assertEquals(image.getImage().getBufferedImage().getRGB(0,0),0xffff0000);Assert.assertEquals(image.getImage().getBufferedImage().getRGB(1,0),0xff00ff00);
            // BC7 mode 5: two identical opaque red endpoints, zero indices.
            byte[] block=new byte[]{32,(byte)255,63,0,0,0,(byte)252,(byte)255,3,0,0,0,0,0,0,0};Files.write(resource,block);image.resourceFormat=0x10d;image.resourceWidth=image.resourceHeight=4;Assert.assertEquals(image.getImage().getBufferedImage().getRGB(0,0),0xffff0000);Assert.assertEquals(image.replacementImage.getLength(),0);
            // Copy straight-alpha colors without lossy premultiply/unpremultiply rounding.
            image.resourceFormat=0x100;image.resourceWidth=2;image.resourceHeight=1;
            Files.write(resource,new byte[]{37,83,(byte)129,1,0,0,0,0});
            Assert.assertEquals(image.getImage().getBufferedImage().getRGB(0,0),0x01255381);
        }finally{Files.deleteIfExists(resource);Files.deleteIfExists(path);Files.deleteIfExists(dir);}
    }

    private static MATRIX matrix(float scale,int x){MATRIX m=new MATRIX();m.hasScale=true;m.hasRotate=true;m.scaleX=scale;m.scaleY=scale/2;m.rotateSkew0=0.25f;m.rotateSkew1=-0.5f;m.translateX=x;m.translateY=-x;return m;}
    private static FILLSTYLE fill(int type){FILLSTYLE f=new FILLSTYLE();f.fillStyleType=type;if(type==0){f.color=new RGBA(1,2,3,4);}else if(type>=64){f.bitmapId=3;f.bitmapMatrix=matrix(20,37);}else{f.gradientMatrix=matrix(20,53);GRADIENT g=type==19?new FOCALGRADIENT():new GRADIENT();g.spreadMode=2;g.interpolationMode=1;GRADRECORD a=new GRADRECORD();a.ratio=10;a.color=new RGBA(11,22,33,44);GRADRECORD z=new GRADRECORD();z.ratio=240;z.color=new RGBA(55,66,77,88);g.gradientRecords=new GRADRECORD[]{a,z};if(type==19)((FOCALGRADIENT)g).focalPoint=-0.5f;f.gradient=g;}return f;}
    @Test(dataProvider="layouts") public void gradientBitmapFillsAndFilledStrokesRoundTrip(int layout)throws Exception{
        for(int type:new int[]{0,16,18,19,64,65,66,67}){
            IggyFile file=load(fixture(layout,false));SWF swf=reparse(IggyToSwfConvertor.getSwf(file));DefineShape4Tag shape=find(swf,DefineShape4Tag.class,1);shape.shapes.fillStyles.fillStyles[0]=fill(type);LINESTYLE2 line=shape.shapes.lineStyles.lineStyles2[0];line.hasFillFlag=true;line.fillType=fill(type);line.startCapStyle=1;line.endCapStyle=2;line.joinStyle=2;line.miterLimitFactor=3.5f;line.noHScaleFlag=true;line.pixelHintingFlag=true;line.noClose=true;shape.setModified(true);
            DefineMorphShape2Tag morph=find(swf,DefineMorphShape2Tag.class,2);MORPHFILLSTYLE mf=new MORPHFILLSTYLE();mf.fillStyleType=type;mf.startColor=new RGBA(1,2,3,4);mf.endColor=new RGBA(5,6,7,8);if(type>=64){mf.bitmapId=3;mf.startBitmapMatrix=matrix(20,37);mf.endBitmapMatrix=matrix(40,77);}else if(type!=0){mf.startGradientMatrix=matrix(20,53);mf.endGradientMatrix=matrix(40,97);MORPHGRADIENT g=type==19?new MORPHFOCALGRADIENT():new MORPHGRADIENT();g.spreadMode=2;g.interpolationMode=1;MORPHGRADRECORD r=new MORPHGRADRECORD();r.startRatio=11;r.endRatio=222;r.startColor=new RGBA(3,6,9,12);r.endColor=new RGBA(99,88,77,66);g.gradientRecords=new MORPHGRADRECORD[]{r};if(type==19){((MORPHFOCALGRADIENT)g).startFocalPoint=-0.25f;((MORPHFOCALGRADIENT)g).endFocalPoint=0.75f;}mf.gradient=g;}morph.morphFillStyles.fillStyles[0]=mf;MORPHLINESTYLE2 ml=morph.morphLineStyles.lineStyles2[0];ml.hasFillFlag=true;ml.fillType=mf;ml.joinStyle=2;ml.miterLimitFactor=4;ml.noVScaleFlag=true;morph.setModified(true);
            SWF expected=reparse(swf);IggyObjectData a=IggyVectorConvertor.fromSwf(expected,layout==2,find(expected,DefineShape4Tag.class,1)),ma=IggyVectorConvertor.fromSwf(expected,layout==2,find(expected,DefineMorphShape2Tag.class,2));SwfToIggyConvertor.updateIggy(file.getSwf(),expected);SWF result=reparse(IggyToSwfConvertor.getSwf(load(save(file))));IggyObjectData b=IggyVectorConvertor.fromSwf(result,layout==2,find(result,DefineShape4Tag.class,1)),mb=IggyVectorConvertor.fromSwf(result,layout==2,find(result,DefineMorphShape2Tag.class,2));Assert.assertEquals(b.data,a.data,"fill="+type);Assert.assertEquals(b.characters,a.characters);Assert.assertEquals(mb.data,ma.data,"morph fill="+type);Assert.assertEquals(mb.characters,ma.characters);
            FILLSTYLE actual=find(result,DefineShape4Tag.class,1).shapes.fillStyles.fillStyles[0];if(type>=64)Assert.assertEquals(actual.bitmapId,3);else if(type!=0){Assert.assertEquals(actual.gradient.spreadMode,2);Assert.assertEquals(((RGBA)actual.gradient.gradientRecords[0].color).alpha,44);if(type==19)Assert.assertEquals(((FOCALGRADIENT)actual.gradient).focalPoint,-0.5f);}
        }
    }

    @Test(dataProvider="layouts") public void changesExternalTextureMetadataWithoutRewritingMainBlock(int layout)throws Exception{
        if(layout==0)return;IggyFile file=load(fixture(layout,true));byte[] main=file.getSwf().getOriginalData();SWF swf=reparse(IggyToSwfConvertor.getSwf(file));IggyExternalImage image=find(swf,IggyExternalImage.class,3);image.resourceOffset=1234;image.resourceWidth=8;image.resourceHeight=4;image.resourceFormat=0x10d;image.setModified(true);SwfToIggyConvertor.updateIggy(file.getSwf(),reparse(swf));IggyFile loaded=load(save(file));Assert.assertEquals(loaded.getSwf().getOriginalData(),main);byte[] record=loaded.getTextureMetadata().get(0).getRecords().get(0).getData();Assert.assertEquals(record[0]&255,210);Assert.assertEquals(record[1]&255,4);Assert.assertEquals(record[6]&255,8);Assert.assertEquals(record[8]&255,4);
    }

    @Test(dataProvider="layouts") public void importsAdditionalStyleGroupsAndPreservesTheirIndices(int layout)throws Exception{
        IggyFile file=load(fixture(layout,false));SWF swf=reparse(IggyToSwfConvertor.getSwf(file));DefineShape4Tag shape=find(swf,DefineShape4Tag.class,1);List<SHAPERECORD> records=shape.shapes.shapeRecords;records.remove(records.size()-1);
        StyleChangeRecord styles=new StyleChangeRecord();styles.stateNewStyles=true;styles.fillStyles=new FILLSTYLEARRAY();styles.fillStyles.fillStyles=new FILLSTYLE[]{fill(0)};styles.lineStyles=new LINESTYLEARRAY();styles.lineStyles.lineStyles2=new LINESTYLE2[0];records.add(styles);
        StyleChangeRecord move=new StyleChangeRecord();move.stateMoveTo=true;move.moveDeltaX=300;move.moveDeltaY=400;move.stateFillStyle0=true;move.fillStyle0=1;move.stateFillStyle1=true;move.fillStyle1=0;move.stateLineStyle=true;move.lineStyle=0;records.add(move);StraightEdgeRecord edge=new StraightEdgeRecord();edge.deltaX=100;edge.deltaY=0;edge.generalLineFlag=true;edge.simplify();edge.calculateBits();records.add(edge);records.add(new EndShapeRecord());shape.setModified(true);
        SwfToIggyConvertor.updateIggy(file.getSwf(),reparse(swf));IggyFile loaded=load(save(file));DefineShape4Tag actual=find(reparse(IggyToSwfConvertor.getSwf(loaded)),DefineShape4Tag.class,1);Assert.assertEquals(actual.shapes.fillStyles.fillStyles.length,2);Assert.assertEquals(((RGBA)actual.shapes.fillStyles.fillStyles[1].color).alpha,4);boolean second=false;for(SHAPERECORD record:actual.shapes.shapeRecords)if(record instanceof StyleChangeRecord&&((StyleChangeRecord)record).fillStyle0==2)second=true;Assert.assertTrue(second);
    }

    @Test(dataProvider="layouts") public void promotesMismatchedMorphLinesToQuadratics(int layout)throws Exception{
        IggyFile file=load(fixture(layout,false));SWF swf=reparse(IggyToSwfConvertor.getSwf(file));DefineMorphShape2Tag morph=find(swf,DefineMorphShape2Tag.class,2);
        for(int i=0;i<morph.endEdges.shapeRecords.size();i++)if(morph.endEdges.shapeRecords.get(i) instanceof StraightEdgeRecord){CurvedEdgeRecord curve=new CurvedEdgeRecord();curve.controlDeltaX=200;curve.controlDeltaY=50;curve.anchorDeltaX=200;curve.anchorDeltaY=-50;curve.calculateBits();morph.endEdges.shapeRecords.set(i,curve);break;}morph.setModified(true);
        SwfToIggyConvertor.updateIggy(file.getSwf(),reparse(swf));DefineMorphShape2Tag result=find(IggyToSwfConvertor.getSwf(load(save(file))),DefineMorphShape2Tag.class,2);for(SHAPERECORD record:result.startEdges.shapeRecords)if(record instanceof CurvedEdgeRecord){Assert.assertEquals(((CurvedEdgeRecord)record).controlDeltaX,100);Assert.assertEquals(((CurvedEdgeRecord)record).controlDeltaY,0);return;}Assert.fail("Missing promoted edge");
    }

    @Test(dataProvider="layouts") public void convertsPngInJpegTagToLosslessForTheDll(int layout)throws Exception{
        IggyFile file=load(fixture(layout,false));SWF swf=reparse(IggyToSwfConvertor.getSwf(file));swf.removeTag(find(swf,DefineBitsLossless2Tag.class,3));DefineBitsJPEG2Tag image=new DefineBitsJPEG2Tag(swf);image.characterID=3;image.setImage(png(0xff123456));swf.addTag(image);SwfToIggyConvertor.updateIggy(file.getSwf(),reparse(swf));IggyFile loaded=load(save(file));DefineBitsLossless2Tag result=find(IggyToSwfConvertor.getSwf(loaded),DefineBitsLossless2Tag.class,3);Assert.assertEquals(result.getImageCached().getBufferedImage().getRGB(0,0),0xff123456);
    }

    @Test public void rebuildsTextureFixupsWhenEmbeddedReplacementRemovesResource()throws Exception{
        IggyFile original=load(fixture(2,true));IggyIndexBuilder index=new IggyIndexBuilder();List<IggyFixup.Field> fields=Arrays.asList(new IggyFixup.Field(0,4),new IggyFixup.Field(16,5),new IggyFixup.Field(22,4),new IggyFixup.Field(24,4),new IggyFixup.Field(26,4));IggyFixup.writeFields(index,fields,28);byte[] program=index.getIndexBytes();MemoryDataStream out=new MemoryDataStream();new IggyHeader(0x900,1,64,1,0,0,new byte[12],4).writeToDataStream(out);int offset=96;
        for(int i=0;i<4;i++){byte[] data=i==3?program:original.getEntryData(i);IggySubFileEntry entry=new IggySubFileEntry(i==0?1:i==2?8:0,data.length,0,offset);if(i==3)entry.targetIndex=1;entry.writeToDataStream(out);offset+=data.length;}for(int i=0;i<4;i++)out.writeBytes(i==3?program:original.getEntryData(i));IggyFile file=load(out.getAllBytes());SWF swf=reparse(IggyToSwfConvertor.getSwf(file));find(swf,IggyExternalImage.class,3).setImage(png(0xff765432));SwfToIggyConvertor.updateIggy(file.getSwf(),reparse(swf));IggyFile loaded=load(save(file));Assert.assertTrue(loaded.getTextureMetadata().get(0).getRecords().isEmpty());Assert.assertEquals(find(IggyToSwfConvertor.getSwf(loaded),DefineBitsLossless2Tag.class,3).getImageCached().getBufferedImage().getRGB(0,0),0xff765432);
    }

    @Test public void externalImageSurvivesXmlExportAndImport()throws Exception{
        SWF swf=IggyToSwfConvertor.getSwf(load(fixture(2,true)));Path xml=Files.createTempFile("iggy-profile-", ".xml");
        try{new com.jpexs.decompiler.flash.exporters.swf.SwfXmlExporter().exportXml(swf,xml.toFile());SWF imported=new SWF();try(InputStream in=Files.newInputStream(xml)){new com.jpexs.decompiler.flash.importers.SwfXmlImporter().importSwf(imported,in,xml.getParent().toFile());}IggyExternalImage image=find(imported,IggyExternalImage.class,3);Assert.assertEquals(image.width,2);Assert.assertEquals(image.resourceFormat,0x100);Assert.assertEquals(image.resourceOffset,0L);Assert.assertEquals(find(reparse(imported),IggyExternalImage.class,3).width,2);}finally{Files.deleteIfExists(xml);}
    }

    @Test public void bundleWriteBackUsesIggyProfile()throws Exception{
        Path path=Files.createTempFile("iggy-profile-bundle-", ".iggy");try{Files.write(path,fixture(2,true));IggySwfBundle bundle=new IggySwfBundle(path.toFile());String key=bundle.getKeys().iterator().next();SWF swf;try(InputStream in=bundle.getOpenable(key)){swf=new SWF(in,false,false,"UTF-8",new IggySwfProfile());}find(swf,IggyExternalImage.class,3).setImage(png(0xffabc123));ByteArrayOutputStream out=new ByteArrayOutputStream();swf.saveTo(out);Assert.assertTrue(bundle.putOpenable(key,new ByteArrayInputStream(out.toByteArray())));Assert.assertEquals(find(IggyToSwfConvertor.getSwf(new IggyFile(path.toFile())),DefineBitsLossless2Tag.class,3).getImageCached().getBufferedImage().getRGB(0,0),0xffabc123);}finally{Files.deleteIfExists(path);}
    }
}

