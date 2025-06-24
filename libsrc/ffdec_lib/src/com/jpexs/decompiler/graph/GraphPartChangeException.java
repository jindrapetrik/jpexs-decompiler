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
package com.jpexs.decompiler.graph;

import java.util.List;

/**
 * Exception when part of the graph is changed.
 *
 * @author JPEXS
 */
public class GraphPartChangeException extends Exception {

    /**
     * IP
     */
    private final int ip;

    /**
     * Output
     */
    private final List<GraphTargetItem> output;

    /**
     * Constructs a new GraphPartChangeException
     *
     * @param output Output
     * @param ip IP
     */
    public GraphPartChangeException(List<GraphTargetItem> output, int ip) {
        this.output = output;
        this.ip = ip;
    }

    /**
     * Gets the IP
     *
     * @return IP
     */
    public int getIp() {
        return ip;
    }

    /**
     * Gets the output
     *
     * @return Output
     */
    public List<GraphTargetItem> getOutput() {
        return output;
    }

}
