package tests
{
   public class TestCatchFinally
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
      
      public function TestCatchFinally()
      {
         method
            name "tests:TestCatchFinally/TestCatchFinally"
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
               name "tests:TestCatchFinally/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 4
                  initscopedepth 5
                  maxscopedepth 12
                  trait slot QName(PackageInternalNs("tests"),"a")
                     slotid 1
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
                     pushbyte 5
                     coerce_a
                     setslot 1
            ofs0012:
                     getscopeobject 1
                     pushbyte 9
                     coerce_a
                     setslot 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCatchFinally"),ProtectedNamespace("tests:TestCatchFinally"),StaticProtectedNs("tests:TestCatchFinally"),PrivateNamespace("TestCatchFinally.as$0")])
                     pushstring "intry"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCatchFinally"),ProtectedNamespace("tests:TestCatchFinally"),StaticProtectedNs("tests:TestCatchFinally"),PrivateNamespace("TestCatchFinally.as$0")]), 1
            ofs0020:
                     jump ofs003b
            ofs0024:
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
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCatchFinally"),ProtectedNamespace("tests:TestCatchFinally"),StaticProtectedNs("tests:TestCatchFinally"),PrivateNamespace("TestCatchFinally.as$0")])
                     pushstring "incatch"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCatchFinally"),ProtectedNamespace("tests:TestCatchFinally"),StaticProtectedNs("tests:TestCatchFinally"),PrivateNamespace("TestCatchFinally.as$0")]), 1
                     popscope
                     kill 2
            ofs003b:
                     pushbyte -1
            ofs003d:
                     jump ofs005c
            ofs0041:
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
                     jump ofs005c
                     label
                     pop
            ofs0057:
                     label
                     getlocal3
                     kill 3
                     throw
            ofs005c:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCatchFinally"),ProtectedNamespace("tests:TestCatchFinally"),StaticProtectedNs("tests:TestCatchFinally"),PrivateNamespace("TestCatchFinally.as$0")])
                     pushstring "infinally"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCatchFinally"),ProtectedNamespace("tests:TestCatchFinally"),StaticProtectedNs("tests:TestCatchFinally"),PrivateNamespace("TestCatchFinally.as$0")]), 1
                     lookupswitch ofs006b, [ofs0057]
            ofs006b:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCatchFinally"),ProtectedNamespace("tests:TestCatchFinally"),StaticProtectedNs("tests:TestCatchFinally"),PrivateNamespace("TestCatchFinally.as$0")])
                     pushstring "after"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCatchFinally"),ProtectedNamespace("tests:TestCatchFinally"),StaticProtectedNs("tests:TestCatchFinally"),PrivateNamespace("TestCatchFinally.as$0")]), 1
                     returnvoid
                  end ; code
                  try from ofs0012 to ofs0020 target ofs0024 type null name QName(PackageNamespace(""),"e") end
                  try from ofs0012 to ofs003d target ofs0041 type null name null end
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
            findpropstrict Multiname("TestCatchFinally",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestCatchFinally")
            returnvoid
         end ; code
      end ; body
   end ; method
   
