package tests
{
	
	public class TestRegExp
	{
		public function run():*
		{
			var a1:* = /[a-z\r\n0-9\\]+/i;
			var a2:* = /[a-z\r\n0-9\\]+/i;
			var b1:* = /[0-9AB]+/;
			var b2:* = /[0-9AB]+/;
		}
	}
}
