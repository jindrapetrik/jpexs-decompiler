package tests;

class TestHaxeCastMidExpression {
    public function new() {
    }

    static function esc(s:String):String {
        return s;
    }

    public function run(obj:Dynamic):String {
        if (Std.isOfType(obj, String)) {
            return "\"" + esc(cast(obj, String)) + "\"";
        }
        return "?";
    }
}
