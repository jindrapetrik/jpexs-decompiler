package tests;

class TestHaxeCatchAll {
    public var out:String = "";

    public function new() {
    }

    public function run(s:String):Void {
        try {
            out += s;
        } catch (e:Dynamic) {
            out += "err " + e;
        }
        out += "end";
    }
}
