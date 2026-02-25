package tests
{
   import tests_classes.mypackage1.TestClass;
   import tests_classes.mypackage2.TestClass;
   
   public class TestVarFqn
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
      
      private var c1:tests_classes.mypackage1.TestClass;
      
      private var c2:tests_classes.mypackage2.TestClass;
      
      public function TestVarFqn()
      {
         method
            name "tests:TestVarFqn/TestVarFqn"
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
      
      public function run(TestClass:int) : *
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestVarFqn/run"
               flag NEED_ACTIVATION
               param QName(PackageNamespace(""),"int")
               returns null
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 5
                  maxscopedepth 7
                  trait slot QName(PackageInternalNs("tests"),"TestClass")
                     slotid 1
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"b")
                     slotid 2
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"f")
                     slotid 3
                     type QName(PackageNamespace(""),"Function")
                  end ; trait
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "TestClass", 0, 0
                     debug 1, "+$activation", 1, 0
                     newactivation
                     dup
                     setlocal2
                     pushscope
                     getscopeobject 1
                     getlocal1
                     setslot 1
                     getscopeobject 1
                     getscopeobject 1
                     getslot 1
                     pushbyte 5
                     add
                     convert_i
                     setslot 2
                     getscopeobject 1
                     newfunction 2
                     coerce QName(PackageNamespace(""),"Function")
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
            findpropstrict Multiname("TestVarFqn",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestVarFqn")
            returnvoid
         end ; code
      end ; body
   end ; method
   
