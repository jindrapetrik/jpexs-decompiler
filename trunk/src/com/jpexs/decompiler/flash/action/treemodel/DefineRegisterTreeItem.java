/*
 * Copyright (C) 2013 JPEXS
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
package com.jpexs.decompiler.flash.action.treemodel;

/**
 *
 * @author JPEXS
 */
public class DefineRegisterTreeItem extends TreeItem {

    private String identifier;
    private int register;

    public DefineRegisterTreeItem(String identifier, int register) {
        super(null, PRECEDENCE_PRIMARY);
        this.identifier = identifier;
        this.register = register;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "var " + identifier;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
