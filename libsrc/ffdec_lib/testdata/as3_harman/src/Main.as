package
{
    import flash.display.Sprite;
    import flash.events.Event;  
    import flash.utils.ByteArray;
    import flash.system.System;
    import flash.display.Loader;
    import flash.display.StageAlign;
    import flash.display.StageScaleMode; 
    import flash.system.LoaderContext;   
    import flash.system.ApplicationDomain;
    
    public class Main extends Sprite
    {               
    
        Operators;     
    
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
            
            var decrypted:ByteArray = System.decryptBlob(EncryptedCustomKeyByteArray, "secret_key");
            
            this.swfLoader = new Loader();
            var lc:LoaderContext = new LoaderContext(false, ApplicationDomain.currentDomain);
            lc.allowCodeImport = true;
            
            this.swfLoader.contentLoaderInfo.addEventListener(Event.COMPLETE, onSWFLoaded);
            this.swfLoader.loadBytes(decrypted, lc);
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

//Include also this class
EncryptedByteArray;
