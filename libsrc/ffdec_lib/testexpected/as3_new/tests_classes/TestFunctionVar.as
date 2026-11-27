package tests_classes
{
   public class TestFunctionVar
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
      
      private var scrollEngine:Function;
      
      public function TestFunctionVar()
      {
         method
            name "tests_classes:TestFunctionVar/TestFunctionVar"
            flag NEED_ACTIVATION
            returns null
            
            body
               maxstack 2
               localcount 2
               initscopedepth 5
               maxscopedepth 7
               
               code
                  getlocal0
                  pushscope
                  getlocal0
                  newfunction 4
                  setproperty QName(PrivateNamespace("tests_classes:TestFunctionVar"),"scrollEngine")
                  debug 1, "+$activation", 0, 0
                  newactivation
                  dup
                  setlocal1
                  pushscope
                  getlocal0
                  constructsuper 0
                  getlocal0
                  callpropvoid QName(PrivateNamespace("tests_classes:TestFunctionVar"),"scrollEngine"), 0
                  getlocal0
                  newfunction 1
                  setproperty QName(PrivateNamespace("tests_classes:TestFunctionVar"),"scrollEngine")
                  getlocal0
                  callpropvoid QName(PrivateNamespace("tests_classes:TestFunctionVar"),"scrollEngine"), 0
                  returnvoid
               end ; code
            end ; body
         end ; method
      }
   }
}

method
   name ""
   returns null
   
   body
      maxstack 2
      localcount 1
      initscopedepth 1
      maxscopedepth 3
      
      code
         getlocal0
         pushscope
         findpropstrict Multiname("TestFunctionVar",[PackageNamespace("tests_classes")])
         getlex QName(PackageNamespace(""),"Object")
         pushscope
         getlex QName(PackageNamespace(""),"Object")
         newclass 0
         popscope
         initproperty QName(PackageNamespace("tests_classes"),"TestFunctionVar")
         returnvoid
      end ; code
   end ; body
end ; method

