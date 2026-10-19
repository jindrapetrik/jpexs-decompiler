/**
 * DDSReader.java
 * 
 * Copyright (c) 2015 Kenji Sasaki
 * Released under the MIT license.
 * https://github.com/npedotnet/DDSReader/blob/master/LICENSE
 * 
 * English document
 * https://github.com/npedotnet/DDSReader/blob/master/README.md
 * 
 * Japanese document
 * http://3dtech.jp/wiki/index.php?DDSReader
 * 
 */

package net.npe.dds;

public final class DDSReader {
	
	public static final Order ARGB = new Order(16, 8, 0, 24);
	public static final Order ABGR = new Order(0, 8, 16, 24);
	
	public static int getHeight(byte [] buffer) {
		return (buffer[12] & 0xFF) | (buffer[13] & 0xFF) << 8 | (buffer[14] & 0xFF) << 16 | (buffer[15] & 0xFF) << 24;
	}
	
	public static int getWidth(byte [] buffer) {
		return (buffer[16] & 0xFF) | (buffer[17] & 0xFF) << 8 | (buffer[18] & 0xFF) << 16 | (buffer[19] & 0xFF) << 24;
	}
	
	public static int getMipmap(byte [] buffer) {
		return (buffer[28] & 0xFF) | (buffer[29] & 0xFF) << 8 | (buffer[30] & 0xFF) << 16 | (buffer[31] & 0xFF) << 24;
	}
	
	public static int getPixelFormatFlags(byte [] buffer) {
		return (buffer[80] & 0xFF) | (buffer[81] & 0xFF) << 8 | (buffer[82] & 0xFF) << 16 | (buffer[83] & 0xFF) << 24;
	}
	
	public static int getFourCC(byte [] buffer) {
		return (buffer[84] & 0xFF) << 24 | (buffer[85] & 0xFF) << 16 | (buffer[86] & 0xFF) << 8 | (buffer[87] & 0xFF);
	}
	
	public static int getBitCount(byte [] buffer) {
		return (buffer[88] & 0xFF) | (buffer[89] & 0xFF) << 8 | (buffer[90] & 0xFF) << 16 | (buffer[91] & 0xFF) << 24;
	}
	
	public static int getRedMask(byte [] buffer) {
		return (buffer[92] & 0xFF) | (buffer[93] & 0xFF) << 8 | (buffer[94] & 0xFF) << 16 | (buffer[95] & 0xFF) << 24;
	}
	
	public static int getGreenMask(byte [] buffer) {
		return (buffer[96] & 0xFF) | (buffer[97] & 0xFF) << 8 | (buffer[98] & 0xFF) << 16 | (buffer[99] & 0xFF) << 24;
	}
	
	public static int getBlueMask(byte [] buffer) {
		return (buffer[100] & 0xFF) | (buffer[101] & 0xFF) << 8 | (buffer[102] & 0xFF) << 16 | (buffer[103] & 0xFF) << 24;
	}
	
	public static int getAlphaMask(byte [] buffer) {
		return (buffer[104] & 0xFF) | (buffer[105] & 0xFF) << 8 | (buffer[106] & 0xFF) << 16 | (buffer[107] & 0xFF) << 24;
	}
	
