package app {
    public class ApplicationBase extends ApplicationAncestor {
        [Bindable]
        public var applicationCaption:String = "Base application";

        public function formatCaption():String {
            return applicationCaption + " - " + ancestorCaption;
        }

        public function recordClick():void {
            applicationCaption = "The inherited button was clicked";
        }
    }
}
