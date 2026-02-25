package tests
{
   public class TestIncDec5
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
      
      public function TestIncDec5()
      {
         method
            name "tests:TestIncDec5/TestIncDec5"
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
               name "tests:TestIncDec5/run"
               returns null
               
               body
                  maxstack 4
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     findpropstrict QName(PrivateNamespace("TestIncDec5.as$0"),"TestClass1")
                     constructprop QName(PrivateNamespace("TestIncDec5.as$0"),"TestClass1"), 0
                     coerce_a
                     setlocal1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     pushstring "++a.attrib with result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     getlocal1
                     dup
                     setlocal2
                     getproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     increment
                     dup
                     setlocal3
                     getlocal2
                     getlocal3
                     setproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     kill 3
                     kill 2
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     pushstring "--a.attrib with result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     getlocal1
                     dup
                     setlocal2
                     getproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     decrement
                     dup
                     setlocal3
                     getlocal2
                     getlocal3
                     setproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     kill 3
                     kill 2
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     pushstring "++a.attrib no result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")]), 1
                     getlocal1
                     dup
                     setlocal2
                     getproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     increment
                     setlocal3
                     getlocal2
                     getlocal3
                     setproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     kill 3
                     kill 2
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     pushstring "--a.attrib no result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")]), 1
                     getlocal1
                     dup
                     setlocal2
                     getproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     decrement
                     setlocal3
                     getlocal2
                     getlocal3
                     setproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec5"),ProtectedNamespace("tests:TestIncDec5"),StaticProtectedNs("tests:TestIncDec5"),PrivateNamespace("TestIncDec5.as$0")])
                     kill 3
                     kill 2
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
      }
   }
   
   class TestClass1
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
      
      public var attrib:int = 5;
      
      public function TestClass1()
      {
         method
            name "TestIncDec5.as$0:TestClass1/TestClass1"
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
            findpropstrict Multiname("TestIncDec5",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestIncDec5")
            findpropstrict Multiname("TestClass1",[PrivateNamespace("TestIncDec5.as$0")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 1
            popscope
            initproperty QName(PrivateNamespace("TestIncDec5.as$0"),"TestClass1")
            returnvoid
         end ; code
      end ; body
   end ; method
   
