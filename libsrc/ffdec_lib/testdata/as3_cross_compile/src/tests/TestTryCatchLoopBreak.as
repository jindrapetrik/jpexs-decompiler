package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchLoopBreak
	{
		
	
		public function run() : void
		{
			var a:int;
			a = 0;			
			trace("before loop");
			try
			{
				trace("in try1");
				while (a < 10){ 
					trace("a=" + a); 
					a++;
				}
				trace("in try2"); 
				
			}
			catch(e:Error)
			{
				trace("in catch"); 
			}				

			trace("after");			
			
		}
		
	}

}