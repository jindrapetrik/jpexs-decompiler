package tests
{
	
	public class TestDoWhile4
	{
		public function run():*
		{
			var k:int = 8;
			do
			{
				if (k == 9)
				{
					trace("h");
					if (k == 9)
					{
						trace("f");
						continue;
					}
					trace("b");
				}
				trace("gg");
			} while (k < 10);
			trace("ss");
		}
	}
}
