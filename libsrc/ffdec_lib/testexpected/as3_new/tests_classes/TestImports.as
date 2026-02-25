package tests_classes
{
   import tests_classes.myjson.JSON;
   import tests_classes.myjson2.JSON;
   
   public class TestImports
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
      
      public function TestImports()
      {
         method
            name "tests_classes:TestImports/TestImports"
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
               name "tests_classes:TestImports/run"
               returns null
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "j1", 0, 16
                     debug 1, "j2", 1, 17
                     findpropstrict QName(PackageNamespace("tests_classes.myjson"),"JSON")
                     constructprop QName(PackageNamespace("tests_classes.myjson"),"JSON"), 0
                     coerce QName(PackageNamespace("tests_classes.myjson"),"JSON")
                     setlocal1
                     findpropstrict QName(PackageNamespace("tests_classes.myjson2"),"JSON")
                     constructprop QName(PackageNamespace("tests_classes.myjson2"),"JSON"), 0
                     coerce QName(PackageNamespace("tests_classes.myjson2"),"JSON")
                     setlocal2
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestImports"),ProtectedNamespace("tests_classes:TestImports"),StaticProtectedNs("tests_classes:TestImports"),PrivateNamespace("TestImports.as$0")])
                     getlocal1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestImports"),ProtectedNamespace("tests_classes:TestImports"),StaticProtectedNs("tests_classes:TestImports"),PrivateNamespace("TestImports.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestImports"),ProtectedNamespace("tests_classes:TestImports"),StaticProtectedNs("tests_classes:TestImports"),PrivateNamespace("TestImports.as$0")])
                     getlocal2
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestImports"),ProtectedNamespace("tests_classes:TestImports"),StaticProtectedNs("tests_classes:TestImports"),PrivateNamespace("TestImports.as$0")]), 1
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
            findpropstrict Multiname("TestImports",[PackageNamespace("tests_classes")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests_classes"),"TestImports")
            returnvoid
         end ; code
      end ; body
   end ; method
   
