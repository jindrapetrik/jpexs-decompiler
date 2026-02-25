package tests
{
   public class TestGetProtected
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
      
      protected var attr:int = 5;
      
      public function TestGetProtected()
      {
         method
            name "tests:TestGetProtected/TestGetProtected"
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
               name "tests:TestGetProtected/run"
               returns null
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "c", 0, 15
                     debug 1, "a", 1, 17
                     findpropstrict QName(PrivateNamespace("TestGetProtected.as$0"),"InnerClass")
                     constructprop QName(PrivateNamespace("TestGetProtected.as$0"),"InnerClass"), 0
                     coerce QName(PrivateNamespace("TestGetProtected.as$0"),"InnerClass")
                     setlocal1
                     getlocal1
                     pushbyte 2
                     setproperty Multiname("attr",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGetProtected"),ProtectedNamespace("tests:TestGetProtected"),StaticProtectedNs("tests:TestGetProtected"),PrivateNamespace("TestGetProtected.as$0")])
                     getlocal0
                     getproperty QName(ProtectedNamespace("tests:TestGetProtected"),"attr")
                     convert_i
                     setlocal2
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGetProtected"),ProtectedNamespace("tests:TestGetProtected"),StaticProtectedNs("tests:TestGetProtected"),PrivateNamespace("TestGetProtected.as$0")])
                     getlocal2
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGetProtected"),ProtectedNamespace("tests:TestGetProtected"),StaticProtectedNs("tests:TestGetProtected"),PrivateNamespace("TestGetProtected.as$0")]), 1
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
      }
   }
   
   class InnerClass
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
      
      public var attr:int = 1;
      
      public function InnerClass()
      {
         method
            name "TestGetProtected.as$0:InnerClass/InnerClass"
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
            findpropstrict Multiname("TestGetProtected",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestGetProtected")
            findpropstrict Multiname("InnerClass",[PrivateNamespace("TestGetProtected.as$0")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 1
            popscope
            initproperty QName(PrivateNamespace("TestGetProtected.as$0"),"InnerClass")
            returnvoid
         end ; code
      end ; body
   end ; method
   
