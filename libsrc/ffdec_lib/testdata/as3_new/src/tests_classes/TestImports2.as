package tests_classes
{
	import tests_classes.myjson.JSON;
	
    public class TestImports2
	{
		public function run():*
		{
			var j:tests_classes.myjson.JSON = new tests_classes.myjson.JSON(); //Should not collide with toplevel AS3 JSON class 
		}
	}
}
