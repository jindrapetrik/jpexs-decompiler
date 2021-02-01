package tests
{
	
	public class TestDotParent
	{
		public function run():*
		{			
			var d:* = new TestClass1();
			var k:* = null;
			
			k.(d.attrib++, 0);
			trace("between");		
			var g:* = k.(d.attrib++, 0);
			trace("end");
		}
	}
}

class TestClass1
{
	public var attrib:int = 5;
}