package tests;

class TestHaxeCatchAllInLoop {
    public var out:String = "";

    public function new() {
    }

    public function run(a:Array<String>):Void {
        for (s in a) {
            if (s != "x") {
                continue;
            }
            try {
                out += s;
                return;
            } catch (e:Dynamic) {
                out += "err";
            }
        }
    }
}
