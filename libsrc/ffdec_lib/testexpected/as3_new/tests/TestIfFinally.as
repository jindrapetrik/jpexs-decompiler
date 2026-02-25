package tests
{
   public class TestIfFinally
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
      
      public function TestIfFinally()
      {
         method
            name "tests:TestIfFinally/TestIfFinally"
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
               name "tests:TestIfFinally/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 4
                  initscopedepth 5
                  maxscopedepth 12
                  trait slot QName(PackageInternalNs("tests"),"a")
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
                     getlex QName(PackageNamespace(""),"Math")
                     callproperty QName(PackageNamespace(""),"random"), 0
                     convert_i
                     setslot 1
                     getscopeobject 1
                     getslot 1
                     pushbyte 5
                     ifne ofs0071
            ofs001f:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfFinally"),ProtectedNamespace("tests:TestIfFinally"),StaticProtectedNs("tests:TestIfFinally"),PrivateNamespace("TestIfFinally.as$0")])
                     pushstring "in try body"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfFinally"),ProtectedNamespace("tests:TestIfFinally"),StaticProtectedNs("tests:TestIfFinally"),PrivateNamespace("TestIfFinally.as$0")]), 1
            ofs0026:
                     jump ofs0041
            ofs002a:
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
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfFinally"),ProtectedNamespace("tests:TestIfFinally"),StaticProtectedNs("tests:TestIfFinally"),PrivateNamespace("TestIfFinally.as$0")])
                     pushstring "in catch"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfFinally"),ProtectedNamespace("tests:TestIfFinally"),StaticProtectedNs("tests:TestIfFinally"),PrivateNamespace("TestIfFinally.as$0")]), 1
                     popscope
                     kill 2
            ofs0041:
                     pushbyte -1
            ofs0043:
                     jump ofs0062
            ofs0047:
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
                     pushbyte 0
                     jump ofs0062
                     label
                     pop
            ofs005d:
                     label
                     getlocal3
                     kill 3
                     throw
            ofs0062:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfFinally"),ProtectedNamespace("tests:TestIfFinally"),StaticProtectedNs("tests:TestIfFinally"),PrivateNamespace("TestIfFinally.as$0")])
                     pushstring "in finally"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfFinally"),ProtectedNamespace("tests:TestIfFinally"),StaticProtectedNs("tests:TestIfFinally"),PrivateNamespace("TestIfFinally.as$0")]), 1
                     lookupswitch ofs0071, [ofs005d]
            ofs0071:
                     returnvoid
                  end ; code
                  try from ofs001f to ofs0026 target ofs002a type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
                  try from ofs001f to ofs0043 target ofs0047 type null name null end
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
            findpropstrict Multiname("TestIfFinally",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestIfFinally")
            returnvoid
         end ; code
      end ; body
   end ; method
   
