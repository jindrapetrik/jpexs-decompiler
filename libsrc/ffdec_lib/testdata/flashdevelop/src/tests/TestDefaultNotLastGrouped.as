package tests
{
	
	public class TestDefaultNotLastGrouped
	{
		public function run():*
		{
			var k:* = 10;
			switch (k)
			{
			default: 
			case "six": 
				trace("def and 6");
			case "five": 
				trace("def and 6 and 5");
				break;
			case "four": 
				trace("4");
			}
			trace("after switch");
		}
	}
}
