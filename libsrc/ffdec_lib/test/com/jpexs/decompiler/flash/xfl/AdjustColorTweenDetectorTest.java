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
package com.jpexs.decompiler.flash.xfl;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AdjustColorTweenDetectorTest {
    private final FilterTweenDetectorTest helper=new FilterTweenDetectorTest();
    @Test
    public void detectsFourIndependentSignedChannels() throws Exception {
        StringBuilder xml=new StringBuilder();int[][] values=new int[31][4];
        for(int f=0;f<=30;f++) {
            double t=f/30.0,q=t+.5*(t<.5?t:1-t)*(1-2*t);
            values[f]=new int[]{(int)Math.round(-40+80*t*t*t),(int)Math.round(-30+60*t*t),(int)Math.round(-40+80*q),-90+6*f};
            xml.append(helper.frame(f,f==30?5:1,"<AdjustColorFilter brightness=\""+values[f][0]+"\" contrast=\""+values[f][1]+"\" saturation=\""+values[f][2]+"\" hue=\""+values[f][3]+"\"/>"));
        }
        Document doc=helper.detect(xml.toString());
        String[] names={"Brightness","Contrast","Saturation","Hue"};
        for(int c=0;c<4;c++) {
            NodeList keys=helper.property(doc,"AdjColor_"+names[c]).getElementsByTagName("Keyframe");
            Assert.assertEquals(keys.getLength(),31);
            for(int f=0;f<=30;f++) {
                Element key=(Element)keys.item(f);Assert.assertEquals(key.getAttribute("timevalue"),Integer.toString(f*1000));
                Assert.assertEquals(Double.parseDouble(key.getAttribute("anchor").split(",")[1]),(double)values[f][c]);
            }
        }
        Element hold=(Element)doc.getElementsByTagName("DOMFrame").item(1);
        Assert.assertEquals(hold.getAttribute("index"),"31");Assert.assertEquals(hold.getAttribute("duration"),"4");
    }
    @Test
    public void matchesCapturedNativeChannelNames() throws Exception {
        Document nativeDoc=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new java.io.File("testdata/motion_object/31_adjust_brightness/native_motion.xml"));
        for(String name:new String[]{"Brightness","Contrast","Saturation","Hue"}) {
            helper.property(nativeDoc,"AdjColor_"+name);
        }
        StringBuilder xml=new StringBuilder();for(int f=0;f<=30;f++)xml.append(helper.frame(f,1,"<AdjustColorFilter brightness=\""+(f-15)+"\"/>"));
        Document exported=helper.detect(xml.toString());
        for(String name:new String[]{"Brightness","Contrast","Saturation","Hue"})helper.property(exported,"AdjColor_"+name);
        NodeList properties=exported.getElementsByTagName("Property");
        for(int i=0;i<properties.getLength();i++)Assert.assertFalse(((Element)properties.item(i)).getAttribute("id").startsWith("AdjustColor_"));
    }
    @Test
    public void preservesDefaultsWithoutBlurSettings() throws Exception {
        StringBuilder xml=new StringBuilder();for(int f=0;f<=30;f++)xml.append(helper.frame(f,1,"<AdjustColorFilter hue=\""+(-90+f*6)+"\"/>"));
        Document doc=helper.detect(xml.toString());
        for(String name:new String[]{"Brightness","Contrast","Saturation"}) {
            NodeList keys=helper.property(doc,"AdjColor_"+name).getElementsByTagName("Keyframe");
            Assert.assertEquals(keys.getLength(),1);Assert.assertEquals(((Element)keys.item(0)).getAttribute("anchor"),"0,0");
        }
        NodeList properties=doc.getElementsByTagName("Property");
        for(int i=0;i<properties.getLength();i++){String id=((Element)properties.item(i)).getAttribute("id");Assert.assertFalse(id.equals("AdjustColor_Quality")||id.equals("AdjustColor_Knockout"));}
    }
    @Test
    public void preservesOrderWithSolidFilters() throws Exception {
        StringBuilder xml=new StringBuilder();for(int f=0;f<=30;f++)xml.append(helper.frame(f,1,"<BlurFilter/><AdjustColorFilter brightness=\""+(f-15)+"\"/><GlowFilter strength=\"1\"/>"));
        Document doc=helper.detect(xml.toString());StringBuilder order=new StringBuilder();NodeList nodes=doc.getElementsByTagName("PropertyContainer");
        for(int i=0;i<nodes.getLength();i++){String id=((Element)nodes.item(i)).getAttribute("id");if(id.endsWith("_Filter"))order.append(id).append(';');}
        Assert.assertEquals(order.toString(),"Blur_Filter;AdjustColor_Filter;Glow_Filter;");
    }
    @Test
    public void preservesInvalidDuplicatesAndIrregularSamples() throws Exception {
        for(String attributes:new String[]{"brightness=\"101\"","hue=\"181\"","contrast=\"1.5\"","saturation=\"NaN\"","quality=\"2\"","isEnabled=\"false\""}) {
            StringBuilder xml=new StringBuilder();
            String dynamic=attributes.startsWith("hue=")?"brightness":"hue";
            for(int f=0;f<15;f++)xml.append(helper.frame(f,1,"<AdjustColorFilter "+attributes+" "+dynamic+"=\""+f+"\"/>"));
            Assert.assertEquals(helper.detect(xml.toString()).getElementsByTagName("AnimationCore").getLength(),0);
        }
        for(boolean duplicate:new boolean[]{true,false}) {
            StringBuilder xml=new StringBuilder();for(int f=0;f<15;f++)xml.append(helper.frame(f,1,"<AdjustColorFilter hue=\""+(duplicate?f:f%2==0?-90:90)+"\"/>"+(duplicate?"<AdjustColorFilter/>":"")));
            Assert.assertEquals(helper.detect(xml.toString()).getElementsByTagName("AnimationCore").getLength(),0);
        }
    }
}
