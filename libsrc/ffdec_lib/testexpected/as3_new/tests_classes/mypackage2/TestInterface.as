package tests_classes.mypackage2
{
   public interface TestInterface
   {
      
      method
         name ""
         returns null
         
         body
            maxstack 1
            localcount 1
            initscopedepth 2
            maxscopedepth 3
            
            code
               getlocal0
               pushscope
               returnvoid
            end ; code
         end ; body
      end ; method
      
      function testMethod2() : void;
   }
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
         findpropstrict Multiname("TestInterface",[PackageNamespace("tests_classes.mypackage2")])
         pushnull
         newclass 0
         initproperty QName(PackageNamespace("tests_classes.mypackage2"),"TestInterface")
         returnvoid
      end ; code
   end ; body
end ; method

