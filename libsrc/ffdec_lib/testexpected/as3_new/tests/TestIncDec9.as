package tests
{
   public class TestIncDec9
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
      
      private var attrib:int = 0;
      
      public function TestIncDec9()
      {
         method
            name "tests:TestIncDec9/TestIncDec9"
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
               name "tests:TestIncDec9/run"
               returns null
               
               body
                  maxstack 4
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")])
                     pushstring "++attrib with result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")])
                     getlocal0
                     dup
                     setlocal1
                     getproperty QName(PrivateNamespace("tests:TestIncDec9"),"attrib")
                     increment_i
                     dup
                     setlocal2
                     getlocal1
                     getlocal2
                     setproperty QName(PrivateNamespace("tests:TestIncDec9"),"attrib")
                     kill 2
                     kill 1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")])
                     pushstring "--attrib with result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")])
                     getlocal0
                     dup
                     setlocal1
                     getproperty QName(PrivateNamespace("tests:TestIncDec9"),"attrib")
                     decrement_i
                     dup
                     setlocal2
                     getlocal1
                     getlocal2
                     setproperty QName(PrivateNamespace("tests:TestIncDec9"),"attrib")
                     kill 2
                     kill 1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")])
                     pushstring "++attrib no result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")]), 1
                     getlocal0
                     dup
                     setlocal1
                     getproperty QName(PrivateNamespace("tests:TestIncDec9"),"attrib")
                     increment_i
                     setlocal2
                     getlocal1
                     getlocal2
                     setproperty QName(PrivateNamespace("tests:TestIncDec9"),"attrib")
                     kill 2
                     kill 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")])
                     pushstring "--attrib no result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec9"),ProtectedNamespace("tests:TestIncDec9"),StaticProtectedNs("tests:TestIncDec9"),PrivateNamespace("TestIncDec9.as$0")]), 1
                     getlocal0
                     dup
                     setlocal1
                     getproperty QName(PrivateNamespace("tests:TestIncDec9"),"attrib")
                     decrement_i
                     setlocal2
                     getlocal1
                     getlocal2
                     setproperty QName(PrivateNamespace("tests:TestIncDec9"),"attrib")
                     kill 2
                     kill 1
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
            findpropstrict Multiname("TestIncDec9",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestIncDec9")
            returnvoid
         end ; code
      end ; body
   end ; method
   
