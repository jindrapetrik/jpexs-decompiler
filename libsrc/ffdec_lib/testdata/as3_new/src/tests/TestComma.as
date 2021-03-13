package tests
{
	
	public class TestComma
	{
		public function run():*
		{
			var a:int = 5;
			var b:int = 0;
			trace(a > 4 ? (b = 5, a) : 35);
		}
	}
}