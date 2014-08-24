class Box extends MovieClip{
	// declare class members
	var box_mc:MovieClip;
	// Constructor that takes mc as argument
	public function Box(passed_mc:MovieClip){
		// assign passed mc to our class member
		box_mc = passed_mc;
	}
	// Methods 
	public function moveUp(){
		box_mc._y -= 1;
	}
	
	public function moveDown(){
		box_mc._y += 20;
	}
}