	public static int [] read(byte [] buffer, Order order, int mipmapLevel) {
		
		// header
		int width = getWidth(buffer);
		int height = getHeight(buffer);
		int mipmap = getMipmap(buffer);

		// type
		int type = getType(buffer);
		if(type == 0) return null;
		
		// offset
		int offset = 128; // header size
		if(mipmapLevel > 0 && mipmapLevel < mipmap) {
			for(int i=0; i<mipmapLevel; i++) {
				switch(type) {
				case DXT1: offset += 8*((width+3)/4)*((height+3)/4); break;
				case DXT2:
				case DXT3:
				case DXT4:
				case DXT5: offset += 16*((width+3)/4)*((height+3)/4); break;
				case A1R5G5B5:
				case X1R5G5B5:
				case A4R4G4B4:
				case X4R4G4B4:
				case R5G6B5:
				case R8G8B8:
				case A8B8G8R8:
				case X8B8G8R8:
				case A8R8G8B8:
				case X8R8G8B8: offset += (type&0xFF)*width*height; break;
				}
				width /= 2;
				height /= 2;
			}
			if(width <= 0) width = 1;
			if(height <= 0) height = 1;
		}
		
		int [] pixels = null;
		switch(type) {
		case DXT1: pixels = decodeDXT1(width, height, offset, buffer, order); break;
		case DXT2: pixels = decodeDXT2(width, height, offset, buffer, order); break;
		case DXT3: pixels = decodeDXT3(width, height, offset, buffer, order); break;
		case DXT4: pixels = decodeDXT4(width, height, offset, buffer, order); break;
		case DXT5: pixels = decodeDXT5(width, height, offset, buffer, order); break;
		case A1R5G5B5: pixels = readA1R5G5B5(width, height, offset, buffer, order); break;
		case X1R5G5B5: pixels = readX1R5G5B5(width, height, offset, buffer, order); break;
		case A4R4G4B4: pixels = readA4R4G4B4(width, height, offset, buffer, order); break;
		case X4R4G4B4: pixels = readX4R4G4B4(width, height, offset, buffer, order); break;
		case R5G6B5: pixels = readR5G6B5(width, height, offset, buffer, order); break;
		case R8G8B8: pixels = readR8G8B8(width, height, offset, buffer, order); break;
		case A8B8G8R8: pixels = readA8B8G8R8(width, height, offset, buffer, order); break;
		case X8B8G8R8: pixels = readX8B8G8R8(width, height, offset, buffer, order); break;
		case A8R8G8B8: pixels = readA8R8G8B8(width, height, offset, buffer, order); break;
		case X8R8G8B8: pixels = readX8R8G8B8(width, height, offset, buffer, order); break;
		}
		
		return pixels;
	}
	
	private static int getType(byte [] buffer) {
		
		int type = 0;
		
		int flags = getPixelFormatFlags(buffer);
		
		if((flags & 0x04) != 0) {
			// DXT
			type = getFourCC(buffer);
		}
		else if((flags & 0x40) != 0) {
			// RGB
			int bitCount = getBitCount(buffer);
			int redMask = getRedMask(buffer);
			int greenMask = getGreenMask(buffer);
			int blueMask = getBlueMask(buffer);
			int alphaMask = ((flags&0x01) != 0) ? getAlphaMask(buffer) : 0; // 0x01 alpha
			if(bitCount == 16) {
				if(redMask==A1R5G5B5_MASKS[0] && greenMask==A1R5G5B5_MASKS[1] && blueMask==A1R5G5B5_MASKS[2] && alphaMask==A1R5G5B5_MASKS[3]) {
					// A1R5G5B5
					type = A1R5G5B5;
				}
				else if(redMask==X1R5G5B5_MASKS[0] && greenMask==X1R5G5B5_MASKS[1] && blueMask==X1R5G5B5_MASKS[2] && alphaMask==X1R5G5B5_MASKS[3]) {
					// X1R5G5B5
					type = X1R5G5B5;
				}
				else if(redMask==A4R4G4B4_MASKS[0] && greenMask==A4R4G4B4_MASKS[1] && blueMask==A4R4G4B4_MASKS[2] && alphaMask==A4R4G4B4_MASKS[3]) {
					// A4R4G4B4
					type = A4R4G4B4;
				}
				else if(redMask==X4R4G4B4_MASKS[0] && greenMask==X4R4G4B4_MASKS[1] && blueMask==X4R4G4B4_MASKS[2] && alphaMask==X4R4G4B4_MASKS[3]) {
					// X4R4G4B4
					type = X4R4G4B4;
				}
				else if(redMask==R5G6B5_MASKS[0] && greenMask==R5G6B5_MASKS[1] && blueMask==R5G6B5_MASKS[2] && alphaMask==R5G6B5_MASKS[3]) {
					// R5G6B5
					type = R5G6B5;
				}
				else {
					// Unsupported 16bit RGB image
				}
			}
			else if(bitCount == 24) {
				if(redMask==R8G8B8_MASKS[0] && greenMask==R8G8B8_MASKS[1] && blueMask==R8G8B8_MASKS[2] && alphaMask==R8G8B8_MASKS[3]) {
					// R8G8B8
					type = R8G8B8;
				}
				else {
					// Unsupported 24bit RGB image
				}
			}
			else if(bitCount == 32) {
				if(redMask==A8B8G8R8_MASKS[0] && greenMask==A8B8G8R8_MASKS[1] && blueMask==A8B8G8R8_MASKS[2] && alphaMask==A8B8G8R8_MASKS[3]) {
					// A8B8G8R8
					type = A8B8G8R8;
				}
				else if(redMask==X8B8G8R8_MASKS[0] && greenMask==X8B8G8R8_MASKS[1] && blueMask==X8B8G8R8_MASKS[2] && alphaMask==X8B8G8R8_MASKS[3]) {
					// X8B8G8R8
					type = X8B8G8R8;
				}
				else if(redMask==A8R8G8B8_MASKS[0] && greenMask==A8R8G8B8_MASKS[1] && blueMask==A8R8G8B8_MASKS[2] && alphaMask==A8R8G8B8_MASKS[3]) {
					// A8R8G8B8
					type = A8R8G8B8;
				}
				else if(redMask==X8R8G8B8_MASKS[0] && greenMask==X8R8G8B8_MASKS[1] && blueMask==X8R8G8B8_MASKS[2] && alphaMask==X8R8G8B8_MASKS[3]) {
					// X8R8G8B8
					type = X8R8G8B8;
				}
				else {
					// Unsupported 32bit RGB image
				}
			}
		}
		else {
			// YUV or LUMINANCE image
		}
		
		return type;
		
	}
	
