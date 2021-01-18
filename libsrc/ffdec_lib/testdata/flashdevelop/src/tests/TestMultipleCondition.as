package tests
{
	
	public class TestMultipleCondition
	{
		public function run():*
		{
			var a:* = 5;
			var b:* = 8;
			var c:* = 9;
			if ((a <= 4 || b <= 8) && c == 7)
			{
				trace("onTrue");
			}
			else
			{
				trace("onFalse");
			}
		}
	}
}
