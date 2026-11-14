/*
 * Copyright (C) 2026 JPEXS. Distributed under the GNU LGPL v3 or later,
 * with no warranty, as described in the FFDec library license.
 *
 * Authoring-player callbacks for the generated component preview SWF.
 * This is FFDec's implementation of Flash's live-preview interface.
 * Regenerate component_preview.bin from this xfl directory using Apache Flex:
 * mxmlc -source-path=preview -target-player=11.2 -swf-version=9 -debug=false
 *   -static-link-runtime-shared-libraries=true -output=component_preview.bin
 *   preview/fl/livepreview/LivePreviewParent.as
 */
package fl.livepreview {
    import flash.display.MovieClip;
    import flash.display.StageAlign;
    import flash.display.StageScaleMode;
    import flash.external.ExternalInterface;

    public class LivePreviewParent extends MovieClip {
        public var myInstance:Object;

        public function LivePreviewParent() {
            super();
            myInstance = getChildAt(0);
            if (stage) {
                stage.align = StageAlign.TOP_LEFT;
                stage.scaleMode = StageScaleMode.NO_SCALE;
            }
            if (ExternalInterface.available) {
                ExternalInterface.addCallback("onResize", onResize);
                ExternalInterface.addCallback("onUpdate", onUpdate);
            }
        }

        public function onResize(width:Number, height:Number):void {
            if ("setSize" in myInstance && myInstance.setSize is Function) {
                myInstance.setSize(width, height);
            } else {
                myInstance.width = width;
                myInstance.height = height;
            }
        }

        public function onUpdate(...values):void {
            try { myInstance.componentInspectorSetting = true; } catch (e:Error) { }
            for (var i:int = 0; i + 1 < values.length; i += 2) {
                try { myInstance[String(values[i])] = values[i + 1]; } catch (e:Error) { }
            }
            try { myInstance.componentInspectorSetting = false; } catch (e:Error) { }
            if ("drawNow" in myInstance && myInstance.drawNow is Function) myInstance.drawNow();
        }
    }
}
