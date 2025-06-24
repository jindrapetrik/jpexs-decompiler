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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.ecma.Undefined;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an ActionScript object.
 *
 * @author JPEXS
 */
public class ActionScriptObject implements Cloneable {

    /**
     * Properties of the object
     */
    protected Map<Integer, Object> properties = new HashMap<>();

    /**
     * Members of the object
     */
    protected Map<String, Object> members = new HashMap<>();

    /**
     * Object that this object extends
     */
    protected Object extendsObj;

    /**
     * Objects that this object implements
     */
    protected List<Object> implementsObjs = new ArrayList<>();

    /**
     * Constructor.
     */
    public ActionScriptObject() {

    }

    /**
     * Clears all members of the object
     */
    public void clearMembers() {
        for (Object o : members.values()) {
            if (o instanceof ActionScriptObject) {
                ((ActionScriptObject) o).clear();
            }
        }
        members.clear();
    }

    /**
     * Clears all properties of the object
     */
    public void clearProperties() {
        properties.clear();
    }

    /**
     * Clears all members and properties of the object
     */
    public void clear() {
        clearMembers();
        clearProperties();
    }

    /**
     * Gets implements objects
     *
     * @return Implements objects
     */
    public List<Object> getImplementsObjs() {
        return implementsObjs;
    }

    /**
     * Sets implements objects
     *
     * @param implementsObjs Implements objects
     */
    public void setImplementsObjs(List<Object> implementsObjs) {
        this.implementsObjs = implementsObjs;
    }

    /**
     * Sets extends object
     *
     * @param extendsObj Extends object
     */
    public void setExtendsObj(Object extendsObj) {
        this.extendsObj = extendsObj;
    }

    /**
     * Gets extends object
     *
     * @return Extends object
     */
    public Object getExtendsObj() {
        return extendsObj;
    }

    /**
     * Removes a member from the object
     *
     * @param path Path to the member
     */
    public void removeMember(String path) {
        String[] pathParts;
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

    /**
     * Enumerates all members of the object
     *
     * @return List of member names
     */
    public List<String> enumerate() {
        return new ArrayList<>(members.keySet());
    }

    /**
     * Sets a property of the object
     *
     * @param index Index of the property
     * @param value Value of the property
     */
    public void setProperty(int index, Object value) {
        properties.put(index, value);
    }

    /**
     * Gets a property of the object
     *
     * @param index Index of the property
     * @return Value of the property
     */
    public Object getProperty(int index) {
        if (!properties.containsKey(index)) {
            return Undefined.INSTANCE;
        }
        return properties.get(index);
    }

    /**
     * Sets a member of the object
     *
     * @param path Path to the member
     * @param value Value of the member
     */
    public void setMember(String path, Object value) {
        String[] pathParts;
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

    /**
     * Clones the object
     *
     * @return Cloned object
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            //ignore
        }
        return null;
    }

    /**
     * Gets path to a member
     *
     * @param obj Member
     * @return Path to the member
     */
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

    /**
     * Gets a member of the object
     *
     * @param name Name of the member
     * @return Member
     */
    protected Object getThisMember(String name) {
        return members.get(name);
    }

    /**
     * Gets a member of the object
     *
     * @param path Path to the member
     * @return Member
     */
    public Object getMember(String path) {
        String[] pathParts;
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
