package com.jpexs.decompiler.flash.flv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class VIDEODATA extends DATA{
   public int frameType;
   public int codecId;
   public byte[] videoData;

   public VIDEODATA(int frameType, int codecId, byte[] videoData) {
      this.frameType = frameType;
      this.codecId = codecId;
      this.videoData = videoData;
   }

   
   
   @Override
   public byte[] getBytes() {
      ByteArrayOutputStream baos=new ByteArrayOutputStream();
      try{
         FLVOutputStream flv=new FLVOutputStream(baos);
         flv.writeUB(4, frameType);
         flv.writeUB(4, codecId);
         flv.write(videoData);
      }catch(IOException ex){
         //ignore
      }
      return baos.toByteArray();
   }
}
