package tests
{
   public class TestMultipleCondition
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
      
      public function TestMultipleCondition()
      {
         method
            name "tests:TestMultipleCondition/TestMultipleCondition"
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
               name "tests:TestMultipleCondition/run"
               returns null
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     debug 1, "b", 1, 14
                     debug 1, "c", 2, 15
                     pushbyte 5
                     coerce_a
                     setlocal1
                     pushbyte 8
                     coerce_a
                     setlocal2
                     pushbyte 9
                     coerce_a
                     setlocal3
                     getlocal1
                     pushbyte 4
                     lessequals
                     dup
                     iftrue ofs002b
                     pop
                     getlocal2
                     pushbyte 8
                     lessequals
            ofs002b:
                     dup
                     iffalse ofs0035
                     pop
                     getlocal3
                     pushbyte 7
                     equals
            ofs0035:
                     iffalse ofs0044
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestMultipleCondition"),ProtectedNamespace("tests:TestMultipleCondition"),StaticProtectedNs("tests:TestMultipleCondition"),PrivateNamespace("TestMultipleCondition.as$0")])
                     pushstring "onTrue"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestMultipleCondition"),ProtectedNamespace("tests:TestMultipleCondition"),StaticProtectedNs("tests:TestMultipleCondition"),PrivateNamespace("TestMultipleCondition.as$0")]), 1
                     jump ofs004b
            ofs0044:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestMultipleCondition"),ProtectedNamespace("tests:TestMultipleCondition"),StaticProtectedNs("tests:TestMultipleCondition"),PrivateNamespace("TestMultipleCondition.as$0")])
                     pushstring "onFalse"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestMultipleCondition"),ProtectedNamespace("tests:TestMultipleCondition"),StaticProtectedNs("tests:TestMultipleCondition"),PrivateNamespace("TestMultipleCondition.as$0")]), 1
            ofs004b:
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
            findpropstrict Multiname("TestMultipleCondition",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestMultipleCondition")
            returnvoid
         end ; code
      end ; body
   end ; method
   
