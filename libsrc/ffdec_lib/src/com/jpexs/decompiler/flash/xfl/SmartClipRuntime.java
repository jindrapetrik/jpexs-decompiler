/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.helpers.ByteArrayRange;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Recognizes shipped Flash 5 Smart Clips without relying on lost Library names.
 * Only runtime scripts and their timeline structure are compared. Skins and
 * instance parameter values may differ; arbitrary user clips stay ordinary clips.
 */
final class SmartClipRuntime {

    private final SWF swf;

    private final Map<Integer, String> signatures = new HashMap<>();

    SmartClipRuntime(SWF swf) {
        this.swf = swf;
    }

    String signature(DefineSpriteTag sprite) throws InterruptedException {
        return signature(sprite, new HashSet<>());
    }

    private String signature(DefineSpriteTag sprite, Set<Integer> visiting) throws InterruptedException {
        String cached = signatures.get(sprite.spriteId);
        if (cached != null) {
            return cached;
        }
        if (!visiting.add(sprite.spriteId)) {
            return "cycle";
        }
        StringBuilder scripts = new StringBuilder();
        int frame = 0;
        for (Tag tag : sprite.getTags()) {
            ComponentConverter.checkInterrupted();
            if (tag instanceof ShowFrameTag) {
                frame++;
            } else if (tag instanceof DoActionTag) {
                scripts.append(frame).append(":frame:");
                appendBytes(scripts, ((DoActionTag) tag).getActionBytes());
            } else if (tag instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) tag;
                if (place.getCharacterId() >= 0) {
                    CharacterTag child = swf.getCharacter(place.getCharacterId());
                    if (child instanceof DefineSpriteTag) {
                        String childSignature = signature((DefineSpriteTag) child, visiting);
                        if (!childSignature.isEmpty()) {
                            scripts.append(frame).append(':').append(place.getDepth())
                                    .append(":child:").append(childSignature).append(';');
                        }
                    }
                }
                if (place.getClipActions() != null) {
                    for (CLIPACTIONRECORD record : place.getClipActions().clipActionRecords) {
                        scripts.append(frame).append(':').append(place.getDepth())
                                .append(':').append(record.eventFlags.getHeader(record.keyCode, false)).append(':');
                        appendBytes(scripts, record.getActionBytes());
                    }
                }
            }
        }
        visiting.remove(sprite.spriteId);
        String result = scripts.length() == 0 ? "" : digest(scripts.toString());
        signatures.put(sprite.spriteId, result);
        return result;
    }

    private static void appendBytes(StringBuilder text, ByteArrayRange bytes) {
        for (byte value : bytes.getRangeData()) {
            text.append(Character.forDigit((value & 255) >> 4, 16));
            text.append(Character.forDigit(value & 15, 16));
        }
        text.append(';');
    }

    private static String digest(String text) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            appendBytes(result, new ByteArrayRange(bytes));
            return result.substring(0, result.length() - 1);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
