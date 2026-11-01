/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.easygui.ShapeTool;
import java.awt.Color;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonContextualTaskGroup;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;

/**
 * Contextual shape tools, with one shared selection across all bands.
 *
 * @author JPEXS
 */
final class ShapeToolsRibbon {

    private final JRibbon ribbon;
    private final RibbonTask task;
    private final RibbonContextualTaskGroup context;
    private final CommandToggleButtonGroup tools = new CommandToggleButtonGroup();
    private final Map<ShapeTool, JCommandToggleButton> buttons = new EnumMap<>(ShapeTool.class);

    ShapeToolsRibbon(JRibbon ribbon, Function<String, String> translate, Consumer<ShapeTool> selectTool) {
        this.ribbon = ribbon;
        tools.setAllowsClearingSelection(false);
        JRibbonBand drawing = createBand("drawing", translate, selectTool,
                ShapeTool.BRUSH, ShapeTool.PENCIL, ShapeTool.PEN, ShapeTool.SHAPES, ShapeTool.ERASER);
        JRibbonBand color = createBand("color", translate, selectTool,
                ShapeTool.PAINT, ShapeTool.INK, ShapeTool.GRADIENT_TRANSFORM, ShapeTool.EYEDROPPER);
        JRibbonBand modify = createBand("modify", translate, selectTool,
                ShapeTool.SELECT, ShapeTool.LASSO, ShapeTool.EDIT_POINTS, ShapeTool.ANCHOR_POINT);
        task = new RibbonTask(translate.apply("menu.shapes"), drawing, color, modify);
        context = new RibbonContextualTaskGroup("", new Color(0, 128, 0), task);
        ribbon.addContextualTaskGroup(context);
        tools.setSelected(buttons.get(ShapeTool.SELECT), true);
    }

    private JRibbonBand createBand(String id, Function<String, String> translate, Consumer<ShapeTool> selectTool, ShapeTool... modes) {
        JRibbonBand band = new JRibbonBand(translate.apply("menu.shapes." + id),
                View.getResizableIcon(icon(modes[0]), 32));
        for (ShapeTool mode : modes) {
            JCommandToggleButton button = new JCommandToggleButton(translate.apply("menu.shapes.tool." + mode.getId()),
                    View.getResizableIcon(icon(mode), 32));
            tools.add(button);
            buttons.put(mode, button);
            button.addActionListener(event -> {
                tools.setSelected(button, true);
                selectTool.accept(mode);
            });
            band.addCommandButton(button, RibbonElementPriority.TOP);
        }
        band.setResizePolicies(CoreRibbonResizePolicies.getCorePoliciesNone(band));
        return band;
    }

    private static String icon(ShapeTool tool) {
        return "shapetool" + tool.getId().toLowerCase(Locale.ROOT) + "32";
    }

    void update(boolean editing, ShapeTool selected) {
        tools.setSelected(buttons.get(selected), true);
        boolean visible = ribbon.isVisible(context);
        if (visible != editing) {
            ribbon.setVisible(context, editing);
            if (editing) {
                ribbon.setSelectedTask(task);
            }
        }
    }
}
