package tests;

class TestHaxeDowncastCoerce {
    public var out:Float = 0;

    public function new() {
    }

    function radius(c:Circle):Float {
        return c.radius;
    }

    public function run(s:Shape, i:IShape):Void {
        var c:Circle = cast s;
        out = c.radius + radius(cast i);
        var base:Shape = c;
        out += radius(cast base);
    }
}
