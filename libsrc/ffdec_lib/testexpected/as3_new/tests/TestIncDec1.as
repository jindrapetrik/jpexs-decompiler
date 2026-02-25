package tests
{
   public class TestIncDec1
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
      
      public function TestIncDec1()
      {
         method
            name "tests:TestIncDec1/TestIncDec1"
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
               name "tests:TestIncDec1/run"
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
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")])
                     pushstring "++a with result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")])
                     getlocal1
                     increment
                     dup
                     coerce_a
                     setlocal1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")])
                     pushstring "--a with result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")])
                     getlocal1
                     decrement
                     dup
                     coerce_a
                     setlocal1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")])
                     pushstring "++a no result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")]), 1
                     inclocal 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")])
                     pushstring "--a no result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec1"),ProtectedNamespace("tests:TestIncDec1"),StaticProtectedNs("tests:TestIncDec1"),PrivateNamespace("TestIncDec1.as$0")]), 1
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
            findpropstrict Multiname("TestIncDec1",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestIncDec1")
            returnvoid
         end ; code
      end ; body
   end ; method
   
