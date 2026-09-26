/*
 *  Copyright (C) 2010-2026 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

/**
 * Read-only presentation of the effective display-list state at a depth.
 *
 * @author JPEXS
 */
public class DepthStateTreePanel extends JPanel {

    private static final int MAX_OBJECT_DEPTH = 5;

    private final JTree tree;

    public DepthStateTreePanel() {
        super(new BorderLayout());
        tree = new JTree();
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setEditable(false);
        add(new FasterScrollPane(tree), BorderLayout.CENTER);
        clear();
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        if (View.isOceanic()) {
            tree.setBackground(Color.white);
            renderer.setUI(new BasicLabelUI());
            renderer.setOpaque(false);
            renderer.setBackgroundNonSelectionColor(Color.white);
        }
        tree.setCellRenderer(renderer);
    }

    public final void clear() {
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("root")));
    }

    public void setDepthState(DepthState state) {
        if (state == null) {
            clear();
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("DepthState");
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        addValue(root, "depth", state.depth, visited, 0);
        addValue(root, "characterId", state.characterId < 0 ? null : state.characterId, visited, 0);
        addValue(root, "matrix", state.matrix, visited, 0);
        addValue(root, "colorTransform", state.colorTransForm, visited, 0);
        addValue(root, "ratio", state.ratio < 0 ? 0 : state.ratio, visited, 0);
        addValue(root, "clipDepth", state.clipDepth < 0 ? null : state.clipDepth, visited, 0);
        addValue(root, "instanceName", state.instanceName, visited, 0);
        addValue(root, "clipActions", state.clipActions, visited, 0);        
        addValue(root, "className", state.className, visited, 0);
        addValue(root, "cacheAsBitmap", state.cacheAsBitmap, visited, 0);
        addValue(root, "blendMode", state.blendMode, visited, 0);
        addValue(root, "filters", state.filters == null || state.filters.isEmpty() ? null : state.filters, visited, 0);
        addValue(root, "visible", state.isVisible, visited, 0);
        addValue(root, "backgroundColor", state.backGroundColor, visited, 0);
        addValue(root, "hasImage", state.hasImage, visited, 0);
        addValue(root, "amfData", state.amfData, visited, 0);
        
        tree.setModel(new DefaultTreeModel(root));
    }

    private void addValue(DefaultMutableTreeNode parent, String name, Object value, Set<Object> visited, int depth) {
        if (value == null) {
            parent.add(new DefaultMutableTreeNode(name + ":"));
            return;
        }
        if (isSimpleValue(value)) {
            parent.add(new DefaultMutableTreeNode(name + ": " + value));
            return;
        }

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
        parent.add(node);

        if (value instanceof MATRIX) {
            MATRIX matrix = (MATRIX) value;
            addSimpleValue(node, "scaleX", matrix.getScaleXFloat());
            addSimpleValue(node, "scaleY", matrix.getScaleYFloat());
            addSimpleValue(node, "rotateSkew0", matrix.getRotateSkew0Float());
            addSimpleValue(node, "rotateSkew1", matrix.getRotateSkew1Float());
            addSimpleValue(node, "translateX", matrix.translateX);
            addSimpleValue(node, "translateY", matrix.translateY);
            return;
        }
        if (value instanceof ColorTransform) {
            ColorTransform colorTransform = (ColorTransform) value;
            addSimpleValue(node, "redMultiplier", colorTransform.getRedMulti());
            addSimpleValue(node, "greenMultiplier", colorTransform.getGreenMulti());
            addSimpleValue(node, "blueMultiplier", colorTransform.getBlueMulti());
            addSimpleValue(node, "alphaMultiplier", colorTransform.getAlphaMulti());
            addSimpleValue(node, "redAdd", colorTransform.getRedAdd());
            addSimpleValue(node, "greenAdd", colorTransform.getGreenAdd());
            addSimpleValue(node, "blueAdd", colorTransform.getBlueAdd());
            addSimpleValue(node, "alphaAdd", colorTransform.getAlphaAdd());
            return;
        }
        if (value instanceof RGB) {
            RGB color = (RGB) value;
            addSimpleValue(node, "red", color.red);
            addSimpleValue(node, "green", color.green);
            addSimpleValue(node, "blue", color.blue);
            if (color instanceof RGBA) {
                addSimpleValue(node, "alpha", ((RGBA) color).alpha);
            }
            return;
        }
        if (value instanceof List) {
            List<?> values = (List<?>) value;
            for (int i = 0; i < values.size(); i++) {
                addValue(node, "[" + i + "]", values.get(i), visited, depth + 1);
            }
            return;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                addValue(node, "[" + i + "]", Array.get(value, i), visited, depth + 1);
            }
            return;
        }
        if (depth >= MAX_OBJECT_DEPTH || visited.contains(value)) {
            node.setUserObject(name + ": " + value);
            return;
        }

        visited.add(value);
        node.setUserObject(name + " (" + value.getClass().getSimpleName() + ")");
        for (Field field : value.getClass().getFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    || field.isAnnotationPresent(Calculated.class)
                    || field.isAnnotationPresent(Reserved.class)
                    || field.isAnnotationPresent(Internal.class)) {
                continue;
            }
            try {
                addValue(node, field.getName(), field.get(value), visited, depth + 1);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                // Public fields should be accessible. Omit an inaccessible value.
            }
        }
        visited.remove(value);
    }

    private boolean isSimpleValue(Object value) {
        return value instanceof Number
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof String
                || value instanceof Enum;
    }

    private void addSimpleValue(DefaultMutableTreeNode parent, String name, Object value) {
        parent.add(new DefaultMutableTreeNode(name + ": " + EcmaScript.toString(value)));
    }
}
