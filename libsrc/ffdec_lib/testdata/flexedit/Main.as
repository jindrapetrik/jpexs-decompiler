package  {
	
	import flash.display.MovieClip;
	import flash.text.TextField;
	import pkg.MyClass;

	
	public class Main extends MovieClip {
		
		private var mc:MyClass;
		
		
		public function Main() {
			this.mc = new MyClass(5);
			var display_txt:TextField = new TextField();
			display_txt.text = "Hello myclass: "+this.mc.getVal()+" char:"+this.mc.getAChar();
			display_txt.width = 300;
			addChild(display_txt);
			

		}
	}
	
}
