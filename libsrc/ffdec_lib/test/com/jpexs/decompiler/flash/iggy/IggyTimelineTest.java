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
import com.jpexs.decompiler.flash.tags.*;
import com.jpexs.decompiler.flash.types.*;
import com.jpexs.decompiler.flash.types.filters.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyTimelineTest {
    @DataProvider(name="layouts") public Object[][] layouts() { return new Object[][]{{0},{1},{2}}; }
    private static final class Fixture {
        MemoryDataStream data = new MemoryDataStream();
        List<IggyFixup.Field> fields = new ArrayList<>();
        boolean wide; int layout; long text,sprite,place,importPlace;
        Fixture(int layout) { this.layout=layout;wide=layout==2; }
        void put(long at,long value,int width,int op)throws Exception {
            long end=data.position();data.seek(at,SeekMode.SET);
            if(width==8)data.writeUI64(value);else if(width==4)data.writeUI32(value);else if(width==2)data.writeUI16((int)value);else data.writeUI8((int)value);
            data.seek(end,SeekMode.SET);if(op!=0)fields.add(new IggyFixup.Field(at,op));
        }
        void u16(long at,int v)throws Exception{put(at,v,2,4);}
        void u32(long at,long v)throws Exception{put(at,v,4,5);}
        void f32(long at,float v)throws Exception{u32(at,Float.floatToIntBits(v));}
        void ref(long at,long target)throws Exception{put(at,target==0?1:target-at,wide?8:4,2);}
        long allocate(int n)throws Exception{data.pad8bytes();long at=data.position();data.writeBytes(new byte[n]);return at;}
        long string(String value)throws Exception{long at=allocate((value.length()+1)*2);for(int i=0;i<value.length();i++)u16(at+2L*i,value.charAt(i));u16(at+2L*value.length(),0);return at;}
        long node(int type,int size)throws Exception{long at=allocate(size);ref(at,0);put(at+(wide?8:4),type,1,0);return at;}
    }
    private static byte[] fixture(int layout)throws Exception{
        return fixture(layout, 0);
    }
    private static byte[] fixture(int layout, int classFlags)throws Exception{
        Fixture f=new Fixture(layout);boolean w=f.wide;int p=w?8:4;
        f.allocate(w?184:layout==1?128:64);
        int[] roots=w?new int[]{0,8,16,24,32,40,112,120,136,144,152,160}:layout==1?new int[]{0,4,8,12,16,20,80,84,96,100,104,108}:new int[]{0,8,12,44,56,60};
        for(int at:roots)f.ref(at,0);
        f.u32(w?64:layout==1?40:4,4);f.u32(w?68:layout==1?44:32,3);
        if(layout!=0)f.f32(w?88:64,24);
        long dictionary=f.allocate(4*p);f.ref(0,dictionary);for(int i=0;i<4;i++)f.ref(dictionary+p*i,0);
        f.text=f.allocate(w?104:68);f.put(f.text,0x0001ff06,4,0);
        for(int at:w?new int[]{16,56,96}:new int[]{8,36,64})f.ref(f.text+at,0);
        f.ref(dictionary+p,f.text);
        f.sprite=f.allocate(w?72:36);f.put(f.sprite,0x0002ff03,4,0);
        for(int at:w?new int[]{16,32,48,56,64}:new int[]{8,16,24,28,32})f.ref(f.sprite+at,0);
        f.u16(f.sprite+(w?40:20),2);f.u16(f.sprite+(w?42:22),5);f.ref(dictionary+2*p,f.sprite);

        f.place=f.node(2,classFlags==0?(w?64:48):(w?88:60)); // Include the class pointer only when used.
        f.put(f.place+p+1,1|classFlags,1,3);f.put(f.place+p+2,127,1,0);f.put(f.place+p+3,15,1,0);
        f.u32(f.place+p+4,2);f.u16(f.place+(w?40:36),3);f.ref(f.place+(w?48:40),f.sprite);
        int m=w?16:12;for(int i=0;i<6;i++)f.f32(f.place+m+4*i,new float[]{1,0.25f,-0.5f,1.5f,1.25f,-2.5f}[i]);
        if(classFlags!=0)f.ref(f.place+(w?80:56),f.string("ui.MyClass"));
        long label=f.node(5,w?24:12);f.put(label+p+1,1,1,0);f.ref(label+(w?16:8),f.string("beginning"));f.ref(f.place,label);
        long show=f.node(1,w?16:8);f.ref(label,show);
        long ratio=f.node(4,w?24:16);f.u16(ratio+p+4,3);f.u16(ratio+p+6,12345);f.u32(ratio+p+8,2);f.ref(show,ratio);
        f.importPlace=f.node(2,w?64:48);f.put(f.importPlace+p+1,4,1,3);f.put(f.importPlace+p+2,127,1,0);f.put(f.importPlace+p+3,15,1,0);
        f.u32(f.importPlace+p+4,2);f.u16(f.importPlace+(w?40:36),4);f.ref(f.importPlace+(w?48:40),layout==0?f.text:0);f.ref(ratio,f.importPlace);
        long sound=f.node(6,w?56:40);int start=w?12:8;f.u16(sound+start,7);f.put(sound+start+3,1,1,0);
        f.ref(sound+(w?16:12),0);f.u32(sound+(w?24:16),10);f.u32(sound+(w?28:20),1000);f.u32(sound+(w?36:28),3);f.u32(sound+(w?40:32),1);
        long env=f.allocate(8);f.u32(env,123);f.u16(env+4,100);f.u16(env+6,200);f.ref(sound+(w?48:36),env);f.ref(f.importPlace,sound);
        long show2=f.node(1,w?16:8);f.ref(sound,show2);
        long remove=f.node(3,w?24:12);f.u16(remove+p+2,3);f.ref(remove+(w?16:8),f.sprite);f.ref(show2,remove);
        long show3=f.node(1,w?16:8);f.ref(remove,show3);
        f.ref(w?16:8,f.place);f.ref(w?24:12,show3);
        long spriteShow=f.node(1,w?16:8),spriteShow2=f.node(1,w?16:8);f.ref(spriteShow,spriteShow2);
        f.ref(f.sprite+(w?56:28),spriteShow);f.ref(f.sprite+(w?64:32),spriteShow2);
        if(layout!=0){
            long fixes=f.allocate(p+4),entries=f.allocate(8);f.ref(fixes,entries);f.u32(fixes+p,1);
            f.u32(entries,f.importPlace+(w?48:40));f.u32(entries+4,3);f.ref(w?136:96,fixes);
        }
        byte[] payload=f.data.getAllBytes();IggyIndexBuilder index=new IggyIndexBuilder();IggyFixup.writeFields(index,f.fields,payload.length);
        byte[][]blocks={payload,index.getIndexBytes()};MemoryDataStream out=new MemoryDataStream();
        new IggyHeader(0x900,1,w?64:32,1,0,0,new byte[12],2).writeToDataStream(out);int offset=64;
        for(int i=0;i<2;i++){new IggySubFileEntry(i==0?1:0,blocks[i].length,0,offset).writeToDataStream(out);offset+=blocks[i].length;}
        out.writeBytes(blocks[0]);out.writeBytes(blocks[1]);return out.getAllBytes();
    }
    private static IggyFile load(byte[] bytes)throws Exception{Path p=Files.createTempFile("iggy-timeline", ".iggy");try{Files.write(p,bytes);return new IggyFile(p.toFile());}finally{Files.delete(p);}}
    private static byte[] save(IggyFile file)throws Exception{file.updateFlashEntry();MemoryDataStream out=new MemoryDataStream();file.writeToDataStream(out);return out.getAllBytes();}
    private static SWF swf(SWF input)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();input.saveTo(out);return new SWF(new ByteArrayInputStream(out.toByteArray()),false);}
    private static List<Tag> controls(Iterable<Tag> tags){List<Tag>r=new ArrayList<>();for(Tag t:tags)if(t instanceof PlaceObject3Tag||t instanceof FrameLabelTag||t instanceof ShowFrameTag||t instanceof RemoveObject2Tag||t instanceof StartSoundTag||t instanceof StartSound2Tag)r.add(t);return r;}

    @Test(dataProvider="layouts") public void exportsRootSpritesMatricesLabelsImportsRatioAndSound(int layout)throws Exception{
        IggyFile file=load(fixture(layout));SWF swf=swf(IggyToSwfConvertor.getSwf(file));List<Tag> tags=controls(swf.getTags());
        Assert.assertEquals(tags.size(),9);PlaceObject3Tag p=(PlaceObject3Tag)tags.get(0);
        Assert.assertEquals(p.characterId,2);Assert.assertEquals(p.depth,3);Assert.assertTrue(p.placeFlagHasMatrix);
        Assert.assertEquals(p.matrix.scaleX,1f);Assert.assertEquals(p.matrix.scaleY,1.5f);Assert.assertEquals(p.matrix.rotateSkew0,-0.5f);Assert.assertEquals(p.matrix.rotateSkew1,0.25f);
        Assert.assertEquals(p.matrix.translateX,25);Assert.assertEquals(p.matrix.translateY,-50);
        FrameLabelTag label=(FrameLabelTag)tags.get(1);Assert.assertEquals(label.name,"beginning");Assert.assertTrue(label.namedAnchor);
        PlaceObject3Tag ratio=(PlaceObject3Tag)tags.get(3);Assert.assertTrue(ratio.placeFlagMove);Assert.assertEquals(ratio.ratio,12345);
        Assert.assertEquals(((PlaceObject3Tag)tags.get(4)).characterId,layout==0?1:3);
        SOUNDINFO sound=((StartSoundTag)tags.get(5)).soundInfo;Assert.assertEquals(sound.inPoint,10L);Assert.assertEquals(sound.outPoint,1000L);Assert.assertEquals(sound.loopCount,3);Assert.assertTrue(sound.syncNoMultiple);Assert.assertEquals(sound.envelopeRecords[0].pos44,123L);
        Assert.assertEquals(((RemoveObject2Tag)tags.get(7)).depth,3);
        DefineSpriteTag sprite=null;for(Tag t:swf.getTags())if(t instanceof DefineSpriteTag)sprite=(DefineSpriteTag)t;
        Assert.assertNotNull(sprite);Assert.assertEquals(sprite.spriteId,2);Assert.assertEquals(sprite.frameCount,2);Assert.assertEquals(controls(sprite.getTags()).size(),2);
    }

    @Test(dataProvider="layouts") public void unchangedSwfRoundTripPreservesOriginalCompactLists(int layout)throws Exception{
        byte[] bytes=fixture(layout);IggyFile file=load(bytes);SWF swf=swf(IggyToSwfConvertor.getSwf(file));
        SwfToIggyConvertor.updateIggy(file.getSwf(),swf);Assert.assertEquals(save(file),bytes);
    }

    @Test(dataProvider="layouts") public void editsRootAndSpriteListsAndRebuildsRelocations(int layout)throws Exception{
        IggyFile file=load(fixture(layout));SWF swf=IggyToSwfConvertor.getSwf(file);List<Tag> tags=controls(swf.getTags());
        PlaceObject3Tag p=(PlaceObject3Tag)tags.get(0);p.depth=9;p.matrix.translateX=123;p.placeFlagHasName=true;p.name="longer instance name";
        p.placeFlagHasClipDepth=true;p.clipDepth=12;p.placeFlagHasBlendMode=true;p.blendMode=8;p.placeFlagHasCacheAsBitmap=true;p.bitmapCache=1;
        p.placeFlagHasColorTransform=true;CXFORMWITHALPHA cx=new CXFORMWITHALPHA();cx.hasAddTerms=true;cx.hasMultTerms=true;cx.redMultTerm=128;cx.greenMultTerm=256;cx.blueMultTerm=384;cx.alphaMultTerm=192;cx.redAddTerm=-12;cx.greenAddTerm=5;cx.blueAddTerm=7;cx.alphaAddTerm=-3;p.colorTransform=cx;
        p.placeFlagHasFilterList=true;p.surfaceFilterList=Arrays.<FILTER>asList(new BLURFILTER());
        ((FrameLabelTag)tags.get(1)).name="edited label";
        DefineSpriteTag sprite=null;for(Tag t:swf.getTags())if(t instanceof DefineSpriteTag)sprite=(DefineSpriteTag)t;
        PlaceObject2Tag child=new PlaceObject2Tag(swf);child.placeFlagHasCharacter=true;child.characterId=1;child.depth=5;child.placeFlagHasMatrix=true;child.matrix=new MATRIX();sprite.addTag(0,child);
        SwfToIggyConvertor.updateIggy(file.getSwf(),swf(swf));byte[] saved=save(file);IggyFile loaded=load(saved);
        SWF again=swf(IggyToSwfConvertor.getSwf(loaded));List<Tag> changed=controls(again.getTags());PlaceObject3Tag first=(PlaceObject3Tag)changed.get(0);
        Assert.assertEquals(first.depth,9);Assert.assertEquals(first.matrix.translateX,123);Assert.assertEquals(first.name,"longer instance name");Assert.assertEquals(first.clipDepth,12);Assert.assertEquals(first.blendMode,8);Assert.assertTrue(first.placeFlagHasCacheAsBitmap);
        Assert.assertEquals(first.colorTransform.redMultTerm,128);Assert.assertEquals(first.colorTransform.alphaAddTerm,-3);Assert.assertEquals(first.surfaceFilterList.get(0).id,1);
        Assert.assertEquals(((FrameLabelTag)changed.get(1)).name,"edited label");Assert.assertEquals(loaded.getSwf().getFrameLabels().get(0).getName(),"edited label");
        Assert.assertEquals(((PlaceObject3Tag)changed.get(4)).characterId,layout==0?1:3);
        Assert.assertEquals(loaded.getSwf().getSpriteTimelines().get(2).getCommands().get(0).getType(),2);
        Assert.assertEquals(save(loaded),saved);
    }

    @Test(dataProvider="layouts") public void allFiltersRoundTripThroughDllLayouts(int layout)throws Exception{
        IggyFile file=load(fixture(layout));SWF swf=new SWF();PlaceObject3Tag place=new PlaceObject3Tag(swf);
        place.placeFlagMove=true;place.depth=3;place.placeFlagHasFilterList=true;
        DROPSHADOWFILTER drop=new DROPSHADOWFILTER();drop.angle=1.25;drop.distance=2.5;drop.innerShadow=true;drop.knockout=true;
        GLOWFILTER glow=new GLOWFILTER();glow.strength=2.5f;
        BEVELFILTER bevel=new BEVELFILTER();bevel.onTop=true;bevel.knockout=true;bevel.angle=1.25;bevel.distance=2.5;
        GRADIENTGLOWFILTER gg=new GRADIENTGLOWFILTER();gg.angle=1.25;gg.distance=2.5;
        CONVOLUTIONFILTER conv=new CONVOLUTIONFILTER();conv.matrixX=2;conv.matrixY=2;conv.matrix=new float[]{1,2,3,4};
        COLORMATRIXFILTER cm=new COLORMATRIXFILTER();cm.matrix[4]=25.5f;
        GRADIENTBEVELFILTER gb=new GRADIENTBEVELFILTER();gb.angle=1.25;gb.distance=2.5;
        place.surfaceFilterList=Arrays.<FILTER>asList(drop,new BLURFILTER(),glow,bevel,gg,conv,cm,gb);
        List<Tag> tags=Arrays.<Tag>asList(place,new ShowFrameTag(swf));
        IggyTimeline.Replacement replacement=IggyTimelineConvertor.fromSwf(swf,layout==2,tags,1);
        IggyTimeline t=new IggyTimeline(replacement.data,replacement.first,layout==2);
        PlaceObject3Tag result=(PlaceObject3Tag)IggyTimelineConvertor.toSwf(swf,file.getSwf(),t,1).get(0);
        Assert.assertEquals(result.surfaceFilterList.size(),8);
        for(int i=0;i<8;i++)Assert.assertEquals(result.surfaceFilterList.get(i).id,i);
        Assert.assertTrue(((DROPSHADOWFILTER)result.surfaceFilterList.get(0)).innerShadow);
        Assert.assertTrue(((DROPSHADOWFILTER)result.surfaceFilterList.get(0)).knockout);
        Assert.assertEquals(((GLOWFILTER)result.surfaceFilterList.get(2)).strength,2.5f);
        Assert.assertTrue(((BEVELFILTER)result.surfaceFilterList.get(3)).onTop);
        Assert.assertEquals(((CONVOLUTIONFILTER)result.surfaceFilterList.get(5)).matrix,new float[]{1,2,3,4});
        Assert.assertEquals(((COLORMATRIXFILTER)result.surfaceFilterList.get(6)).matrix[4],25.5f,0.00001f);
        IggyTimeline.Replacement repeated=IggyTimelineConvertor.fromSwf(swf,layout==2,Arrays.<Tag>asList(result,new ShowFrameTag(swf)),1);
        Assert.assertEquals(repeated.data,replacement.data);
    }

    @Test public void classNameUsesTheExistingFlashPlayerEncoding()throws Exception{
        SWF swf=new SWF();swf.version=10;swf.frameCount=1;
        PlaceObject3Tag p=new PlaceObject3Tag(swf);p.placeFlagHasCharacter=true;p.characterId=17;p.placeFlagHasClassName=true;p.className="ui.MyClass";p.depth=2;swf.addTag(p);swf.addTag(new ShowFrameTag(swf));swf.addTag(new EndTag(swf));
        PlaceObject3Tag decoded=(PlaceObject3Tag)controls(swf(swf).getTags()).get(0);
        Assert.assertEquals(decoded.getCharacterId(),-1);Assert.assertEquals(decoded.className,"ui.MyClass");
    }

    @Test(dataProvider="layouts") public void timelineEditsPreserveIggyClassBindingsWithFlashPlayerEncoding(int layout)throws Exception{
        for(int flags:new int[]{32,8}) {
            byte[] original=fixture(layout,flags);IggyFile unchanged=load(original);
            SWF unedited=swf(IggyToSwfConvertor.getSwf(unchanged));
            SwfToIggyConvertor.updateIggy(unchanged.getSwf(),unedited);Assert.assertEquals(save(unchanged),original);
            IggyFile file=load(original);SWF swf=swf(IggyToSwfConvertor.getSwf(file));
            PlaceObject3Tag place=(PlaceObject3Tag)controls(swf.getTags()).get(0);
            if(flags==32){Assert.assertEquals(place.getCharacterId(),-1);Assert.assertEquals(place.getClassName(),"ui.MyClass");}
            else{Assert.assertEquals(place.getCharacterId(),2);Assert.assertNull(place.getClassName());}
            FrameLabelTag label=new FrameLabelTag(swf);label.name="Edited";swf.addTag(0,label);
            SwfToIggyConvertor.updateIggy(file.getSwf(),swf(swf));IggySwf reopened=load(save(file)).getSwf();
            IggyTimeline timeline=reopened.getTimeline();IggyTimeline.Command command=timeline.getCommands().get(1);
            long field=command.getAddress()+(layout==2?48:40);
            Assert.assertEquals(reopened.getDictionaryId(field,timeline.reference(field)),2);
            Assert.assertEquals(timeline.string(timeline.reference(command.getAddress()+(layout==2?80:56))),"ui.MyClass");
        }
    }

    @Test(dataProvider="layouts") public void insertsRemovesCommandsAndChangesFrameCounts(int layout)throws Exception{
        IggyFile file=load(fixture(layout));SWF swf=IggyToSwfConvertor.getSwf(file);
        Tag removed=null;DefineSpriteTag sprite=null;
        for(Tag tag:swf.getTags()) { if(tag instanceof RemoveObject2Tag)removed=tag;if(tag instanceof DefineSpriteTag)sprite=(DefineSpriteTag)tag; }
        swf.removeTag(removed);swf.addTag(swf.getTags().size()-1,new ShowFrameTag(swf));swf.frameCount=4;
        sprite.addTag(sprite.getTags().size()-1,new ShowFrameTag(swf));sprite.frameCount=3;
        SwfToIggyConvertor.updateIggy(file.getSwf(),swf(swf));byte[] saved=save(file);IggyFile reopened=load(saved);
        Assert.assertEquals(reopened.getSwf().getHdr().getFrameCount(),4L);
        Assert.assertEquals(reopened.getSwf().getTimelineFrameCount(2),3);
        int shows=0;for(IggyTimeline.Command c:reopened.getSwf().getTimeline().getCommands()){if(c.getType()==1)shows++;Assert.assertNotEquals(c.getType(),3);}
        Assert.assertEquals(shows,4);Assert.assertEquals(reopened.getSwf().getSpriteTimelines().get(2).getCommands().size(),3);
        Assert.assertEquals(save(reopened),saved);
    }

    @Test public void boundsChecksCatchTruncatedAuxiliariesAndCycles()throws Exception{
        for(boolean wide:new boolean[]{false,true}) {
            MemoryDataStream out=new MemoryDataStream();out.writeBytes(new byte[8]);long at=8;
            if(wide)out.writeUI64(1);else out.writeUI32(1);
            out.writeUI8(2);out.writeUI8(0);out.writeUI8(127);out.writeUI8(15);
            try{new IggyTimeline(out.getAllBytes(),at,wide);Assert.fail("Expected truncated placement");}catch(IOException expected){}
            MemoryDataStream cycle=new MemoryDataStream();cycle.writeBytes(new byte[8]);
            if(wide)cycle.writeUI64(0);else cycle.writeUI32(0);cycle.writeUI8(1);cycle.writeBytes(new byte[wide?7:3]);
            try{new IggyTimeline(cycle.getAllBytes(),8,wide);Assert.fail("Expected timeline cycle");}catch(IOException expected){}
        }
    }
}
