package tests_uses
{
   public class TestClass2
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
      
      public function TestClass2()
      {
         method
            name "tests_uses:TestClass2/TestClass2"
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
      
      public function classMethod() : void
      {
         trait method QName(PackageNamespace(""),"classMethod")
            dispid 0
            method
               name "tests_uses:TestClass2/classMethod"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestClass2"),ProtectedNamespace("tests_uses:TestClass2"),StaticProtectedNs("tests_uses:TestClass2"),PrivateNamespace("TestClass2.as$0")])
                     pushstring "class2Method"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestClass2"),ProtectedNamespace("tests_uses:TestClass2"),StaticProtectedNs("tests_uses:TestClass2"),PrivateNamespace("TestClass2.as$0")]), 1
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
            findpropstrict Multiname("TestClass2",[PackageNamespace("tests_uses")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests_uses"),"TestClass2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
