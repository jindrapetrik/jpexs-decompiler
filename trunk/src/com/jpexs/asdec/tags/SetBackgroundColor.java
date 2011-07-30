package com.jpexs.asdec.tags;

import com.jpexs.asdec.types.RGB;

public class SetBackgroundColor extends Tag {

	private RGB backgroundColor;

	public SetBackgroundColor(byte[] data) {
		super(9, data);
		backgroundColor = new RGB();
		backgroundColor.red = data[0] & 0xff;
		backgroundColor.green = data[1] & 0xff;
		backgroundColor.blue = data[2] & 0xff;
	}

	@Override
	public String toString() {
		return "SetBackgroundColor";
	}
}
