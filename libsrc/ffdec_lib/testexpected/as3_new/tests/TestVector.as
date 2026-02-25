package tests
{
   public class TestVector
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
      
      public function TestVector()
      {
         method
            name "tests:TestVector/TestVector"
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
               name "tests:TestVector/run"
               returns null
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "v", 0, 13
                     debug 1, "a", 1, 16
                     getlex QName(PackageNamespace("__AS3__.vec"),"Vector")
                     getlex QName(PackageNamespace(""),"String")
                     applytype 1
                     construct 0
                     coerce TypeName(QName(PackageNamespace("__AS3__.vec"),"Vector")<QName(PackageNamespace(""),"String")>)
                     setlocal1
                     getlocal1
                     pushstring "hello"
                     callpropvoid Multiname("push",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestVector"),ProtectedNamespace("tests:TestVector"),StaticProtectedNs("tests:TestVector"),PrivateNamespace("TestVector.as$0")]), 1
                     getlocal1
                     pushbyte 0
                     pushstring "hi"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestVector"),ProtectedNamespace("tests:TestVector"),StaticProtectedNs("tests:TestVector"),PrivateNamespace("TestVector.as$0")])
                     pushbyte 5
                     convert_i
                     setlocal2
                     getlocal1
                     getlocal2
                     pushbyte 8
                     multiply
                     pushbyte 39
                     subtract
                     pushstring "hi2"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestVector"),ProtectedNamespace("tests:TestVector"),StaticProtectedNs("tests:TestVector"),PrivateNamespace("TestVector.as$0")])
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestVector"),ProtectedNamespace("tests:TestVector"),StaticProtectedNs("tests:TestVector"),PrivateNamespace("TestVector.as$0")])
                     getlocal1
                     pushbyte 0
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestVector"),ProtectedNamespace("tests:TestVector"),StaticProtectedNs("tests:TestVector"),PrivateNamespace("TestVector.as$0")])
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestVector"),ProtectedNamespace("tests:TestVector"),StaticProtectedNs("tests:TestVector"),PrivateNamespace("TestVector.as$0")]), 1
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
            findpropstrict Multiname("TestVector",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestVector")
            returnvoid
         end ; code
      end ; body
   end ; method
   
