package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchLoopBreak4
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
					if (a > 5){
						trace("a");
						if (a > 6){
							trace("b");
							break;
						}
						trace("c");
					}
					trace("in catch1b");
					if (a > 10){
						trace("d");
						if (a > 11){
							trace("e");
							break;
						}
						trace("f");
					}
					trace("in catch1c");
				}				
			}
			trace("after"); 
			
		}
		
	}

}