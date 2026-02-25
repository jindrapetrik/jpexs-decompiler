package tests_classes
{
   import tests_classes.myjson.JSON;
   
   public class TestImports2
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
      
      public function TestImports2()
      {
         method
            name "tests_classes:TestImports2/TestImports2"
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
               name "tests_classes:TestImports2/run"
               returns null
               
               body
                  maxstack 1
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "j", 0, 15
                     findpropstrict QName(PackageNamespace("tests_classes.myjson"),"JSON")
                     constructprop QName(PackageNamespace("tests_classes.myjson"),"JSON"), 0
                     coerce QName(PackageNamespace("tests_classes.myjson"),"JSON")
                     setlocal1
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
            findpropstrict Multiname("TestImports2",[PackageNamespace("tests_classes")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests_classes"),"TestImports2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
