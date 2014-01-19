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

import com.jpexs.helpers.StreamSearch;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class BinarySWFBundle implements SWFBundle {
    private final SWFSearch search;
    public BinarySWFBundle(InputStream is){
        search=new SWFSearch(new StreamSearch(is));        
    }

    @Override
    public int length() {
        return search.length();
    }

    @Override
    public Set<String> getKeys() {
        Set<String> ret=new HashSet<>();
        for(int i=0;i<length();i++){
            ret.add("["+i+"]");
        }
        return ret;
    }

    @Override
    public SWF getSWF(String key) {
        if(!key.startsWith("[")){
            return null;
        }
        if(!key.endsWith("]")){
            return null;
        }
        key = key.substring(1,key.length()-1);
        try{
            int index=Integer.parseInt(key);
            if(index<0 || index>=length()){
                return null;
            }
            return search.get(null, index);
        }catch(IOException iex){
            return null;
        }catch(NumberFormatException nfe){
            return null;
        }
    }

    @Override
    public Map<String, SWF> getAll() {
        Map<String,SWF> ret=new HashMap<>();
        for(String key:getKeys()){
            ret.put(key, getSWF(key));
        }
        return ret;
    }

    @Override
    public String getExtension() {
        return "bin";
    }
    
}
