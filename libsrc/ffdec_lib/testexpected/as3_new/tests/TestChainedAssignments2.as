package tests
{
   public class TestChainedAssignments2
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
      
      public function TestChainedAssignments2()
      {
         method
            name "tests:TestChainedAssignments2/TestChainedAssignments2"
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
               name "tests:TestChainedAssignments2/run"
               returns null
               
               body
                  maxstack 5
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "e", 0, 14
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChainedAssignments2"),ProtectedNamespace("tests:TestChainedAssignments2"),StaticProtectedNs("tests:TestChainedAssignments2"),PrivateNamespace("TestChainedAssignments2.as$0")])
                     pushstring "e.attrib1 = e.attrib2 = e.attrib3 = 10;"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChainedAssignments2"),ProtectedNamespace("tests:TestChainedAssignments2"),StaticProtectedNs("tests:TestChainedAssignments2"),PrivateNamespace("TestChainedAssignments2.as$0")]), 1
                     findpropstrict QName(PrivateNamespace("TestChainedAssignments2.as$0"),"TestClass")
                     constructprop QName(PrivateNamespace("TestChainedAssignments2.as$0"),"TestClass"), 0
                     coerce QName(PrivateNamespace("TestChainedAssignments2.as$0"),"TestClass")
                     setlocal1
                     getlocal1
                     getlocal1
                     getlocal1
                     pushbyte 10
                     dup
                     setlocal2
                     setproperty Multiname("attrib3",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChainedAssignments2"),ProtectedNamespace("tests:TestChainedAssignments2"),StaticProtectedNs("tests:TestChainedAssignments2"),PrivateNamespace("TestChainedAssignments2.as$0")])
                     getlocal2
                     kill 2
                     dup
                     setlocal2
                     setproperty Multiname("attrib2",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChainedAssignments2"),ProtectedNamespace("tests:TestChainedAssignments2"),StaticProtectedNs("tests:TestChainedAssignments2"),PrivateNamespace("TestChainedAssignments2.as$0")])
                     getlocal2
                     kill 2
                     setproperty Multiname("attrib1",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChainedAssignments2"),ProtectedNamespace("tests:TestChainedAssignments2"),StaticProtectedNs("tests:TestChainedAssignments2"),PrivateNamespace("TestChainedAssignments2.as$0")])
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
      }
   }
   
   class TestClass
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
      
      public var attrib1:int;
      
      public var attrib2:int;
      
      public var attrib3:int;
      
      public function TestClass()
      {
         method
            name "TestChainedAssignments2.as$0:TestClass/TestClass"
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
            findpropstrict Multiname("TestChainedAssignments2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestChainedAssignments2")
            findpropstrict Multiname("TestClass",[PrivateNamespace("TestChainedAssignments2.as$0")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 1
            popscope
            initproperty QName(PrivateNamespace("TestChainedAssignments2.as$0"),"TestClass")
            returnvoid
         end ; code
      end ; body
   end ; method
   
