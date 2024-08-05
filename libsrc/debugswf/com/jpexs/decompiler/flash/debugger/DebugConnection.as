package com.jpexs.decompiler.flash.debugger {
	
	import flash.net.Socket;
	import flash.utils.ByteArray;
	import flash.events.Event;
	import flash.display.Sprite;
	import flash.utils.getTimer;
	import flash.events.ProgressEvent;
    
	public class DebugConnection {

		private static var s:Socket;
		private static var q = [];
		private static var inited:Boolean = false;
		private static var name:String;
		private static var failed:Boolean = false;
        private static var fillByteArrays = [];
        private static var fillByteArraysEvents = [];
        private static var lenBytes:Array = [
            -1, -1, -1, -1
        ];
        private static var lenBytePos:int = 0;
        private static var len:int = 0;
        private static var readBa:ByteArray;
        
        
		public static const DEBUG_VERSION_MAJOR = 1;
		public static const DEBUG_VERSION_MINOR = 3;
		
		public static const MSG_STRING = 0;
		public static const MSG_LOADER_URL = 1;
		public static const MSG_LOADER_BYTES = 2;
		public static const MSG_DUMP_BYTEARRAY = 3;			
		public static const MSG_REQUEST_BYTEARRAY = 4;
        public static const MSG_LOADER_URL_INFO = 5;
        public static const MSG_LOADER_MODIFY_BYTES = 6;     
        
        private static var connected:Boolean = false;   
        
        public static const SHOW_TRACE = false;
		
		private static function sendQueue(){
            if (SHOW_TRACE) trace("debugswf: client " + name + " sending queue");
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
            if (SHOW_TRACE) trace("debugswf: initing client " + sname);
			name = sname;
			inited = true;
			try {
				s = new Socket();
				s.addEventListener(Event.CONNECT, onSocketConnect);
                s.addEventListener(ProgressEvent.SOCKET_DATA, onSocketData);
				var port:int = 0;
				port = 123456;
                if (SHOW_TRACE) trace("debugswf: connecting client " + sname + " to localhost:" + port)	;				
				s.connect("localhost",port);
				
				inited = true;            		
			} catch (e:SecurityError) {
				if (SHOW_TRACE) trace("Debugger helper failed to connect to localhost");
				failed = true;
			}
		}
		
        private static function onSocketConnect(event:Event):void {
            if (SHOW_TRACE) trace("debugswf: client " + name + " connected");
            sendHeader();			
            connected = true;
            sendQueue();                         
        }
        
        
        private static function onSocketData(event:ProgressEvent):void {
			while (s.bytesAvailable > 0) {
                if (lenBytePos < 4) {
					lenBytes[lenBytePos] = s.readUnsignedByte();
					lenBytePos++;
					if (lenBytePos == 4) {
						len = (lenBytes[0]<<24)+(lenBytes[1]<<16)+(lenBytes[2]<<8)+lenBytes[3];
						readBa = new ByteArray();
					}
				} else {
					var readLen:int = s.bytesAvailable <= len ? s.bytesAvailable : len;
					s.readBytes(readBa, readBa.length, readLen);                
					len -= readLen;
                    if (len == 0) {                 
                        lenBytePos = 0;
						var ba:ByteArray = fillByteArrays.pop();
						var pos = ba.position;
						ba.position = 0;					
						ba.length = 0;
						ba.writeBytes(readBa);
						if (pos > ba.length) {
							ba.position = ba.length;
						} else {
							ba.position = pos;
						}
                        if (SHOW_TRACE) trace("debugswf: client " + name + " received bytearray of " + ba.length + " bytes");
                        var onComplete = fillByteArraysEvents.pop();                        
                        if (onComplete != null) {
                            if (SHOW_TRACE) trace("debugswf: client " + name + " calling onComplete");                        
                            onComplete.call(onComplete);
                        }
					}
				}
			}
        }
        
		
		public static function writeLoaderURL(url){
			writeMsg(url,MSG_LOADER_URL);
		}
		
		
		public static function writeLoaderBytes(data:ByteArray){
			writeMsg(data,MSG_LOADER_BYTES);
		}
        
        public static function modifyLoaderBytesWithUrl(data:ByteArray, outputData:ByteArray, url:String, onComplete:Function){
            writeMsg({"inputData": data, "outputData": outputData, "url" : url, "onComplete" : onComplete},MSG_LOADER_MODIFY_BYTES);
		}
		
		public static function writeCommaSeparatedToByteArray(s:String, ba:ByteArray) {
			var bytes:Array = s.split(",");
			var pos:uint = ba.position;
			ba.position = 0;					
			ba.length = 0;
			for (var i:int = 0; i < bytes.length; i++) {
				ba.writeByte(bytes[i]);
			}
			if (pos > ba.length) {
				ba.position = ba.length;
			} else {
				ba.position = pos;
			}
		}
		
		public static function readCommaSeparatedFromByteArray(ba:ByteArray): String {
			var s:String = "";
			var splitter = "";
			for (var i:int = 0; i < ba.length; i++) {
				s += splitter;
				s += ba[i];
				splitter = ",";
			}
			return s;
		}
		
		private static function sendHeader() {
			if (!s.connected) {
				return;
			}
			writeStringNull("debug.version.major="+DEBUG_VERSION_MAJOR+";debug.version.minor="+DEBUG_VERSION_MINOR);
			writeString(name);
		}
		
		public static function writeMsg(msg,msgType=0){
			if (failed) {
				return;
			}
			if(!inited) {
                var max:int = 100000;
                var randomInt:int = Math.floor(Math.random() * (max + 1));
				initClient("rand" + randomInt);
			}
			if ((msg is ByteArray) && msgType == MSG_DUMP_BYTEARRAY) {
				var ba2:ByteArray = new ByteArray();
				ba2.writeBytes(msg);
				msg = ba2;
			}
			
			if(connected) {		
                if (SHOW_TRACE) trace("debugswf: client " + name +" sending msg " + msgType);	
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
					case MSG_DUMP_BYTEARRAY:
						writeBytes(msg);
						break;
					case MSG_REQUEST_BYTEARRAY:
                        fillByteArrays.push(msg);
                        fillByteArraysEvents.push(null);
						break;
                    case MSG_LOADER_MODIFY_BYTES:
                        writeString(msg["url"]);
                        writeBytes(msg["inputData"]);
                        fillByteArraysEvents.push(msg["onComplete"]);
						fillByteArrays.push(msg["outputData"]); 
                        break;
				}
				s.flush();
                if (SHOW_TRACE) trace("debugswf: client " + name +" msg " + msgType + " sent");	
			}else{
                if (SHOW_TRACE) trace("debugswf: client " + name +" pushing msg " + msgType + " to queue");
				q.push({type:msgType,data:msg});
			}			
		}

	}
	
}
