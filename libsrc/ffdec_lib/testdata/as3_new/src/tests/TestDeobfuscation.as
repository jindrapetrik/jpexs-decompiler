package tests
{
	
	public class TestDeobfuscation
	{
		public function run():*
		{
			var r:int = Math.random();
			var f:Boolean = false;
			
			if(r > 5 || f)
		    {
			  return "okay";
		    }
		    return "after";
		}
	}
}
