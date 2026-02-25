package tests
{
   public class TestStringCoerce
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
      
      private var a:Object = null;
      
      public function TestStringCoerce()
      {
         method
            name "tests:TestStringCoerce/TestStringCoerce"
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
               name "tests:TestStringCoerce/run"
               returns null
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "text1", 0, 15
                     debug 1, "text2", 1, 16
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestStringCoerce"),"a")
                     pushstring "test"
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStringCoerce"),ProtectedNamespace("tests:TestStringCoerce"),StaticProtectedNs("tests:TestStringCoerce"),PrivateNamespace("TestStringCoerce.as$0")])
                     coerce_s
                     setlocal1
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestStringCoerce"),"a")
                     pushstring "test"
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStringCoerce"),ProtectedNamespace("tests:TestStringCoerce"),StaticProtectedNs("tests:TestStringCoerce"),PrivateNamespace("TestStringCoerce.as$0")])
                     coerce_s
                     coerce_s
                     setlocal2
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
            findpropstrict Multiname("TestStringCoerce",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestStringCoerce")
            returnvoid
         end ; code
      end ; body
   end ; method
   
