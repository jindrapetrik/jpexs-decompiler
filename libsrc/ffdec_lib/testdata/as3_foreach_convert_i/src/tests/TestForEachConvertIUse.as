package tests {
  public class TestForEachConvertIUse {
    public function run():int {
      var values:Vector.<Number> = new <Number>[1.9, 2.1, 3.7];
      var total:int = 0;
      for each (var n:* in values) {
        total += int(n);
      }
      return total;
    }
  }
}
