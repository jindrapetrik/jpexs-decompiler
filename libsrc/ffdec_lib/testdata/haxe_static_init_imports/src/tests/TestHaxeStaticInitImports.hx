package tests;

import other.Consts;

class TestHaxeStaticInitImports {
    public static var p:Pair = new Pair(Consts.B);
    public static var q:Int = Consts.B + 1;
}
