package tests
{
   public class TestChain2
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
      
      public function TestChain2()
      {
         method
            name "tests:TestChain2/TestChain2"
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
               name "tests:TestChain2/run"
               returns null
               
               body
                  maxstack 2
                  localcount 6
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "g", 0, 13
                     debug 1, "h", 1, 14
                     debug 1, "extraLine", 2, 15
                     debug 1, "r", 3, 16
                     debug 1, "t", 4, 17
                     pushnull
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal1
                     pushfalse
                     convert_b
                     setlocal2
                     pushfalse
                     convert_b
                     setlocal3
                     pushbyte 7
                     convert_i
                     setlocal 4
                     pushbyte 0
                     convert_i
                     setlocal 5
                     getlocal0
                     callproperty QName(PrivateNamespace("tests:TestChain2"),"getInt"), 0
                     convert_i
                     setlocal 5
                     getlocal 5
                     pushbyte 1
                     add
                     getlocal1
                     getproperty QName(PackageNamespace(""),"length")
                     ifnlt ofs0047
                     inclocal_i 5
                     pushtrue
                     convert_b
                     setlocal2
            ofs0047:
                     getlocal 5
                     pushbyte 0
                     ifnge ofs0056
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChain2"),ProtectedNamespace("tests:TestChain2"),StaticProtectedNs("tests:TestChain2"),PrivateNamespace("TestChain2.as$0")])
                     pushstring "ch"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestChain2"),ProtectedNamespace("tests:TestChain2"),StaticProtectedNs("tests:TestChain2"),PrivateNamespace("TestChain2.as$0")]), 1
            ofs0056:
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
         
         private function getInt() : int
         {
            trait method QName(PrivateNamespace("tests:TestChain2"),"getInt")
               dispid 0
               method
                  name "tests:TestChain2/private/getInt"
                  returns QName(PackageNamespace(""),"int")
                  
                  body
                     maxstack 1
                     localcount 1
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        pushbyte 5
                        returnvalue
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
               findpropstrict Multiname("TestChain2",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestChain2")
               returnvoid
            end ; code
         end ; body
      end ; method
      
