package tests
{
	
	public class TestSwitchDefault
	{
		public function run():*
		{
			var a:* = 5;
			switch (a)
			{
			case 57 * a: 
				trace("fiftyseven multiply a");
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
		}
	}
}
