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
				if (d){
					return "D";
				}
			}
			finally
			{
				if (c) {
					return "C";
				}
			}
			trace("after");
			return "X";
		}
		
	}

}