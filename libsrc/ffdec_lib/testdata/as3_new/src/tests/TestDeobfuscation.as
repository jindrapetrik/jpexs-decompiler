package tests
{
	
	public class TestDeobfuscation
	{
		public function run():*
		{
			var r:int = Math.random();
			var t:Boolean = true;
			var f:Boolean = false;
			
			if (r > 5 && t)
			{
				trace("A");
			}
			if (r > 10 || f)
			{
				trace("B");
			}
			if (t && r > 15)
			{
				trace("C");
			}
			if (f || r > 20)
			{
				trace("D");				
			}
			
			if (f)
			{
				trace("trash1");
			}
			
			if (!t)
			{
				trace("trash2");
			}
		}
	}
}
