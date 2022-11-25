package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryFinallyReturnNested2
	{
		
		public function run() : String
		{
			var a:int = Math.random() * 5;
			try
			{
				try
				{
					try
					{
						trace("in try2");
						if (a > 4)
						{
							return "RET";
						}
					}
					catch (e2:Error)
					{
						trace("in catch2:e");
					}
					catch (e2:EOFError)
					{
						trace("in catch2:eof");
					}
					finally
					{
						trace("in finally2");
					}
					trace("after2");
				}
				catch (e1:Error)
				{
					trace("in catch1:e");
				}
				catch (e1:EOFError)
				{
					trace("in catch1:eof");
				}
				finally
				{
					trace("in finally1");
				}
				trace("after1");
			}		
			finally
			{
				trace("in finally0");
			}
			trace("after0");
			return "RETFINAL";
		}
		
	}

}