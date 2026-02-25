package tests_classes.mypackage1
{
   import flash.utils.Dictionary;
   import tests_classes.mypackage2.*;
   
   public class TestClass3
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
      
      private var c:tests_classes.mypackage1.TestClass;
      
      public function TestClass3()
      {
         method
            name "tests_classes.mypackage1:TestClass3/TestClass3"
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
               name "tests_classes.mypackage1:TestClass3/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 3
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 18
                     findpropstrict QName(PackageNamespace("flash.utils"),"Dictionary")
                     constructprop QName(PackageNamespace("flash.utils"),"Dictionary"), 0
                     coerce QName(PackageNamespace("flash.utils"),"Dictionary")
                     setlocal1
                     getlocal1
                     pushstring "test"
                     pushbyte 5
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes.mypackage1"),PackageInternalNs("tests_classes.mypackage1"),PackageNamespace("tests_classes.mypackage2"),PrivateNamespace("tests_classes.mypackage1:TestClass3"),ProtectedNamespace("tests_classes.mypackage1:TestClass3"),StaticProtectedNs("tests_classes.mypackage1:TestClass3"),PrivateNamespace("TestClass3.as$0")])
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes.mypackage1"),PackageInternalNs("tests_classes.mypackage1"),PackageNamespace("tests_classes.mypackage2"),PrivateNamespace("tests_classes.mypackage1:TestClass3"),ProtectedNamespace("tests_classes.mypackage1:TestClass3"),StaticProtectedNs("tests_classes.mypackage1:TestClass3"),PrivateNamespace("TestClass3.as$0")])
                     getlocal1
                     pushstring "test"
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes.mypackage1"),PackageInternalNs("tests_classes.mypackage1"),PackageNamespace("tests_classes.mypackage2"),PrivateNamespace("tests_classes.mypackage1:TestClass3"),ProtectedNamespace("tests_classes.mypackage1:TestClass3"),StaticProtectedNs("tests_classes.mypackage1:TestClass3"),PrivateNamespace("TestClass3.as$0")])
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes.mypackage1"),PackageInternalNs("tests_classes.mypackage1"),PackageNamespace("tests_classes.mypackage2"),PrivateNamespace("tests_classes.mypackage1:TestClass3"),ProtectedNamespace("tests_classes.mypackage1:TestClass3"),StaticProtectedNs("tests_classes.mypackage1:TestClass3"),PrivateNamespace("TestClass3.as$0")]), 1
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
            findpropstrict Multiname("TestClass3",[PackageNamespace("tests_classes.mypackage1")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests_classes.mypackage1"),"TestClass3")
            returnvoid
         end ; code
      end ; body
   end ; method
   
