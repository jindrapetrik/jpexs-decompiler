package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchLoopBreak3
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
					break;
				}	
				catch(e:EOFError)
				{
					trace("in catch2"); 
					break;
				}
				
			}
			trace("after"); 
			
		}
		
	}

}