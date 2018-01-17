/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.iggy.streams;

import com.jpexs.decompiler.flash.iggy.streams.AbstractDataStream;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface StructureInterface {

    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException;

    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException;
}
