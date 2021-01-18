package tests
{
	
	public class TestWhileAnd
	{
		public function run():*
		{
			var a:int = 5;
			var b:int = 10;
			while (a < 10 && b > 1)
			{
				a++;
				b--;
			}
			a = 7;
			b = 9;
		}
	}
}
