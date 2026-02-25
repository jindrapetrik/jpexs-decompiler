package tests
{
   public class TestRegExp
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
      
      public function TestRegExp()
      {
         method
            name "tests:TestRegExp/TestRegExp"
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
               name "tests:TestRegExp/run"
               returns null
               
               body
                  maxstack 3
                  localcount 9
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "r", 0, 13
                     debug 1, "a1", 1, 14
                     debug 1, "a2", 2, 15
                     debug 1, "b1", 3, 16
                     debug 1, "b2", 4, 17
                     debug 1, "n1", 5, 18
                     debug 1, "n2", 6, 19
                     debug 1, "n3", 7, 20
                     getlex Multiname("NaN",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestRegExp"),ProtectedNamespace("tests:TestRegExp"),StaticProtectedNs("tests:TestRegExp"),PrivateNamespace("TestRegExp.as$0")])
                     convert_d
                     setlocal1
                     getlex QName(PackageNamespace(""),"RegExp")
                     pushstring "[a-z\\r\\n0-9\\\\]+"
                     pushstring "i"
                     construct 2
                     coerce_a
                     setlocal2
                     getlex QName(PackageNamespace(""),"RegExp")
                     pushstring "[a-z\\r\\n0-9\\\\]+"
                     pushstring "i"
                     construct 2
                     coerce_a
                     setlocal3
                     getlex QName(PackageNamespace(""),"RegExp")
                     pushstring "[0-9AB]+"
                     construct 1
                     coerce_a
                     setlocal 4
                     getlex QName(PackageNamespace(""),"RegExp")
                     pushstring "[0-9AB]+"
                     construct 1
                     coerce_a
                     setlocal 5
                     pushbyte 5
                     convert_d
                     setlocal 6
                     pushbyte 2
                     convert_d
                     setlocal 7
                     pushbyte 1
                     convert_d
                     setlocal 8
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestRegExp"),ProtectedNamespace("tests:TestRegExp"),StaticProtectedNs("tests:TestRegExp"),PrivateNamespace("TestRegExp.as$0")])
                     pushstring "not a regexp 1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestRegExp"),ProtectedNamespace("tests:TestRegExp"),StaticProtectedNs("tests:TestRegExp"),PrivateNamespace("TestRegExp.as$0")]), 1
                     getlocal 6
                     getlocal 7
                     divide
                     getlocal 8
                     divide
                     convert_d
                     setlocal1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestRegExp"),ProtectedNamespace("tests:TestRegExp"),StaticProtectedNs("tests:TestRegExp"),PrivateNamespace("TestRegExp.as$0")])
                     pushstring "not a regexp 2"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestRegExp"),ProtectedNamespace("tests:TestRegExp"),StaticProtectedNs("tests:TestRegExp"),PrivateNamespace("TestRegExp.as$0")]), 1
                     getlocal1
                     getlocal 6
                     getlocal 7
                     divide
                     divide
                     convert_d
                     setlocal1
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
            findpropstrict Multiname("TestRegExp",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestRegExp")
            returnvoid
         end ; code
      end ; body
   end ; method
   
