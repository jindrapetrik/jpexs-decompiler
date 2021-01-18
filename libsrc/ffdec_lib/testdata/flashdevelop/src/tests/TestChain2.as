package tests
{
	
	public class TestChain2
	{
		public function run():*
		{
			var g:Array = null;
			var h:Boolean = false;
			var extraLine:Boolean = false;
			var r:int = 7;
			var t:int = 0;
			t = this.getInt();
			if (t + 1 < g.length)
			{
				t++;
				h = true;
			}
			if (t >= 0)
			{
				trace("ch");
			}
		}
		
		private function getInt():int
		{
			return 5;
		}
	}
}
