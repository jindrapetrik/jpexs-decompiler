package tests
{
	
	public class TestForInIf
	{
		public function run():*
		{
			var arr:Array = ["a", "b", "c"];
			var b:int = 5;
			
			for (var a:String in arr){
				
				if (b == 5){
					if (b > 7){
						trace("b>7");						
					}else{
						return;
					}
				}
				trace("forend");
			}			
		}
	}
}