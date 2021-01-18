package tests
{
	
	public class TestTry
	{
		public function run():*
		{
			var i:int = 0;
			i = 7;
			try
			{
				trace("try body");
			}
			catch (e:DefinitionError)
			{
				trace("catched DefinitionError");
			}
			catch (e:Error)
			{
				trace("Error message:" + e.message);
				trace("Stacktrace:" + e.getStackTrace());
			}
			finally
			{
				trace("Finally part");
			}
			trace("end");
		}
	}
}
