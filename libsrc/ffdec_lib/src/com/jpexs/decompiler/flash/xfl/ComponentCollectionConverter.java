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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.model.CallPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructPropAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IncLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NameValuePair;
import com.jpexs.decompiler.flash.abc.avm2.model.NewArrayAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NumberValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.LtAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Recognizes the complete collection-copy loop emitted by Flash, without executing it. */
final class ComponentCollectionConverter {

    static final class Result {
        String providerClass;
        String itemClass;
        List<Map<String, String>> rows = new ArrayList<>();

        String serialize(ComponentDefinition.Parameter schema) {
            if (!"Collection".equals(schema.type)) {
                return null;
            }
            String[] header = schema.defaultValue.split(", ");
            if (header.length != 5 || !providerClass.equals(header[0]) || !itemClass.equals(header[1])) {
                return null;
            }
            if (rows.isEmpty()) {
                return schema.defaultValue;
            }
            List<String> columns = new ArrayList<>(rows.get(0).keySet());
            List<String> values = new ArrayList<>(Arrays.asList(header[0], header[1], header[2], String.valueOf(columns.size())));
            for (String column : columns) {
                values.add(column);
                values.add("5"); // CS6 String column
                values.add("");
                values.add("");
            }
            values.add(String.valueOf(rows.size()));
            for (Map<String, String> row : rows) {
                if (!row.keySet().equals(rows.get(0).keySet())) {
                    return null;
                }
                for (String column : columns) {
                    String value = row.get(column);
                    // Keep unsupported multiline values in the original AS initializer.
                    if (value.contains("\n") || value.contains("\r")) {
                        return null;
                    }
                    values.add(value.replace("&", "&amp;").replace("<", "&lt;")
                            .replace(">", "&gt;").replace("\"", "&quot;").replace(",", "&amp;#2C"));
                }
            }
            return String.join(", ", values);
        }
    }

    private static SetLocalAVM2Item setLocal(GraphTargetItem item) {
        if (item instanceof DeclarationAVM2Item) {
            item = ((DeclarationAVM2Item) item).assignment;
        }
        return item instanceof SetLocalAVM2Item ? (SetLocalAVM2Item) item : null;
    }

    private static int register(GraphTargetItem item) {
        item = item.getNotCoerced();
        return item instanceof LocalRegAVM2Item ? ((LocalRegAVM2Item) item).regIndex : -1;
    }

    private static String construction(ABC abc, GraphTargetItem value) {
        value = value.getNotCoerced();
        if (!(value instanceof ConstructPropAVM2Item)) {
            return null;
        }
        ConstructPropAVM2Item construct = (ConstructPropAVM2Item) value;
        if (!construct.args.isEmpty() || !(construct.propertyName instanceof FullMultinameAVM2Item)
                || !(construct.object instanceof FindPropertyAVM2Item)) {
            return null;
        }
        FullMultinameAVM2Item name = (FullMultinameAVM2Item) construct.propertyName;
        if (name.name != null || name.namespace != null
                || !construct.propertyName.equals(((FindPropertyAVM2Item) construct.object).propertyName)) {
            return null;
        }
        return ComponentConverter.qualifiedName(abc, name.multinameIndex);
    }

    private static boolean property(GraphTargetItem item, int object, int index, String name) {
        item = item.getNotCoerced();
        if (!(item instanceof GetPropertyAVM2Item)) {
            return false;
        }
        GetPropertyAVM2Item get = (GetPropertyAVM2Item) item;
        return register(get.object) == object && propertyName(get.propertyName, index, name);
    }

    private static boolean propertyName(GraphTargetItem item, int index, String name) {
        if (!(item instanceof FullMultinameAVM2Item)) {
            return false;
        }
        FullMultinameAVM2Item multiname = (FullMultinameAVM2Item) item;
        return multiname.namespace == null && (name == null ? multiname.name != null && register(multiname.name) == index
                : multiname.name == null && name.equals(ComponentConverter.name(item)));
    }

