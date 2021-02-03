package tests
{
	
	public class TestSwitch
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
			}
		}
	}
}
