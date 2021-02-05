package tests
{
	
	public class TestTryIf
	{
		public function run():*
		{
			var a:int = Math.random();
			try
			{
				if (a > 5 && a < 50)
				{
					trace("in limits");
				}
				trace("next");
			}
			catch (e:Error)
			{
				trace("in catch");				
			}
		}
	}
}
