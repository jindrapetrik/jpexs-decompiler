package tests
{
	
	public class TestIncDec7
	{
		
		public function run():*
		{
			var a:* = [1,2,3,4,5];
            var index:int = 0;
            
            trace("a[++index]");
			trace(a[++index]);
            
            trace("a[--index]");
			trace(a[--index]);                                
		}
	}
}
