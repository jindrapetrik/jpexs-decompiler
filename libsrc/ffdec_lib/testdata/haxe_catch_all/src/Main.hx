import tests.TestHaxeCatchAll;
import tests.TestHaxeCatchAllInLoop;

class Main {
    public static function main() {
        new TestHaxeCatchAll().run("a");
        new TestHaxeCatchAllInLoop().run(["a"]);
    }
}
