package tests
{
   import flash.utils.getDefinitionByName;
   
   public class TestCallCall
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
      
      public function TestCallCall()
      {
         method
            name "tests:TestCallCall/TestCallCall"
            returns null
            
            body
               maxstack 1
               localcount 1
               initscopedepth 4
               maxscopedepth 5
               
               code
                  getlocal0
                  pushscope
                  getlocal0
                  constructsuper 0
                  returnvoid
               end ; code
            end ; body
         end ; method
      }
      
      public function run() : *
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestCallCall/run"
               returns null
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "o", 0, 15
                     debug 1, "o2", 1, 16
                     findpropstrict QName(PackageNamespace("flash.utils"),"getDefinitionByName")
                     pushstring "Object"
                     constructprop QName(PackageNamespace("flash.utils"),"getDefinitionByName"), 1
                     getglobalscope
                     call 0
                     coerce_a
                     setlocal1
                     findpropstrict QName(PackageNamespace("flash.utils"),"getDefinitionByName")
                     pushstring "Object"
                     callproperty QName(PackageNamespace("flash.utils"),"getDefinitionByName"), 1
                     construct 0
                     coerce_a
                     setlocal2
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
            findpropstrict Multiname("TestCallCall",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestCallCall")
            returnvoid
         end ; code
      end ; body
   end ; method
   
