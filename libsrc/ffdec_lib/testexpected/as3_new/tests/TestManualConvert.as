package tests
{
   public class TestManualConvert
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
      
      public function TestManualConvert()
      {
         method
            name "tests:TestManualConvert/TestManualConvert"
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
               name "tests:TestManualConvert/run"
               returns null
               
               body
                  maxstack 3
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestManualConvert"),ProtectedNamespace("tests:TestManualConvert"),StaticProtectedNs("tests:TestManualConvert"),PrivateNamespace("TestManualConvert.as$0")])
                     pushstring "String(this).length"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestManualConvert"),ProtectedNamespace("tests:TestManualConvert"),StaticProtectedNs("tests:TestManualConvert"),PrivateNamespace("TestManualConvert.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestManualConvert"),ProtectedNamespace("tests:TestManualConvert"),StaticProtectedNs("tests:TestManualConvert"),PrivateNamespace("TestManualConvert.as$0")])
                     findpropstrict QName(PackageNamespace(""),"String")
                     getlocal0
                     callproperty QName(PackageNamespace(""),"String"), 1
                     getproperty Multiname("length",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestManualConvert"),ProtectedNamespace("tests:TestManualConvert"),StaticProtectedNs("tests:TestManualConvert"),PrivateNamespace("TestManualConvert.as$0")])
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestManualConvert"),ProtectedNamespace("tests:TestManualConvert"),StaticProtectedNs("tests:TestManualConvert"),PrivateNamespace("TestManualConvert.as$0")]), 1
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
            findpropstrict Multiname("TestManualConvert",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestManualConvert")
            returnvoid
         end ; code
      end ; body
   end ; method
   
