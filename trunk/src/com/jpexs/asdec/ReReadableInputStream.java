package com.jpexs.asdec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class ReReadableInputStream extends InputStream {

   InputStream is;
   ByteArrayOutputStream baos=new ByteArrayOutputStream();
   byte []converted;
   int pos=0;
   int count=0;
   
   public byte[] getAllRead(){
      return baos.toByteArray();
   }

   public int getPos() {
      return pos;
   }
   
   
   
   public ReReadableInputStream(InputStream is) {
      this.is=is;
   }
   

   public void setPos(int pos) throws IOException{
      if(pos>count){
         skip(pos-count);
      }
      this.pos=pos;
   }
   
   @Override
   public int read() throws IOException {
      if(pos<count){
         if(converted==null){
            converted=baos.toByteArray();
         }
         int ret=converted[pos]&0xff;
         pos++;
         return ret;
      }
      int i= is.read();
      baos.write(i);
      count++;
      pos++;
      converted=null;
      return i;
   }

   @Override
   public int available() throws IOException {
      return (pos<count?count-pos:0)+is.available();
   }      
}
