package tests
{
	
	public class TestChainedAssignments
	{
        private var prop:int;
    
		public function run():*
		{
			var a:int = 0;
			var b:int = 0;
			var c:int = 0;
			var d:int = 0;
            var f:int = 0;
			d = c = b = a = 5;
			var e:TestClass2 = TestClass2.createMe("test");
			e.attrib1 = e.attrib2 = e.attrib3 = this.getCounter();
			this.traceIt(e.toString());
            prop = f = a = 4;
            if (f == 2) {
                trace("OK: " + f);                
            }
		}
		
		private function getCounter() : int
		{
			return 5;
		}
		
		private function traceIt(s:String) : void
		{
			trace(s);
		}
	}
}

class TestClass2 {
	public var attrib1:int;	
	public var attrib2:int;
	public var attrib3:int;
	
	public function TestClass2(a1:String)
	{
		trace("Class2 construct");
	}
		
	public static function createMe(a1:String):TestClass2
	{
		return new TestClass2(a1);
	}
	
	public function toString() : String
	{
		return "tc2";
	}
}
