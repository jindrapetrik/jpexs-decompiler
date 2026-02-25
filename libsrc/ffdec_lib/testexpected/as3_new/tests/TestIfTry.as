package tests
{
   public class TestIfTry
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
      
      public function TestIfTry()
      {
         method
            name "tests:TestIfTry/TestIfTry"
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
               name "tests:TestIfTry/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 5
                  maxscopedepth 10
                  trait slot QName(PackageInternalNs("tests"),"c")
                     slotid 1
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"i")
                     slotid 2
                     type QName(PackageNamespace(""),"int")
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
                     pushbyte 0
                     setslot 1
                     getscopeobject 1
                     pushbyte 0
                     setslot 2
                     getscopeobject 1
                     pushtrue
                     convert_b
                     setslot 3
                     getscopeobject 1
                     getslot 3
                     iffalse ofs0052
                     getscopeobject 1
                     pushbyte 5
                     setslot 1
                     getscopeobject 1
                     pushbyte 0
                     setslot 2
                     jump ofs0046
            ofs0035:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfTry"),ProtectedNamespace("tests:TestIfTry"),StaticProtectedNs("tests:TestIfTry"),PrivateNamespace("TestIfTry.as$0")])
                     pushstring "xx"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfTry"),ProtectedNamespace("tests:TestIfTry"),StaticProtectedNs("tests:TestIfTry"),PrivateNamespace("TestIfTry.as$0")]), 1
                     getscopeobject 1
                     getscopeobject 1
                     getslot 2
                     increment_i
                     setslot 2
            ofs0046:
                     getscopeobject 1
                     getslot 2
                     getscopeobject 1
                     getslot 1
                     iflt ofs0035
            ofs0052:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfTry"),ProtectedNamespace("tests:TestIfTry"),StaticProtectedNs("tests:TestIfTry"),PrivateNamespace("TestIfTry.as$0")])
                     pushstring "in try"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfTry"),ProtectedNamespace("tests:TestIfTry"),StaticProtectedNs("tests:TestIfTry"),PrivateNamespace("TestIfTry.as$0")]), 1
            ofs0059:
                     jump ofs0074
            ofs005d:
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
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfTry"),ProtectedNamespace("tests:TestIfTry"),StaticProtectedNs("tests:TestIfTry"),PrivateNamespace("TestIfTry.as$0")])
                     pushstring "in catch"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfTry"),ProtectedNamespace("tests:TestIfTry"),StaticProtectedNs("tests:TestIfTry"),PrivateNamespace("TestIfTry.as$0")]), 1
                     popscope
                     kill 2
            ofs0074:
                     returnvoid
                  end ; code
                  try from ofs0052 to ofs0059 target ofs005d type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
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
            findpropstrict Multiname("TestIfTry",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestIfTry")
            returnvoid
         end ; code
      end ; body
   end ; method
   