        //JPEXS: made public
	public static int [] decodeDXT1(int width, int height, int offset, byte [] buffer, Order order) {
		int [] pixels = new int[width*height];
		int index = offset;
		int w = (width+3)/4;
		int h = (height+3)/4;
		for(int i=0; i<h; i++) {
			for(int j=0; j<w; j++) {
				int c0 = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8; index += 2;
				int c1 = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8; index += 2;
				for(int k=0; k<4; k++) {
					if(4*i+k >= height) break;
					int t0 = (buffer[index] & 0x03);
					int t1 = (buffer[index] & 0x0C) >> 2;
					int t2 = (buffer[index] & 0x30) >> 4;
					int t3 = (buffer[index++] & 0xC0) >> 6;
					pixels[4*width*i+4*j+width*k+0] = getDXTColor(c0, c1, 0xFF, t0, order);
					if(4*j+1 >= width) continue;
					pixels[4*width*i+4*j+width*k+1] = getDXTColor(c0, c1, 0xFF, t1, order);
					if(4*j+2 >= width) continue;
					pixels[4*width*i+4*j+width*k+2] = getDXTColor(c0, c1, 0xFF, t2, order);
					if(4*j+3 >= width) continue;
					pixels[4*width*i+4*j+width*k+3] = getDXTColor(c0, c1, 0xFF, t3, order);
				}
			}
		}
		return pixels;
	}
	
        //JPEXS: made public
	public static int [] decodeDXT2(int width, int height, int offset, byte [] buffer, Order order) {
		return decodeDXT3(width, height, offset, buffer, order);
	}
	
        //JPEXS: made public
	public static int [] decodeDXT3(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int w = (width+3)/4;
		int h = (height+3)/4;
		int [] pixels = new int[width*height];
		int [] alphaTable = new int[16];
		for(int i=0; i<h; i++) {
			for(int j=0; j<w; j++) {
				// create alpha table(4bit to 8bit)
				for(int k=0; k<4; k++) {
					int a0 = (buffer[index++] & 0xFF);
					int a1 = (buffer[index++] & 0xFF);
					// 4bit alpha to 8bit alpha
					alphaTable[4*k+0] = 17 * ((a0 & 0xF0)>>4);
					alphaTable[4*k+1] = 17 * (a0 & 0x0F);
					alphaTable[4*k+2] = 17 * ((a1 & 0xF0)>>4);
					alphaTable[4*k+3] = 17 * (a1 & 0x0F);
				}
				int c0 = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8; index += 2;
				int c1 = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8; index += 2;
				for(int k=0; k<4; k++) {
					if(4*i+k >= height) break;
					int t0 = (buffer[index] & 0x03);
					int t1 = (buffer[index] & 0x0C) >> 2;
					int t2 = (buffer[index] & 0x30) >> 4;
					int t3 = (buffer[index++] & 0xC0) >> 6;
					pixels[4*width*i+4*j+width*k+0] = getDXTColor(c0, c1, alphaTable[4*k+0], t0, order);
					if(4*j+1 >= width) continue;
					pixels[4*width*i+4*j+width*k+1] = getDXTColor(c0, c1, alphaTable[4*k+1], t1, order);
					if(4*j+2 >= width) continue;
					pixels[4*width*i+4*j+width*k+2] = getDXTColor(c0, c1, alphaTable[4*k+2], t2, order);
					if(4*j+3 >= width) continue;
					pixels[4*width*i+4*j+width*k+3] = getDXTColor(c0, c1, alphaTable[4*k+3], t3, order);
				}
			}
		}
		return pixels;
	}
	
