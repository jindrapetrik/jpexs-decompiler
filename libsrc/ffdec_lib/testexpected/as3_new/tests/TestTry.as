package tests
{
   public class TestTry
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
      
      public function TestTry()
      {
         method
            name "tests:TestTry/TestTry"
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
               name "tests:TestTry/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 4
                  initscopedepth 5
                  maxscopedepth 14
                  trait slot QName(PackageInternalNs("tests"),"i")
                     slotid 1
                     type QName(PackageNamespace(""),"int")
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
                     pushbyte 0
                     setslot 1
                     getscopeobject 1
                     pushbyte 7
                     setslot 1
            ofs0017:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")])
                     pushstring "try body"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")]), 1
            ofs001e:
                     jump ofs0066
            ofs0022:
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
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")])
                     pushstring "catched DefinitionError"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")]), 1
                     popscope
                     kill 2
                     jump ofs0066
            ofs003d:
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
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")])
                     pushstring "Error message:"
                     getlex Multiname("e",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")])
                     getproperty Multiname("message",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")])
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")])
                     pushstring "Stacktrace:"
                     getlex Multiname("e",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")])
                     callproperty Multiname("getStackTrace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")]), 0
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")]), 1
                     popscope
                     kill 2
            ofs0066:
                     pushbyte -1
            ofs0068:
                     jump ofs0087
            ofs006c:
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
                     jump ofs0087
                     label
                     pop
            ofs0082:
                     label
                     getlocal3
                     kill 3
                     throw
            ofs0087:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")])
                     pushstring "Finally part"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")]), 1
                     lookupswitch ofs0096, [ofs0082]
            ofs0096:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")])
                     pushstring "end"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTry"),ProtectedNamespace("tests:TestTry"),StaticProtectedNs("tests:TestTry"),PrivateNamespace("TestTry.as$0")]), 1
                     returnvoid
                  end ; code
                  try from ofs0017 to ofs001e target ofs0022 type QName(PackageNamespace(""),"DefinitionError") name QName(PackageNamespace(""),"e") end
                  try from ofs0017 to ofs001e target ofs003d type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
                  try from ofs0017 to ofs0068 target ofs006c type null name null end
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
            findpropstrict Multiname("TestTry",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestTry")
            returnvoid
         end ; code
      end ; body
   end ; method
   
