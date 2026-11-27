package com.jpexs.decompiler.flash.debugger {

    import flash.display.Loader;
    import flash.display.LoaderInfo;
    import flash.errors.IOError;
    import flash.events.Event;
    import flash.events.EventDispatcher;
    import flash.events.HTTPStatusEvent;
    import flash.events.IOErrorEvent;
    import flash.events.ProgressEvent;
    import flash.events.SecurityErrorEvent;
    import flash.net.URLLoader;
    import flash.net.URLLoaderDataFormat;
    import flash.net.URLRequest;
    import flash.system.LoaderContext;
    import flash.utils.ByteArray;
    import flash.utils.Dictionary;

    public class DebugLoader extends Loader {

        private static const forwardedEvents:Array = [
            Event.OPEN, ProgressEvent.PROGRESS, HTTPStatusEvent.HTTP_STATUS,
            "httpResponseStatus", IOErrorEvent.IO_ERROR, SecurityErrorEvent.SECURITY_ERROR
        ];
        private static const owners:Dictionary = new Dictionary(true);

        private var urlLoader:URLLoader;
        private var loadedContext:LoaderContext;
        private var loadedRequest:URLRequest;
        private var events:EventDispatcher;
        private var generation:uint = 0;
        private var waitingForModifiedData:Boolean = false;
        private var forwardNetworkEvents:Boolean = false;

        public function DebugLoader() {
            super();
            events = new EventDispatcher(contentLoaderInfo);
            // Both keys must be weak: a strong owner would keep its LoaderInfo alive.
            var owner:Dictionary = new Dictionary(true);
            owner[this] = true;
            owners[contentLoaderInfo] = owner;
            contentLoaderInfo.addEventListener(Event.OPEN, suppressNetworkEvent, false, int.MAX_VALUE);
            contentLoaderInfo.addEventListener(ProgressEvent.PROGRESS, suppressNetworkEvent, false, int.MAX_VALUE);
            contentLoaderInfo.addEventListener(HTTPStatusEvent.HTTP_STATUS, suppressNetworkEvent, false, int.MAX_VALUE);
            contentLoaderInfo.addEventListener("httpResponseStatus", suppressNetworkEvent, false, int.MAX_VALUE);
        }

        // Called by instrumented listener calls and method references. Dispatching
        // directly on LoaderInfo is forbidden by Flash Player (error #2118).
        public static function bridgeEventListener(args:Array, receiver:*, method:*):* {
            if (receiver is LoaderInfo) {
                var owner:Dictionary = owners[receiver];
                if (owner != null) {
                    for (var key:Object in owner) {
                        var loader:DebugLoader = key as DebugLoader;
                        if (method === receiver.addEventListener) {
                            method = loader.addContentEventListener;
                        } else if (method === receiver.removeEventListener) {
                            method = loader.removeContentEventListener;
                        }
                        break;
                    }
                }
            }
            return args == null ? method : method.apply(receiver, args);
        }

        private function addContentEventListener(...args):void {
            contentLoaderInfo.addEventListener.apply(contentLoaderInfo, args);
            events.addEventListener.apply(events, args);
        }

        private function removeContentEventListener(...args):void {
            contentLoaderInfo.removeEventListener.apply(contentLoaderInfo, args);
            events.removeEventListener.apply(events, args);
        }

        private function suppressNetworkEvent(event:Event):void {
            // loadBytes may report progress again after the download. Do not
            // deliver a second OPEN or reset the application's download progress.
            if (forwardNetworkEvents) {
                event.stopImmediatePropagation();
            }
        }

        public override function load(request:URLRequest, context:LoaderContext = null):void {
            cancelPendingLoad();
            loadedRequest = request;
            loadedContext = context;
            forwardNetworkEvents = true;
            var loader:URLLoader = new URLLoader();
            urlLoader = loader;
            loader.dataFormat = URLLoaderDataFormat.BINARY;
            loader.addEventListener(Event.COMPLETE, onURLLoaderComplete);
            for each (var type:String in forwardedEvents) {
                loader.addEventListener(type, onURLLoaderEvent);
            }
            try {
                loader.load(request);
            } catch (error:Error) {
                if (urlLoader === loader) {
                    detachURLLoader();
                    if (error is SecurityError || error is IOError) {
                        loadOriginalRequest();
                        return;
                    }
                }
                throw error;
            }
        }

        private function detachURLLoader():URLLoader {
            var loader:URLLoader = urlLoader;
            urlLoader = null;
            if (loader != null) {
                loader.removeEventListener(Event.COMPLETE, onURLLoaderComplete);
                for each (var type:String in forwardedEvents) {
                    loader.removeEventListener(type, onURLLoaderEvent);
                }
            }
            return loader;
        }

        private function cancelPendingLoad():void {
            generation++;
            waitingForModifiedData = false;
            var loader:URLLoader = detachURLLoader();
            if (loader != null) {
                loader.close();
            }
        }

        private function onURLLoaderEvent(event:Event):void {
            if (event.currentTarget !== urlLoader) {
                return;
            }
            if (event.type == IOErrorEvent.IO_ERROR || event.type == SecurityErrorEvent.SECURITY_ERROR) {
                // URLLoader may require a cross-domain policy where Loader does
                // not. Let the native Loader determine the final result, and
                // deliver its events instead of the intermediate failure.
                detachURLLoader();
                loadOriginalRequest();
                return;
            }
            events.dispatchEvent(event.clone());
        }

        private function loadOriginalRequest():void {
            forwardNetworkEvents = false;
            super.load(loadedRequest, loadedContext);
        }

        private function onURLLoaderComplete(event:Event):void {
            if (event.currentTarget !== urlLoader) {
                return;
            }
            var loader:URLLoader = detachURLLoader();
            loadBytesInternal(loader.data as ByteArray, loadedContext, loadedRequest.url);
        }

        private function loadBytesInternal(bytes:ByteArray, context:LoaderContext = null, url:String = ""):void {
            var modifiedBytes:ByteArray = new ByteArray();
            var currentGeneration:uint = generation;
            waitingForModifiedData = true;
            DebugConnection.modifyLoaderBytesWithUrl(bytes, modifiedBytes, url, function():void {
                if (currentGeneration == generation) {
                    waitingForModifiedData = false;
                    loadModifiedBytes(modifiedBytes, context);
                }
            });
        }

        private function loadModifiedBytes(bytes:ByteArray, context:LoaderContext):void {
            // INIT and COMPLETE belong to this load, not to URLLoader.COMPLETE.
            super.loadBytes(bytes, context);
        }

        public override function loadBytes(bytes:ByteArray, context:LoaderContext = null):void {
            cancelPendingLoad();
            forwardNetworkEvents = false;
            loadBytesInternal(bytes, context);
        }

        public override function close():void {
            if (urlLoader != null || waitingForModifiedData) {
                cancelPendingLoad();
            } else {
                super.close();
            }
        }

        public override function unload():void {
            cancelPendingLoad();
            forwardNetworkEvents = false;
            super.unload();
        }

        public override function unloadAndStop(gc:Boolean = true):void {
            cancelPendingLoad();
            forwardNetworkEvents = false;
            super.unloadAndStop(gc);
        }

        public override function toString():String {
            return "[object Loader]";
        }
    }
}
