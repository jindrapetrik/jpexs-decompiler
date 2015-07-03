/**
 * Packer version 3.0 (final)
 * Copyright 2004-2007, Dean Edwards
 * Web: {@link http://dean.edwards.name/}
 * 
 * This software is licensed under the MIT license
 * Web: {@link http://www.opensource.org/licenses/mit-license}
 * 
 * Ported to Java by Pablo Santiago based on C# version by Jesse Hansen, <twindagger2k @ msn.com>
 * Web: {@link http://jpacker.googlecode.com/}
 * Email: <pablo.santiago @ gmail.com>
 */
package com.jpacker;

import com.jpacker.encoders.BasicEncoder;
import com.jpacker.encoders.Encoder;
import com.jpacker.encoders.HighAsciiEncoder;
import com.jpacker.encoders.MidEncoder;
import com.jpacker.encoders.NormalEncoder;
import com.jpacker.encoders.NumericEncoder;

/**
 * Enum of encoding levels
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 *
 */
public enum JPackerEncoding {

    /**
     * No encoding
     */
    NONE(0, "", null),
    /**
     * Base<sub>10</sub> : [0-9]
     */
    NUMERIC(10, "String", new NumericEncoder()),
    /**
     * Base<sub>36</sub> : [0-z]
     */
    MID(36, "function(c){return c.toString(a)}", new MidEncoder()),
    /**
     * Base<sub>52</sub> : [a-Z]
     */
    BASIC(52, "function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>25?String.fromCharCode(c+39):String.fromCharCode(c+97));", new BasicEncoder()),
    /**
     * Base<sub>62</sub> : [0-Z]
     */
    NORMAL(62, "function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))}", new NormalEncoder()),
    /**
     * Base<sub>95</sub> : [¡-ÿ]
     */
    HIGH_ASCII(95, "function(c){return(c<a?\"\":e(c/a))String.fromCharCode(c%a+161)}", new HighAsciiEncoder());
    private final int encodingBase;
    private final String encode;
    private Encoder encoder;

    JPackerEncoding(int encodingBase, String encode, Encoder encoder) {
        this.encodingBase = encodingBase;
        this.encode = encode;
        this.encoder = encoder;
    }

    public int getEncodingBase() {
        return encodingBase;
    }

    public String getEncode() {
        return encode;
    }

    public Encoder getEncoder() {
        return encoder;
    }
}
