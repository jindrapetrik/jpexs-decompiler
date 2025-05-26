/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.tags.base;

/**
 * Exception for unsupported sound rate.
 *
 * @author JPEXS
 */
public class UnsupportedSamplingRateException extends SoundImportException {

    private final int soundRate;
    private final int[] supportedRates;

    /**
     * Constructor.
     * @param soundRate Unsupported sound rate
     * @param supportedRates Supported sound rates
     */
    public UnsupportedSamplingRateException(int soundRate, int[] supportedRates) {
        super("Unsupported sound rate: " + soundRate);
        this.soundRate = soundRate;
        this.supportedRates = supportedRates;
    }

    /**
     * Gets supported sound rates.
     * @return Supported sound rates
     */
    public int[] getSupportedRates() {
        return supportedRates;
    }

    /**
     * Gets unsupported sound rate.
     * @return Unsupported sound rate
     */
    public int getSoundRate() {
        return soundRate;
    }

}
