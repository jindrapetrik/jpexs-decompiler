package tests
{
	
	public class TestMissingDefault
	{
		public function run():*
		{
			var jj:int = 1;
			switch (jj)
			{
			case 1: 
				jj = 1;
				break;
			case 2: 
				jj = 2;
				break;
			default: 
				jj = 3;
			}
		}
	}
}
