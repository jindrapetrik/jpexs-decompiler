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
		
				
		
		private static function sendQueue(){
			var qo = q;
				q = [];
				for each(var m in qo){
					writeMsg(m);
				}
		}
		
		private static function writeString(msg){
			var b:ByteArray = new ByteArray();			
   			b.writeUTFBytes(msg);						
			s.writeByte((b.length>>8) & 0xff);
			s.writeByte(b.length & 0xff);
			s.writeBytes(b,0,b.length);						
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
		
		public static function writeMsg(msg){
			if(!inited){
				initClient("");
			}
			if(s.connected){							
				if(first){
					s.writeByte(0);
					writeString(name);
					first = false;					
				}				
				writeString(msg);
			}else{
				q.push(msg);
			}			
		}

	}
	
}
