package 
{
    import flash.events.Event;
    import flash.utils.ByteArray;
    import flash.display.Loader;
    import flash.display.Sprite;
    import flash.display.StageAlign;
    import flash.display.StageScaleMode;

    public class Main extends Sprite 
    {
        private var swfLoader: Loader;
        
        public function Main():void 
        {
            if (stage) {
                init();
            } else {
                addEventListener(Event.ADDED_TO_STAGE, init);
            }
        }

        private function init(e:Event = null):void 
        {
            removeEventListener(Event.ADDED_TO_STAGE, init);
            
            var encrypted:ByteArray = new EncryptedByteArray();
            var decrypted:ByteArray = decrypt(encrypted);
            
            this.swfLoader = new Loader();
            this.swfLoader.contentLoaderInfo.addEventListener(Event.COMPLETE, onSWFLoaded);
            this.swfLoader.loadBytes(decrypted);
        }
        
        private function decrypt(encrypted: ByteArray): ByteArray
        {
            var decrypted:ByteArray = new ByteArray();
            
            for (var i:int = 0; i < encrypted.length; i++) {
                decrypted[i] = encrypted[i] ^ 65;
            }
            return decrypted;
        }
    
        private function onSWFLoaded(event:Event):void {
            var loadedSWF:Sprite = this.swfLoader.content as Sprite;

            addChild(loadedSWF);

            stage.scaleMode = StageScaleMode.NO_SCALE;
            stage.align = StageAlign.TOP_LEFT;

            // loadedSWF.x = ...
            // loadedSWF.y = ...
            // loadedSWF.width = ...
            // loadedSWF.height = ...
        }
    }
}
