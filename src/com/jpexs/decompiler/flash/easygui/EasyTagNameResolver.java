/*
 *  Copyright (C) 2024-2025 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.gui.TagNameResolverInterface;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;

/**
 *
 * @author JPEXS
 */
public class EasyTagNameResolver implements TagNameResolverInterface {

    @Override
    public String getTagName(Tag tag) {
        if (tag instanceof ImageTag) {
            ImageTag it = (ImageTag) tag;                                      
            return EasyStrings.translate("item.image") + " " + it.getCharacterId() + it.getImageFormat().getExtension();
        }
        if (tag instanceof ShapeTag) {
            ShapeTag st = (ShapeTag) tag;
            return EasyStrings.translate("item.graphic") + " " + st.getCharacterId();
        }
        if (tag instanceof MorphShapeTag) {
            MorphShapeTag mst = (MorphShapeTag) tag;
            return EasyStrings.translate("item.shapeTween") + " " + mst.getCharacterId();
        }
        if (tag instanceof TextTag) {
            TextTag t = (TextTag) tag;
            return EasyStrings.translate("item.text") + " " + t.getCharacterId();
        }
        if (tag instanceof FontTag) {
            FontTag f = (FontTag) tag;
            return EasyStrings.translate("item.font") + " " + f.getCharacterId();
        }
        if (tag instanceof DefineSpriteTag) {
            DefineSpriteTag st = (DefineSpriteTag) tag;
            return EasyStrings.translate("item.movieClip") + " " + st.getCharacterId();
        }
        if (tag instanceof ButtonTag) {
            ButtonTag bt = (ButtonTag) tag;
            return EasyStrings.translate("item.button") + " " + bt.getCharacterId();
        }
        if (tag instanceof SoundTag) {
            SoundTag st = (SoundTag) tag;
            return EasyStrings.translate("item.sound") + (st.getCharacterId() == -1 ? "" : " " + st.getCharacterId());
        }
        if (tag instanceof DefineVideoStreamTag) {
            DefineVideoStreamTag vt = (DefineVideoStreamTag) tag;
            return EasyStrings.translate("item.video") + " " + vt.getCharacterId();
        }
        return EasyStrings.translate("item.unknown");
    }    
}
