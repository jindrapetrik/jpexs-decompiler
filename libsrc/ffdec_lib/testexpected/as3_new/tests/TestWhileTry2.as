package tests
{
   import flash.errors.EOFError;
   
   public class TestWhileTry2
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
      
      public function TestWhileTry2()
      {
         method
            name "tests:TestWhileTry2/TestWhileTry2"
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
               name "tests:TestWhileTry2/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 5
                  maxscopedepth 12
                  trait slot QName(PackageInternalNs("tests"),"i")
                     slotid 1
                     type null
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"j")
                     slotid 2
                     type null
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
                     pushundefined
                     coerce_a
                     setslot 2
                     getscopeobject 1
                     pushbyte 0
                     coerce_a
                     setslot 1
                     jump ofs0083
            ofs001c:
                     label
            ofs001d:
                     getscopeobject 1
                     pushbyte 0
                     coerce_a
                     setslot 2
                     jump ofs0039
            ofs0028:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileTry2"),ProtectedNamespace("tests:TestWhileTry2"),StaticProtectedNs("tests:TestWhileTry2"),PrivateNamespace("TestWhileTry2.as$0")])
                     pushstring "a"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileTry2"),ProtectedNamespace("tests:TestWhileTry2"),StaticProtectedNs("tests:TestWhileTry2"),PrivateNamespace("TestWhileTry2.as$0")]), 1
                     getscopeobject 1
                     getscopeobject 1
                     getslot 2
                     increment
                     setslot 2
            ofs0039:
                     getscopeobject 1
                     getslot 2
                     pushbyte 20
                     iflt ofs0028
            ofs0043:
                     jump ofs0073
            ofs0047:
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
                     popscope
                     kill 2
                     jump ofs007a
                     jump ofs0073
            ofs005f:
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
                     popscope
                     kill 2
                     jump ofs007a
            ofs0073:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileTry2"),ProtectedNamespace("tests:TestWhileTry2"),StaticProtectedNs("tests:TestWhileTry2"),PrivateNamespace("TestWhileTry2.as$0")])
                     pushstring "after_try"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileTry2"),ProtectedNamespace("tests:TestWhileTry2"),StaticProtectedNs("tests:TestWhileTry2"),PrivateNamespace("TestWhileTry2.as$0")]), 1
            ofs007a:
                     getscopeobject 1
                     getscopeobject 1
                     getslot 1
                     increment
                     setslot 1
            ofs0083:
                     getscopeobject 1
                     getslot 1
                     pushbyte 100
                     iflt ofs001c
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileTry2"),ProtectedNamespace("tests:TestWhileTry2"),StaticProtectedNs("tests:TestWhileTry2"),PrivateNamespace("TestWhileTry2.as$0")])
                     pushstring "end"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileTry2"),ProtectedNamespace("tests:TestWhileTry2"),StaticProtectedNs("tests:TestWhileTry2"),PrivateNamespace("TestWhileTry2.as$0")]), 1
                     returnvoid
                  end ; code
                  try from ofs001d to ofs0043 target ofs0047 type QName(PackageNamespace("flash.errors"),"EOFError") name QName(PackageNamespace(""),"e") end
                  try from ofs001d to ofs0043 target ofs005f type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
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
            findpropstrict Multiname("TestWhileTry2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestWhileTry2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
