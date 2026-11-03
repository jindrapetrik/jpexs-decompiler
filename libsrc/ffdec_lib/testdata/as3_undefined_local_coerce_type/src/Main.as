package {
  import flash.display.Sprite;
  import tests.TestNumericDefaultLocalCoerceType;
  import tests.TestUndefinedLocalCoerceType;

  public class Main extends Sprite {
    public function Main() {
      new TestNumericDefaultLocalCoerceType();
      new TestUndefinedLocalCoerceType();
    }
  }
}
