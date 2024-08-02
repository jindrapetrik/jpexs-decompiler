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
        public function MainClassesAir()
        {
            if (stage) init();
            else addEventListener(Event.ADDED_TO_STAGE, init);
        }
        
        private function init(e:Event = null):void
        {
            removeEventListener(Event.ADDED_TO_STAGE, init);
            
            new TestBinaryData();
            new TestFont();
            new TestFontCFF();
            new TestImage();
            new TestImageGrid();
            //new TestImageSvg();
            new TestSameImage();
            new TestSound();
            new TestSwfSymbol();
        }    
    
    }

}
