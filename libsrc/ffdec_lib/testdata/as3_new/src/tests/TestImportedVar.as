package tests
{
	import tests_classes.myvar;
	
	public class TestImportedVar
	{
		public function run():void
		{
			trace(myvar);
			//myvar++;
			myvar = 5;
		}
	}
}
