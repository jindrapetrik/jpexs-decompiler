package com.jpexs.decompiler.flash.debugger {
	
	import flash.net.Socket;
	import flash.utils.ByteArray;
	import flash.events.Event;
	import flash.display.Sprite;
	
	public class DebugConnection {

		private static var s:Socket;
		private static var q = [];
		private static var first:Boolean = true;
		private static var inited:Boolean = false;
		private static var name:String;
		public static const DEBUG_VERSION_MAJOR = 1;
		public static const DEBUG_VERSION_MINOR = 2;
		
		public static const MSG_STRING = 0;
		public static const MSG_LOADER_URL = 1;
		public static const MSG_LOADER_BYTES = 2;
		public static const MSG_LONGSTRING = 3;		
		public static const MSG_LOADER_URL_GETBYTES = 4;
		public static const MSG_LOADER_BYTES_GETBYTES = 5;
						
				
		
		private static function sendQueue(){
			var qo = q;
				q = [];
				for each(var m in qo){
					writeMsg(m.data,m.type);
				}
		}
		
		private static function writeStringNull(msg){
			var b:ByteArray = new ByteArray();			
   			b.writeUTFBytes(msg);						
			s.writeBytes(b,0,b.length);	
			s.writeByte(0);
		}
		
		private static function writeString(msg){
			var b:ByteArray = new ByteArray();			
   			b.writeUTFBytes(msg);				
			s.writeByte((b.length>>8) & 0xff);
			s.writeByte(b.length & 0xff);
			s.writeBytes(b,0,b.length);						
		}
		
		private static function writeLongString(msg){
			var b:ByteArray = new ByteArray();			
   			b.writeUTFBytes(msg);				
			writeBytes(b);				
		}
		
		private static function writeBytes(b:ByteArray){
			s.writeByte((b.length>>24) & 0xff);			
			s.writeByte((b.length>>16) & 0xff);			
			s.writeByte((b.length>>8) & 0xff);
			s.writeByte(b.length & 0xff);
			s.writeBytes(b,0,b.length);						
		}
		
		private static function readBytes():ByteArray {
			var b:ByteArray = new ByteArray();						
			var a1 = s.readUnsignedByte();
			var a2 = s.readUnsignedByte();
			var a3 = s.readUnsignedByte();
			var a4 = s.readUnsignedByte();
			var len = (a1<<24)+(a2<<16)+(a3<<8)+a4;
			
   			s.readBytes(b,0,len);
			return b;
		}
		
		private static function readString():String {
			var b:ByteArray = new ByteArray();						
			var a1 = s.readUnsignedByte();
			var a2 = s.readUnsignedByte();
			var len = (a1<<8)+a2;
			
   			return s.readUTFBytes(len);			
		}
		
		private static function readLongString():String {
			var b:ByteArray = readBytes();
			return b.readUTFBytes(b.length);
		}
		
		public static function initClient(sname){
			if(inited){
				return;
			}
			name = sname;
			inited = true;
			s = new Socket();						
			s.addEventListener(Event.CONNECT, function(){
				sendQueue();   
			});
			var port:int = 0;
			port = 123456;		
			s.connect("localhost",port);
			inited = true;
		}
		
		
		public static function writeLoaderURL(url){
			writeMsg(url,MSG_LOADER_URL);
		}
		
		
		public static function writeLoaderBytes(data:ByteArray){
			writeMsg(data,MSG_LOADER_BYTES);
		}
		
		
		
		public static function writeMsg(msg,msgType=0){
			if(!inited){
				initClient("");
			}
			if(s.connected){							
				if(first){
					//s.writeByte(0);
					writeStringNull("debug.version.major="+DEBUG_VERSION_MAJOR+";debug.version.minor="+DEBUG_VERSION_MINOR);
					writeString(name);
					first = false;					
				}		
				s.writeByte(msgType);
				switch(msgType){
					case MSG_STRING:
						writeString(msg);
						break;
					case MSG_LOADER_URL:
						writeString(msg);
						break;
					case MSG_LOADER_BYTES:
						writeBytes(msg);
						break;
					case MSG_LOADER_URL_GETBYTES:
						writeString(msg);						
						break;
					case MSG_LOADER_BYTES_GETBYTES:
						writeBytes(msg);						
						break;
				}				
			}else{
				q.push({type:msgType,data:msg});
			}			
		}

	}
	
}
