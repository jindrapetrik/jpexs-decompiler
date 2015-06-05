package  {
	
	import flash.display.*;
	import flash.text.TextField;
	import flash.events.MouseEvent;
	
	public class RunMain extends MovieClip {
		
		private var myTextBox:TextField;
		
		public function RunMain() {
			myTextBox = new TextField();    
			myTextBox.text = "";    
			addChild(myTextBox);  

			var rectangleShape:Shape = new Shape();
			rectangleShape.graphics.beginFill(0xFF0000);
			rectangleShape.graphics.drawRect(0, 0, 100, 25);
			rectangleShape.graphics.endFill();

			var btnTextBox:TextField = new TextField();    
			btnTextBox.text = "EXECUTE";    

			var simpleButtonSprite:Sprite = new Sprite();
			simpleButtonSprite.name = "simpleButtonSprite";
			simpleButtonSprite.addChild(rectangleShape);
			simpleButtonSprite.addChild(btnTextBox);

			var simpleButton:SimpleButton = new SimpleButton();
			simpleButton.upState = simpleButtonSprite;
			simpleButton.overState = simpleButtonSprite;
			simpleButton.downState = simpleButtonSprite;
			simpleButton.hitTestState = simpleButtonSprite;
			simpleButton.x = 200;
			simpleButton.y = 100;
			simpleButton.addEventListener(MouseEvent.CLICK, this.clickListener);
			addChild(simpleButton);
		}
		
		function clickListener(e:MouseEvent){
			myTextBox.text = "Result:" + Run.run();
		}
	}
}
