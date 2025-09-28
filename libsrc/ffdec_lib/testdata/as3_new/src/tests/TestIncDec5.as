package tests
{
	
	public class TestIncDec5
	{
		
		public function run():*
		{
			var a:* = new TestClass1();
			
            trace("++a.attrib with result");
			trace(++a.attrib);
            
            trace("--a.attrib with result");
			trace(--a.attrib);
            
            trace("++a.attrib no result");
			++a.attrib;
            
            trace("--a.attrib no result");
			--a.attrib;
		}
	}
}

class TestClass1
{
	public var attrib:int = 5;
}
