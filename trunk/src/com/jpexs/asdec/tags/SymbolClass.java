package com.jpexs.asdec.tags;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jpexs.asdec.SWFInputStream;

public class SymbolClass extends Tag {
	private int tagIDs[];
	private String classNames[];

	public SymbolClass(byte[] data, int version) throws IOException {
		super(76, data);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        int numSymbols = sis.readUI16();
        tagIDs = new int[numSymbols];
        classNames = new String[numSymbols];
        for (int ii = 0; ii < numSymbols; ii++) {
        	int tagID = sis.readUI16();
        	String className = sis.readString();
        	tagIDs[ii] = tagID;
        	classNames[ii] = className;
        }
	}

	@Override
	public String toString() {
		return "SymbolClass";
	}
}
