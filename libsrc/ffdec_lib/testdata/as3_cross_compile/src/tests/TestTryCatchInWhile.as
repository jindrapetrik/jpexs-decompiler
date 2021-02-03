package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchInWhile
	{
		
		public function run() : void
		{
			trace("before loop");
			while (true)
			{
				try
				{
					trace("in try");
					while (true)
					{
						trace("a");
					}
					//not reachable in ASC2
					//trace("after inner while");
				}
				catch (e:EOFError)
				{
					continue;
				}
				catch (e:Error)
				{
					continue;
				}
			}
			//not reachable in ASC2:
			//trace("after loop");
		}
		
	}

}