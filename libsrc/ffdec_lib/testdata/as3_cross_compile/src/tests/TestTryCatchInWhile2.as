package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchInWhile2
	{
		
		public function run() : void
		{
			var a:int;
			a = 0;			
			trace("before loop");
			while (a > 5)
			{
				try
				{
					trace("in try");
					if (a == 6){
						continue;
					}
					if (a == 7){
						break;
					}
					trace("after inner while");
				}
				catch (e:EOFError)
				{
					continue;
				}
				catch (e:Error)
				{
					if (a == 8){
						break;
					}
					continue;
				}
				a++;
			}
			//not reachable in ASC2:
			//trace("after loop");
		}
		
	}

}