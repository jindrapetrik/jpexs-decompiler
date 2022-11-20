package com.jpexs.decompiler.flash.abc;

import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class ABCOpenException extends IOException {

    public ABCOpenException(String message) {
        super(message);        
    }
    
    public ABCOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}
