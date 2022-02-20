package tests
{
	
	public class TestCallLocal
	{
		public function getF(): Function
		{
			return function(a:int, b:int):int {
				return a + b;
			};
		}
		
		public function run():*
		{
			var f:Function = getF();
			var b:int = f(1, 3);
		}
	}
}
