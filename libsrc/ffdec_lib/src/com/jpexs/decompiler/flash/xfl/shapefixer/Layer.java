/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jpexs.decompiler.flash.xfl.shapefixer;

import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class Layer {
    FILLSTYLEARRAY fillStyleArray = null;
    LINESTYLEARRAY lineStyleArray = null; 
    List<Path> paths = new ArrayList<>();  
    
    public void round(boolean wasSmall) {
        for (Path p : paths) {
            p.round(wasSmall);
        }
    }
}
