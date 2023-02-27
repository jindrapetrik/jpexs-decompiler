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
            
            var n1:Number = 5;
            var n2:Number = 2;
            var n3:Number = 1;            
            var r:Number;
            
            trace("not a regexp 1");
            r = n1 / n2 / n3;         
            trace("not a regexp 2")               
            r /= n1 / n2;
		}
	}
}
