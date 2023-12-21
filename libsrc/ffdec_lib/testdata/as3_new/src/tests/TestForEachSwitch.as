package tests {

    public class TestForEachSwitch {
        public function run() : *{
            var a:Boolean = true;
            var b:Boolean = true;
            var c:Boolean = true;
            var s:int = 5;
            var obj:Object = {};

            for each(var name:String in obj) {
                if (a) {
                    switch (s) {
                    case 1:
                        trace("1");
                        if (b) {
                            trace("1b");
                        }
                    case 2:
                        trace("2");
                        if (c) {
                            trace("2c");
                        }
                        break;
                    case 3:
                        trace("3");
                        break;
                    case 4:
                        trace("4");
                        break;
                    default:
                        break;
                    }
                }
                trace("before_continue");
            }
        }                
    }
}
