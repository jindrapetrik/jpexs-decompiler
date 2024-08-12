package tests
{
	
	public class TestOptimization
	{
		public function run():*
		{
            // Add more than 3 variables. 
            // Optimization happens from register 4 on.
            // (setlocal X takes more bytes than dup)
			var a:int = 1;
            var b:int = 2;
            var c:int = 3;
            
            var d:int = 4; //setlocal N
            var e:int = d + 5; //getlocal N is replaced with dup before setlocal N
            
            //We must leave this case intact:
            var f:int;
            var g:int;
            var h:int;
            var i:int = h = g = f;   
		}
	}
}
