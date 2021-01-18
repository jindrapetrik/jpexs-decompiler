package tests
{
	
	public class TestStringConcat
	{
		public function run():*
		{
			var k:int = 8;
			this.traceIt("hello" + 5 * 6);
			this.traceIt("hello" + (k - 1));
			this.traceIt("hello" + 5 + 6);
		}
		
		private function traceIt(s:String) : void
		{
			trace(s);
		}
	}
}
