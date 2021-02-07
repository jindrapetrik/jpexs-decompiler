package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchInWhile4
	{
		
		public function run() : void
		{
			var a:int;
			a = 0;
			
			while (true) { 
				
				try
				{
					trace("try2");	//12-21
					if (a == 10){
						trace("br");
						break;
					}
					return;
				}
				catch(e:Error)
				{
					trace("in catch2"); 
				}
				trace("a=" + a); 
			}
			trace("after");  //61-66
		}
		
	}

}