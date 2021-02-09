package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchLoopBreak5
	{
		
	
		public function run() : void
		{
			var a:int;
			a = 0;			
			trace("before loop");			
			while (true) { 			
				try
				{
					trace("in try"); 
				}
				catch(e:Error)
				{
					trace("in catch1"); 
					while (true){
						trace("xx");						
						if (a > 5){
							break;
						}
						trace("yy");
					}
					trace("in catch1c");
				}				
			}
			//trace("after"); 
			
		}
		
	}

}