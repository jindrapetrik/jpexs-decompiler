/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class ActionScriptObject implements Cloneable {

    protected Map<Integer, Object> properties = new HashMap<>();

    protected Map<String, Object> members = new HashMap<>();

    protected Object extendsObj;

    protected List<Object> implementsObjs = new ArrayList<>();

    public void clearMembers() {
        for (Object o : members.values()) {
            if (o instanceof ActionScriptObject) {
                ((ActionScriptObject) o).clear();
            }
        }
        members.clear();
    }

    public void clearProperties() {
        properties.clear();
    }

    public void clear() {
        clearMembers();
        clearProperties();
    }

    public List<Object> getImplementsObjs() {
        return implementsObjs;
    }

    public void setImplementsObjs(List<Object> implementsObjs) {
        this.implementsObjs = implementsObjs;
    }

    public void setExtendsObj(Object extendsObj) {
        this.extendsObj = extendsObj;
    }

    public Object getExtendsObj() {
        return extendsObj;
    }

    public void removeMember(String path) {
        String pathParts[];
        if (path.startsWith("/")) {
            pathParts = path.substring(1).split("/");
        } else {
            pathParts = path.split(".");
        }
        ActionScriptObject obj = this;
        for (int i = 0; i < pathParts.length; i++) {
            String part = pathParts[i];
            Object member = obj.getMember(part);
            if (i == pathParts.length - 1) {
                obj.members.remove(part);
            } else if (member instanceof ActionScriptObject) {
                obj = (ActionScriptObject) member;
            } else {
                break;
            }
        }
    }

    public List<String> enumerate() {
        return new ArrayList<>(members.keySet());
    }

    public void setProperty(int index, Object value) {
        properties.put(index, value);
    }

    public Object getProperty(int index) {
        if (!properties.containsKey(index)) {
            return Undefined.INSTANCE;
        }
        return properties.get(index);
    }

    public void setMember(String path, Object value) {
        String pathParts[];
        if (path.startsWith("/")) {
            pathParts = path.substring(1).split("/");
        } else {
            pathParts = path.split(".");
        }
        ActionScriptObject obj = this;
        for (int i = 0; i < pathParts.length; i++) {
            String part = pathParts[i];
            Object member = obj.getMember(part);
            if (i == pathParts.length - 1) {
                obj.members.put(part, value);
            } else if (member instanceof ActionScriptObject) {
                obj = (ActionScriptObject) member;
            } else {
                break;
            }
        }
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            //ignore
        }
        return null;
    }

    public String getMemberPath(Object obj) {
        if (obj == this) {
            return "";
        }

        for (String memberName : members.keySet()) {
            Object member = members.get(memberName);
            if (member == obj) {
                return memberName;
            }
            if (member instanceof ActionScriptObject) {
                String ret = ((ActionScriptObject) member).getMemberPath(obj);
                if (ret != null) {
                    return memberName + "." + ret;
                }
            }
        }
        return null;
    }

    protected Object getThisMember(String name) {
        return members.get(name);
    }

    public Object getMember(String path) {
        String pathParts[];
        if (path.startsWith("/")) {
            pathParts = path.substring(1).split("/");
        } else {
            pathParts = path.split(".");
        }
        ActionScriptObject obj = this;
        for (int i = 0; i < pathParts.length; i++) {
            String part = pathParts[i];
            if (i == pathParts.length - 1) {
                return obj.getThisMember(part);
            } else {
                Object member = obj.getMember(part);
                if (member instanceof ActionScriptObject) {
                    obj = (ActionScriptObject) member;
                } else {
                    break;
                }
            }
        }
        return Undefined.INSTANCE;
    }
}
