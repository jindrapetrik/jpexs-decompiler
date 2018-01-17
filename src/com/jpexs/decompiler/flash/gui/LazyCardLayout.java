/*
 *  Copyright (C) 2010-2018 JPEXS
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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class LazyCardLayout extends CardLayout {

    private final Map<String, Component> registeredComponents = new HashMap<>();

    @Override
    public void show(Container parent, String name) {

        Component component = registeredComponents.get(name);
        if (component != null && component.getParent() != parent) {
            parent.add(component, name);
        }

        super.show(parent, name);
    }

    public void registerLayout(Component component, String card) {
        registeredComponents.put(card, component);
    }
}
