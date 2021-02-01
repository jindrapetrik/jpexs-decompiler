package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestDoWhile3 
	{
		private var ch:String;
		
		public function run() : void
		{
			do
			{
				nextChar();
			} while ( ch != '\n' && ch != '' )
		}
		
		private function nextChar() : void
		{
			trace("process next char");
		}
				
	}

}