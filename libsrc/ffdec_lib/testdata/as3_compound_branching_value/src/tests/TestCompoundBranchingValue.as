package tests {
  public class TestCompoundBranchingValue {
    private var _s:String = "";
    private static var sCount:int;
    public var n:int;

    public function run(i:int, f:Boolean):String {
      _s += (i == 0 ? "" : ", ") + "x";
      sCount += f ? 1 : 2;
      n += i > 2 && i < 10 ? i : -i;
      return _s;
    }
  }
}
