package com.jpexs.asdec.tags;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jpexs.asdec.SWFInputStream;

public class DefineFontName extends Tag {

	private int fontId;
	private String fontName;
	private String fontCopyright;

	public DefineFontName(byte[] data, int version) throws IOException {
		super(88, data);
		SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        fontId = sis.readUI16();
        fontName = sis.readString();
        fontCopyright = sis.readString();
	}

	@Override
	public String toString() {
		return "DefineFontName";
	}

}
