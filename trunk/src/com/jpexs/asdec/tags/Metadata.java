package com.jpexs.asdec.tags;

import java.io.UnsupportedEncodingException;

public class Metadata extends Tag {

	private String xmlMetadata;

	public Metadata(byte[] data) {
		super(77, data);
		try {
			xmlMetadata = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// cannot get here
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "Metadata";
	}
}
