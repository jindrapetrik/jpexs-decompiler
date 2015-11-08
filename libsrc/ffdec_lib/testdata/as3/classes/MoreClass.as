package classes {
	
	public class MoreClass {

		public function MoreClass() {
			trace("hello from public class");
		}

	}
	
}

import flash.utils.getDefinitionByName;
class SubTest {
	public function go(){
		getDefinitionByName("aa");
		trace("hello from private class");
	}
}
