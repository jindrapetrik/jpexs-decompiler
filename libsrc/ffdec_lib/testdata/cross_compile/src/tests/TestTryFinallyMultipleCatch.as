package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryFinallyMultipleCatch
	{
		
		public function run() : void
		{
			trace("before try");
			try
			{
				trace("in try");
			}
			catch (e:Error)
			{
				trace("in catch Error");
			}
			catch (e:EOFError)
			{
				trace("in catch EOFError");
			}
			finally
			{
				trace("in finally");
			}
			trace("after");
		}
		
	}

}