package tests
{
	
	public class TestIgnoreAndOr
	{
		public function run():*
		{
			var k:int = Math.random();
			if (k > 5 && true)
			{
				trace("A");
			}
			if (k > 10 || false)
			{
				trace("B");
			}
			if (true && k > 15)
			{
				trace("C");
			}
			if (false || k > 20)
			{
				trace("D");				
			}
		}
	}
}
