import tests.Shape;
import tests.TestHaxeDowncastCoerce;

class Main {
    public static function main() {
        new TestHaxeDowncastCoerce().run(new tests.Circle(), new tests.Circle());
    }
}
