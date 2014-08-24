class Enemy implements Moving {
	var x:Number;
	var tst:Number = 5;
	var tst2:Number;
	static var stat_tst:Number=6;
	
	
	static function sfunc(){
		trace("hu");
	}
	
	// constructor
	function Enemy(px:Number) {
		var k=57;
		k=k*27;
		var c=k;
		x = px+c;
	}
	function moveLeft(lx:Number) {
		x -= lx;
		tst=7;
		//hovno="bagr";
		trace("moveLeft = " + x);
	}
	function moveRight(rx:Number) {
		x += rx;
		trace("moveRight = " + x);
	}
	function moveUp(uy:Number) {
		// leave it empty , dont need it
		// but must implement it.
	}
	function moveDown(dy:Number) {

	}
}