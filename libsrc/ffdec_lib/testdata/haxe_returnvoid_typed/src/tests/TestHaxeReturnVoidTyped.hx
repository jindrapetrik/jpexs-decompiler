package tests;

class TestHaxeReturnVoidTyped {
    public var kind:Kind = Square;

    public function new() {
    }

    public function run(x:Float):Float {
        switch (kind) {
            case Circle:
                return x * 3;
            case Square:
                return x * x;
            case Line:
                return x;
        }
    }
}
