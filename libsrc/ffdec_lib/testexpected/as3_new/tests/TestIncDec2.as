package tests
{
   public class TestIncDec2
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
      
      public function TestIncDec2()
      {
         method
            name "tests:TestIncDec2/TestIncDec2"
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
               name "tests:TestIncDec2/run"
               returns null
               
               body
                  maxstack 3
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     pushbyte 5
                     coerce_a
                     setlocal1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")])
                     pushstring "a++ with result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")])
                     getlocal1
                     dup
                     increment
                     coerce_a
                     setlocal1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")])
                     pushstring "a-- with result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")])
                     getlocal1
                     dup
                     decrement
                     coerce_a
                     setlocal1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")])
                     pushstring "a++ no result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")]), 1
                     inclocal 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")])
                     pushstring "a-- no result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec2"),ProtectedNamespace("tests:TestIncDec2"),StaticProtectedNs("tests:TestIncDec2"),PrivateNamespace("TestIncDec2.as$0")]), 1
                     declocal 1
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
            findpropstrict Multiname("TestIncDec2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestIncDec2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
