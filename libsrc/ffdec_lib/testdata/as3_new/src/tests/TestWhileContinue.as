package tests
{
	
	public class TestWhileContinue
	{
		public function run():*
		{
			var a:* = 5;
			while (true)
			{
				if (a == 9)
				{
					if (a == 8)
					{
						continue;
					}
					if (a == 9)
					{
						break;
					}
					trace("hello 1");
				}
				trace("hello2");
			}
		}
	}
}
