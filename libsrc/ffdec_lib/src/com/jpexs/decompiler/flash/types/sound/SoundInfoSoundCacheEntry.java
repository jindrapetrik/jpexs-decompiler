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
package com.jpexs.decompiler.flash.types.sound;

import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import java.util.Objects;

/**
 * Sound info and sound tag cache entry.
 *
 * @author JPEXS
 */
public class SoundInfoSoundCacheEntry {

    public SOUNDINFO soundInfo;
    public SoundTag soundTag;
    
    public SoundInfoSoundCacheEntry(SOUNDINFO soundInfo, SoundTag soundTag) {
        this.soundInfo = soundInfo;
        this.soundTag = soundTag;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.soundInfo);
        hash = 97 * hash + Objects.hashCode(this.soundTag);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SoundInfoSoundCacheEntry other = (SoundInfoSoundCacheEntry) obj;
        if (!Objects.equals(this.soundInfo, other.soundInfo)) {
            return false;
        }
        return Objects.equals(this.soundTag, other.soundTag);
    }

}
