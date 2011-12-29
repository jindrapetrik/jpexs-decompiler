/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jpexs.asdec;

import com.jpexs.asdec.tags.Tag;
import com.jpexs.asdec.types.RECT;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Class representing SWF file
 *
 * @author JPEXS
 */
public class SWF {

    /**
     * Tags inside of file
     */
    public List<Tag> tags = new ArrayList<Tag>();
    /**
     * Rectangle for the display
     */
    public RECT displayRect;
    /**
     * Movie frame rate
     */
    public int frameRate;
    /**
     * Number of frames in movie
     */
    public int frameCount;
    /**
     * Version of SWF
     */
    public int version;
    /**
     * Size of the file
     */
    public long fileSize;

    /**
     * Use compression
     */
    public boolean compressed = false;

    /**
     * Gets all tags with specified id
     *
     * @param tagId Identificator of tag type
     * @return List of tags
     */
    public List<Tag> getTagData(int tagId) {
        List<Tag> ret = new ArrayList<Tag>();
        for (Tag tag : tags) {
            if (tag.getId() == tagId) {
                ret.add(tag);
            }
        }
        return ret;
    }

    /**
     * Saves this SWF into new file
     *
     * @param os OutputStream to save SWF in
     * @throws IOException
     */
    public void saveTo(OutputStream os) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SWFOutputStream sos = new SWFOutputStream(baos, version);
            sos.writeRECT(displayRect);
            sos.writeUI8(0);
            sos.writeUI8(frameRate);
            sos.writeUI16(frameCount);

            sos.writeTags(tags);
            sos.writeUI16(0);
            sos.close();
            if (compressed) {
                os.write('C');
            } else {
                os.write('F');
            }
            os.write('W');
            os.write('S');
            os.write(version);
            byte data[] = baos.toByteArray();
            sos = new SWFOutputStream(os, version);
            sos.writeUI32(data.length + 8);

            if (compressed) {
                os = new DeflaterOutputStream(os);
            }
            os.write(data);
        }
        finally {
            if (os != null)
                os.close();
        }

    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @throws IOException
     */
    public SWF(InputStream is) throws IOException {
        byte hdr[] = new byte[3];
        is.read(hdr);
        String shdr = new String(hdr);
        if ((!shdr.equals("FWS")) && (!shdr.equals("CWS"))) {
            throw new IOException("Invalid SWF file");
        }
        version = is.read();
        SWFInputStream sis = new SWFInputStream(is, version);
        fileSize = sis.readUI32();

        if (hdr[0] == 'C') {
            sis = new SWFInputStream(new InflaterInputStream(is), version);
            compressed = true;
        }


        displayRect = sis.readRECT();
        sis.readUI8();
        frameRate = sis.readUI8();
        frameCount = sis.readUI16();
        tags = sis.readTagList();
    }


    /**
     * Compress SWF file
     *
     * @param fis Input stream
     * @param fos Output stream
     */
    public static void fws2cws(InputStream fis, OutputStream fos) {
        try {
            byte swfHead[] = new byte[8];
            fis.read(swfHead);

            swfHead[0] = 'C';
            fos.write(swfHead);
            fos = new DeflaterOutputStream(fos);
            int i = 0;
            while ((i = fis.read()) != -1) {
                fos.write(i);
            }

            fis.close();
            fos.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }

    }

    /**
     * Decompress SWF file
     *
     * @param fis Input stream
     * @param fos Output stream
     */
    public static void cws2fws(InputStream fis, OutputStream fos) {
        try {
            byte swfHead[] = new byte[8];
            fis.read(swfHead);
            InflaterInputStream iis = new InflaterInputStream(fis);
            swfHead[0] = 'F';
            fos.write(swfHead);
            int i = 0;
            while ((i = iis.read()) != -1) {
                fos.write(i);
            }

            fis.close();
            fos.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }

    }
}
