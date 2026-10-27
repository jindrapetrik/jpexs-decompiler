package app {
    import spark.modules.Module;

    public class FeatureModuleBase extends Module {
        [Bindable]
        public var caption:String = "Custom module base";
    }
}
