package tests
{
	
	public class TestInnerTry
	{
		public function run():*
		{
			try
			{
				try
				{
					trace("try body 1");
				}
				catch (e:DefinitionError)
				{
					trace("catched DefinitionError");
				}
				trace("after try 1");
			}
			catch (e:Error)
			{
				trace("catched Error");
			}
			finally
			{
				trace("finally block");
			}
		}
	}
}
