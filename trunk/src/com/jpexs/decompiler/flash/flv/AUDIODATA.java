package com.jpexs.decompiler.flash.flv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class AUDIODATA extends DATA {

    public static final int SOUNDFORMAT_UNCOMPRESSED_NE = 0;
    public static final int SOUNDFORMAT_ADPCM = 1;
    public static final int SOUNDFORMAT_MP3 = 2;
    public static final int SOUNDFORMAT_UNCOMPRESSED_LE = 3;
    public static final int SOUNDFORMAT_NELLYMOSER_16 = 4;
    public static final int SOUNDFORMAT_NELLYMOSER_8 = 5;
    public static final int SOUNDFORMAT_NELLYMOSER = 6;
    public static final int SOUNDFORMAT_SPEEX = 11;
    public static final int SOUNDRATE_5K5 = 0;
    public static final int SOUNDRATE_11K = 1;
    public static final int SOUNDRATE_22K = 2;
    public static final int SOUNDRATE_44K = 3;
    public static final int SOUNDSIZE_8BIT = 0;
    public static final int SOUNDSIZE_16BIT = 1;
    public static final int SOUNDTYPE_MONO = 0;
    public static final int SOUNDTYPE_STEREO = 1;
    public int soundFormat;
    public int soundRate;
    public int soundSize;
    public int soundType;
    public byte[] soundData;

    public AUDIODATA(int soundFormat, int soundRate, int soundSize, int soundType, byte[] soundData) {
        this.soundFormat = soundFormat;
        this.soundRate = soundRate;
        this.soundSize = soundSize;
        this.soundType = soundType;
        this.soundData = soundData;
    }

    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            FLVOutputStream flv = new FLVOutputStream(baos);
            flv.writeUB(4, soundFormat);
            flv.writeUB(2, soundRate);
            flv.writeUB(1, soundSize);
            flv.writeUB(1, soundType);
            flv.write(soundData);
        } catch (IOException ex) {
            //ignore
        }
        return baos.toByteArray();
    }
}
