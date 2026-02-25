package tests
{
   public class TestChainedAssignments3
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
      
      public function TestChainedAssignments3()
      {
         method
            name "tests:TestChainedAssignments3/TestChainedAssignments3"
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
               name "tests:TestChainedAssignments3/run"
               returns null
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 15
                     debug 1, "b", 1, 16
                     pushbyte 0
                     convert_i
                     setlocal1
                     pushbyte 0
                     convert_i
                     setlocal2
                     getlocal0
                     pushbyte 4
                     convert_i
                     dup
                     setlocal2
                     convert_i
                     dup
                     setlocal1
                     setproperty QName(PrivateNamespace("tests:TestChainedAssignments3"),"prop")
                     getlocal1
                     pushbyte 2
                     ifne ofs002f
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChainedAssignments3"),ProtectedNamespace("tests:TestChainedAssignments3"),StaticProtectedNs("tests:TestChainedAssignments3"),PrivateNamespace("TestChainedAssignments3.as$0")])
                     pushstring "OK: "
                     getlocal1
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChainedAssignments3"),ProtectedNamespace("tests:TestChainedAssignments3"),StaticProtectedNs("tests:TestChainedAssignments3"),PrivateNamespace("TestChainedAssignments3.as$0")]), 1
            ofs002f:
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
            findpropstrict Multiname("TestChainedAssignments3",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestChainedAssignments3")
            returnvoid
         end ; code
      end ; body
   end ; method
   
