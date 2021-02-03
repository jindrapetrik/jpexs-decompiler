package tests
{
	import flash.errors.EOFError;
	
	public class TestWhileTry
	{
		public function run():*
		{
			while (true)
			{
				try
				{
					while (true)
					{
						trace("a");
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
			}
		}
	}
}
