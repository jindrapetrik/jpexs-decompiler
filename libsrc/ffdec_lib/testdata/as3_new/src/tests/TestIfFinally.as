package tests
{
	
	public class TestIfFinally
	{
		public function run():*
		{
			var a:int = Math.random();
			if (a == 5)
			{
				try
				{
					trace("in try body");
				}
				catch (e:Error)
				{
					trace("in catch");
				}
				finally
				{
					trace("in finally");
				}
			}
		}
	}
}
