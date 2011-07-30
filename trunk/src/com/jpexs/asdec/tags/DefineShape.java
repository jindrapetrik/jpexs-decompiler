package com.jpexs.asdec.tags;

import java.io.IOException;

public class DefineShape extends Tag {

	public DefineShape(byte[] data, int version) throws IOException {
		super(2, data);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "DefineShape";
	}
}
