package tests_classes
{
   public const myconst:int = 29;
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
         findproperty QName(PackageNamespace("tests_classes"),"myconst")
         pushbyte 29
         initproperty QName(PackageNamespace("tests_classes"),"myconst")
         returnvoid
      end ; code
   end ; body
end ; method

