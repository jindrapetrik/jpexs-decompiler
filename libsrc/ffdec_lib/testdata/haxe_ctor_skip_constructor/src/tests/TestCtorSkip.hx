package tests;

class TestCtorSkip extends TestCtorBase {
    public var b:Int;

    public function new(a:Int) {
        super(a);
        b = a * 2;
    }
}
