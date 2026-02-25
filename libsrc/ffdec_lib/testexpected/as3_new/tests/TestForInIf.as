package tests
{
   public class TestForInIf
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
      
      public function TestForInIf()
      {
         method
            name "tests:TestForInIf/TestForInIf"
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
               name "tests:TestForInIf/run"
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
                     debug 1, "arr", 1, 14
                     debug 1, "b", 2, 15
                     pushnull
                     coerce_s
                     setlocal1
                     pushstring "a"
                     pushstring "b"
                     pushstring "c"
                     newarray 3
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal2
                     pushbyte 5
                     convert_i
                     setlocal3
                     pushbyte 0
                     setlocal 4
                     getlocal2
                     coerce_a
                     setlocal 5
                     jump ofs0054
            ofs002f:
                     label
                     getlocal 5
                     getlocal 4
                     nextname
                     coerce_s
                     setlocal1
                     getlocal3
                     pushbyte 5
                     ifne ofs004d
                     getlocal3
                     pushbyte 7
                     ifnle ofs0046
                     returnvoid
            ofs0046:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInIf"),ProtectedNamespace("tests:TestForInIf"),StaticProtectedNs("tests:TestForInIf"),PrivateNamespace("TestForInIf.as$0")])
                     pushstring "b>7"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInIf"),ProtectedNamespace("tests:TestForInIf"),StaticProtectedNs("tests:TestForInIf"),PrivateNamespace("TestForInIf.as$0")]), 1
            ofs004d:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInIf"),ProtectedNamespace("tests:TestForInIf"),StaticProtectedNs("tests:TestForInIf"),PrivateNamespace("TestForInIf.as$0")])
                     pushstring "forend"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInIf"),ProtectedNamespace("tests:TestForInIf"),StaticProtectedNs("tests:TestForInIf"),PrivateNamespace("TestForInIf.as$0")]), 1
            ofs0054:
                     hasnext2 5, 4
                     iftrue ofs002f
                     kill 5
                     kill 4
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
            findpropstrict Multiname("TestForInIf",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForInIf")
            returnvoid
         end ; code
      end ; body
   end ; method
   
