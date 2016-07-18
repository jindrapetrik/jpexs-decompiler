package com.jpexs.decompiler.flash.amf.amf3;

import java.io.IOException;

public class PrematureEndOfTheStreamException extends IOException {

    public PrematureEndOfTheStreamException() {
        super("Premature end of the stream reached");
    }

}
