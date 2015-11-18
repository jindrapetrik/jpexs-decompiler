package 
{

	import flash.display.*;
	import flash.text.TextField;
	import flash.events.MouseEvent;
	import flash.external.ExternalInterface;
	import flash.system.fscommand;

	public class RunMain extends MovieClip
	{

		private var myTextBox:TextField;

		public function RunMain()
		{
			myTextBox = new TextField();
			myTextBox.text = "";
			myTextBox.width = 400;
			myTextBox.multiline = true;
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

			if (ExternalInterface.available)
			{
				ExternalInterface.addCallback("testFunc", testFunction);
			}

			var result = testFunction();
			myText.text = result;
			fscommand("run", result);
		}

		function testFunction()
		{
			try
			{
				var result = Run.run();
				return "Result:" + result + " Type:" + typeof(result);
			}
			catch (ex)
			{
				return "Error:" + ex;
			}
		}

		function clickListener(e:MouseEvent)
		{
			var result = testFunction();
			myTextBox.text = result;

			fscommand("run", result);
		}
	}
}