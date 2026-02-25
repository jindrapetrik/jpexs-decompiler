package tests
{
   public class TestCollidingTry
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
      
      public function TestCollidingTry()
      {
         method
            name "tests:TestCollidingTry/TestCollidingTry"
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
               name "tests:TestCollidingTry/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 5
                  maxscopedepth 12
                  trait slot QName(PackageInternalNs("tests"),"e")
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
            ofs0011:
                     getscopeobject 1
                     pushbyte 0
                     setslot 1
            ofs0017:
                     jump ofs0034
            ofs001b:
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
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCollidingTry"),ProtectedNamespace("tests:TestCollidingTry"),StaticProtectedNs("tests:TestCollidingTry"),PrivateNamespace("TestCollidingTry.as$0")])
                     getscopeobject 2
                     getslot 1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCollidingTry"),ProtectedNamespace("tests:TestCollidingTry"),StaticProtectedNs("tests:TestCollidingTry"),PrivateNamespace("TestCollidingTry.as$0")]), 1
                     popscope
                     kill 2
            ofs0034:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCollidingTry"),ProtectedNamespace("tests:TestCollidingTry"),StaticProtectedNs("tests:TestCollidingTry"),PrivateNamespace("TestCollidingTry.as$0")])
                     pushstring "x"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCollidingTry"),ProtectedNamespace("tests:TestCollidingTry"),StaticProtectedNs("tests:TestCollidingTry"),PrivateNamespace("TestCollidingTry.as$0")]), 1
            ofs003b:
                     jump ofs0058
            ofs003f:
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
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCollidingTry"),ProtectedNamespace("tests:TestCollidingTry"),StaticProtectedNs("tests:TestCollidingTry"),PrivateNamespace("TestCollidingTry.as$0")])
                     getscopeobject 2
                     getslot 1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCollidingTry"),ProtectedNamespace("tests:TestCollidingTry"),StaticProtectedNs("tests:TestCollidingTry"),PrivateNamespace("TestCollidingTry.as$0")]), 1
                     popscope
                     kill 2
            ofs0058:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCollidingTry"),ProtectedNamespace("tests:TestCollidingTry"),StaticProtectedNs("tests:TestCollidingTry"),PrivateNamespace("TestCollidingTry.as$0")])
                     pushstring "y"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCollidingTry"),ProtectedNamespace("tests:TestCollidingTry"),StaticProtectedNs("tests:TestCollidingTry"),PrivateNamespace("TestCollidingTry.as$0")]), 1
                     returnvoid
                  end ; code
                  try from ofs0011 to ofs0017 target ofs001b type null name QName(PackageNamespace(""),"e") end
                  try from ofs0034 to ofs003b target ofs003f type null name QName(PackageNamespace(""),"e") end
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
            findpropstrict Multiname("TestCollidingTry",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestCollidingTry")
            returnvoid
         end ; code
      end ; body
   end ; method
   
