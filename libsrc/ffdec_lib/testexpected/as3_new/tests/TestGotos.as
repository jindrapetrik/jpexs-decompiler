package tests
{
   public class TestGotos
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
      
      public function TestGotos()
      {
         method
            name "tests:TestGotos/TestGotos"
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
      
      final public function run(param1:Object) : int
      {
         trait method QName(PackageNamespace(""),"run")
            flag FINAL
            dispid 0
            method
               name "tests:TestGotos/run"
               flag NEED_ACTIVATION
               param QName(PackageNamespace(""),"Object")
               returns QName(PackageNamespace(""),"int")
               
               body
                  maxstack 3
                  localcount 4
                  initscopedepth 5
                  maxscopedepth 10
                  trait slot QName(PackageInternalNs("tests"),"param1")
                     slotid 1
                     type QName(PackageNamespace(""),"Object")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"a")
                     slotid 2
                     type QName(PackageNamespace(""),"Boolean")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"b")
                     slotid 3
                     type QName(PackageNamespace(""),"Boolean")
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
                     coerce QName(PackageNamespace(""),"Object")
                     setslot 1
                     getscopeobject 1
                     pushtrue
                     convert_b
                     setslot 2
                     getscopeobject 1
                     pushfalse
                     convert_b
                     setslot 3
                     getscopeobject 1
                     getslot 2
                     iffalse ofs0036
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos"),ProtectedNamespace("tests:TestGotos"),StaticProtectedNs("tests:TestGotos"),PrivateNamespace("TestGotos.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos"),ProtectedNamespace("tests:TestGotos"),StaticProtectedNs("tests:TestGotos"),PrivateNamespace("TestGotos.as$0")]), 1
                     jump ofs007d
            ofs0036:
                     getscopeobject 1
                     getslot 3
                     iffalse ofs0049
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos"),ProtectedNamespace("tests:TestGotos"),StaticProtectedNs("tests:TestGotos"),PrivateNamespace("TestGotos.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos"),ProtectedNamespace("tests:TestGotos"),StaticProtectedNs("tests:TestGotos"),PrivateNamespace("TestGotos.as$0")]), 1
                     jump ofs007d
            ofs0049:
                     getscopeobject 1
                     getslot 2
                     iffalse ofs0054
                     pushbyte 7
                     returnvalue
            ofs0054:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos"),ProtectedNamespace("tests:TestGotos"),StaticProtectedNs("tests:TestGotos"),PrivateNamespace("TestGotos.as$0")])
                     pushstring "x"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos"),ProtectedNamespace("tests:TestGotos"),StaticProtectedNs("tests:TestGotos"),PrivateNamespace("TestGotos.as$0")]), 1
            ofs005b:
                     jump ofs0076
            ofs005f:
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
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos"),ProtectedNamespace("tests:TestGotos"),StaticProtectedNs("tests:TestGotos"),PrivateNamespace("TestGotos.as$0")])
                     pushstring "z"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos"),ProtectedNamespace("tests:TestGotos"),StaticProtectedNs("tests:TestGotos"),PrivateNamespace("TestGotos.as$0")]), 1
                     popscope
                     kill 3
            ofs0076:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos"),ProtectedNamespace("tests:TestGotos"),StaticProtectedNs("tests:TestGotos"),PrivateNamespace("TestGotos.as$0")])
                     pushstring "after"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos"),ProtectedNamespace("tests:TestGotos"),StaticProtectedNs("tests:TestGotos"),PrivateNamespace("TestGotos.as$0")]), 1
            ofs007d:
                     pushbyte 89
                     returnvalue
                  end ; code
                  try from ofs0049 to ofs005b target ofs005f type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
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
            findpropstrict Multiname("TestGotos",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestGotos")
            returnvoid
         end ; code
      end ; body
   end ; method
   
