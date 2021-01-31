package tests 
{
	import flash.errors.EOFError;
	
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchLoop 
	{
		
		public function run() : void
		{
			var i:int = 0;
			while (i < 100)
			{
				try
				{
					var j:int = 0;
					while (j < 20)
					{
						trace("a");
						j++;
					}
				}
				catch (e:EOFError)
				{
					continue;
				}
				catch (e:Error)
				{
					continue;
				}
				trace("after_try");
				i++;
			}
			trace("end");
		}
		
	}

}