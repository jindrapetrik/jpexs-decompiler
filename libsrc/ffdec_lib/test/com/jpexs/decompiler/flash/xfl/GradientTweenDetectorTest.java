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
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class GradientTweenDetectorTest {
    private final FilterTweenDetectorTest helper = new FilterTweenDetectorTest();
    private String entries(int f, int count) {
        String xml = "<GradientEntry color=\"#2040c0\" alpha=\"0\" ratio=\"0\"/>"
                + "<GradientEntry color=\"" + String.format("#%02xc020", 128 + f) + "\" alpha=\"" + (160 + f) / 255.0
                + "\" ratio=\"" + (64 + f * 3) / 255.0 + "\"/>";
        if (count == 4) xml += "<GradientEntry color=\"#40c0e0\" ratio=\"0.9\"/>";
        return xml + "<GradientEntry color=\"#e04020\" ratio=\"1\"/>";
    }
    @Test
    public void writesNativeGradientColorsAndIndexes() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f=0; f<=30; f++) xml.append(helper.frame(f,1,"<GradientGlowFilter>"+entries(f,3)+"</GradientGlowFilter>"));
        Document doc=helper.detect(xml.toString());
        NodeList keys=helper.property(doc,"GradientGlow_Gradient").getElementsByTagName("Keyframe");
        Assert.assertEquals(keys.getLength(),31);
        for (int f=0; f<=30; f++) {
            Element key=(Element)keys.item(f);
            Assert.assertEquals(key.getAttribute("timevalue"),Integer.toString(f*1000));
            Assert.assertEquals(key.getAttribute("indexes"),"0,"+(64+f*3)+",255");
            Assert.assertEquals(key.getAttribute("colors"),String.format("0x2040c000,0x%02xc020%02x,0xe04020ff",128+f,160+f));
            Assert.assertFalse(key.hasAttribute("anchor"));
        }
    }
    @Test
    public void preservesNativeTypesAndConstantGradient() throws Exception {
        for (String prefix:new String[]{"GradientGlow","GradientBevel"}) {
            int type=0;
            for(String mode:new String[]{"inner","outer","full"}) {
                StringBuilder xml=new StringBuilder();
                for(int f=0; f<=30; f++) xml.append(helper.frame(f,1,"<"+prefix+"Filter type=\""+mode+"\" blurX=\""+f+"\">"+entries(0,3)+"</"+prefix+"Filter>"));
                Document doc=helper.detect(xml.toString());
                Assert.assertEquals(helper.property(doc,prefix+"_Type").getAttribute("value"),Integer.toString(type++));
                Assert.assertEquals(helper.property(doc,prefix+"_Gradient").getElementsByTagName("Keyframe").getLength(),1);
                Assert.assertEquals(helper.property(doc,prefix+"_BlurX").getElementsByTagName("Keyframe").getLength(),31);
            }
        }
        Document nativeDoc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("testdata/motion_object/25_gradient_glow/native_motion.xml"));
        Element key=(Element)helper.property(nativeDoc,"GradientGlow_Gradient").getElementsByTagName("Keyframe").item(0);
        Assert.assertEquals(key.getAttribute("colors"),"0x2040c000,0x80c020a0,0xe04020ff");
        Assert.assertEquals(key.getAttribute("indexes"),"0,128,255");
    }
    @Test
    public void splitsAtTopologyAndModeChanges() throws Exception {
        for(boolean topology:new boolean[]{true,false}) {
            StringBuilder xml=new StringBuilder();
            for(int f=0; f<30; f++) xml.append(helper.frame(f,1,"<GradientBevelFilter type=\""+(!topology&&f>=15?"outer":"inner")+"\">"+entries(f,topology&&f>=15?4:3)+"</GradientBevelFilter>"));
            NodeList frames=helper.detect(xml.toString()).getElementsByTagName("DOMFrame");
            for(int i=0;i<frames.getLength();i++) {
                Element frame=(Element)frames.item(i);int first=Integer.parseInt(frame.getAttribute("index"));
                int count=frame.hasAttribute("duration")?Integer.parseInt(frame.getAttribute("duration")):1;
                Assert.assertTrue(first>=15||first+count<=15);
            }
        }
    }
    @Test
    public void retainsHeldGradientSamplesAndFinalHold() throws Exception {
        StringBuilder xml=new StringBuilder();
        for(int f=0;f<=30;f++) {
            int held=(int)Math.round(f/3.0);
            String body=entries(held,3).replace("ratio=\""+(64+held*3)/255.0+"\"", "ratio=\""+(64+held)/255.0+"\"");
            xml.append(helper.frame(f,f==30?5:1,"<GradientGlowFilter>"+body+"</GradientGlowFilter>"));
        }
        Document doc=helper.detect(xml.toString());
        NodeList keys=helper.property(doc,"GradientGlow_Gradient").getElementsByTagName("Keyframe");
        Assert.assertEquals(keys.getLength(),31);
        Assert.assertEquals(((Element)keys.item(1)).getAttribute("colors"),((Element)keys.item(0)).getAttribute("colors"));
        Element hold=(Element)doc.getElementsByTagName("DOMFrame").item(1);
        Assert.assertEquals(hold.getAttribute("index"),"31");Assert.assertEquals(hold.getAttribute("duration"),"4");
    }
    @Test
    public void rejectsMalformedAndIrregularGradients() throws Exception {
        for(String body:new String[]{"", "<GradientEntry color=\"#112233\" unknown=\"1\"/>",
                "<GradientEntry color=\"#112233\" ratio=\"0.8\"/><GradientEntry color=\"#223344\" ratio=\"0.2\"/>",
                "<GradientEntry color=\"#112233\" alpha=\"2\"/>","<GradientEntry color=\"invalid\"/>"}) {
            StringBuilder xml=new StringBuilder();
            for(int f=0;f<15;f++)xml.append(helper.frame(f,1,"<GradientGlowFilter blurX=\""+f+"\">"+body+"</GradientGlowFilter>"));
            Assert.assertEquals(helper.detect(xml.toString()).getElementsByTagName("AnimationCore").getLength(),0);
        }
        StringBuilder xml=new StringBuilder();
        for(int f=0;f<15;f++)xml.append(helper.frame(f,1,"<GradientGlowFilter>"+entries(f%2==0?0:40,3)+"</GradientGlowFilter>"));
        Assert.assertEquals(helper.detect(xml.toString()).getElementsByTagName("AnimationCore").getLength(),0);
    }
    @Test
    public void retainsOrderedGradientAndSolidFilterStack() throws Exception {
        StringBuilder xml=new StringBuilder();
        for(int f=0;f<=30;f++)xml.append(helper.frame(f,1,"<GradientGlowFilter type=\"outer\">"+entries(f,3)+"</GradientGlowFilter><BlurFilter blurX=\"4\"/><GradientBevelFilter type=\"full\">"+entries(0,3)+"</GradientBevelFilter>"));
        Document doc=helper.detect(xml.toString());StringBuilder order=new StringBuilder();
        NodeList containers=doc.getElementsByTagName("PropertyContainer");
        for(int i=0;i<containers.getLength();i++){String id=((Element)containers.item(i)).getAttribute("id");if(id.endsWith("_Filter"))order.append(id).append(';');}
        Assert.assertEquals(order.toString(),"GradientGlow_Filter;Blur_Filter;GradientBevel_Filter;");
        Assert.assertEquals(helper.property(doc,"GradientGlow_Type").getAttribute("value"),"1");
        Assert.assertEquals(helper.property(doc,"GradientBevel_Type").getAttribute("value"),"2");
    }
}
