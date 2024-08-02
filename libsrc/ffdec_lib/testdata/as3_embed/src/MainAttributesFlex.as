package
{
    import flash.display.Sprite;
    import flash.events.Event;
    import embed_attributes.TestEmbedFlex;
    
    /**
     * ...
     * @author JPEXS
     */
    public class MainAttributesFlex extends Sprite
    {                
        
        public function MainAttributesFlex()
        {
            if (stage) init();
            else addEventListener(Event.ADDED_TO_STAGE, init);
        }
        
        private function init(e:Event = null):void
        {
            removeEventListener(Event.ADDED_TO_STAGE, init);
            
            new TestEmbedFlex();
        }    
    
    }

}
