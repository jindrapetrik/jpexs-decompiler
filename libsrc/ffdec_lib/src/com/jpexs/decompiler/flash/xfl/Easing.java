/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.math.BezierEdge;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class Easing extends JFrame {

    public Easing() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 800);
        setContentPane(new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                int ease = 100;
                
                //290
                GeneralPath gp = new GeneralPath();
                gp.moveTo(0, 290);
                g.setColor(Color.RED);
                for (int i = 0; i <= 100; i++) {
                    double ptSize = 290 / 100.0;
                    double pct = framePercentToRatio(i, ease);
                    //System.out.println("pct="+pct);
                    /*g.drawLine((int)Math.round(i*ptSize), 290-(int)Math.round(pct*ptSize),
                           (int)Math.round(i*ptSize), 290-(int)Math.round(pct*ptSize));                   */
                    gp.lineTo(i*ptSize, 290-pct*ptSize);
                }
                Graphics2D g2d = (Graphics2D)g;
                g2d.draw(gp);    
                g.setColor(Color.black);
                g.drawRect(0, 0, 290, 290);
            }
            
        });
    }
    
    
    
    public static double framePercentToRatio(double framePercent, int ease) {
        BezierEdge be = new BezierEdge(0,0,50,65535 / 2 + ease * 65535 / 100 / 2, 100,65535);
        BezierEdge line = new BezierEdge(framePercent, 0, framePercent, 65535);
        return be.getIntersections(line).get(0).getY();
    }
    
    public static double ratioToFramePercent(double ratio, int ease) {
        BezierEdge be = new BezierEdge(0,0,50,50 + ease / 2.0, 100,65535);
        BezierEdge line = new BezierEdge(0, ratio, 100, ratio);
        return be.getIntersections(line).get(0).getX();
    }
    
    public static Integer getEaseFromShapeRatios(List<Integer> ratios) {
        if (ratios.isEmpty()) {
            return null;
        }
        if (ratios.get(0) != 0) {
            ratios.add(0, 0);
        }
        if (ratios.get(ratios.size() - 1) != 65535) {
            ratios.add(65535);
        }
       
        int ease = 100;
        while(true) {
            double minDist = Double.MAX_VALUE;
            double maxDist = Double.MIN_VALUE;

            for (int f = 0; f < ratios.size(); f++) {
                double framePct = f * 100 / (double)(ratios.size() - 1);
                double tweenPct = ratios.get(f);
                double tweenPctShouldBe = Math.round(framePercentToRatio(framePct, ease));
                double dist = tweenPctShouldBe - tweenPct;
            
                if (dist > maxDist) {
                    maxDist = dist;
                }
                if (dist < minDist) {
                    minDist = dist;
                }
            }
            if (minDist > -5 && maxDist < 5) {
                break;
            }     
            if (ease == -100) {
                return null;
            }            
            ease--;
        }
        return ease;
    }
    
    public static void main(String[] args) throws Exception {
        
        SWF swf = new SWF(new FileInputStream("c:\\flash_testdata\\morphshape_ease\\morphshape_ease.swf"), true, false);
        List<Integer> ratios = new ArrayList<>();
        for (Tag t : swf.getTags()) {
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) t;
                int ratio = place.getRatio();
                if (ratio > -1) {
                    ratios.add(ratio);
                }
            }
        }
        
        Integer ease = getEaseFromShapeRatios(ratios);
        System.err.println("ease = " + ease);
        
        //System.setProperty("sun.java2d.uiScale", "1.0");
        //new Easing().setVisible(true);
    }
}
