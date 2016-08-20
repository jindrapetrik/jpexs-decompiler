package com.jpexs.decompiler.flash.importers;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class As3ScriptReplaceException extends Exception {

    private List<As3ScriptReplaceExceptionItem> exceptionItems;

    public As3ScriptReplaceException(List<As3ScriptReplaceExceptionItem> exceptionItems) {
        this.exceptionItems = exceptionItems;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        for (As3ScriptReplaceExceptionItem item : exceptionItems) {
            sb.append(item.toString()).append("\r\n");
        }
        return sb.toString();
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    public As3ScriptReplaceException(As3ScriptReplaceExceptionItem exceptionItem) {
        this.exceptionItems = new ArrayList<>();
        this.exceptionItems.add(exceptionItem);
    }

    public List<As3ScriptReplaceExceptionItem> getExceptionItems() {
        return exceptionItems;
    }

}
