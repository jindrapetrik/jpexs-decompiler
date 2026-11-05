package tests;

class TestHaxeShadowedField {
    public var key:String;
    public var item:Int;

    public function new(key:String, item:Int) {
        this.key = key;
        this.item = item;
    }

    public function run(key:String):String {
        var old = this.key;
        this.key = key;
        return old + key;
    }
}
