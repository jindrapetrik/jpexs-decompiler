package classes
{

	public dynamic class TestClass2
	{
		public var attrib1:int;
		public var attrib2:int;
		public var attrib3:int;

		public function TestClass2(a1:String)
		{
			trace("Class2 construct");
		}

		public static function createMe(a1:String):TestClass2
		{
			return new TestClass2(a1);
		}
	}
}