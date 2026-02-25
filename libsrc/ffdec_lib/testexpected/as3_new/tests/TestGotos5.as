package tests
{
   public class TestGotos5
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
      
      public function TestGotos5()
      {
         method
            name "tests:TestGotos5/TestGotos5"
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
      
      public function run() : void
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestGotos5/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "j", 0, 13
                     debug 1, "s", 1, 14
                     debug 1, "i", 2, 15
                     pushbyte 0
                     convert_i
                     setlocal1
                     pushstring "A"
                     coerce_s
                     setlocal2
                     pushbyte 0
                     convert_i
                     setlocal3
                     jump ofs0056
            ofs0021:
                     label
                     getlocal2
                     pushstring "B"
                     ifne ofs0034
                     getlocal2
                     pushstring "C"
                     ifne ofs0034
                     jump ofs0054
            ofs0034:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos5"),ProtectedNamespace("tests:TestGotos5"),StaticProtectedNs("tests:TestGotos5"),PrivateNamespace("TestGotos5.as$0")])
                     pushstring "D"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos5"),ProtectedNamespace("tests:TestGotos5"),StaticProtectedNs("tests:TestGotos5"),PrivateNamespace("TestGotos5.as$0")]), 1
                     pushbyte 0
                     convert_i
                     setlocal1
                     jump ofs004d
            ofs0043:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos5"),ProtectedNamespace("tests:TestGotos5"),StaticProtectedNs("tests:TestGotos5"),PrivateNamespace("TestGotos5.as$0")])
                     pushstring "E"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos5"),ProtectedNamespace("tests:TestGotos5"),StaticProtectedNs("tests:TestGotos5"),PrivateNamespace("TestGotos5.as$0")]), 1
                     inclocal_i 1
            ofs004d:
                     getlocal1
                     pushbyte 29
                     iflt ofs0043
            ofs0054:
                     inclocal_i 3
            ofs0056:
                     getlocal3
                     pushbyte 10
                     iflt ofs0021
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
            findpropstrict Multiname("TestGotos5",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestGotos5")
            returnvoid
         end ; code
      end ; body
   end ; method
   
