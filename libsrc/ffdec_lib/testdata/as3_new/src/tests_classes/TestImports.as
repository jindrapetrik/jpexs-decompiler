package tests_classes
{
	import tests_classes.myjson.JSON;
	import tests_classes.myjson2.JSON;
	
	public class TestImports
	{
		public function run():*
		{
			var j1:tests_classes.myjson.JSON = new tests_classes.myjson.JSON();
			var j2:tests_classes.myjson2.JSON = new tests_classes.myjson2.JSON();
			trace(j1);
			trace(j2);
		}
	}
}
