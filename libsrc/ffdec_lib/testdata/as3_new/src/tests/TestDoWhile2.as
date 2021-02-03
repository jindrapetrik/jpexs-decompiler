package tests
{
	
	public class TestDoWhile2
	{
		public function run():*
		{
			var k:int = 5;
			do
			{
				k++;
				if (k == 7)
				{
					k = 5 * k;
				}
				else
				{
					k = 5 - k;
				}
				k--;
			} while (k < 9);
			
			return 2;
		}
	}
}
