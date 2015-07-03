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
 * Basic (base52) encoder: [a-Z]
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 *
 */
public class BasicEncoder implements Encoder {

    @Override
    public String encode(int c) {
        return (c < 52 ? "" : encode(c / 52)) + (((c = c % 52)) > 25 ? String.valueOf((char) (c + 39)) : String.valueOf((char) (c + 97)));
    }
}
