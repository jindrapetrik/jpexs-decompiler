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
 * License along with this library. */
package com.jpexs.decompiler.flash.search;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.cache.ScriptDecompiledListener;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class ActionScriptSearch {

    public List<ActionSearchResult> searchAs2(SWF swf, final String txt, boolean ignoreCase, boolean regexp, boolean pcode, ScriptSearchListener listener) {
        if (txt != null && !txt.isEmpty()) {
            Map<String, ASMSource> asms = swf.getASMs(false);
            final List<ActionSearchResult> found = new ArrayList<>();
            Pattern pat = regexp
                    ? Pattern.compile(txt, ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0)
                    : Pattern.compile(Pattern.quote(txt), ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0);

            int pos = 0;
            List<Future<HighlightedText>> futures = new ArrayList<>();
            try {
                for (Map.Entry<String, ASMSource> item : asms.entrySet()) {
                    pos++;
                    ASMSource asm = item.getValue();

                    if (pcode) {
                        if (listener != null) {
                            listener.onSearch(pos, asms.size(), item.getKey());
                        }

                        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
                        asm.getASMSource(ScriptExportMode.PCODE, writer, null);
                        String text = writer.toString();
                        if (pat.matcher(text).find()) {
                            found.add(new ActionSearchResult(asm, pcode, item.getKey()));
                        }
                    } else {
                        int fpos = pos;
                        Future<HighlightedText> text = SWF.getCachedFuture(asm, null, new ScriptDecompiledListener<HighlightedText>() {
                            @Override
                            public void onStart() {
                                if (listener != null) {
                                    listener.onDecompile(fpos, asms.size(), item.getKey());
                                }
                            }

                            @Override
                            public void onComplete(HighlightedText result) {
                                if (listener != null) {
                                    listener.onSearch(fpos, asms.size(), item.getKey());
                                }

                                if (pat.matcher(result.text).find()) {
                                    ActionSearchResult searchResult = new ActionSearchResult(asm, pcode, item.getKey());
                                    found.add(searchResult);
                                }
                            }
                        });

                        futures.add(text);
                    }
                }
            } catch (InterruptedException ex) {
                for (Future<HighlightedText> future : futures) {
                    future.cancel(true);
                }
            }

            return found;
        }

        return null;
    }

    public List<ABCSearchResult> searchAs3(final SWF swf, final String txt, boolean ignoreCase, boolean regexp, boolean pcode, ScriptSearchListener listener) {
        // todo: pcode seach
        if (txt != null && !txt.isEmpty()) {
            List<String> ignoredClasses = new ArrayList<>();
            List<String> ignoredNss = new ArrayList<>();

            if (Configuration._ignoreAdditionalFlexClasses.get()) {
                swf.getFlexMainClass(ignoredClasses, ignoredNss);
            }

            final List<ABCSearchResult> found = new ArrayList<>();
            List<ScriptPack> allpacks = swf.getAS3Packs();
            final Pattern pat = regexp
                    ? Pattern.compile(txt, ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0)
                    : Pattern.compile(Pattern.quote(txt), ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0);

            int pos = 0;
            List<Future<HighlightedText>> futures = new ArrayList<>();
            try {
                loop:
                for (final ScriptPack pack : allpacks) {
                    pos++;
                    if (!pack.isSimple && Configuration.ignoreCLikePackages.get()) {
                        continue;
                    }
                    if (Configuration._ignoreAdditionalFlexClasses.get()) {
                        String fullName = pack.getClassPath().packageStr.add(pack.getClassPath().className, pack.getClassPath().namespaceSuffix).toRawString();
                        if (ignoredClasses.contains(fullName)) {
                            continue;
                        }
                        for (String ns : ignoredNss) {
                            if (fullName.startsWith(ns + ".")) {
                                continue loop;
                            }
                        }
                    }

                    if (pcode) {
                        if (listener != null) {
                            listener.onSearch(pos, allpacks.size(), pack.getClassPath().toString());
                        }

                        List<MethodId> methodInfos = new ArrayList<>();
                        pack.getMethodInfos(methodInfos);

                        ABC abc = pack.abc;
                        for (MethodId methodInfo : methodInfos) {
                            int bodyIndex = abc.findBodyIndex(methodInfo.getMethodIndex());
                            if (bodyIndex != -1) {
                                MethodBody body = abc.bodies.get(bodyIndex);
                                HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
                                abc.bodies.get(bodyIndex).getCode().toASMSource(abc.constants, abc.method_info.get(body.method_info), body, ScriptExportMode.PCODE, writer);
                                String text = writer.toString();
                                if (pat.matcher(text).find()) {
                                    ABCSearchResult searchResult = new ABCSearchResult(pack, methodInfo.getClassIndex(), methodInfo.getTraitId());
                                    found.add(searchResult);
                                }
                            }
                        }
                    } else {
                        int fpos = pos;
                        Future<HighlightedText> text = SWF.getCachedFuture(pack, new ScriptDecompiledListener<HighlightedText>() {
                            @Override
                            public void onStart() {
                                if (listener != null) {
                                    listener.onDecompile(fpos, allpacks.size(), pack.getClassPath().toString());
                                }
                            }

                            @Override
                            public void onComplete(HighlightedText result) {
                                if (listener != null) {
                                    listener.onSearch(fpos, allpacks.size(), pack.getClassPath().toString());
                                }

                                if (pat.matcher(result.text).find()) {
                                    ABCSearchResult searchResult = new ABCSearchResult(pack);
                                    found.add(searchResult);
                                }
                            }
                        });

                        futures.add(text);
                    }
                }

                for (Future<HighlightedText> future : futures) {
                    try {
                        future.get();
                    } catch (ExecutionException ex) {
                        Logger.getLogger(ActionScriptSearch.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (InterruptedException ex) {
                for (Future<HighlightedText> future : futures) {
                    future.cancel(true);
                }
            }

            return found;
        }

        return null;
    }
}
