package tests_classes.mypackage3
{
   public class TestClass
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
      
      public function TestClass()
      {
         method
            name "tests_classes.mypackage3:TestClass/TestClass"
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
      
      public function testCall() : String
      {
         trait method QName(PackageNamespace(""),"testCall")
            dispid 0
            method
               name "tests_classes.mypackage3:TestClass/testCall"
               returns QName(PackageNamespace(""),"String")
               
               body
                  maxstack 2
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes.mypackage3"),PackageInternalNs("tests_classes.mypackage3"),PrivateNamespace("tests_classes.mypackage3:TestClass"),ProtectedNamespace("tests_classes.mypackage3:TestClass"),StaticProtectedNs("tests_classes.mypackage3:TestClass"),PrivateNamespace("TestClass.as$0")])
                     pushstring "pkg3hello"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes.mypackage3"),PackageInternalNs("tests_classes.mypackage3"),PrivateNamespace("tests_classes.mypackage3:TestClass"),ProtectedNamespace("tests_classes.mypackage3:TestClass"),StaticProtectedNs("tests_classes.mypackage3:TestClass"),PrivateNamespace("TestClass.as$0")]), 1
                     pushstring "pkg3hello"
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
            findpropstrict Multiname("TestClass",[PackageNamespace("tests_classes.mypackage3")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests_classes.mypackage3"),"TestClass")
            returnvoid
         end ; code
      end ; body
   end ; method
   
