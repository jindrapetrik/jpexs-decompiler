package tests
{
	
	public class TestLogicalComputing
	{
		public function run():*
		{
			var b:Boolean = false;
			var i:* = 5;
			var j:* = 7;
			if (i > j)
			{
				j = 9;
				b = true;
			}
			b = (i == 0 || i == 1) && j == 0;
		}
	}
}
