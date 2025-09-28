/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.annotations.Table;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GenCommand tag - Command to Generator in Flash Templates.
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class GenCommandTag extends Tag {
    public static final int ID = 49;

    public static final String NAME = "GenCommand";
    
    public static final int TYPE_MOVIE = 0x01;
    public static final int TYPE_BUTTON = 0x02;
    public static final int TYPE_GRAPHICS = 0x04;
    public static final int TYPE_GLOBAL = 0x08;
    
    public static final int FLAG_LOOP = 0x0010;
    public static final int FLAG_PLAY_ONCE = 0x0020;
    public static final int FLAG_SINGLE_FRAME = 0x0040;
    
    
        
    public boolean typeMovie = false;

    public boolean typeButton = false;

    public boolean typeGraphics = false;

    public boolean typeGlobal = false;
    
    @SWFType(BasicType.UI16)
    public int depth = 0;
    
    @Conditional("typeGraphics")
    public boolean flagLoop = false;
    
    @Conditional("typeGraphics")
    public boolean flagPlayOnce = false;
    
    @Conditional("typeGraphics")
    public boolean flagSingleFrame = false;
        
    @SWFType(BasicType.UI16)
    @Conditional("typeGraphics")
    public int frameNum = 0;    
    
    public String command = "NoCommand";
    
    
    @SWFArray(value = "name", countField = "paramCount")
    @Table(value = "parameters", itemName = "parameter")
    public List<String> parameterNames = new ArrayList<>();

    @SWFArray(value = "value", countField = "paramCount")
    @Table(value = "parameters", itemName = "parameter")
    public List<String> commandValues = new ArrayList<>();
    
    
    public GenCommandTag(SWF swf) {
        super(swf, ID, NAME, null);       
    }
    
    public GenCommandTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        int type = sis.readUI16("type");
        typeMovie = (type & TYPE_MOVIE) > 0;
        typeButton = (type & TYPE_BUTTON) > 0;
        typeGraphics = (type & TYPE_GRAPHICS) > 0;
        typeGlobal = (type & TYPE_GLOBAL) > 0;
        depth = sis.readUI16("depth");
        if (typeGraphics) {
            int flags = sis.readUI16("flags");
            flagLoop = (flags & FLAG_LOOP) > 0;
            flagPlayOnce = (flags & FLAG_PLAY_ONCE) > 0;
            flagSingleFrame = (flags & FLAG_SINGLE_FRAME) > 0;
            
            frameNum = sis.readUI16("frameNum");
        }
        String fullCommandStr = sis.readString("command");
        if (!fullCommandStr.contains(" ")) {
            this.command = fullCommandStr;
            return;
        }
        this.command = fullCommandStr.substring(0, fullCommandStr.indexOf(" "));
        
        boolean inValue = false;
        StringBuilder sb = new StringBuilder();
        String name = "";
        for (int i = fullCommandStr.indexOf(" ") + 1; i < fullCommandStr.length(); i++) {
            Character c = fullCommandStr.charAt(i);
            Character cNext = i == fullCommandStr.length() - 1 ? null : fullCommandStr.charAt(i + 1);
            
            if (inValue) {
                
                if (c == '/' && cNext == 'n') {
                    sb.append('\n');
                    i++;
                } else if (c == '/' && cNext == 'r') {
                    sb.append('\r');
                    i++;
                } else if (c == '\\' && cNext != null) {
                    sb.append(cNext);
                    i++;
                } else if (c == '"') {
                    String value = sb.toString();
                    parameterNames.add(name);
                    commandValues.add(value);
                    sb = new StringBuilder();
                    inValue = false;                    
                    i++;
                } else {
                    sb.append(c);
                }
            } else if (c == '=' && cNext == '"') {            
                inValue = true;
                name = sb.toString();
                sb = new StringBuilder();
                i++;
            } else {
                sb.append(c);
            }
        }
    }
    
    public String getCommandString() {
        StringBuilder sb = new StringBuilder();
        sb.append(command);
        sb.append(" ");
        for (int i = 0; i < parameterNames.size(); i++) {
            String name = parameterNames.get(i);
            String value = commandValues.get(i);
            sb.append(name);
            sb.append("=\"");
            sb.append(value.replace("\n", "/n").replace("\r", "/r").replace("\\", "\\\\").replace("\"", "\\\""));
            sb.append("\"");
            sb.append(" ");
        }                
        return sb.toString();
    }

    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        int type = 0;
        if (typeMovie) {
            type |= TYPE_MOVIE;
        }
        if (typeButton) {
            type |= TYPE_BUTTON;
        }
        if (typeGraphics) {
            type |= TYPE_GRAPHICS;
        }
        if (typeGlobal) {
            type |= TYPE_GLOBAL;
        }
        sos.writeUI16(type);
        sos.writeUI16(depth);
        if (typeGraphics) {
            int flags = 0;
            if (flagLoop) {
                flags |= FLAG_LOOP;
            }
            if (flagPlayOnce) {
                flags |= FLAG_PLAY_ONCE;
            }
            if (flagSingleFrame) {
                flags |= FLAG_SINGLE_FRAME;
            }
            sos.writeUI16(flags);
            sos.writeUI16(frameNum);
        }
        sos.writeString(getCommandString());
    }

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        String shortCommand = command;
        if (shortCommand.contains(".")) {
            shortCommand = shortCommand.substring(shortCommand.lastIndexOf(".") + 1);
        }
        ret.put("cmd", shortCommand);
        ret.put("dpt", "" + depth);
        return ret;
    }
    
    
}
