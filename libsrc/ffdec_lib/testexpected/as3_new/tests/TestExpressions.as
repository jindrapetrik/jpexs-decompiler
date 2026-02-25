package tests
{
   public class TestExpressions
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
      
      public function TestExpressions()
      {
         method
            name "tests:TestExpressions/TestExpressions"
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
               name "tests:TestExpressions/run"
               flag NEED_ARGUMENTS
               returns null
               
               body
                  maxstack 2
                  localcount 6
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "arguments", 0, 0
                     debug 1, "arr", 1, 13
                     debug 1, "i", 2, 14
                     debug 1, "j", 3, 15
                     pushnull
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal2
                     pushbyte 5
                     convert_i
                     setlocal3
                     pushbyte 5
                     convert_i
                     setlocal 4
                     getlocal3
                     pushbyte 2
                     divide
                     convert_i
                     dup
                     setlocal3
                     convert_i
                     setlocal3
                     getlocal3
                     pushbyte 1
                     equals
                     dup
                     iftrue ofs003a
                     pop
                     getlocal3
                     pushbyte 2
                     equals
            ofs003a:
                     iffalse ofs0047
                     getlocal1
                     getlocal3
                     callpropvoid Multiname("concat",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestExpressions"),ProtectedNamespace("tests:TestExpressions"),StaticProtectedNs("tests:TestExpressions"),PrivateNamespace("TestExpressions.as$0")]), 1
                     jump ofs006a
            ofs0047:
                     getlocal3
                     pushbyte 0
                     ifne ofs005b
                     getlocal 4
                     dup
                     increment_i
                     convert_i
                     setlocal 4
                     convert_i
                     setlocal3
                     jump ofs006a
            ofs005b:
                     getlocal2
                     dup
                     setlocal 5
                     pushbyte 0
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestExpressions"),ProtectedNamespace("tests:TestExpressions"),StaticProtectedNs("tests:TestExpressions"),PrivateNamespace("TestExpressions.as$0")])
                     getlocal 5
                     call 0
                     pop
                     kill 5
            ofs006a:
                     getlocal3
                     pushbyte 0
                     equals
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
            findpropstrict Multiname("TestExpressions",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestExpressions")
            returnvoid
         end ; code
      end ; body
   end ; method
   
