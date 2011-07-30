package com.jpexs.asdec.tags;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jpexs.asdec.SWFInputStream;

public class FrameLabel extends Tag {

	private String name;

	public FrameLabel(byte[] data, int version) throws IOException {
		super(43, data);
		SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        name = sis.readString();
	}

	@Override
	public String toString() {
		return "FrameLabel";
	}
}
