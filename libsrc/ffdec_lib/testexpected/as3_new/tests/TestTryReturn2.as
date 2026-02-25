package tests
{
   public class TestTryReturn2
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
      
      public function TestTryReturn2()
      {
         method
            name "tests:TestTryReturn2/TestTryReturn2"
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
      
      public function run() : String
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestTryReturn2/run"
               flag NEED_ACTIVATION
               returns QName(PackageNamespace(""),"String")
               
               body
                  maxstack 3
                  localcount 5
                  initscopedepth 5
                  maxscopedepth 12
                  trait slot QName(PackageInternalNs("tests"),"a")
                     slotid 1
                     type QName(PackageNamespace(""),"Boolean")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"b")
                     slotid 2
                     type QName(PackageNamespace(""),"Boolean")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"d")
                     slotid 3
                     type QName(PackageNamespace(""),"Boolean")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"e")
                     slotid 4
                     type QName(PackageNamespace(""),"Boolean")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"c")
                     slotid 5
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
                     pushfalse
                     convert_b
                     setslot 5
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTryReturn2"),ProtectedNamespace("tests:TestTryReturn2"),StaticProtectedNs("tests:TestTryReturn2"),PrivateNamespace("TestTryReturn2.as$0")])
                     pushstring "before"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTryReturn2"),ProtectedNamespace("tests:TestTryReturn2"),StaticProtectedNs("tests:TestTryReturn2"),PrivateNamespace("TestTryReturn2.as$0")]), 1
                     getscopeobject 1
                     pushtrue
                     convert_b
                     setslot 1
                     getscopeobject 1
                     pushfalse
                     convert_b
                     setslot 2
                     getscopeobject 1
                     pushtrue
                     convert_b
                     setslot 5
                     getscopeobject 1
                     pushfalse
                     convert_b
                     setslot 3
                     getscopeobject 1
                     pushtrue
                     convert_b
                     setslot 4
            ofs0036:
                     getscopeobject 1
                     getslot 1
                     iffalse ofs004f
                     pushstring "A"
                     coerce_a
                     setlocal2
                     pushbyte 0
                     jump ofs00b6
                     label
                     pop
            ofs004a:
                     label
                     getlocal2
                     kill 2
                     returnvalue
            ofs004f:
                     getscopeobject 1
                     getslot 2
                     iffalse ofs0068
                     pushstring "B"
                     coerce_a
                     setlocal2
                     pushbyte 1
                     jump ofs00b6
                     label
                     pop
            ofs0063:
                     label
                     getlocal2
                     kill 2
                     returnvalue
            ofs0068:
                     jump ofs0095
            ofs006c:
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
                     getscopeobject 1
                     getslot 5
                     iffalse ofs0092
                     pushstring "C"
                     coerce_a
                     setlocal2
                     pushbyte 2
                     jump ofs00b6
                     label
                     pop
            ofs008d:
                     label
                     getlocal2
                     kill 2
                     returnvalue
            ofs0092:
                     popscope
                     kill 2
            ofs0095:
                     pushbyte -1
            ofs0097:
                     jump ofs00b6
            ofs009b:
                     getlocal0
                     pushscope
                     getlocal1
                     pushscope
                     newcatch 1
                     dup
                     setlocal2
                     pushscope
                     popscope
                     kill 2
                     coerce_a
                     setlocal3
                     pushbyte 3
                     jump ofs00b6
                     label
                     pop
            ofs00b1:
                     label
                     getlocal3
                     kill 3
                     throw
            ofs00b6:
                     getscopeobject 1
                     getslot 3
                     iffalse ofs00d1
                     pushstring "D"
                     coerce_a
                     setlocal 4
                     pushbyte 4
                     jump ofs00c9
            ofs00c9:
                     label
                     pop
            ofs00cb:
                     label
                     getlocal 4
                     kill 4
                     returnvalue
            ofs00d1:
                     getscopeobject 1
                     getslot 4
                     iffalse ofs00ec
                     pushstring "E"
                     coerce_a
                     setlocal 4
                     pushbyte 5
                     jump ofs00e4
            ofs00e4:
                     label
                     pop
            ofs00e6:
                     label
                     getlocal 4
                     kill 4
                     returnvalue
            ofs00ec:
                     lookupswitch ofs0103, [ofs004a, ofs0063, ofs008d, ofs00b1, ofs00cb, ofs00e6]
            ofs0103:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTryReturn2"),ProtectedNamespace("tests:TestTryReturn2"),StaticProtectedNs("tests:TestTryReturn2"),PrivateNamespace("TestTryReturn2.as$0")])
                     pushstring "after"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTryReturn2"),ProtectedNamespace("tests:TestTryReturn2"),StaticProtectedNs("tests:TestTryReturn2"),PrivateNamespace("TestTryReturn2.as$0")]), 1
                     pushstring "X"
                     returnvalue
                  end ; code
                  try from ofs0036 to ofs0068 target ofs006c type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
                  try from ofs0036 to ofs0097 target ofs009b type null name null end
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
            findpropstrict Multiname("TestTryReturn2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestTryReturn2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
