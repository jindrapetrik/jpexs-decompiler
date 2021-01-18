package tests
{
	
	public class TestInnerFunctions
	{
		public function run(a:String):*
		{
			var s:int = 0;
			var innerFunc:Function = function(b:String):*
			{
				trace(b);
			};
			var k:int = 5;
			if (k == 6)
			{
				s = 8;
			}
			innerFunc(a);
		}
	}
}
