package tests_classes {
    public class TestFunctionVar {
        private var scrollEngine:Function = function():void {};
        public function TestFunctionVar() {
            super();
            this.scrollEngine();
            this.scrollEngine = function():void { trace("Hello, World!"); };
            this.scrollEngine();
        }
    }
}