package tests
{
	
	public class TestIfTry
	{
		public function run():*
		{
			var b:Boolean = true;
			
			if (b)
			{
				var c:int = 5;
				for (var i:int = 0; i < c; i++)
				{
					trace("xx");
				}				
			}	
			try			
			{
				trace("in try");				
			}
			catch (e:Error)
			{
				trace("in catch");
			}
		}
	}
}