package tests
{
	
	public class TestPrecedenceX
	{
		public function run():*
		{
			var a:* = 5;
			var b:* = 2;
			var c:* = 3;
			var d:* = a << (b >>> c);
			var e:* = a << b >>> c;
		}
	}
}
