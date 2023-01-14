package tests
{
	
	public class TestDeclarationInterface
	{
		public function run(): MyIFace
		{
			var i:MyIFace = null;
			var n:int = 2;
			switch(n) {
				case 0:
					i = new MyClass();
					break;
				case 1:
					i = new MyClass2();
					break;
			}
			return i;
		}
	}
}


interface MyIFace {
	
}

class MyClass implements MyIFace {
	
}

class MyClass2 implements MyIFace {
	
}