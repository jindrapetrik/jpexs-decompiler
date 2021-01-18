package tests
{
	
	public class TestForEach
	{
		public function run():*
		{
			var list:Array = null;
			var item:* = undefined;
			list = new Array();
			list[0] = "first";
			list[1] = "second";
			list[2] = "third";
			for each (item in list)
			{
				trace("item #" + item);
			}
		}
	}
}
