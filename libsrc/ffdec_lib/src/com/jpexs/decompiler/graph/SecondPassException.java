package com.jpexs.decompiler.graph;

/**
 *
 * @author JPEXS
 */
public class SecondPassException extends RuntimeException {
    private final SecondPassData data;

    public SecondPassException(SecondPassData data) {
        this.data = data;
    }

    public SecondPassData getData() {
        return data;
    }

}
