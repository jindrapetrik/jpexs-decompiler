package tests
{
	
	public class TestInnerFunctionScope
	{		
		public function run(a:String):*
		{
			var innerFunc:Function = function(b:String):*
			{
				testProm = 4;
				trace(testProm);
			};
			
			innerFunc(a);
		}
	}
}

var testProm:int = 5;