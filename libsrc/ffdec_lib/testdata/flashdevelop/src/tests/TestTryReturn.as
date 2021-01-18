package tests
{
	
	public class TestTryReturn
	{
		public function run():*
		{
			var i:int = 0;
			var b:Boolean = false;
			try
			{
				i = 0;
				b = true;
				if (i > 0)
				{
					while (this.testDoWhile2())
					{
						if (b)
						{
							return 5;
						}
					}
				}
				i++;
				return 2;
			}
			catch (e:Error)
			{
			}
			return 4;
		}
		
		public function testDoWhile2() :Boolean{
			return true;
		}
	}
}
