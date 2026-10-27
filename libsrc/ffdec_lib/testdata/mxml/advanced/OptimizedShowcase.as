package {
    import flash.events.MouseEvent;
    import flash.geom.Point;
    import mx.core.mx_internal;
    import mx.collections.ArrayCollection;
    import mx.states.State;
    import mx.states.AddItems;
    import spark.components.Application;
    import spark.components.VGroup;
    import spark.components.Label;
    import spark.components.Button;
    import spark.components.List;
    import spark.layouts.VerticalLayout;
    import spark.effects.Fade;

    use namespace mx_internal;

    /** A reproducible fixture for the Flex SDK's compact descriptor format. */
    public class OptimizedShowcase extends Application {
        public var container:VGroup;
        public var optimizedLabel:Label;
        public var optimizedButton:Button;
        public var optimizedList:List;
        public var points:Vector.<Point>;

        [Bindable]
        public var message:String = "Optimized descriptor content";

        public function OptimizedShowcase() {
            super();
            generateMXMLAttributes([
                3, "width", true, 640, "height", true, 480,
                "points", undefined, [null, function(values:Array):Vector.<Point> { return Vector.<Point>(values); },
                    Point, 2, "x", true, 2, "y", true, 4, 0, 0, 0, null],
                1, "backgroundColor", true, 0xEAF4FF,
                0, 0]);
            setMXMLDescriptor([
                VGroup, 2, "id", true, "container",
                "layout", false, [VerticalLayout, 2, "gap", true, 12, "paddingLeft", true, 20, 0, 0, 0, null],
                0, 0, 0, [
                    Label, 2, "id", true, "optimizedLabel", "text", true, "Initial optimized label",
                    1, "color", true, 0x224466,
                    1, "showEffect", false, [Fade, 1, "duration", true, 200, 0, 0, 0, null],
                    0, null,
                    Button, 2, "id", true, "optimizedButton", "label", true, "Update optimized message",
                    0, 0, 1, "click", handleClick, null,
                    List, 2, "id", true, "optimizedList", "dataProvider", false, [
                        ArrayCollection, 1, "source", null, [
                            Object, 1, "label", true, "First optimized item", 0, 0, 0, null,
                            Object, 1, "label", true, "Second optimized item", 0, 0, 0, null],
                        0, 0, 0, null],
                    0, 0, 0, null]]);
            states = [new State({name: "normal", overrides: []}), new State({name: "expanded", overrides: [
                new AddItems().initializeFromObject({destination: "container", propertyName: "mxmlContent", position: "after",
                    relativeTo: ["optimizedButton"], itemsDescriptor: [
                        Label, 2, "id", true, "stateLabel", "text", true, "Visible in the expanded state", 0, 0, 0, null,
                        Label, 1, "text", true, "A second state item", 0, 0, 0, null]})]})];
            currentState = "normal";
            mx_internal::setupBindings([1, function():String { return message; },
                function(value:String):void { optimizedLabel.text = value; }, "optimizedLabel.text",
                0, 2, "message", "propertyChange", 0, null, null]);
        }

        private function handleClick(event:MouseEvent):void {
            message = "The optimized button was clicked";
        }
    }
}
