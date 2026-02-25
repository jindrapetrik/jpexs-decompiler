package tests
{
   import tests_classes.myvar;
   
   public class TestImportedVar
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
      
      public function TestImportedVar()
      {
         method
            name "tests:TestImportedVar/TestImportedVar"
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
               name "tests:TestImportedVar/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImportedVar"),ProtectedNamespace("tests:TestImportedVar"),StaticProtectedNs("tests:TestImportedVar"),PrivateNamespace("TestImportedVar.as$0")])
                     findproperty QName(PackageNamespace("tests_classes"),"myvar")
                     getproperty QName(PackageNamespace("tests_classes"),"myvar")
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImportedVar"),ProtectedNamespace("tests:TestImportedVar"),StaticProtectedNs("tests:TestImportedVar"),PrivateNamespace("TestImportedVar.as$0")]), 1
                     findproperty QName(PackageNamespace("tests_classes"),"myvar")
                     pushbyte 5
                     setproperty QName(PackageNamespace("tests_classes"),"myvar")
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
            findpropstrict Multiname("TestImportedVar",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestImportedVar")
            returnvoid
         end ; code
      end ; body
   end ; method
   
