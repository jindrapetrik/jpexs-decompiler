package tests
{
	
	public class TestInnerIf
	{
		public function run():*
		{
			var a:* = 5;
			var b:* = 4;
			if (a == 5)
			{
				if (b == 6)
				{
					trace("b==6");
				}
				else
				{
					trace("b!=6");
				}
			}
			else if (b == 7)
			{
				trace("b==7");
			}
			else
			{
				trace("b!=7");
			}
			
			trace("end");
		}
	}
}
