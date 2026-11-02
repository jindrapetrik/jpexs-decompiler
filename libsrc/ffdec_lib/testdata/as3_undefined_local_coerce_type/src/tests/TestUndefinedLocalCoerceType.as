package tests {
  public class TestUndefinedLocalCoerceType {
    private static var sChains:Vector.<Vector.<String>> = new Vector.<Vector.<String>>();

    public function run(name:String):int {
      var chain:Vector.<String>;
      var untyped;
      if (sChains.length > 0) {
        chain = sChains.pop();
        chain[0] = name;
      } else {
        chain = new <String>[name];
      }
      untyped = chain.length;
      return chain.length + untyped;
    }
  }
}
