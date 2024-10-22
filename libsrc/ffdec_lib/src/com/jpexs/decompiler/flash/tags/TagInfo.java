/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Information for basic tag info panel.
 *
 * @author JPEXS
 */
public class TagInfo {

    private SWF swf;
    private final Map<String, List<TagInfoItem>> infos = new LinkedHashMap<>();

    public TagInfo(SWF swf) {
        this.swf = swf;
    }

    public SWF getSwf() {
        return swf;
    }

    public void addInfo(String categoryName, String name, Object value) {
        categoryName = "general"; // temporary add everything to general category
        List<TagInfoItem> category = infos.get(categoryName);
        if (category == null) {
            category = new ArrayList<>();
            infos.put(categoryName, category);
        }

        category.add(new TagInfoItem(name, value));
    }

    public Map<String, List<TagInfoItem>> getInfos() {
        return infos;
    }

    public boolean isEmpty() {
        return infos.isEmpty();
    }

    public class TagInfoItem {

        private final String name;

        private final Object value;

        public TagInfoItem(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }
    }
}
