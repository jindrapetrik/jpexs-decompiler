package {
    import flash.display.MovieClip;
	import flash.text.TextField;

    public class MyComponent extends MovieClip {
        private var _opacity:Number = 1;
		private var _text:String = "Default text";
		private var _boxColor:uint = 0x3399FF;       


        public function MojeKomponenta() {
            stop();
			updateText();
        }

        [Inspectable(type="Number", defaultValue="1")]
        public function get opacity():Number {
            return _opacity;
        }

        public function set opacity(value:Number):void {
            _opacity = Math.max(0, Math.min(1, value));
            alpha = _opacity;
        }
		
		[Inspectable(type="String", defaultValue="Default text")]
        public function get text():String {
            return _text;
        }

        public function set text(value:String):void {
            _text = value == null ? "" : value;
            updateText();
        }

        private function updateText():void {
            var field:TextField =
                getChildByName("labelField") as TextField;

            if (field != null) {
                field.text = _text;
            }
        }
		
        [Inspectable(name="myColor", type="Color", defaultValue="#3399FF")]
        public function set boxColor(value:uint):void
        {
            _boxColor = value;
        }
		
		public function get boxColor():uint
        {
            return _boxColor;
        }
    }
}