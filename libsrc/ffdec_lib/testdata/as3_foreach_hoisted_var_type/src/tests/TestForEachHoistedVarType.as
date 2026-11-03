package tests {
  public class TestForEachHoistedVarType {
    public function run(list:XMLList):String {
      var node:XML;
      for each (node in list) {
        return node.@value;
      }
      return null;
    }
  }
}
