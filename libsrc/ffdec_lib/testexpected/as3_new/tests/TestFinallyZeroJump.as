package tests
{
   public class TestFinallyZeroJump
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
      
      public function TestFinallyZeroJump()
      {
         method
            name "tests:TestFinallyZeroJump/TestFinallyZeroJump"
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
      
      public function run(param1:String) : String
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestFinallyZeroJump/run"
               flag NEED_ACTIVATION
               param QName(PackageNamespace(""),"String")
               returns QName(PackageNamespace(""),"String")
               
               body
                  maxstack 3
                  localcount 6
                  initscopedepth 5
                  maxscopedepth 12
                  trait slot QName(PackageInternalNs("tests"),"param1")
                     slotid 1
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"str")
                     slotid 2
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "param1", 0, 0
                     debug 1, "+$activation", 1, 0
                     newactivation
                     dup
                     setlocal2
                     pushscope
                     getscopeobject 1
                     getlocal1
                     coerce_s
                     setslot 1
                     getscopeobject 1
                     getscopeobject 1
                     getslot 1
                     coerce_s
                     setslot 2
            ofs001f:
                     jump ofs003f
            ofs0023:
                     getlocal0
                     pushscope
                     getlocal2
                     pushscope
                     newcatch 0
                     dup
                     setlocal3
                     dup
                     pushscope
                     swap
                     setslot 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestFinallyZeroJump"),ProtectedNamespace("tests:TestFinallyZeroJump"),StaticProtectedNs("tests:TestFinallyZeroJump"),PrivateNamespace("TestFinallyZeroJump.as$0")])
                     pushstring "error is :"
                     getlex Multiname("e",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestFinallyZeroJump"),ProtectedNamespace("tests:TestFinallyZeroJump"),StaticProtectedNs("tests:TestFinallyZeroJump"),PrivateNamespace("TestFinallyZeroJump.as$0")])
                     getproperty Multiname("message",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestFinallyZeroJump"),ProtectedNamespace("tests:TestFinallyZeroJump"),StaticProtectedNs("tests:TestFinallyZeroJump"),PrivateNamespace("TestFinallyZeroJump.as$0")])
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestFinallyZeroJump"),ProtectedNamespace("tests:TestFinallyZeroJump"),StaticProtectedNs("tests:TestFinallyZeroJump"),PrivateNamespace("TestFinallyZeroJump.as$0")]), 1
                     popscope
                     kill 3
            ofs003f:
                     pushbyte -1
            ofs0041:
                     jump ofs0062
            ofs0045:
                     getlocal0
                     pushscope
                     getlocal2
                     pushscope
                     newcatch 1
                     dup
                     setlocal3
                     pushscope
                     popscope
                     kill 3
                     coerce_a
                     setlocal 4
                     pushbyte 0
                     jump ofs0062
                     label
                     pop
            ofs005c:
                     label
                     getlocal 4
                     kill 4
                     throw
            ofs0062:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestFinallyZeroJump"),ProtectedNamespace("tests:TestFinallyZeroJump"),StaticProtectedNs("tests:TestFinallyZeroJump"),PrivateNamespace("TestFinallyZeroJump.as$0")])
                     pushstring "hi "
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestFinallyZeroJump"),ProtectedNamespace("tests:TestFinallyZeroJump"),StaticProtectedNs("tests:TestFinallyZeroJump"),PrivateNamespace("TestFinallyZeroJump.as$0")]), 1
                     pushbyte 5
                     pushbyte 4
                     ifne ofs0086
                     getscopeobject 1
                     getslot 2
                     coerce_a
                     setlocal 5
                     pushbyte 1
                     jump ofs007e
            ofs007e:
                     label
                     pop
            ofs0080:
                     label
                     getlocal 5
                     kill 5
                     returnvalue
            ofs0086:
                     pushstring "hu"
                     getscopeobject 1
                     getslot 2
                     add
                     coerce_a
                     setlocal 5
                     pushbyte 2
                     jump ofs0096
            ofs0096:
                     label
                     pop
            ofs0098:
                     label
                     getlocal 5
                     kill 5
                     returnvalue
                     lookupswitch ofs00ac, [ofs005c, ofs0080, ofs0098]
            ofs00ac:
                     pushundefined
                     returnvalue
                  end ; code
                  try from ofs001f to ofs001f target ofs0023 type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
                  try from ofs001f to ofs0041 target ofs0045 type null name null end
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
            findpropstrict Multiname("TestFinallyZeroJump",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestFinallyZeroJump")
            returnvoid
         end ; code
      end ; body
   end ; method
   
