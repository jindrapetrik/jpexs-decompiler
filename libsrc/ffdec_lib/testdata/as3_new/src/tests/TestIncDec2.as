package tests
{
	
	public class TestIncDec2
	{
		
		public function run():*
		{
			var a:* = 5;
			
            trace("a++ with result");
			trace(a++);
			
            trace("a-- with result");
			trace(a--);
            
            trace("a++ no result");
            a++;	
            
            trace("a-- no result");
            a--;			
		}
	}
}
