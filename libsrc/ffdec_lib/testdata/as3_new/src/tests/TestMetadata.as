package tests
{
	[MyClassTag(cls1 = "class 1", cls2 = "class 2")]
	public class TestMetadata
	{
        [MyVarTag(var1 = "var 1", var2 = "var 2")]
        public var v:int = 5;
        
        [MyConstTag]
        public const C:int = 10;
    
        [MySingleTag("tag")]
		public function run(): void
		{
			trace("hello");         
		}
	}
}