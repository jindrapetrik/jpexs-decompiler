package tests
{
   public class TestTernarOperator
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
      
      public function TestTernarOperator()
      {
         method
            name "tests:TestTernarOperator/TestTernarOperator"
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
               name "tests:TestTernarOperator/run"
               returns null
               
               body
                  maxstack 3
                  localcount 6
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     debug 1, "b", 1, 14
                     debug 1, "c", 2, 15
                     debug 1, "d", 3, 16
                     debug 1, "e", 4, 17
                     pushbyte 5
                     coerce_a
                     setlocal1
                     pushbyte 4
                     coerce_a
                     setlocal2
                     pushbyte 4
                     coerce_a
                     setlocal3
                     pushbyte 78
                     coerce_a
                     setlocal 4
                     getlocal1
                     getlocal2
                     ifne ofs0045
                     getlocal3
                     getlocal 4
                     ifne ofs003f
                     pushbyte 1
                     jump ofs0041
            ofs003f:
                     pushbyte 7
            ofs0041:
                     jump ofs0047
            ofs0045:
                     pushbyte 3
            ofs0047:
                     coerce_a
                     setlocal 5
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTernarOperator"),ProtectedNamespace("tests:TestTernarOperator"),StaticProtectedNs("tests:TestTernarOperator"),PrivateNamespace("TestTernarOperator.as$0")])
                     pushstring "e="
                     getlocal 5
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestTernarOperator"),ProtectedNamespace("tests:TestTernarOperator"),StaticProtectedNs("tests:TestTernarOperator"),PrivateNamespace("TestTernarOperator.as$0")]), 1
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
            findpropstrict Multiname("TestTernarOperator",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestTernarOperator")
            returnvoid
         end ; code
      end ; body
   end ; method
   
