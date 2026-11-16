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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.GenCommandTag;
import com.jpexs.decompiler.flash.tags.NameCharacterTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.flash.fla.reader.BinaryFlaDocument;
import com.jpexs.flash.fla.reader.FlaDocumentBuilder;
import com.jpexs.flash.fla.reader.FlaRecord;
import com.jpexs.flash.fla.reader.FlaStream;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.xml.stream.XMLStreamException;

/**
 * Carries Generator objects around the temporary XFL, which has no native
 * representation for them. Private proxy instances are replaced in FlaComDoc's
 * model before writing binary FLA; they never enter the exported document.
 */
final class GeneratorTemplateConverter {

    private final String proxy = "__ffdec_generator_" + UUID.randomUUID().toString();
    private final Map<GenCommandTag, String> markers = new IdentityHashMap<>();
    private final Map<String, GenCommandTag> commands = new LinkedHashMap<>();
    private final Map<GenCommandTag, Map<String, String>> attachedMarkers = new IdentityHashMap<>();
    private final Map<String, String> attachedNames = new LinkedHashMap<>();
    private final Set<String> frameMarkers = new java.util.HashSet<>();
    private final Set<String> removableMarkers = new java.util.HashSet<>();
    private final Set<Object> consumedLibraries = new java.util.HashSet<>();
    private final Set<CharacterTag> attachedCharacters = Collections.newSetFromMap(new IdentityHashMap<CharacterTag, Boolean>());
    private final Set<Object> graphicLibraries = new java.util.HashSet<>();
    private final Set<Object> movieLibraries = new java.util.HashSet<>();
    private final List<GenCommandTag> globals = new ArrayList<>();
    private boolean hasObjects;

    GeneratorTemplateConverter(ReadOnlyTagList tags) {
        collect(tags);
    }

    boolean isEmpty() {
        return !hasObjects && globals.isEmpty();
    }

    private void collect(ReadOnlyTagList tags) {
        Map<Integer, CharacterTag> depths = new LinkedHashMap<>();
        for (Tag tag : tags) {
            if (tag instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag placement = (PlaceObjectTypeTag) tag;
                if (placement.getCharacterId() >= 0) {
                    depths.put(placement.getDepth(), tag.getSwf().getCharacter(placement.getCharacterId()));
                }
            } else if (tag instanceof RemoveTag) {
                depths.remove(((RemoveTag) tag).getDepth());
            }
            if (tag instanceof GenCommandTag) {
                GenCommandTag command = (GenCommandTag) tag;
                if (command.typeGlobal) {
                    globals.add(command);
                } else {
                    hasObjects = true;
                    CharacterTag character = depths.get(command.depth);
                    if (character != null && !isObjectCommand(character)) {
                        attachedCharacters.add(character);
                    }
                }
            } else if (tag instanceof DefineSpriteTag) {
                collect(((DefineSpriteTag) tag).getTags());
            }
        }
    }

    void keepAttachedCharactersInLibrary(List<CharacterTag> nonLibraryShapes) {
        nonLibraryShapes.removeAll(attachedCharacters);
    }

    void library(XFLXmlWriter writer, Map<String, byte[]> files) throws XMLStreamException {
        if (!hasObjects) {
            return;
        }
        XFLXmlWriter symbol = new XFLXmlWriter();
        symbol.writeStartElement("DOMSymbolItem", new String[]{"xmlns", "http://ns.adobe.com/xfl/2008/", "name", proxy});
        symbol.writeStartElement("timeline");
        symbol.writeStartElement("DOMTimeline");
        symbol.writeStartElement("layers");
        symbol.writeStartElement("DOMLayer");
        symbol.writeStartElement("frames");
        symbol.writeStartElement("DOMFrame", new String[]{"index", "0"});
        symbol.writeEndElement();
        for (int i = 0; i < 6; i++) {
            symbol.writeEndElement();
        }
        files.put(proxy + ".xml", Utf8Helper.getBytes(symbol.toString()));
        writer.writeStartElement("Include", new String[]{"href", proxy + ".xml"});
        writer.writeEndElement();
    }

    void instance(GenCommandTag command, MATRIX matrix, XFLXmlWriter writer) throws XMLStreamException {
        instance(command, proxy, matrix, writer);
    }

