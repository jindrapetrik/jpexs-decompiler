package  {
	
	public class MyClass {
		public var a1:int = 1;
		public var a2:int = 2;
		
		private var a1x:int = 3;
		private var a2x:int = 4;
		
		protected var a1y:int = 5;
		protected var a2y:int = 6;		
		
		
		public static var a1:int = 7;
		public static var a2:int = 8;

		private static var a1x:int = 9;
		private static var a2x:int = 10;		

		protected static var a1y:int = 11;
		protected static var a2y:int = 12;
		
		myns var a1:int = 13;
		myns var a2:int = 14;

		myns2 var a1:int = 15;
		myns2 var a2:int = 16;



		
		public function f1():int {
			return 17;
		}
		
		public function f2():int {
			return 18;
		}
				
        
		protected function f1x():int {
			return 19;
		}
		
		protected function f2x():int {
			return 20;
		}				
		
		
		private function f1y():int {
			return 21;
		}
		
		private function f2y():int {
			return 22;
		}
		
		
		public static function f1():int {
			return 23;
		}
		public static function f2():int {
			return 24;
		}
				
				
        protected static function f1x():int {
			return 25;
		}
		protected static function f2x():int {
			return 26;
		}				
		
		
		private static function f1y():int {
			return 27;
		}
		private static function f2y():int {
			return 28;
		}
		
		myns function f1():int {
			return 29;
		}
		myns function f2():int {
			return 30;
		}
		
		myns2 function f1():int {
			return 31;
		}
		myns2 function f2():int {
			return 32;
		}		
											
											
		public function getResult():int {
			var inst_a = this.a1 + this.a2 + this.a1x + this.a2x + this.a1y + this.a2y;
			var inst_f = this.f1() + this.f2() + this.f1x() + this.f2x() + this.f1y() + this.f2y();
			var inst = inst_a + inst_f;
			
			var stat_a = MyClass.a1 + MyClass.a2 + MyClass.a1x + MyClass.a2x + MyClass.a1y + MyClass.a2y;
			var stat_f = MyClass.f1() + MyClass.f2() + MyClass.f1x() + MyClass.f2x() + MyClass.f1y() + MyClass.f2y();									
			var stat = stat_a + stat_f;
			
			var ns_a = myns::a1 + myns::a2 + myns2::a1 + myns2::a2;
			var ns_f = myns::f1() + myns::f2() + myns2::f1() + myns2::f2();
			var ns = ns_a + ns_f;
														
			return inst+stat+ns; //528
		}
	}
	
}
