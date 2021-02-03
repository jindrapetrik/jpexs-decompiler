package tests
{
	
	public class TestRest
	{
		public function run(firstp:int, ... restval):int
		{
			trace("firstRest:" + restval[0]);
			return firstp;
		}
	}
}
