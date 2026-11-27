package com.jpexs.classes {			
	public class MyClass {


		use namespace myusernamespace;
		
		public function MyClass() {
			var c:MyNsClass = new MyNsClass();
			c.run();
		}

	}
	
}

namespace myusernamespace = "http://www.jpexs.com/myns";	