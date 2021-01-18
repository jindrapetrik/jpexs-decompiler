package tests
{
	
	public class TestTernarOperator
	{
		public function run():*
		{
			var a:* = 5;
			var b:* = 4;
			var c:* = 4;
			var d:* = 78;
			var e:* = a == b ? c == d ? 1 : 7 : 3;
			trace("e=" + e);
		}
	}
}
