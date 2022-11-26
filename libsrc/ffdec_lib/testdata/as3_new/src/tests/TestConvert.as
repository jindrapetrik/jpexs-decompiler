package tests
{
	
	public class TestConvert
	{
        private var n:int = 1;
        private var ns:String = "b";
        
        public static var TEST:String = "Hello";
        
        public var TEST:int = 5;
        
		public function run():void
		{
            var s:String = "a";
            var i:int = int(s);
            var j:int = n;
            var a:*;
            s = String(j);
            s = ns;
            
            s = String((i == 4) ? "" : i);
            s = (i == 4) ? "" : String(i);
            
            s = TestConvert.TEST;
            i = this.TEST;
            
            i = Number("4") * 5;
            i = a * 6;
            i = a;
            
            var o:Object = {
            0: "A",
            1: "B",
            2: "C"
            };           
            
            i = int(s.charAt(10));
		}
	}
}
