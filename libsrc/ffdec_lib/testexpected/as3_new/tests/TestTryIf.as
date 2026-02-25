package tests
{
   public class TestTryIf
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
      
      public function TestTryIf()
      {
         method
            name "tests:TestTryIf/TestTryIf"
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
               name "tests:TestTryIf/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 5
                  maxscopedepth 10
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
            ofs0015:
                     getscopeobject 1
                     getslot 1
                     pushbyte 5
                     greaterthan
                     dup
                     iffalse ofs0029
                     pop
                     getscopeobject 1
                     getslot 1
                     pushbyte 50
                     lessthan
            ofs0029:
                     iffalse ofs0034
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTryIf"),ProtectedNamespace("tests:TestTryIf"),StaticProtectedNs("tests:TestTryIf"),PrivateNamespace("TestTryIf.as$0")])
                     pushstring "in limits"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTryIf"),ProtectedNamespace("tests:TestTryIf"),StaticProtectedNs("tests:TestTryIf"),PrivateNamespace("TestTryIf.as$0")]), 1
            ofs0034:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTryIf"),ProtectedNamespace("tests:TestTryIf"),StaticProtectedNs("tests:TestTryIf"),PrivateNamespace("TestTryIf.as$0")])
                     pushstring "next"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTryIf"),ProtectedNamespace("tests:TestTryIf"),StaticProtectedNs("tests:TestTryIf"),PrivateNamespace("TestTryIf.as$0")]), 1
            ofs003b:
                     jump ofs0056
            ofs003f:
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
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTryIf"),ProtectedNamespace("tests:TestTryIf"),StaticProtectedNs("tests:TestTryIf"),PrivateNamespace("TestTryIf.as$0")])
                     pushstring "in catch"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTryIf"),ProtectedNamespace("tests:TestTryIf"),StaticProtectedNs("tests:TestTryIf"),PrivateNamespace("TestTryIf.as$0")]), 1
                     popscope
                     kill 2
            ofs0056:
                     returnvoid
                  end ; code
                  try from ofs0015 to ofs003b target ofs003f type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
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
            findpropstrict Multiname("TestTryIf",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestTryIf")
            returnvoid
         end ; code
      end ; body
   end ; method
   
