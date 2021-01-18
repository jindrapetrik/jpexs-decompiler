package tests
{
	
	public class TestForEachObjectArray
	{
		public function run():*
		{
			var list:Array = null;
			var test:Array = null;
			list = new Array();
			list[0] = "first";
			list[1] = "second";
			list[2] = "third";
			test = new Array();
			test[0] = 0;
			for each (test[0] in list)
			{
				trace("item #" + test[0]);
			}
		}
	}
}
