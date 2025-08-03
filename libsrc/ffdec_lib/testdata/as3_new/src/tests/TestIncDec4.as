package tests
{
	
	public class TestIncDec4
	{
		
		public function run():*
		{
			var a:* = [1, 2, 3, 4, 5];
			
            trace("a[2]++ with result");
			trace(a[2]++);
            
            trace("a[2]-- with result");
			trace(a[2]--);
            
            trace("a[2]++ no result");
			a[2]++;
            
            trace("a[2]-- no result");
			a[2]--;
		}
	}
}
