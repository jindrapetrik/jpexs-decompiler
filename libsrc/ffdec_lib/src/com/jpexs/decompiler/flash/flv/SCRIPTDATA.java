/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.decompiler.flash.flv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class SCRIPTDATA extends DATA {

    public List<SCRIPTDATAOBJECT> data;

    public SCRIPTDATA(List<SCRIPTDATAOBJECT> data) {
        this.data = data;
    }

    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FLVOutputStream fos = new FLVOutputStream(baos)) {
            for (SCRIPTDATAOBJECT d : data) {
                fos.writeSCRIPTDATAOBJECT(d);
            }
            fos.writeUI24(9); //SCRIPTDATAOBJECTEND
        } catch (IOException ex) {
            Logger.getLogger(SCRIPTDATA.class.getName()).log(Level.SEVERE, "i/o error", ex);
        }
        return baos.toByteArray();
    }

    public static SCRIPTDATA onMetaData(double duration, double width, double height, double videodatarate, double framerate, double videocodecid, double audiosamplerate, double audiosamplesize, boolean stereo, double audiocodecid, double filesize) {
        List<SCRIPTDATAOBJECT> list = new ArrayList<>();
        List<SCRIPTDATAVARIABLE> values = new ArrayList<>();
        values.add(new SCRIPTDATAVARIABLE("duration", new SCRIPTDATAVALUE(duration)));
        values.add(new SCRIPTDATAVARIABLE("width", new SCRIPTDATAVALUE(width)));
        values.add(new SCRIPTDATAVARIABLE("height", new SCRIPTDATAVALUE(height)));
        values.add(new SCRIPTDATAVARIABLE("videodatarate", new SCRIPTDATAVALUE(videodatarate)));
        values.add(new SCRIPTDATAVARIABLE("framerate", new SCRIPTDATAVALUE(framerate)));
        values.add(new SCRIPTDATAVARIABLE("videocodecid", new SCRIPTDATAVALUE(videocodecid)));
        values.add(new SCRIPTDATAVARIABLE("audiosamplerate", new SCRIPTDATAVALUE(audiosamplerate)));
        values.add(new SCRIPTDATAVARIABLE("audiosamplesize", new SCRIPTDATAVALUE(audiosamplesize)));
        values.add(new SCRIPTDATAVARIABLE("stereo", new SCRIPTDATAVALUE(stereo)));
        values.add(new SCRIPTDATAVARIABLE("audiocodecid", new SCRIPTDATAVALUE(audiocodecid)));
        values.add(new SCRIPTDATAVARIABLE("filesize", new SCRIPTDATAVALUE(filesize)));
        SCRIPTDATAVALUE valuesList = new SCRIPTDATAVALUE(8, values);
        SCRIPTDATAOBJECT metaData = new SCRIPTDATAOBJECT("onMetaData", valuesList);
        list.add(metaData);
        SCRIPTDATA ret = new SCRIPTDATA(list);
        return ret;
    }
}