    static Result read(ABC abc, List<GraphTargetItem> items, int start) {
        if (start + 4 >= items.size()) {
            return null;
        }
        SetLocalAVM2Item provider = setLocal(items.get(start));
        SetLocalAVM2Item array = setLocal(items.get(start + 1));
        SetLocalAVM2Item index = setLocal(items.get(start + 2));
        if (provider == null || array == null || index == null
                || !(array.value.getNotCoerced() instanceof NewArrayAVM2Item)
                || !(index.value instanceof IntegerValueAVM2Item) || ((IntegerValueAVM2Item) index.value).intValue() != 0
                || !(items.get(start + 3) instanceof WhileItem) || !(items.get(start + 4) instanceof SetPropertyAVM2Item)) {
            return null;
        }
        WhileItem loop = (WhileItem) items.get(start + 3);
        if (loop.expression.size() != 1 || !(loop.expression.get(0) instanceof LtAVM2Item) || loop.commands.size() != 5) {
            return null;
        }
        LtAVM2Item condition = (LtAVM2Item) loop.expression.get(0);
        SetLocalAVM2Item item = setLocal(loop.commands.get(0));
        SetLocalAVM2Item object = setLocal(loop.commands.get(1));
        if (item == null || object == null || register(condition.leftSide) != index.regIndex
                || !property(condition.rightSide, array.regIndex, -1, "length")
                || !property(object.value, array.regIndex, index.regIndex, null)
                || !(loop.commands.get(2) instanceof ForInAVM2Item)
                || !(loop.commands.get(3) instanceof CallPropertyAVM2Item)
                || !(loop.commands.get(4) instanceof IncLocalAVM2Item)
                || ((IncLocalAVM2Item) loop.commands.get(4)).regIndex != index.regIndex) {
            return null;
        }
        ForInAVM2Item copy = (ForInAVM2Item) loop.commands.get(2);
        int key = register(copy.expression.object);
        if (register(copy.expression.collection) != object.regIndex || copy.commands.size() != 1
                || !(copy.commands.get(0) instanceof SetPropertyAVM2Item)) {
            return null;
        }
        SetPropertyAVM2Item assignment = (SetPropertyAVM2Item) copy.commands.get(0);
        CallPropertyAVM2Item add = (CallPropertyAVM2Item) loop.commands.get(3);
        SetPropertyAVM2Item finalAssignment = (SetPropertyAVM2Item) items.get(start + 4);
        List<Integer> registers = Arrays.asList(provider.regIndex, array.regIndex, index.regIndex, item.regIndex, object.regIndex, key);
        if (new HashSet<>(registers).size() != 6 || registers.stream().anyMatch(r -> r <= 0)
                || register(assignment.object) != item.regIndex || !propertyName(assignment.propertyName, key, null)
                || !property(assignment.value, object.regIndex, key, null)
                || register(add.receiver) != provider.regIndex || !propertyName(add.propertyName, -1, "addItem")
                || add.arguments.size() != 1 || register(add.arguments.get(0)) != item.regIndex
                || register(finalAssignment.value) != provider.regIndex) {
            return null;
        }
        Result result = new Result();
        result.providerClass = construction(abc, provider.value);
        result.itemClass = construction(abc, item.value);
        if (!"fl.data.DataProvider".equals(result.providerClass)
                || !("fl.data.SimpleCollectionItem".equals(result.itemClass) || "fl.data.TileListCollectionItem".equals(result.itemClass))) {
            return null;
        }
        for (GraphTargetItem row : ((NewArrayAVM2Item) array.value.getNotCoerced()).values) {
            if (!(row instanceof NewObjectAVM2Item)) {
                return null;
            }
            Map<String, String> values = new LinkedHashMap<>();
            for (NameValuePair pair : ((NewObjectAVM2Item) row).pairs) {
                if (!(pair.name instanceof StringAVM2Item)) {
                    return null;
                }
                String name = (String) pair.name.getResult();
                String value;
                if (pair.value instanceof StringAVM2Item) {
                    value = (String) pair.value.getResult();
                } else if ("data".equals(name) && (pair.value instanceof NumberValueAVM2Item
                        || pair.value instanceof TrueItem || pair.value instanceof FalseItem || pair.value instanceof NullAVM2Item)) {
                    value = pair.value instanceof NullAVM2Item ? "null" : String.valueOf(pair.value.getResult());
                } else {
                    return null;
                }
                if (!("label".equals(name) || ("fl.data.SimpleCollectionItem".equals(result.itemClass)
                        ? "data".equals(name) : "source".equals(name))) || values.put(name, value) != null) {
                    return null;
                }
            }
            result.rows.add(values);
        }
        return result;
    }
}
