package tests
{
   public class TestXml2
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
      
      public function TestXml2()
      {
         method
            name "tests:TestXml2/TestXml2"
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
               name "tests:TestXml2/run"
               returns null
               
               body
                  maxstack 2
                  localcount 8
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "x1", 0, 13
                     debug 1, "x2", 1, 14
                     debug 1, "x3", 2, 20
                     debug 1, "x4", 3, 33
                     debug 1, "x5", 4, 34
                     debug 1, "x_invalid", 5, 35
                     debug 1, "a", 6, 36
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<elem name=\"aaa\" value=\"xxx&#10;\"/>"
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setlocal1
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<elem name=\"aaa\" value=\"xxx\"/>"
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setlocal2
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<elem name=\"aaa\" value=\"xxx\">\r\n                <sub title=\"yyy\">\r\n                    ampersand: &amp;\r\n                </sub>    \r\n                <sub/>            \r\n            </elem>"
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setlocal3
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<elem>\r\n                <elem>\r\n                    A\r\n                </elem>\r\n                <elem>\r\n                    B\r\n                </elem>\r\n                <elem>\r\n                    <elem>\r\n\{24} C\r\n                    </elem>\r\n                </elem>\r\n            </elem>"
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setlocal 4
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<elem attr=\"abc&#13;&#10;&#9;def\"></elem>"
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setlocal 5
                     findpropstrict QName(PackageNamespace(""),"XML")
                     pushstring "<aaa >> invalid \"\n"
                     constructprop QName(PackageNamespace(""),"XML"), 1
                     coerce QName(PackageNamespace(""),"XML")
                     setlocal 6
                     pushbyte 5
                     convert_i
                     setlocal 7
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml2"),ProtectedNamespace("tests:TestXml2"),StaticProtectedNs("tests:TestXml2"),PrivateNamespace("TestXml2.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml2"),ProtectedNamespace("tests:TestXml2"),StaticProtectedNs("tests:TestXml2"),PrivateNamespace("TestXml2.as$0")]), 1
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
            findpropstrict Multiname("TestXml2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestXml2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
