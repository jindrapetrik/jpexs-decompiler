package com.jpexs.asdec.tags;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jpexs.asdec.SWFInputStream;

public class ProductInfo extends Tag {

	private long productID;
	private long edition;
	private int majorVersion;
	private int minorVersion;
	private long buildLow;
	private long buildHigh;
	private long compilationDate;

	public ProductInfo(byte[] data, int version) throws IOException {
		super(41, data);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        /*
         * 0: Unknown
		 * 1: Macromedia Flex for J2EE
		 * 2: Macromedia Flex for .NET
		 * 3: Adobe Flex
         */
        productID = sis.readUI32();

		/*
		 * 0: Developer Edition
		 * 1: Full Commercial Edition
		 * 2: Non Commercial Edition
		 * 3: Educational Edition
		 * 4: Not For Resale (NFR) Edition
		 * 5: Trial Edition
		 * 6: None
		 */
        edition = sis.readUI32();
        majorVersion = sis.readUI8();
        minorVersion = sis.readUI8();
        buildLow = sis.readUI32();
        buildHigh = sis.readUI32();
        compilationDate = sis.readUI32() & 0xffffffffL;
        compilationDate |= sis.readUI32() << 32;
	}

	@Override
	public String toString() {
		return "ProductInfo";
	}
}
