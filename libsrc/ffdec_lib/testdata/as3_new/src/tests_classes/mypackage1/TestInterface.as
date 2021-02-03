package tests_classes.mypackage1
{
	import tests_classes.mypackage2.TestInterface;

	public interface TestInterface extends tests_classes.mypackage2.TestInterface
	{
		function testMethod1() : void;
	}
}