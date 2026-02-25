package tests
{
   public class TestExecutionOrder
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
      
      public function TestExecutionOrder()
      {
         method
            name "tests:TestExecutionOrder/TestExecutionOrder"
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
      
      private static function create() : Object
      {
         trait method QName(PrivateNamespace("tests:TestExecutionOrder"),"create")
            flag FINAL
            dispid 3
            method
               name "tests:TestExecutionOrder/private/create"
               returns QName(PackageNamespace(""),"Object")
               
               body
                  maxstack 1
                  localcount 1
                  initscopedepth 3
                  maxscopedepth 4
                  
                  code
                     getlocal0
                     pushscope
                     findpropstrict QName(PrivateNamespace("TestExecutionOrder.as$0"),"MyClass")
                     constructprop QName(PrivateNamespace("TestExecutionOrder.as$0"),"MyClass"), 0
                     returnvalue
                  end ; code
               end ; body
            end ; method
         }
         
         public function run() : *
         {
            trait method QName(PackageNamespace(""),"run")
               dispid 0
               method
                  name "tests:TestExecutionOrder/run"
                  returns null
                  
                  body
                     maxstack 3
                     localcount 2
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        debug 1, "m", 0, 18
                        pushnull
                        coerce QName(PrivateNamespace("TestExecutionOrder.as$0"),"MyClass")
                        setlocal1
                        getlocal1
                        findpropstrict QName(PrivateNamespace("tests:TestExecutionOrder"),"create")
                        callproperty QName(PrivateNamespace("tests:TestExecutionOrder"),"create"), 0
                        getlex QName(PrivateNamespace("TestExecutionOrder.as$0"),"MyClass")
                        astypelate
                        coerce QName(PrivateNamespace("TestExecutionOrder.as$0"),"MyClass")
                        dup
                        setlocal1
                        getproperty Multiname("x",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestExecutionOrder"),ProtectedNamespace("tests:TestExecutionOrder"),StaticProtectedNs("tests:TestExecutionOrder"),PrivateNamespace("TestExecutionOrder.as$0")])
                        pushbyte 5
                        add
                        setproperty Multiname("x",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestExecutionOrder"),ProtectedNamespace("tests:TestExecutionOrder"),StaticProtectedNs("tests:TestExecutionOrder"),PrivateNamespace("TestExecutionOrder.as$0")])
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestExecutionOrder"),ProtectedNamespace("tests:TestExecutionOrder"),StaticProtectedNs("tests:TestExecutionOrder"),PrivateNamespace("TestExecutionOrder.as$0")])
                        getlocal1
                        getproperty Multiname("x",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestExecutionOrder"),ProtectedNamespace("tests:TestExecutionOrder"),StaticProtectedNs("tests:TestExecutionOrder"),PrivateNamespace("TestExecutionOrder.as$0")])
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestExecutionOrder"),ProtectedNamespace("tests:TestExecutionOrder"),StaticProtectedNs("tests:TestExecutionOrder"),PrivateNamespace("TestExecutionOrder.as$0")]), 1
                        returnvoid
                     end ; code
                  end ; body
               end ; method
            }
         }
      }
      
      class MyClass
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
         
         public var x:int = 1;
         
         public var y:int = 1;
         
         public function MyClass()
         {
            method
               name "TestExecutionOrder.as$0:MyClass/MyClass"
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
               findpropstrict Multiname("TestExecutionOrder",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestExecutionOrder")
               findpropstrict Multiname("MyClass",[PrivateNamespace("TestExecutionOrder.as$0")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 1
               popscope
               initproperty QName(PrivateNamespace("TestExecutionOrder.as$0"),"MyClass")
               returnvoid
            end ; code
         end ; body
      end ; method
      
