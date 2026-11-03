package tests {
  public class TestNumericDefaultLocalCoerceType {
    public function run(mask:String):uint {
      var result:uint = 0;
      var cv:uint;
      for (var k:int = 1; k < mask.length; k++) {
        cv = mask.charCodeAt(k) - "x".charCodeAt(0);
        if (cv > 2) {
          cv = 3;
        }
        result |= 1 << cv;
      }
      return result;
    }
  }
}
