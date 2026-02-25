package tests_classes
{
   public class TestRegexpHilight
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
      
      public function TestRegexpHilight()
      {
         method
            name "tests_classes:TestRegexpHilight/TestRegexpHilight"
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
               name "tests_classes:TestRegexpHilight/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 3
                  localcount 5
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "myregexp", 0, 13
                     debug 1, "a", 1, 14
                     debug 1, "b", 2, 15
                     debug 1, "notaregexp", 3, 16
                     getlex QName(PackageNamespace(""),"RegExp")
                     pushstring "[a-z0-9_]+"
                     construct 1
                     coerce QName(PackageNamespace(""),"RegExp")
                     setlocal1
                     pushbyte 10
                     convert_d
                     setlocal2
                     pushbyte 20
                     convert_d
                     setlocal3
                     getlocal2
                     getlocal3
                     divide
                     getlocal3
                     getlocal2
                     divide
                     add
                     convert_d
                     setlocal 4
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestRegexpHilight"),ProtectedNamespace("tests_classes:TestRegexpHilight"),StaticProtectedNs("tests_classes:TestRegexpHilight"),PrivateNamespace("TestRegexpHilight.as$0")])
                     getlocal1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestRegexpHilight"),ProtectedNamespace("tests_classes:TestRegexpHilight"),StaticProtectedNs("tests_classes:TestRegexpHilight"),PrivateNamespace("TestRegexpHilight.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestRegexpHilight"),ProtectedNamespace("tests_classes:TestRegexpHilight"),StaticProtectedNs("tests_classes:TestRegexpHilight"),PrivateNamespace("TestRegexpHilight.as$0")])
                     getlocal 4
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestRegexpHilight"),ProtectedNamespace("tests_classes:TestRegexpHilight"),StaticProtectedNs("tests_classes:TestRegexpHilight"),PrivateNamespace("TestRegexpHilight.as$0")]), 1
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
            findpropstrict Multiname("TestRegexpHilight",[PackageNamespace("tests_classes")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests_classes"),"TestRegexpHilight")
            returnvoid
         end ; code
      end ; body
   end ; method
   
