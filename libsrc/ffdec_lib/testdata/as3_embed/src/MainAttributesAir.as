package
{
    import flash.display.Sprite;
    import flash.events.Event;
    import embed_attributes.TestEmbedAir;
    
    /**
     * ...
     * @author JPEXS
     */
    public class MainAttributesAir extends Sprite
    {        
        
        public function MainAttributesAir()
        {
            if (stage) init();
            else addEventListener(Event.ADDED_TO_STAGE, init);
        }
        
        private function init(e:Event = null):void
        {
            removeEventListener(Event.ADDED_TO_STAGE, init);
        
            new TestEmbedAir();
        }    
    
    }

}
