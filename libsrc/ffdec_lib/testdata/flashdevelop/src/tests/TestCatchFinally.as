package tests
{
	
	public class TestCatchFinally
	{
		public function run():*
		{
			var a:* = 5;
			try
			{
				a = 9;
				trace("intry");
			}
			catch (e:*)
			{
				trace("incatch");
			}
			finally
			{
				trace("infinally");
			}
			trace("after");
		}
	}
}
