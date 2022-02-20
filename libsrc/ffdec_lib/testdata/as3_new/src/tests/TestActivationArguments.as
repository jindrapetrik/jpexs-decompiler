package tests
{
	
	public class TestActivationArguments
	{
		public function run():*
		{
			var func:Function = function(a:int, b:int):int {
				return a + b;
			}
			if (arguments.length > 0){
				trace(arguments[0]);
			}
		}
	}
}
