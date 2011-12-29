/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.abc.avm2.parser.MissingSymbolHandler;

import javax.swing.*;


public class DialogMissingSymbolHandler implements MissingSymbolHandler {

    public boolean missingString(String value) {
        return JOptionPane.showConfirmDialog(null, "String \"" + value + "\" is not present in constants table. Do you want to add it?", "Add String", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public boolean missingInt(long value) {
        return JOptionPane.showConfirmDialog(null, "Integer value \"" + value + "\" is not present in constants table. Do you want to add it?", "Add Integer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public boolean missingUInt(long value) {
        return JOptionPane.showConfirmDialog(null, "Unsigned integer value \"" + value + "\" is not present in constants table. Do you want to add it?", "Add Unsigned integer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public boolean missingDouble(double value) {
        return JOptionPane.showConfirmDialog(null, "Double value \"" + value + "\" is not present in constants table. Do you want to add it?", "Add Double", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
    }

}
