package
{
    import flash.display.Sprite;
    import flash.events.Event;
    import embed_classes.*;
    
    /**
     * ...
     * @author JPEXS
     */
    public class MainClassesAir extends Sprite
    {                
        TestBinaryData;
        TestFont;
        TestFontCFF;
        TestImage;
        TestImageGrid;
        //TestImageSvg;
        TestSameImage;
        TestSound;
        TestSwfSymbol;
        
        public function MainClassesAir()
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
