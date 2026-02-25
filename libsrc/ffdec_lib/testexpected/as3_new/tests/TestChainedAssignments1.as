package tests
{
   public class TestChainedAssignments1
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
      
      public function TestChainedAssignments1()
      {
         method
            name "tests:TestChainedAssignments1/TestChainedAssignments1"
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
               name "tests:TestChainedAssignments1/run"
               returns null
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 14
                     debug 1, "b", 1, 15
                     debug 1, "c", 2, 16
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChainedAssignments1"),ProtectedNamespace("tests:TestChainedAssignments1"),StaticProtectedNs("tests:TestChainedAssignments1"),PrivateNamespace("TestChainedAssignments1.as$0")])
                     pushstring "c = b = a = 5;"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChainedAssignments1"),ProtectedNamespace("tests:TestChainedAssignments1"),StaticProtectedNs("tests:TestChainedAssignments1"),PrivateNamespace("TestChainedAssignments1.as$0")]), 1
                     pushbyte 0
                     convert_i
                     setlocal1
                     pushbyte 0
                     convert_i
                     setlocal2
                     pushbyte 0
                     convert_i
                     setlocal3
                     pushbyte 5
                     convert_i
                     dup
                     setlocal1
                     convert_i
                     dup
                     setlocal2
                     convert_i
                     setlocal3
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
            findpropstrict Multiname("TestChainedAssignments1",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestChainedAssignments1")
            returnvoid
         end ; code
      end ; body
   end ; method
   
