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
 * High-Ascii (base95) encoder: [¡-ÿ] SHOULD BE USED WITH CAUTION! Not fully
 * tested.
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 */
public class HighAsciiEncoder implements Encoder {

    private static String LOOKUP_95 = "¡¢£€¥Š§š©ª«¬­®¯°±²³Žµ¶·ž¹º»ŒœŸ¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";

    @Override
    public String encode(int code) {
        String encoded = "";
        int i = 0;
        do {
            int digit = (code / (int) Math.pow(95, i)) % 95;
            encoded = LOOKUP_95.charAt(digit) + encoded;
            code -= digit * (int) Math.pow(95, i++);
        } while (code > 0);
        return encoded;
    }
}
