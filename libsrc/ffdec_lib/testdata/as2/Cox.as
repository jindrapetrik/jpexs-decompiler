class Cox extends Box{
	public function Cox(passed_mc:MovieClip){
		super(passed_mc);
	}
	// Methods 
	public function testPublic(){
		trace("pub");
	}
	private function testPrivate(){
		trace("priv");
	}
}