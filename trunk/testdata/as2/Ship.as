class Ship implements Moving{
	var y;
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
	}
	function moveRight(rx:Number){
		// empty
	}
}