package tests_classes
{
   public const initializedconst:Object;
}

method
   name ""
   returns null
   
   body
      maxstack 7
      localcount 1
      initscopedepth 1
      maxscopedepth 2
      
      code
         getlocal0
         pushscope
         findproperty QName(PackageNamespace("tests_classes"),"initializedconst")
         pushstring "a"
         pushbyte 1
         pushstring "b"
         pushbyte 2
         pushstring "c"
         pushbyte 3
         newobject 3
         initproperty QName(PackageNamespace("tests_classes"),"initializedconst")
         returnvoid
      end ; code
   end ; body
end ; method

