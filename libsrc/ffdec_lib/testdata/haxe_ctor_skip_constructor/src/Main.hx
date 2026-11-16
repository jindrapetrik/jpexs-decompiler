import tests.TestCtorSkip;

class Main {
    public static function main() {
        new TestCtorSkip(1);
        // makes Haxe start every constructor with if (flash.Boot.skip_constructor) return;
        Type.createEmptyInstance(TestCtorSkip);
    }
}
