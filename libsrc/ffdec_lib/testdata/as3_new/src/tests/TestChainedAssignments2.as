package tests
{
	
	public class TestChainedAssignments2
	{   
		public function run():*
		{
            trace("e.attrib1 = e.attrib2 = e.attrib3 = 10;");
			var e:TestClass = new TestClass();
			e.attrib1 = e.attrib2 = e.attrib3 = 10;
		}				
	}
}

class TestClass {
	public var attrib1:int;	
	public var attrib2:int;
	public var attrib3:int;
}
