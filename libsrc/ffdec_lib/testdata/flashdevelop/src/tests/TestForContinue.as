package tests
{
	
	public class TestForContinue
	{
		public function run():*
		{
			for (var a:* = 0; a < 10; a = a + 1)
			{
				if (a == 9)
				{
					if (a == 5)
					{
						trace("part1");
						continue;
					}
					trace("a=" + a);
					if (a == 7)
					{
						trace("part2");
						continue;
					}
					trace("part3");
				}
				else
				{
					trace("part4");
				}
				trace("part5");
			}
		}
	}
}
