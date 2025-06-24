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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Namespace set in ABC file.
 *
 * @author JPEXS
 */
public class NamespaceSet {

    @Internal
    public boolean deleted;

    public int[] namespaces;

    public NamespaceSet() {
    }

    public NamespaceSet(int[] namespaces) {
        this.namespaces = namespaces;
    }

    public String toString(AVM2ConstantPool constants) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.namespaces.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(constants.getNamespace(namespaces[i]).getNameWithKind(constants));
        }
        return sb.toString();
    }

    public boolean isApiVersioned(AVM2ConstantPool constants) {
        Set<String> namespaceNames = new HashSet<>();
        Set<Integer> namespaceKinds = new HashSet<>();
        for (int n : namespaces) {
            Namespace ns = constants.getNamespace(n);
            String nsName = ns.getRawName(constants);
            if (nsName != null && nsName.length() > 0) {
                int lastChar = nsName.codePointAt(nsName.length() - 1);
                if (lastChar >= Namespace.MIN_API_MARK && lastChar <= Namespace.MAX_API_MARK) {
                    namespaceNames.add(nsName.substring(0, nsName.length() - 1));
                    namespaceKinds.add(ns.kind);
                } else {
                    return false;
                }
            }
        }
        if (namespaceNames.size() != 1) {
            return false;
        }
        if (namespaceKinds.size() != 1) {
            return false;
        }
        return true;
    }

    public List<Integer> getApiVersions(AVM2ConstantPool constants) {
        Set<String> namespaceNames = new HashSet<>();
        Set<Integer> namespaceKinds = new HashSet<>();
        List<Integer> apiVersions = new ArrayList<>();
        for (int n : namespaces) {
            Namespace ns = constants.getNamespace(n);
            String nsName = ns.getRawName(constants);
            if (nsName != null && nsName.length() > 0) {
                int lastChar = nsName.codePointAt(nsName.length() - 1);
                if (lastChar >= Namespace.MIN_API_MARK && lastChar <= Namespace.MAX_API_MARK) {
                    namespaceNames.add(nsName.substring(0, nsName.length() - 1));
                    namespaceKinds.add(ns.kind);
                    int apiVersion = lastChar - Namespace.MIN_API_MARK;
                    if (apiVersion != 0) {
                        apiVersions.add(apiVersion);
                    }
                } else {
                    return new ArrayList<>();
                }
            }
        }
        if (namespaceNames.size() != 1) {
            return new ArrayList<>();
        }
        if (namespaceKinds.size() != 1) {
            return new ArrayList<>();
        }
        return apiVersions;
    }

    public int getNonversionedKind(AVM2ConstantPool constants) {
        Set<String> namespaceNames = new HashSet<>();
        Set<Integer> namespaceKinds = new HashSet<>();
        for (int n : namespaces) {
            Namespace ns = constants.getNamespace(n);
            String nsName = ns.getRawName(constants);
            namespaceKinds.add(ns.kind);
            if (nsName != null && nsName.length() > 0) {
                int lastChar = nsName.codePointAt(nsName.length() - 1);
                if (lastChar >= Namespace.MIN_API_MARK && lastChar <= Namespace.MAX_API_MARK) {
                    namespaceNames.add(nsName.substring(0, nsName.length() - 1));
                } else {
                    namespaceNames.add(nsName);
                }
            } else {
                namespaceNames.add(nsName);
            }
        }
        if (namespaceNames.size() != 1) {
            return 0;
        }
        if (namespaceKinds.size() != 1) {
            return 0;
        }
        return namespaceKinds.iterator().next();
    }

    public DottedChain getNonversionedName(AVM2ConstantPool constants) {
        Set<String> namespaceNames = new HashSet<>();
        for (int n : namespaces) {
            String nsName = constants.getNamespace(n).getRawName(constants);
            if (nsName != null && nsName.length() > 0) {
                int lastChar = nsName.codePointAt(nsName.length() - 1);
                if (lastChar >= Namespace.MIN_API_MARK && lastChar <= Namespace.MAX_API_MARK) {
                    namespaceNames.add(nsName.substring(0, nsName.length() - 1));
                } else {
                    namespaceNames.add(nsName);
                }
            } else {
                namespaceNames.add(nsName);
            }
        }
        if (namespaceNames.size() != 1) {
            return null;
        }
        return DottedChain.parseNoSuffix(namespaceNames.iterator().next());
    }
}
