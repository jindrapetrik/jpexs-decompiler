package tests
{
	import flash.utils.Dictionary;
	
	public class TestForIn
	{
		public function run():*
		{
			var dic:Dictionary = null;
			var item:* = null;
			for (item in dic)
			{
				trace(item);
			}
			for each (item in dic)
			{
				trace(item);
			}
		}
	}
}
