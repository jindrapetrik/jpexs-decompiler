package tests
{
	
	public class TestComplexExpressions
	{
		public function run():*
		{
			var i:int = 0;
			var j:int = 0;
			j = i = i + (i = i + i++);
		}
	}
}
