package tests
{
   public class TestForEachTry
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
      
      public function TestForEachTry()
      {
         method
            name "tests:TestForEachTry/TestForEachTry"
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
               name "tests:TestForEachTry/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 5
                  initscopedepth 5
                  maxscopedepth 10
                  trait slot QName(PackageInternalNs("tests"),"name")
                     slotid 1
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"list")
                     slotid 2
                     type QName(PackageNamespace(""),"Object")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"b")
                     slotid 3
                     type QName(PackageNamespace(""),"Boolean")
                  end ; trait
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "+$activation", 0, 0
                     newactivation
                     dup
                     setlocal1
                     pushscope
                     getscopeobject 1
                     pushnull
                     coerce_s
                     setslot 1
                     getscopeobject 1
                     newobject 0
                     coerce QName(PackageNamespace(""),"Object")
                     setslot 2
                     getscopeobject 1
                     pushtrue
                     convert_b
                     setslot 3
                     pushbyte 0
                     setlocal2
                     getscopeobject 1
                     getslot 2
                     coerce_a
                     setlocal3
                     jump ofs0079
            ofs002c:
                     label
                     getscopeobject 1
                     getlocal3
                     getlocal2
                     nextvalue
                     coerce_s
                     setslot 1
            ofs0035:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachTry"),ProtectedNamespace("tests:TestForEachTry"),StaticProtectedNs("tests:TestForEachTry"),PrivateNamespace("TestForEachTry.as$0")])
                     pushstring "xx"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachTry"),ProtectedNamespace("tests:TestForEachTry"),StaticProtectedNs("tests:TestForEachTry"),PrivateNamespace("TestForEachTry.as$0")]), 1
                     getscopeobject 1
                     getslot 3
                     iffalse ofs004f
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachTry"),ProtectedNamespace("tests:TestForEachTry"),StaticProtectedNs("tests:TestForEachTry"),PrivateNamespace("TestForEachTry.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachTry"),ProtectedNamespace("tests:TestForEachTry"),StaticProtectedNs("tests:TestForEachTry"),PrivateNamespace("TestForEachTry.as$0")]), 1
                     jump ofs0056
            ofs004f:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachTry"),ProtectedNamespace("tests:TestForEachTry"),StaticProtectedNs("tests:TestForEachTry"),PrivateNamespace("TestForEachTry.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachTry"),ProtectedNamespace("tests:TestForEachTry"),StaticProtectedNs("tests:TestForEachTry"),PrivateNamespace("TestForEachTry.as$0")]), 1
            ofs0056:
                     jump ofs0072
            ofs005a:
                     getlocal0
                     pushscope
                     getlocal1
                     pushscope
                     newcatch 0
                     dup
                     setlocal 4
                     dup
                     pushscope
                     swap
                     setslot 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachTry"),ProtectedNamespace("tests:TestForEachTry"),StaticProtectedNs("tests:TestForEachTry"),PrivateNamespace("TestForEachTry.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachTry"),ProtectedNamespace("tests:TestForEachTry"),StaticProtectedNs("tests:TestForEachTry"),PrivateNamespace("TestForEachTry.as$0")]), 1
                     popscope
                     kill 4
            ofs0072:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachTry"),ProtectedNamespace("tests:TestForEachTry"),StaticProtectedNs("tests:TestForEachTry"),PrivateNamespace("TestForEachTry.as$0")])
                     pushstring "D"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachTry"),ProtectedNamespace("tests:TestForEachTry"),StaticProtectedNs("tests:TestForEachTry"),PrivateNamespace("TestForEachTry.as$0")]), 1
            ofs0079:
                     hasnext2 3, 2
                     iftrue ofs002c
                     kill 3
                     kill 2
                     returnvoid
                  end ; code
                  try from ofs0035 to ofs0056 target ofs005a type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
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
            findpropstrict Multiname("TestForEachTry",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForEachTry")
            returnvoid
         end ; code
      end ; body
   end ; method
   
