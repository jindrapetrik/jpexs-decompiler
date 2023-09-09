package
{
    import flash.display.Sprite;
    import flash.events.Event;
    import tests_classes.TestEmbedFlex;
    
    /**
     * ...
     * @author JPEXS
     */
    public class MainFlex extends Sprite
    {        
        
        TestEmbedFlex;
        
        public function MainFlex()
        {
            if (stage) init();
            else addEventListener(Event.ADDED_TO_STAGE, init);
        }
        
        private function init(e:Event = null):void
        {
            removeEventListener(Event.ADDED_TO_STAGE, init);
            // entry point
        }    
    
    }

}