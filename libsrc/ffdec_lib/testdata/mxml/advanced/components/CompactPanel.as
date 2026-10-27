package components {
    import spark.components.Group;
    import spark.components.Label;

    public class CompactPanel extends Group {
        private var compactDescriptor:Array;
        private var attributes:Array;
        public var compactLabel:Label;

        public function CompactPanel() {
            super();
            generateMXMLAttributes(MXMLProperties);
        }

        override public function get MXMLProperties():Array {
            if (attributes == null) {
                attributes = [2, "width", true, 280, "height", true, 80, 0, 0, 0];
            }
            return attributes;
        }

        override public function get MXMLDescriptor():Array {
            if (compactDescriptor == null) {
                compactDescriptor = [Label, 2, "id", true, "compactLabel", "text", true,
                    "A nested component with compact getters", 0, 0, 0, null];
            }
            return compactDescriptor;
        }
    }
}
