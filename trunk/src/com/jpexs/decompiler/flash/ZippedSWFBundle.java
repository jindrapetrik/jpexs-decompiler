/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jpexs.decompiler.flash;

import com.jpexs.helpers.ReReadableInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author JPEXS
 */
public class ZippedSWFBundle implements SWFBundle {
    protected Set<String> keySet=new HashSet<>();
    private final Map<String,SWF> cachedSWFs=new HashMap<>();
    protected ReReadableInputStream is;

    
    public ZippedSWFBundle(InputStream is){
        this.is = new ReReadableInputStream(is);
        ZipInputStream zip=new ZipInputStream(this.is);
            ZipEntry entry;
        try {
            while((entry = zip.getNextEntry())!=null)
            {
                if(entry.getName().toLowerCase().endsWith(".swf")
                || entry.getName().toLowerCase().endsWith(".gfx")){
                    keySet.add(entry.getName());
                }
                //streamMap.put(, is)
            }
        } catch (IOException ex) {
            Logger.getLogger(ZippedSWFBundle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public int length() {
        return keySet.size();
    }

    @Override
    public Set<String> getKeys() {
        return keySet;
    }

    @Override
    public SWF getSWF(String key) {
        if(!keySet.contains(key)){
            return null;
        }
        if(!cachedSWFs.containsKey(key)){
            
            ZipInputStream zip=new ZipInputStream(this.is);
            ZipEntry entry;
            try {
                while((entry = zip.getNextEntry())!=null)
                {
                    if(entry.getName().equals(key)){
                        try {
                            cachedSWFs.put(key, new SWF(zip, null,false));
                        } catch (IOException ex) {
                            Logger.getLogger(ZippedSWFBundle.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ZippedSWFBundle.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ZippedSWFBundle.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        }
        return cachedSWFs.get(key);
    }

    @Override
    public Map<String, SWF> getAll() {
        for(String key:getKeys()){ //cache everything first
            getSWF(key);
        }
        return cachedSWFs;
    }

    @Override
    public String getExtension() {
        return "zip";
    }
    
}