    private void instance(GenCommandTag command, String libraryName, MATRIX matrix,
            XFLXmlWriter writer) throws XMLStreamException {
        String marker = markers.get(command);
        if (marker == null) {
            marker = proxy + "_" + markers.size();
            markers.put(command, marker);
            commands.put(marker, command);
        }
        MATRIX m = matrix == null ? new MATRIX() : matrix;
        writer.writeStartElement("DOMSymbolInstance", new String[]{"libraryItemName", libraryName, "name", marker});
        writer.writeStartElement("matrix");
        writer.writeStartElement("Matrix", new String[]{
            "a", Double.toString((m.hasScale ? m.scaleX : 1)), "b", Double.toString((m.hasRotate ? m.rotateSkew0 : 0)),
            "c", Double.toString((m.hasRotate ? m.rotateSkew1 : 0)), "d", Double.toString((m.hasScale ? m.scaleY : 1)),
            "tx", Double.toString(m.translateX / 20.0), "ty", Double.toString(m.translateY / 20.0)});
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }
        
    void objectInstance(GenCommandTag command, CharacterTag character, String libraryName,
            MATRIX matrix, XFLXmlWriter writer) throws XMLStreamException {
        instance(command, character == null ? proxy : libraryName, matrix, writer);
        if (character != null && character.getSwf().getCharacterIdTag(character.getCharacterId(), NameCharacterTag.ID) == null) {
            removableMarkers.add(markers.get(command));
        }
    }

    boolean isObjectCommand(CharacterTag character) {
        return character == null || character instanceof DefineSpriteTag
                && ((DefineSpriteTag) character).getTags().isEmpty();
    }

    String attachedName(GenCommandTag command, String originalName) {
        Map<String, String> names = attachedMarkers.computeIfAbsent(command, key -> new LinkedHashMap<>());
        String marker = names.get(originalName);
        if (marker == null) {
            marker = proxy + "_attached_" + commands.size();
            names.put(originalName, marker);
            commands.put(marker, command);
            attachedNames.put(marker, originalName);
        }
        return marker;
    }

    static boolean isMovieSettings(GenCommandTag command) {
        return command.command.equals("com.macromedia.generator.commands.MovieSetCommand");
    }

    void frameCommand(GenCommandTag command, XFLXmlWriter writer) throws XMLStreamException {
        instance(command, null, writer);
        frameMarkers.add(markers.get(command));
    }

    BinaryFlaDocument restore(BinaryFlaDocument document) {
        Set<FlaRecord> visited = Collections.newSetFromMap(new IdentityHashMap<FlaRecord, Boolean>());
        for (FlaStream stream : document.getStreams().values()) {
            replace(stream.getRoot(), visited);
        }
        publishSettings(document);
        List<FlaRecord> library = new ArrayList<>(document.getLibraryDescriptors());
        for (FlaRecord item : library) {
            Object id = item.get("libraryId");
            if (graphicLibraries.contains(id) && !movieLibraries.contains(id)) {
                item.set("symbolType", 0);
            }
        }
        Set<String> removedStreams = new java.util.HashSet<>();
        Set<Object> referencedLibraries = new java.util.HashSet<>();
        visited.clear();
        for (FlaStream stream : document.getStreams().values()) {
            collectLibraryReferences(stream.getRoot(), referencedLibraries, visited);
        }
        library.removeIf(item -> {
            Object id = item.get("libraryId");
            if (proxy.equals(item.get("name")) || consumedLibraries.contains(id)
                    && !referencedLibraries.contains(id) && !hasLinkage(item)) {
                removedStreams.add((String) item.get("streamName"));
                return true;
            }
            return false;
        });
        document.getContents().set("library", library);
        FlaDocumentBuilder builder = new FlaDocumentBuilder(document.getAnsiCharset());
        for (FlaStream stream : document.getStreams().values()) {
            if (!removedStreams.contains(stream.getPath())) {
                builder.stream(stream.getPath(), stream.getKind(), stream.getRoot(), stream.getData());
            }
        }
        document.getIssues().forEach(issue -> builder.unsupported(issue.getStreamPath(), issue.toString()));
        return builder.build();
    }

    private static void collectLibraryReferences(FlaRecord record, Set<Object> references, Set<FlaRecord> visited) {
        if (record == null || !visited.add(record)) {
            return;
        }
        if (record.get("libraryId") != null) {
            references.add(record.get("libraryId"));
        }
        for (FlaRecord child : record.getChildren()) {
            collectLibraryReferences(child, references, visited);
        }
    }

    private static boolean hasLinkage(FlaRecord descriptor) {
        FlaRecord linkage = (FlaRecord) descriptor.get("linkage");
        if (linkage == null) {
            return false;
        }
        Object flags = linkage.get("flags");
        return flags instanceof Number && ((Number) flags).longValue() != 0
                || !"".equals(linkage.get("identifier")) || !"".equals(linkage.get("className"))
                || !"".equals(linkage.get("url"));
    }

