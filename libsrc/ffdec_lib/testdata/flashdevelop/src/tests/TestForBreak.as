package tests
{
	
	public class TestForBreak
	{
		public function run():*
		{
			for (var a:* = 0; a < 10; a++)
			{
				if (a == 5)
				{
					break;
				}
				trace("hello:" + a);
			}
		}
	}
}
