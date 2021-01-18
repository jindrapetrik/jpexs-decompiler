package tests
{
	
	public class TestDoWhile
	{
		public function run():*
		{
			var a:* = 8;
			do
			{
				trace("a=" + a);
				a++;
			} while (a < 20);
		
		}
	}
}
