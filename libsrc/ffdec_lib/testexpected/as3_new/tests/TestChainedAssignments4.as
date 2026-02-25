package tests
{
   public class TestChainedAssignments4
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
      
      private var prop:int;
      
      public function TestChainedAssignments4()
      {
         method
            name "tests:TestChainedAssignments4/TestChainedAssignments4"
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
               name "tests:TestChainedAssignments4/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 5
                  localcount 3
                  initscopedepth 5
                  maxscopedepth 7
                  trait slot QName(PackageInternalNs("tests"),"slota")
                     slotid 1
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"slotb")
                     slotid 2
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"slotc")
                     slotid 3
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"f")
                     slotid 4
                     type QName(PackageNamespace(""),"Function")
                  end ; trait
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "+$activation", 0, 0
                     newactivation
                     dup
                     setlocal1
                     pushscope
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChainedAssignments4"),ProtectedNamespace("tests:TestChainedAssignments4"),StaticProtectedNs("tests:TestChainedAssignments4"),PrivateNamespace("TestChainedAssignments4.as$0")])
                     pushstring "slotc = slotb = slota = 5;"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChainedAssignments4"),ProtectedNamespace("tests:TestChainedAssignments4"),StaticProtectedNs("tests:TestChainedAssignments4"),PrivateNamespace("TestChainedAssignments4.as$0")]), 1
                     getscopeobject 1
                     pushbyte 0
                     setslot 1
                     getscopeobject 1
                     pushbyte 0
                     setslot 2
                     getscopeobject 1
                     pushbyte 0
                     setslot 3
                     getscopeobject 1
                     newfunction 2
                     coerce QName(PackageNamespace(""),"Function")
                     setslot 4
                     getscopeobject 1
                     getscopeobject 1
                     getscopeobject 1
                     pushbyte 5
                     dup
                     setlocal2
                     setslot 1
                     getlocal2
                     kill 2
                     dup
                     setlocal2
                     setslot 2
                     getlocal2
                     kill 2
                     setslot 3
                     returnvoid
                  end ; code
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
            findpropstrict Multiname("TestChainedAssignments4",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestChainedAssignments4")
            returnvoid
         end ; code
      end ; body
   end ; method
   
