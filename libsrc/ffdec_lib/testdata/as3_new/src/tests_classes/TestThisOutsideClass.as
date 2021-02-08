package tests_classes
{
	
	public class TestThisOutsideClass
	{
		public var attrib : int = 0;
		public function run():void
		{
			helperFunc.call(this,"hello");
		}
	}
}

function helperFunc(a:String): void
{
	trace(a);
	this.attrib++;
}
