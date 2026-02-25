package tests
{
   public class TestStrings
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
      
      public function TestStrings()
      {
         method
            name "tests:TestStrings/TestStrings"
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
               name "tests:TestStrings/run"
               returns null
               
               body
                  maxstack 2
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrings"),ProtectedNamespace("tests:TestStrings"),StaticProtectedNs("tests:TestStrings"),PrivateNamespace("TestStrings.as$0")])
                     pushstring "hello"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrings"),ProtectedNamespace("tests:TestStrings"),StaticProtectedNs("tests:TestStrings"),PrivateNamespace("TestStrings.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrings"),ProtectedNamespace("tests:TestStrings"),StaticProtectedNs("tests:TestStrings"),PrivateNamespace("TestStrings.as$0")])
                     pushstring "quotes:\"hello!\""
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrings"),ProtectedNamespace("tests:TestStrings"),StaticProtectedNs("tests:TestStrings"),PrivateNamespace("TestStrings.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrings"),ProtectedNamespace("tests:TestStrings"),StaticProtectedNs("tests:TestStrings"),PrivateNamespace("TestStrings.as$0")])
                     pushstring "backslash: \\ "
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrings"),ProtectedNamespace("tests:TestStrings"),StaticProtectedNs("tests:TestStrings"),PrivateNamespace("TestStrings.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrings"),ProtectedNamespace("tests:TestStrings"),StaticProtectedNs("tests:TestStrings"),PrivateNamespace("TestStrings.as$0")])
                     pushstring "single quotes: \'hello!\'"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrings"),ProtectedNamespace("tests:TestStrings"),StaticProtectedNs("tests:TestStrings"),PrivateNamespace("TestStrings.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrings"),ProtectedNamespace("tests:TestStrings"),StaticProtectedNs("tests:TestStrings"),PrivateNamespace("TestStrings.as$0")])
                     pushstring "new line \r\n hello!"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrings"),ProtectedNamespace("tests:TestStrings"),StaticProtectedNs("tests:TestStrings"),PrivateNamespace("TestStrings.as$0")]), 1
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
            findpropstrict Multiname("TestStrings",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestStrings")
            returnvoid
         end ; code
      end ; body
   end ; method
   
