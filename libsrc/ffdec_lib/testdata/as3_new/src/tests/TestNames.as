package tests
{
	import tests_other.myInternal;
    import tests_other.myInternal2;
	
    
    
	public class TestNames
	{
        myInternal var neco:int;
        myInternal2 var neco:int;
        internal var nic:int;
		
		public function run():*
		{
			var ns:* = this.getNamespace();
			var name:* = this.getName();
			var a:* = ns::unnamespacedFunc();
			var b:* = ns::[name];
			trace(b.c);
			var c:* = myInternal::neco; 
            use namespace myInternal2;             
            var d:* = neco;                       
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
        
        myInternal2 function namespacedFunc2() : void
		{
			trace("hello");
		}
	}
}
