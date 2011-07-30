package com.jpexs.asdec.tags;

import java.io.IOException;

public class JPEGTables extends Tag {

	public JPEGTables(byte[] data) throws IOException {
		super(8, data);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "JPEGTables";
	}
}
