package tests
{
	
	public class TestPrecedence
	{
		public function run():*
		{
			var a:* = 0;
			a = (5 + 6) * 7;
			a = 5 * (2 + 3);
			a = 5 + 6 * 7;
			a = 5 * 2 + 2;
			a = 5 * (25 % 3);
			a = 5 % (24 * 307);
			a = 1 / (2 / 3);
			a = 1 / (2 * 3);
			a = 1 * 2 * 3;
			a = 1 * 2 / 3;
			trace("a=" + a);
		}
	}
}
