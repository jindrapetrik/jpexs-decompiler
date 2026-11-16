package tests;

class TestCompoundInitProperty {
    public var p:Point;

    public function new() {
        p = new Point();
    }

    public function run():Void {
        p.x += 1.5;
        p.y -= p.x;
        p.x *= p.y;
    }
}
