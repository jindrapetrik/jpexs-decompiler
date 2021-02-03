package tests
{
	
	public class TestProperty
	{
		public function run():*
		{
			var d:* = new TestClass1();
			var k:* = 7 + 8;
			if (k == 15)
			{
				d.method(d.attrib * 5);
			}
		}
	}
}

class TestClass1
{
	public var attrib:int = 5;

	public function method(i:int):int
	{
		trace("method");
		return 7;
	}
}