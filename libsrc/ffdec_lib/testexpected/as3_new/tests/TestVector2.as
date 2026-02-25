package tests
{
   public class TestVector2
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
      
      public function TestVector2()
      {
         method
            name "tests:TestVector2/TestVector2"
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
               name "tests:TestVector2/run"
               returns null
               
               body
                  maxstack 4
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     debug 1, "b", 1, 14
                     getlex QName(PackageNamespace("__AS3__.vec"),"Vector")
                     getlex QName(PackageNamespace("__AS3__.vec"),"Vector")
                     getlex QName(PackageNamespace(""),"int")
                     applytype 1
                     applytype 1
                     construct 0
                     coerce TypeName(QName(PackageNamespace("__AS3__.vec"),"Vector")<TypeName(QName(PackageNamespace("__AS3__.vec"),"Vector")<QName(PackageNamespace(""),"int")>)>)
                     setlocal1
                     getlex QName(PackageNamespace("__AS3__.vec"),"Vector")
                     getlex QName(PackageNamespace(""),"int")
                     applytype 1
                     pushbyte 3
                     construct 1
                     dup
                     pushbyte 0
                     pushbyte 10
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestVector2"),ProtectedNamespace("tests:TestVector2"),StaticProtectedNs("tests:TestVector2"),PrivateNamespace("TestVector2.as$0")])
                     dup
                     pushbyte 1
                     pushbyte 20
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestVector2"),ProtectedNamespace("tests:TestVector2"),StaticProtectedNs("tests:TestVector2"),PrivateNamespace("TestVector2.as$0")])
                     dup
                     pushbyte 2
                     pushbyte 30
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestVector2"),ProtectedNamespace("tests:TestVector2"),StaticProtectedNs("tests:TestVector2"),PrivateNamespace("TestVector2.as$0")])
                     coerce TypeName(QName(PackageNamespace("__AS3__.vec"),"Vector")<QName(PackageNamespace(""),"int")>)
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
            findpropstrict Multiname("TestVector2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestVector2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
