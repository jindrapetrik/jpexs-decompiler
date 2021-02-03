package tests
{
	import tests_other.myInternal;
	
	public class TestNames
	{
		myInternal var neco:int;
		
		public function run():*
		{
			var ns:* = this.getNamespace();
			var name:* = this.getName();
			var a:* = ns::unnamespacedFunc();
			var b:* = ns::[name];
			trace(b.c);
			var c:* = myInternal::neco;
		}
		
		public function getNamespace():Namespace
		{
			return myInternal;
		}

		public function getName():String
		{
			return "unnamespacedFunc";
		}
		
		myInternal function namespacedFunc() : void
		{
			trace("hello");
		}
	}
}
