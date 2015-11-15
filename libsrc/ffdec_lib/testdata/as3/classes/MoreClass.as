package classes {
	
	public class MoreClass {

		public function MoreClass() {
			trace("hello from public class");
		}

	}
	
}

import flash.utils.getDefinitionByName;
class SubTest {
	private var privat = 5;
	protected var protat = 6;
	public function go(){
		getDefinitionByName("aa");
		trace("hello from private class");
		this.privat = 5;
		this.protat = 6;
	}
}