        //JPEXS: made public
	public static int [] decodeDXT4(int width, int height, int offset, byte [] buffer, Order order) {
		return decodeDXT5(width, height, offset, buffer, order);
	}
	
        //JPEXS: made public
	public static int [] decodeDXT5(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int w = (width+3)/4;
		int h = (height+3)/4;
		int [] pixels = new int[width*height];
		int [] alphaTable = new int[16];
		for(int i=0; i<h; i++) {
			for(int j=0; j<w; j++) {
				// create alpha table
				int a0 = (buffer[index++] & 0xFF);
				int a1 = (buffer[index++] & 0xFF);
				int b0 = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8 | (buffer[index+2] & 0xFF) << 16; index += 3;
				int b1 = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8 | (buffer[index+2] & 0xFF) << 16; index += 3;
				alphaTable[0] = b0 & 0x07;
				alphaTable[1] = (b0 >> 3) & 0x07;
				alphaTable[2] = (b0 >> 6) & 0x07;
				alphaTable[3] = (b0 >> 9) & 0x07;
				alphaTable[4] = (b0 >> 12) & 0x07;
				alphaTable[5] = (b0 >> 15) & 0x07;
				alphaTable[6] = (b0 >> 18) & 0x07;
				alphaTable[7] = (b0 >> 21) & 0x07;
				alphaTable[8] = b1 & 0x07;
				alphaTable[9] = (b1 >> 3) & 0x07;
				alphaTable[10] = (b1 >> 6) & 0x07;
				alphaTable[11] = (b1 >> 9) & 0x07;
				alphaTable[12] = (b1 >> 12) & 0x07;
				alphaTable[13] = (b1 >> 15) & 0x07;
				alphaTable[14] = (b1 >> 18) & 0x07;
				alphaTable[15] = (b1 >> 21) & 0x07;
				int c0 = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8; index += 2;
				int c1 = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8; index += 2;
				for(int k=0; k<4; k++) {
					if(4*i+k >= height) break;
					int t0 = (buffer[index] & 0x03);
					int t1 = (buffer[index] & 0x0C) >> 2;
					int t2 = (buffer[index] & 0x30) >> 4;
					int t3 = (buffer[index++] & 0xC0) >> 6;
					pixels[4*width*i+4*j+width*k+0] = getDXTColor(c0, c1, getDXT5Alpha(a0, a1, alphaTable[4*k+0]), t0, order);
					if(4*j+1 >= width) continue;
					pixels[4*width*i+4*j+width*k+1] = getDXTColor(c0, c1, getDXT5Alpha(a0, a1, alphaTable[4*k+1]), t1, order);
					if(4*j+2 >= width) continue;
					pixels[4*width*i+4*j+width*k+2] = getDXTColor(c0, c1, getDXT5Alpha(a0, a1, alphaTable[4*k+2]), t2, order);
					if(4*j+3 >= width) continue;
					pixels[4*width*i+4*j+width*k+3] = getDXTColor(c0, c1, getDXT5Alpha(a0, a1, alphaTable[4*k+3]), t3, order);
				}
			}
		}
		return pixels;
	}
	
