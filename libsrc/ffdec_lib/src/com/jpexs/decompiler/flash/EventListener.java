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
package com.jpexs.decompiler.flash;

/**
 * Interface for listening to events.
 *
 * @author JPEXS
 */
public interface EventListener {

    /**
     * Handles exporting event.
     *
     * @param type Event type
     * @param index Index of the exported item
     * @param count Total count of exported items
     * @param data Data
     */
    public void handleExportingEvent(String type, int index, int count, Object data);

    /**
     * Handles exported event.
     *
     * @param type Event type
     * @param index Index of the exported item
     * @param count Total count of exported items
     * @param data Data
     */
    public void handleExportedEvent(String type, int index, int count, Object data);

    /**
     * Handles event.
     *
     * @param event Event name
     * @param data Data
     */
    public void handleEvent(String event, Object data);
}
