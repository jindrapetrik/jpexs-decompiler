package tests
{
	
	public class TestForInSwitch
	{
		public function run():*
		{
			var arr:Array = ["a", "b", "c"];
			
			for (var a:String in arr){
				
				switch(a){
					case "a":
						trace("val a");
						break;
					case "b":
						trace("val b");
						break;
					case "c":
						trace("val c");
						//break;
				}
				trace("final");
			}			
		}
	}
}