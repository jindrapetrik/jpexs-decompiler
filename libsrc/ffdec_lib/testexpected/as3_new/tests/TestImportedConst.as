package tests
{
   import tests_classes.myconst;
   
   public class TestImportedConst
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
      
      public function TestImportedConst()
      {
         method
            name "tests:TestImportedConst/TestImportedConst"
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
      
      public function run() : void
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestImportedConst/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImportedConst"),ProtectedNamespace("tests:TestImportedConst"),StaticProtectedNs("tests:TestImportedConst"),PrivateNamespace("TestImportedConst.as$0")])
                     findproperty QName(PackageNamespace("tests_classes"),"myconst")
                     getproperty QName(PackageNamespace("tests_classes"),"myconst")
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImportedConst"),ProtectedNamespace("tests:TestImportedConst"),StaticProtectedNs("tests:TestImportedConst"),PrivateNamespace("TestImportedConst.as$0")]), 1
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
            findpropstrict Multiname("TestImportedConst",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestImportedConst")
            returnvoid
         end ; code
      end ; body
   end ; method
   
