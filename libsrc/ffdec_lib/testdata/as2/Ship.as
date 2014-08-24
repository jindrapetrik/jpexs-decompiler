class Ship implements Moving{
	var y;
	var a;
	var b;
	var c;
	private var d=5;
	// constructor
	function Ship(py:Number){
		y = py;
	}
	function moveUp(uy:Number){
		y *= uy;
		trace("moveUp = "+y);
	}
	function moveDown(dy:Number){
		y *= dy;
		trace("moveDown = "+y);
	}
	function moveLeft(lx:Number){
		// empty
		b = 6;
	}
	function moveRight(rx:Number){
		// empty
		trace(a);
		trace(d);
	}
}