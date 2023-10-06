package tests_classes
{
	
	public class TestSubClass
	{
		public function run():*
		{
            var sc:SubClass = new SubClass();
            sc.a_internal = 1;
            //sc.b_private = 2; 
            sc.c_public = 3;
		}
	}
}

class SubClass {
    internal var a_internal:int;
    private var b_private:int;
    public var c_public:int
}
