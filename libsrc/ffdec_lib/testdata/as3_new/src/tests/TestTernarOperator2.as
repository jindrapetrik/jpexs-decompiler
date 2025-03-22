package tests
{
	
	public class TestTernarOperator2
	{
		public function run():*
		{
			var b:Boolean = true;
            var i:int = 1;
            var j:int = b ? i : i + 1;
            var k:int = i ? j : j + 1;
		}
	}
}
