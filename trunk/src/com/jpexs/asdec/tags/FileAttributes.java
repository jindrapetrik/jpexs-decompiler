/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jpexs.asdec.tags;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jpexs.asdec.SWFInputStream;

public class FileAttributes extends Tag {

	private boolean useDirectBlit;
	private boolean useGPU;
	private boolean hasMetadata;
	private boolean actionScript3;
	private boolean useNetwork;

	public FileAttributes(byte[] data, int version, long pos) throws IOException {
		super(69, data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        sis.readUB(1); // reserved
		// UB[1] == 0  (reserved)
		useDirectBlit = sis.readUB(1) != 0;
		useGPU = sis.readUB(1) != 0;
		hasMetadata = sis.readUB(1) != 0;
		actionScript3 = sis.readUB(1) != 0;
		sis.readUB(2); // reserved
		useNetwork = sis.readUB(1) != 0;
		// UB[24] == 0 (reserved)
	}

	@Override
	public String toString() {
		return "FileAttributes";
	}
}
