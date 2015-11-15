package classes {
	
 class TestNs {

		
		public var pubAttr:int = 5;		
		private var privAttr:int = 5;
		protected var protAttr:int = 5;
		var noAttr:int = 5;
		myInternal var nsAttr:int = 5;


		public static var pubStatAttr:int = 5;		
		private static var privStatAttr:int = 5;
		protected static var protStatAttr:int = 5;
		static var noStatAttr:int = 5;
		myInternal static var nsStatAttr:int = 5;

	
	
		public function testAttr(){
			
			classes.testPkgFunc();
			
			this.pubAttr = 6;
			this.privAttr = 7;
			this.protAttr = 8;
			this.noAttr = 9;
			this.myInternal::nsAttr = 10;
			
			TestNs.pubStatAttr = 6;
			TestNs.privStatAttr = 7;
			TestNs.protStatAttr = 8;
			TestNs.noStatAttr = 9;
			TestNs.myInternal::nsStatAttr = 10;
			
		}
	
		public function pubFunc(){
			
		}
		
		private function privFunc(){
			
		}
		
		protected function protFunc(){
			
		}
		
		function noFunc(){
			
		}
		
		myInternal function nsFunc(){
			
		}
		
		//--------------- static:
		
		public static function pubStatFunc(){
			
		}
		
		private static function privStatFunc(){
			
		}
		
		protected static function protStatFunc(){
			
		}
		
		static function noStatFunc(){
			
		}
		
		myInternal static function nsStatFunc(){
			
		}																

	}			

}

function testSubFunction()
{
	
}

import classes.myInternal;

class SubTestNs {

		public var pubAttr:int = 5;
		private var privAttr:int = 5;
		protected var protAttr:int = 5;
		var noAttr:int = 5;
		myInternal var nsAttr:int = 5;


		public static var pubStatAttr:int = 5;		
		private static var privStatAttr:int = 5;
		protected static var protStatAttr:int = 5;
		static var noStatAttr:int = 5;
		myInternal static var nsStatAttr:int = 5;

	
	
		public function testAttr(){
			this.pubAttr = 6;
			this.privAttr = 7;
			this.protAttr = 8;
			this.noAttr = 9;
			this.myInternal::nsAttr = 10;
			
			SubTestNs.pubStatAttr = 6;
			SubTestNs.privStatAttr = 7;
			SubTestNs.protStatAttr = 8;
			SubTestNs.noStatAttr = 9;
			SubTestNs.myInternal::nsStatAttr = 10;
			
		}
	
		
		public function pubFunc(){
			
		}
		
		private function privFunc(){
			
		}
		
		protected function protFunc(){
			
		}
		
		function noFunc(){
			
		}
		
		myInternal function nsFunc(){
			
		}
		
		//--------------- static:
		
		public static function pubStatFunc(){
			
		}
		
		
		private static function privStatFunc(){
			
		}
		
		protected static function protStatFunc(){
			
		}
		
		static function noStatFunc(){
			
		}
		
		myInternal static function nsStatFunc(){
			
		}																

	}
