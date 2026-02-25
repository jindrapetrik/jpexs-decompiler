package tests_classes
{
   public var myvar:int = 10;
}

method
   name ""
   returns null
   
   body
      maxstack 2
      localcount 1
      initscopedepth 1
      maxscopedepth 2
      
      code
         getlocal0
         pushscope
         findproperty QName(PackageNamespace("tests_classes"),"myvar")
         pushbyte 10
         setproperty QName(PackageNamespace("tests_classes"),"myvar")
         returnvoid
      end ; code
   end ; body
end ; method

