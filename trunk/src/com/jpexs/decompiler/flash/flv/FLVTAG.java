package com.jpexs.decompiler.flash.flv;

/**
 *
 * @author JPEXS
 */
public class FLVTAG {

   public int tagType;
   public long timeStamp;
   public DATA data;
   public static final int DATATYPE_VIDEO = 9;
   public static final int DATATYPE_AUDIO = 8;
   public static final int DATATYPE_SCRIPT_DATA = 18;

   public FLVTAG(long timeStamp, VIDEODATA data) {
      this.tagType = DATATYPE_VIDEO;
      this.timeStamp = timeStamp;
      this.data = data;
   }

   public FLVTAG(long timeStamp, AUDIODATA data) {
      this.tagType = DATATYPE_AUDIO;
      this.timeStamp = timeStamp;
      this.data = data;
   }
}
