package com.jpexs.asdec.tags;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.types.RECT;

public class DefineScalingGrid extends Tag {

	private int characterId;
	private RECT splitter;

	public DefineScalingGrid(byte[] data, int version) throws IOException {
		super(78, data);
		SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterId = sis.readUI16();
        splitter = sis.readRECT();
	}

	@Override
	public String toString() {
		return "DefineScalingGrid";
	}

}
