package tests
{
   public class TestInnerTry
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
      
      public function TestInnerTry()
      {
         method
            name "tests:TestInnerTry/TestInnerTry"
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
               name "tests:TestInnerTry/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 4
                  initscopedepth 5
                  maxscopedepth 14
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "+$activation", 0, 0
                     newactivation
                     dup
                     setlocal1
                     pushscope
            ofs000b:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerTry"),ProtectedNamespace("tests:TestInnerTry"),StaticProtectedNs("tests:TestInnerTry"),PrivateNamespace("TestInnerTry.as$0")])
                     pushstring "try body 1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerTry"),ProtectedNamespace("tests:TestInnerTry"),StaticProtectedNs("tests:TestInnerTry"),PrivateNamespace("TestInnerTry.as$0")]), 1
            ofs0012:
                     jump ofs002d
            ofs0016:
                     getlocal0
                     pushscope
                     getlocal1
                     pushscope
                     newcatch 0
                     dup
                     setlocal2
                     dup
                     pushscope
                     swap
                     setslot 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerTry"),ProtectedNamespace("tests:TestInnerTry"),StaticProtectedNs("tests:TestInnerTry"),PrivateNamespace("TestInnerTry.as$0")])
                     pushstring "catched DefinitionError"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerTry"),ProtectedNamespace("tests:TestInnerTry"),StaticProtectedNs("tests:TestInnerTry"),PrivateNamespace("TestInnerTry.as$0")]), 1
                     popscope
                     kill 2
            ofs002d:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerTry"),ProtectedNamespace("tests:TestInnerTry"),StaticProtectedNs("tests:TestInnerTry"),PrivateNamespace("TestInnerTry.as$0")])
                     pushstring "after try 1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerTry"),ProtectedNamespace("tests:TestInnerTry"),StaticProtectedNs("tests:TestInnerTry"),PrivateNamespace("TestInnerTry.as$0")]), 1
            ofs0034:
                     jump ofs004f
            ofs0038:
                     getlocal0
                     pushscope
                     getlocal1
                     pushscope
                     newcatch 1
                     dup
                     setlocal2
                     dup
                     pushscope
                     swap
                     setslot 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerTry"),ProtectedNamespace("tests:TestInnerTry"),StaticProtectedNs("tests:TestInnerTry"),PrivateNamespace("TestInnerTry.as$0")])
                     pushstring "catched Error"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerTry"),ProtectedNamespace("tests:TestInnerTry"),StaticProtectedNs("tests:TestInnerTry"),PrivateNamespace("TestInnerTry.as$0")]), 1
                     popscope
                     kill 2
            ofs004f:
                     pushbyte -1
            ofs0051:
                     jump ofs0070
            ofs0055:
                     getlocal0
                     pushscope
                     getlocal1
                     pushscope
                     newcatch 2
                     dup
                     setlocal2
                     pushscope
                     popscope
                     kill 2
                     coerce_a
                     setlocal3
                     pushbyte 0
                     jump ofs0070
                     label
                     pop
            ofs006b:
                     label
                     getlocal3
                     kill 3
                     throw
            ofs0070:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerTry"),ProtectedNamespace("tests:TestInnerTry"),StaticProtectedNs("tests:TestInnerTry"),PrivateNamespace("TestInnerTry.as$0")])
                     pushstring "finally block"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerTry"),ProtectedNamespace("tests:TestInnerTry"),StaticProtectedNs("tests:TestInnerTry"),PrivateNamespace("TestInnerTry.as$0")]), 1
                     lookupswitch ofs007f, [ofs006b]
            ofs007f:
                     returnvoid
                  end ; code
                  try from ofs000b to ofs0012 target ofs0016 type QName(PackageNamespace(""),"DefinitionError") name QName(PackageNamespace(""),"e") end
                  try from ofs000b to ofs0034 target ofs0038 type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
                  try from ofs000b to ofs0051 target ofs0055 type null name null end
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
            findpropstrict Multiname("TestInnerTry",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestInnerTry")
            returnvoid
         end ; code
      end ; body
   end ; method
   
