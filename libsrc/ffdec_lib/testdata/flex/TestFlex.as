package
{
	import flash.display.Sprite;
	import flash.text.TextField;
	import classes.*;
	
	public class TestFlex extends Sprite
	{
   
    private var a:Test;
    private var b:TestAlternative;
    private var c:TestArrayElementType;
    private var d:TestBindable;
    private var e:TestDefaultProperty;
    private var f:TestDeprecated;
    private var g:TestEffect_Event;
    private var h:TestSkinParts;
  
  
  
  
  
		public function TestMovie()
		{
			var display_txt:TextField = new TextField();
			display_txt.text = "Hello from Flex App!";
			addChild(display_txt);			
		}
		
	}
}