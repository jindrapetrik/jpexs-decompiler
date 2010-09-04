/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc;


public class NotSameException extends RuntimeException {
    public NotSameException(long pos) {
        super("Streams are not the same at pos:" + pos);
    }
}
