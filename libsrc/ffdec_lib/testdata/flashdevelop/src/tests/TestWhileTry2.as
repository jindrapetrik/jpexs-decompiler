package tests
{
	import flash.errors.EOFError;
	
	public class TestWhileTry2
	{
		public function run():*
		{
			var j:* = undefined;
			for (var i:* = 0; i < 100; i++)
			{
				try
				{
					for (j = 0; j < 20; j++)
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
				trace("after_try");
			}
			trace("end");
		}
	}
}
