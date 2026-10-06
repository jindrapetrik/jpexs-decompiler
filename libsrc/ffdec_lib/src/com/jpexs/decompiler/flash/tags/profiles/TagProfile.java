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
package com.jpexs.decompiler.flash.tags.profiles;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.Map;

/**
 * Profile for reading custom tags
 * 
 * @author JPEXS
 */
public interface TagProfile {
    /**
     * Gets tag profile unique identifier
     * @return 
     */
    public String getId();        
    
    /**
     * Gets tag profile name for display
     * @return 
     */
    public String getName();
    
    /**
     * Decodes tag
     * @param sis Input stream
     * @param tagId Tag id
     * @param data Original data
     * @return Decoded tag
     * @throws IOException On I/O error 
     */
    public Tag decodeTag(SWFInputStream sis, int tagId, ByteArrayRange data) throws IOException;
    
    /**
     * Gets all supported tag
     * @return Mapping from tag id to tag class
     */
    public Map<Integer, Class<? extends Tag>> getSupportedTags();
    
    /**
     * Checks whether this profile supports tag id.
     * @param tagId Tag id
     * @return True when supports
     */
    public boolean supportsTagId(int tagId);
    
    /**
     * Gets class of the tag id
     * @param tagId Tag id
     * @return Tag class
     */
    public Class<? extends Tag> getTagIdClass(int tagId);
    
    /**
     * Checks whether the SWF has specific parameters to support this tag.
     * @param swf
     * @return True when supports
     */
    public boolean supportsSwf(SWF swf);
}
