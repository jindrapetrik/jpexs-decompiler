package tests_classes.mypackage1
{
    import tests_classes.mypackage2.*;
    import flash.utils.Dictionary;
    	
    public class TestClass3
    {
        private var c:tests_classes.mypackage1.TestClass;
        
        public function run() : void {
            var a:Dictionary = new Dictionary();
            a["test"] = 5;
            trace(a["test"]);          
        }
    }
}