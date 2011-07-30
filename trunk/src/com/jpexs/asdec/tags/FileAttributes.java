package com.jpexs.asdec.tags;

public class FileAttributes extends Tag {

	private boolean useDirectBlit;
	private boolean useGPU;
	private boolean hasMetadata;
	private boolean actionScript3;
	private boolean useNetwork;

	public FileAttributes(byte[] data) {
		super(69, data);
		// UB[1] == 0  (reserved)
		useDirectBlit = (data[0] & 0x40) != 0;
		useGPU =        (data[0] & 0x20) != 0;
		hasMetadata =   (data[0] & 0x10) != 0;
		actionScript3 = (data[0] & 0x08) != 0;
		// UB[2] == 0  (reserved)
		useNetwork =    (data[0] & 0x01) != 0;
		// UB[24] == 0 (reserved)
	}

	@Override
	public String toString() {
		return "FileAttributes";
	}
}
