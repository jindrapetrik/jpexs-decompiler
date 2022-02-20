package tests
{
	
	public class TestSwitchIf
	{
		public function run():*
		{
			var code:String = "4";
			var a:Boolean = true;
			switch(int(code) - 2)
			 {
				case 0:
				case 1:
				   if(a)
				   {
					  trace("A");
					  break;
				   }
			 }
			 trace("B");
		}
	}
}
