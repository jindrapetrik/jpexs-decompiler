package tests
{
   public class TestIfElse
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
      
      public function TestIfElse()
      {
         method
            name "tests:TestIfElse/TestIfElse"
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
               name "tests:TestIfElse/run"
               returns null
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     pushbyte 5
                     coerce_a
                     setlocal1
                     getlocal1
                     pushbyte 7
                     ifne ofs001d
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfElse"),ProtectedNamespace("tests:TestIfElse"),StaticProtectedNs("tests:TestIfElse"),PrivateNamespace("TestIfElse.as$0")])
                     pushstring "onTrue"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfElse"),ProtectedNamespace("tests:TestIfElse"),StaticProtectedNs("tests:TestIfElse"),PrivateNamespace("TestIfElse.as$0")]), 1
                     jump ofs0024
            ofs001d:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfElse"),ProtectedNamespace("tests:TestIfElse"),StaticProtectedNs("tests:TestIfElse"),PrivateNamespace("TestIfElse.as$0")])
                     pushstring "onFalse"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfElse"),ProtectedNamespace("tests:TestIfElse"),StaticProtectedNs("tests:TestIfElse"),PrivateNamespace("TestIfElse.as$0")]), 1
            ofs0024:
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
            findpropstrict Multiname("TestIfElse",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestIfElse")
            returnvoid
         end ; code
      end ; body
   end ; method
   