    private void replace(FlaRecord record, Set<FlaRecord> visited) {
        if (record == null || !visited.add(record)) {
            return;
        }
        List<FlaRecord> children = new ArrayList<>(record.getChildren());
        for (int i = 0; i < children.size(); i++) {
            FlaRecord child = children.get(i);
            String marker = (String) child.get("instanceName");
            GenCommandTag command = commands.get(marker);
            if (command != null) {
                if (frameMarkers.contains(marker)) {
                    record.set("extensionString", command.getCommandString());
                    children.remove(i--);
                    continue;
                }
                if (attachedNames.containsKey(marker)) {
                    FlaRecord object = child;
                    if (command.typeGraphics && !child.getClassName().equals("CPicSymbol")) {
                        object = FlaRecord.create("CPicSymbol");
                        child.getFields().forEach(object::set);
                    }
                    object.set("legacyName", command.getCommandString());
                    object.set("instanceName", attachedNames.get(marker));
                    if (command.typeGraphics) {
                        graphicLibraries.add(child.get("libraryId"));
                        object.set("firstFrame", command.frameNum);
                        object.set("loopMode", command.flagSingleFrame ? 2 : command.flagPlayOnce ? 1 : 0);
                    } else if (child.getClassName().equals("CPicSprite")) {
                        movieLibraries.add(child.get("libraryId"));
                    }
                    children.set(i, object);
                    continue;
                }
                if (removableMarkers.contains(marker)) {
                    consumedLibraries.add(child.get("libraryId"));
                }
                FlaRecord object = FlaRecord.create("CCommandObj");
                object.set("revision", 1);
                object.set("matrixWords", child.get("matrixWords"));
                // SWT contains the placement matrix, but not the editor icon's
                // bounds/label. Use the native Flash 4/5 command icon size.
                object.set("bounds", Arrays.asList(-1024, 1024, -1024, 1024));
                object.set("command", command.getCommandString());
                String label = command.command.substring(command.command.lastIndexOf('.') + 1);
                object.set("displayLabel", label.endsWith("Command") ? label.substring(0, label.length() - 7) : label);
                children.set(i, object);
            } else {
                if (child.getClassName().equals("CPicSprite")) {
                    movieLibraries.add(child.get("libraryId"));
                }
                replace(child, visited);
            }
        }
        if (!record.getChildren().isEmpty()) {
            record.set("children", children);
        }
    }

    private void publishSettings(BinaryFlaDocument document) {
        if (globals.isEmpty() && !hasObjects) {
            return;
        }
        FlaRecord settings = (FlaRecord) document.getContents().get("publishSettings");
        if (settings == null) {
            settings = FlaRecord.create("publishSettings");
            document.getContents().set("publishSettings", settings);
        }
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("Vector::Template", "1");
        for (GenCommandTag command : globals) {
            if (isMovieSettings(command)) {
                Map<String, String> names = new LinkedHashMap<>();
                names.put("width", "Width");
                names.put("height", "Height");
                names.put("rate", "FPS");
                names.put("loadorder", "LoadOrder");
                names.put("encoding", "Encoding");
                names.put("color", "Color");
                for (int i = 0; i < command.parameterNames.size(); i++) {
                    String name = names.get(command.parameterNames.get(i));
                    if (name != null) {
                        properties.put("Vector::Generator " + name, command.commandValues.get(i));
                    }
                }
            }
        }
        List<FlaRecord> profiles = records(settings.get("profiles"));
        for (FlaRecord profile : profiles) {
            putProperties(profile, properties);
        }
        FlaRecord legacy = (FlaRecord) settings.get("legacyProperties");
        if (legacy == null) {
            legacy = FlaRecord.create("strings");
            if (!profiles.isEmpty()) {
                legacy.set("entries", new ArrayList<>(records(profiles.get(profiles.size() - 1).get("entries"))));
            }
            settings.set("legacyProperties", legacy);
        }
        putProperties(legacy, properties);
    }

    @SuppressWarnings("unchecked")
    private static List<FlaRecord> records(Object value) {
        return value instanceof List ? (List<FlaRecord>) value : Collections.emptyList();
    }

    private static void putProperties(FlaRecord strings, Map<String, String> properties) {
        List<FlaRecord> entries = new ArrayList<>(records(strings.get("entries")));
        entries.removeIf(entry -> properties.containsKey(entry.get("key")));
        properties.forEach((key, value) -> {
            FlaRecord entry = FlaRecord.create("entry");
            entry.set("key", key);
            entry.set("value", value);
            entries.add(entry);
        });
        strings.set("entries", entries);
    }
}
