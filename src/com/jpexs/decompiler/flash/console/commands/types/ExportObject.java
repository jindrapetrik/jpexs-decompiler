/*
 * Copyright (C) 2024 JPEXS
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
package com.jpexs.decompiler.flash.console.commands.types;

import com.jpexs.decompiler.flash.exporters.modes.BinaryDataExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ButtonExportMode;
import com.jpexs.decompiler.flash.exporters.modes.Font4ExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FrameExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ImageExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MorphShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SymbolClassExportMode;
import com.jpexs.decompiler.flash.exporters.modes.TextExportMode;
import com.jpexs.decompiler.flash.types.sound.SoundExportFormat;
import com.jpexs.decompiler.flash.xfl.FLAVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public enum ExportObject {
    SCRIPT(ScriptExportMode.class),
    IMAGE(ImageExportMode.class),
    SHAPE(ShapeExportMode.class),
    MORPHSHAPE(MorphShapeExportMode.class),
    MOVIE(MovieExportMode.class),
    FONT(FontExportMode.class),
    FONT4(Font4ExportMode.class),
    FRAME(FrameExportMode.class),
    SPRITE(SpriteExportMode.class),
    BUTTON(ButtonExportMode.class),
    SOUND(SoundExportMode.class),
    BINARYDATA(BinaryDataExportMode.class),
    SYMBOLCLASS(SymbolClassExportMode.class),
    TEXT(TextExportMode.class),
    ALL(null),
    FLA(FLAVersion.class),
    XFL(FLAVersion.class);
    
    private Class formatsEnum;
    
    ExportObject(Class formatsEnum) {
        this.formatsEnum = formatsEnum;
    }

    public List<Object> getAllowedFormats() {
        if (formatsEnum == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(formatsEnum.getEnumConstants());
    }
    public List<String> getAllowedFormatsAsStr() {
        if (formatsEnum == null) {
            return new ArrayList<>();
        }
        List<String> ret = new ArrayList<>();
        for (Object o : formatsEnum.getEnumConstants()) {
            ret.add(o.toString().toLowerCase());
        }
        return ret;
    }
}
