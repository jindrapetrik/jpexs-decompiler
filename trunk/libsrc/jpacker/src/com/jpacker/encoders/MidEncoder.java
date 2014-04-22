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
package com.jpacker.encoders;

/**
 * Mid base36 encoder: [0-z]
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 *
 */
public class MidEncoder implements Encoder {

    // lookups seemed like the easiest way to do this since
    // I don't know of an equivalent to .toString(36)
    private static String LOOKUP_36 = "0123456789abcdefghijklmnopqrstuvwxyz";

    @Override
    public String encode(int code) {
        String encoded = "";
        int i = 0;
        do {
            int digit = (code / (int) Math.pow(36, i)) % 36;
            encoded = LOOKUP_36.charAt(digit) + encoded;
            code -= digit * (int) Math.pow(36, i++);
        } while (code > 0);
        return encoded;
    }
}
