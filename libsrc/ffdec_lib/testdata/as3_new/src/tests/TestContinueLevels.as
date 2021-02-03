package tests
{
	
	public class TestContinueLevels
	{
		public function run():*
		{
			var a:* = 5;
			loop123: switch (a)
			{
			case 57 * a: 
				trace("fiftyseven multiply a");
				var b:* = 0;
				while (b < 50)
				{
					if (b == 10)
					{
						break;
					}
					if (b == 15)
					{
						break loop123;
					}
					b = b + 1;
				}
				break;
			case 13: 
				trace("thirteen");
			case 14: 
				trace("fourteen");
				break;
			case 89: 
				trace("eightynine");
				break;
			default: 
				trace("default clause");
			}
			
			loop182: for (var c:* = 0; c < 8; c = c + 1)
			{
				
				loop165: for (var d:* = 0; d < 25; d++)
				{
					
					for (var e:* = 0; e < 50; e++)
					{
						if (e == 9)
						{
							break loop165;
						}
						if (e == 20)
						{
							continue loop182;
						}
						if (e == 8)
						{
							break;
						}
						break loop182;
					}
				}
				trace("hello");
			}
		}
	}
}
