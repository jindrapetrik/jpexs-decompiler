package classes.mypackage1
{
	import classes.mypackage2.TestInterface;

	public interface TestInterface extends classes.mypackage2.TestInterface
	{
		function testMethod1() : void;
	}
}