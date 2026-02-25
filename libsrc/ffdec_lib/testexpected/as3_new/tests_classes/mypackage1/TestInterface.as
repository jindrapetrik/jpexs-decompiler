package tests_classes.mypackage1
{
   import tests_classes.mypackage2.TestInterface;
   
   public interface TestInterface extends tests_classes.mypackage2.TestInterface
   {
      
      method
         name ""
         returns null
         
         body
            maxstack 1
            localcount 1
            initscopedepth 3
            maxscopedepth 4
            
            code
               getlocal0
               pushscope
               returnvoid
            end ; code
         end ; body
      end ; method
      
      function testMethod1() : void;
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
         findpropstrict Multiname("TestInterface",[PackageNamespace("tests_classes.mypackage1")])
         pushnull
         newclass 0
         initproperty QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
         returnvoid
      end ; code
   end ; body
end ; method

