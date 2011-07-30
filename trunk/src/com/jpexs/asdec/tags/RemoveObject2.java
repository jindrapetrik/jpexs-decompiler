package com.jpexs.asdec.tags;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jpexs.asdec.SWFInputStream;

public class RemoveObject2 extends Tag {

	private int depth;

	public RemoveObject2(byte[] data, int version) throws IOException {
		super(28, data);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        SWFInputStream sis = new SWFInputStream(bais, version);
		depth = sis.readUI16();
	}

	@Override
	public String toString() {
		return "RemoveObject2";
	}
}
