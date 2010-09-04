/*
 * Copyright (c) 2010. JPEXS
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
