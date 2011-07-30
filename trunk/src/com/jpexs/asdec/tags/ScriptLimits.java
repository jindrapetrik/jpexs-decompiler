package com.jpexs.asdec.tags;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jpexs.asdec.SWFInputStream;

public class ScriptLimits extends Tag {

	private int maxRecursionDepth;
	private int scriptTimeoutSeconds;

	public ScriptLimits(byte[] data, int version) throws IOException {
		super(65, data);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        maxRecursionDepth = sis.readUI16();
        scriptTimeoutSeconds = sis.readUI16();
	}

	@Override
	public String toString() {
		return "ScriptLimits";
	}
}
