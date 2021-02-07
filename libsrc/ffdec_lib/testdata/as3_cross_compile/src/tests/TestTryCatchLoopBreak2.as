package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchLoopBreak2
	{
		
	
		public function run() : void
		{
			var a:int;
			a = 0;			
			trace("before loop");
			
			while (a < 20) { 
				
				try
				{
					trace("in try");	
					return;
				}
				catch(e:Error)
				{
					trace("in catch"); 
				}
				trace("a=" + a); 
			}
			trace("after"); 						
		}
		
	}

}