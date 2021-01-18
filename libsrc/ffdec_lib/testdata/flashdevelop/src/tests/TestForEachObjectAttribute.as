package tests
{
	
	public class TestForEachObjectAttribute
	{
		private var testPriv:int = 5;
		
		public function run():*
		{
			var list:Array = null;
			list = new Array();
			list[0] = "first";
			list[1] = "second";
			list[2] = "third";
			for each (this.testPriv in list)
			{
				trace("item #" + this.testPriv);
			}
		}
	}
}