	private static int [] readA1R5G5B5(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int [] pixels = new int[width*height];
		for(int i=0; i<height*width; i++) {
			int rgba = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8; index += 2;
			int r = BIT5[(rgba & A1R5G5B5_MASKS[0]) >> 10];
			int g = BIT5[(rgba & A1R5G5B5_MASKS[1]) >> 5];
			int b = BIT5[(rgba & A1R5G5B5_MASKS[2])];
			int a = 255 * ((rgba & A1R5G5B5_MASKS[3]) >> 15);
			pixels[i] = (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
		}
		return pixels;
	}
	
	private static int [] readX1R5G5B5(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int [] pixels = new int[width*height];
		for(int i=0; i<height*width; i++) {
			int rgba = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8; index += 2;
			int r = BIT5[(rgba & X1R5G5B5_MASKS[0]) >> 10];
			int g = BIT5[(rgba & X1R5G5B5_MASKS[1]) >> 5];
			int b = BIT5[(rgba & X1R5G5B5_MASKS[2])];
			int a = 255;
			pixels[i] = (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
		}
		return pixels;
	}
	
	private static int [] readA4R4G4B4(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int [] pixels = new int[width*height];
		for(int i=0; i<height*width; i++) {
			int rgba = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8; index += 2;
			int r = 17 * ((rgba & A4R4G4B4_MASKS[0]) >> 8);
			int g = 17 * ((rgba & A4R4G4B4_MASKS[1]) >> 4);
			int b = 17 * ((rgba & A4R4G4B4_MASKS[2]));
			int a = 17 * ((rgba & A4R4G4B4_MASKS[3]) >> 12);
			pixels[i] = (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
		}
		return pixels;
	}
	
	private static int [] readX4R4G4B4(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int [] pixels = new int[width*height];
		for(int i=0; i<height*width; i++) {
			int rgba = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8; index += 2;
			int r = 17 * ((rgba & A4R4G4B4_MASKS[0]) >> 8);
			int g = 17 * ((rgba & A4R4G4B4_MASKS[1]) >> 4);
			int b = 17 * ((rgba & A4R4G4B4_MASKS[2]));
			int a = 255;
			pixels[i] = (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
		}
		return pixels;
	}
	
	private static int [] readR5G6B5(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int [] pixels = new int[width*height];
		for(int i=0; i<height*width; i++) {
			int rgba = (buffer[index] & 0xFF) | (buffer[index+1] & 0xFF) << 8; index += 2;
			int r = BIT5[((rgba & R5G6B5_MASKS[0]) >> 11)];
			int g = BIT6[((rgba & R5G6B5_MASKS[1]) >> 5)];
			int b = BIT5[((rgba & R5G6B5_MASKS[2]))];
			int a = 255;
			pixels[i] = (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
		}
		return pixels;
	}
	
	private static int [] readR8G8B8(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int [] pixels = new int[width*height];
		for(int i=0; i<height*width; i++) {
			int b = buffer[index++] & 0xFF;
			int g = buffer[index++] & 0xFF;
			int r = buffer[index++] & 0xFF;
			int a = 255;
			pixels[i] = (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
		}
		return pixels;
	}
	
	private static int [] readA8B8G8R8(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int [] pixels = new int[width*height];
		for(int i=0; i<height*width; i++) {
			int r = buffer[index++] & 0xFF;
			int g = buffer[index++] & 0xFF;
			int b = buffer[index++] & 0xFF;
			int a = buffer[index++] & 0xFF;
			pixels[i] = (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
		}
		return pixels;
	}
	
	private static int [] readX8B8G8R8(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int [] pixels = new int[width*height];
		for(int i=0; i<height*width; i++) {
			int r = buffer[index++] & 0xFF;
			int g = buffer[index++] & 0xFF;
			int b = buffer[index++] & 0xFF;
			int a = 255; index++;
			pixels[i] = (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
		}
		return pixels;
	}
	
	private static int [] readA8R8G8B8(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int [] pixels = new int[width*height];
		for(int i=0; i<height*width; i++) {
			int b = buffer[index++] & 0xFF;
			int g = buffer[index++] & 0xFF;
			int r = buffer[index++] & 0xFF;
			int a = buffer[index++] & 0xFF;
			pixels[i] = (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
		}
		return pixels;
	}
	
	private static int [] readX8R8G8B8(int width, int height, int offset, byte [] buffer, Order order) {
		int index = offset;
		int [] pixels = new int[width*height];
		for(int i=0; i<height*width; i++) {
			int b = buffer[index++] & 0xFF;
			int g = buffer[index++] & 0xFF;
			int r = buffer[index++] & 0xFF;
			int a = 255; index++;
			pixels[i] = (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
		}
		return pixels;
	}
	
	private static int getDXTColor(int c0, int c1, int a, int t, Order order) {
		switch(t) {
		case 0: return getDXTColor1(c0, a, order);
		case 1: return getDXTColor1(c1, a, order);
		case 2: return (c0 > c1) ? getDXTColor2_1(c0, c1, a, order) : getDXTColor1_1(c0, c1, a, order);
		case 3: return (c0 > c1) ? getDXTColor2_1(c1, c0, a, order) : 0;
		}
		return 0;
	}
	
	private static int getDXTColor2_1(int c0, int c1, int a, Order order) {
		// 2*c0/3 + c1/3
		int r = (2*BIT5[(c0 & 0xFC00) >> 11] + BIT5[(c1 & 0xFC00) >> 11]) / 3;
		int g = (2*BIT6[(c0 & 0x07E0) >> 5] + BIT6[(c1 & 0x07E0) >> 5]) / 3;
		int b = (2*BIT5[c0 & 0x001F] + BIT5[c1 & 0x001F]) / 3;
		return (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
	}
	
	private static int getDXTColor1_1(int c0, int c1, int a, Order order) {
		// (c0+c1) / 2
		int r = (BIT5[(c0 & 0xFC00) >> 11] + BIT5[(c1 & 0xFC00) >> 11]) / 2;
		int g = (BIT6[(c0 & 0x07E0) >> 5] + BIT6[(c1 & 0x07E0) >> 5]) / 2;
		int b = (BIT5[c0 & 0x001F] + BIT5[c1 & 0x001F]) / 2;
		return (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
	}
	
	private static int getDXTColor1(int c, int a, Order order) {
		int r = BIT5[(c & 0xFC00) >> 11];
		int g = BIT6[(c & 0x07E0) >> 5];
		int b = BIT5[(c & 0x001F)];
		return (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
	}
	
	private static int getDXT5Alpha(int a0, int a1, int t) {
		if(a0 > a1) switch(t) {
		case 0: return a0;
		case 1: return a1;
		case 2: return (6*a0+a1)/7;
		case 3: return (5*a0+2*a1)/7;
		case 4: return (4*a0+3*a1)/7;
		case 5: return (3*a0+4*a1)/7;
		case 6: return (2*a0+5*a1)/7;
		case 7: return (a0+6*a1)/7;
		}
		else switch(t) {
		case 0: return a0;
		case 1: return a1;
		case 2: return (4*a0+a1)/5;
		case 3: return (3*a0+2*a1)/5;
		case 4: return (2*a0+3*a1)/5;
		case 5: return (a0+4*a1)/5;
		case 6: return 0;
		case 7: return 255;
		}
		return 0;
	}
	
	// Image Type
	private static final int DXT1 = (0x44585431);
	private static final int DXT2 = (0x44585432);
	private static final int DXT3 = (0x44585433);
	private static final int DXT4 = (0x44585434);
	private static final int DXT5 = (0x44585435);
	private static final int A1R5G5B5 = ((1<<16)|2);
	private static final int X1R5G5B5 = ((2<<16)|2);
	private static final int A4R4G4B4 = ((3<<16)|2);
	private static final int X4R4G4B4 = ((4<<16)|2);
	private static final int R5G6B5   = ((5<<16)|2);
	private static final int R8G8B8   = ((1<<16)|3);
	private static final int A8B8G8R8 = ((1<<16)|4);
	private static final int X8B8G8R8 = ((2<<16)|4);
	private static final int A8R8G8B8 = ((3<<16)|4);
	private static final int X8R8G8B8 = ((4<<16)|4);
	
	// RGBA Masks
	private static final int [] A1R5G5B5_MASKS = {0x7C00, 0x03E0, 0x001F, 0x8000};
	private static final int [] X1R5G5B5_MASKS = {0x7C00, 0x03E0, 0x001F, 0x0000};
	private static final int [] A4R4G4B4_MASKS = {0x0F00, 0x00F0, 0x000F, 0xF000};
	private static final int [] X4R4G4B4_MASKS = {0x0F00, 0x00F0, 0x000F, 0x0000};
	private static final int [] R5G6B5_MASKS   = {0xF800, 0x07E0, 0x001F, 0x0000};
	private static final int [] R8G8B8_MASKS   = {0xFF0000, 0x00FF00, 0x0000FF, 0x000000};
	private static final int [] A8B8G8R8_MASKS = {0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000};
	private static final int [] X8B8G8R8_MASKS = {0x000000FF, 0x0000FF00, 0x00FF0000, 0x00000000};
	private static final int [] A8R8G8B8_MASKS = {0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000};
	private static final int [] X8R8G8B8_MASKS = {0x00FF0000, 0x0000FF00, 0x000000FF, 0x00000000};

	// BIT4 = 17 * index;
	private static final int [] BIT5 = {0,8,16,25,33,41,49,58,66,74,82,90,99,107,115,123,132,140,148,156,165,173,181,189,197,206,214,222,230,239,247,255};
	private static final int [] BIT6 = {0,4,8,12,16,20,24,28,32,36,40,45,49,53,57,61,65,69,73,77,81,85,89,93,97,101,105,109,113,117,121,125,130,134,138,142,146,150,154,158,162,166,170,174,178,182,186,190,194,198,202,206,210,215,219,223,227,231,235,239,243,247,251,255};

	private DDSReader() {}
	
	private static final class Order {
		Order(int redShift, int greenShift, int blueShift, int alphaShift) {
			this.redShift = redShift;
			this.greenShift = greenShift;
			this.blueShift = blueShift;
			this.alphaShift = alphaShift;
		}
		public int redShift;
		public int greenShift;
		public int blueShift;
		public int alphaShift;
	}
	
}