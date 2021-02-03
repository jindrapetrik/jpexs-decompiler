package tests 
{
	public class TestTryReturn2 
	{
		
		public function run() : String
		{
			trace("before");
			
			var a:Boolean = true;
			var b:Boolean = false;
			var c:Boolean = true;
			var d:Boolean = false;
			var e:Boolean = true;
			try
			{
				if (a)
				{
					return "A";
				}
				if (b)
				{
					return "B";
				}
			}
			catch (e:Error)
			{
				if (c){
					return "C";
				}
			}
			finally
			{				
				if (d) {
					return "D";
				}
				if (e) {
					return "E";
				}
			}
			trace("after");
			return "X";
		}
		
	}

}