/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.AppResources;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class FontHelper {
  
    /**
     * Gets all available fonts in the system
     * @return Map<FamilyName,Map<FontNAme,Font>>
     */
    public static Map<String,Map<String,Font>> getInstalledFonts(){
        Map<String,Map<String,Font>> ret = new HashMap<>();
        Font fonts[] = null;
                
        try {
            Class<?> clFmFactory = Class.forName("sun.font.FontManagerFactory");                
            Object fm = clFmFactory.getDeclaredMethod("getInstance").invoke(null);
            Class<?> clFm = Class.forName("sun.font.SunFontManager");

            //Delete cached installed names
            Field inField = clFm.getDeclaredField("installedNames");
            inField.setAccessible(true);
            inField.set(null, null);

            //Delete cached family names
            Field allFamField = clFm.getDeclaredField("allFamilies");
            allFamField.setAccessible(true);
            allFamField.set(fm,null);

            //Delete cached fonts
            Field allFonField = clFm.getDeclaredField("allFonts");
            allFonField.setAccessible(true);
            allFonField.set(fm, null);

            fonts = (Font[]) clFm.getDeclaredMethod("getAllInstalledFonts").invoke(fm);
        } catch (Throwable ex) {                
            //ignore            
        }
        if(fonts == null){
            fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        }        
        for(Font f:fonts){            
            String fam = f.getFamily(Locale.getDefault());
            //Do not want Java logical fonts
            if(Arrays.asList("Dialog","DialogInput","Monospaced","Serif","SansSerif").contains(fam)){
                continue;
            }
            if(!ret.containsKey(fam)){
                ret.put(fam, new HashMap<String, Font>());
            }
            String face = getFontFace(f);          
            ret.get(f.getFamily()).put(face, f);
        }
        return ret;
    }
    
    public static String getFontFace(Font f){
        String fam = f.getFamily(Locale.getDefault());
        String face = f.getFontName(Locale.getDefault());
        if(face.startsWith(fam)){
            face = face.substring(fam.length()).trim();
        } 
        if(face.startsWith(".")){
            face = face.substring(1);
        }
        return face;
    }
}
