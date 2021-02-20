package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchLoopBreak6
	{
		
	
		public function run() : void
		{
			var a:int;
			a = 0;			
			trace("before loop");			
			while (a < 10) { 			
				try
				{
					trace("in try"); 
				}
				catch(e:Error)
				{
					trace("in catch1"); 
					if (a > 3)
					{
						break;
					}
					try
					{
						trace("in try2"); 
					}
					catch(e:Error)
					{
						trace("in catch2"); 
						if (a > 4)
						{
							break;
						}
					}
				}	
				a++;
			}
			trace("after"); 
			
		}
		
	}

}