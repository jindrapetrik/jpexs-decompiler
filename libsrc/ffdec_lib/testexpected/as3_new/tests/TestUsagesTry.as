package tests
{
   public class TestUsagesTry
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
      
      public function TestUsagesTry()
      {
         method
            name "tests:TestUsagesTry/TestUsagesTry"
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
               name "tests:TestUsagesTry/run"
               flag NEED_ACTIVATION
               returns QName(PackageNamespace(""),"String")
               
               body
                  maxstack 3
                  localcount 4
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
                  trait slot QName(PackageInternalNs("tests"),"k")
                     slotid 3
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
                     pushbyte 5
                     setslot 3
                     jump ofs002e
            ofs0015:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")])
                     pushstring "1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")]), 1
                     jump ofs005c
            ofs0021:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")])
                     pushstring "2"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")]), 1
            ofs0029:
                     label
                     jump ofs005c
            ofs002e:
                     getscopeobject 1
                     getslot 3
                     setlocal2
                     pushbyte 0
                     getlocal2
                     ifstrictne ofs0040
                     pushbyte 0
                     jump ofs004f
            ofs0040:
                     pushbyte 1
                     getlocal2
                     ifstrictne ofs004d
                     pushbyte 1
                     jump ofs004f
            ofs004d:
                     pushbyte -1
            ofs004f:
                     kill 2
                     lookupswitch ofs0029, [ofs0015, ofs0021]
            ofs005c:
                     getscopeobject 1
                     pushtrue
                     convert_b
                     setslot 1
                     getscopeobject 1
                     pushtrue
                     convert_b
                     setslot 2
            ofs0068:
                     getscopeobject 1
                     getslot 2
                     iffalse ofs0081
                     pushstring "B"
                     coerce_a
                     setlocal2
                     pushbyte 0
                     jump ofs00c4
                     label
                     pop
            ofs007c:
                     label
                     getlocal2
                     kill 2
                     returnvalue
            ofs0081:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")]), 1
            ofs0088:
                     jump ofs00a3
            ofs008c:
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
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")])
                     pushstring "E"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")]), 1
                     popscope
                     kill 2
            ofs00a3:
                     pushbyte -1
            ofs00a5:
                     jump ofs00c4
            ofs00a9:
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
                     pushbyte 1
                     jump ofs00c4
                     label
                     pop
            ofs00bf:
                     label
                     getlocal3
                     kill 3
                     throw
            ofs00c4:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")])
                     pushstring "finally"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")]), 1
                     lookupswitch ofs00d6, [ofs007c, ofs00bf]
            ofs00d6:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")])
                     pushstring "after"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUsagesTry"),ProtectedNamespace("tests:TestUsagesTry"),StaticProtectedNs("tests:TestUsagesTry"),PrivateNamespace("TestUsagesTry.as$0")]), 1
                     pushstring "X"
                     returnvalue
                  end ; code
                  try from ofs0068 to ofs0088 target ofs008c type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
                  try from ofs0068 to ofs00a5 target ofs00a9 type null name null end
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
            findpropstrict Multiname("TestUsagesTry",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestUsagesTry")
            returnvoid
         end ; code
      end ; body
   end ; method
   
