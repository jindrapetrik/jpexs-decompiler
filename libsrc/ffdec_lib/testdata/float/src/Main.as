package
{
    import flash.display.Sprite;
    import flash.events.Event;
    import flash.text.TextField;
    import flash.text.TextFormat;
    
    public class Main extends Sprite
    {        
        
        public function Main()
        {
            if (stage) init();
            else addEventListener(Event.ADDED_TO_STAGE, init);
        }
        
        private function init(e:Event = null):void
        {
            removeEventListener(Event.ADDED_TO_STAGE, init);
    
    
            var textFormat:TextFormat = new TextFormat();
            textFormat.size = 24; 
            textFormat.color = 0xFF0000; 
            textFormat.font = "Arial"; 
            textFormat.bold = true; 

            var textField:TextField = new TextField();
            textField.defaultTextFormat = textFormat;
            textField.x = 100; 
            textField.y = 100;
            textField.width = 500;             
            
            var a:float = 10.5f;
            
            textField.text = "Value: " + a;
            
            addChild(textField); 
            
        }    
    
    }

}
