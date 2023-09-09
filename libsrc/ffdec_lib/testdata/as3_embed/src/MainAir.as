package
{
    import flash.display.Sprite;
    import flash.events.Event;
    import tests_classes.TestEmbedAir;
    
    /**
     * ...
     * @author JPEXS
     */
    public class MainAir extends Sprite
    {        
        
        TestEmbedAir;
        
        public function MainAir()
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