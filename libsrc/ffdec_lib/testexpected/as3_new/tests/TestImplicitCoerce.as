package tests
{
   public class TestImplicitCoerce
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
      
      public function TestImplicitCoerce()
      {
         method
            name "tests:TestImplicitCoerce/TestImplicitCoerce"
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
               name "tests:TestImplicitCoerce/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 5
                  localcount 5
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "j", 0, 13
                     debug 1, "i", 1, 14
                     debug 1, "r", 2, 15
                     debug 1, "s", 3, 20
                     pushbyte 2
                     convert_i
                     setlocal1
                     pushbyte 5
                     convert_i
                     setlocal2
                     getlex QName(PackageNamespace(""),"Math")
                     callproperty QName(PackageNamespace(""),"random"), 0
                     coerce_a
                     setlocal3
                     findpropstrict QName(PackageNamespace(""),"Boolean")
                     getlocal1
                     findpropstrict QName(PackageNamespace(""),"Number")
                     getlocal3
                     pushbyte 1
                     equals
                     callproperty QName(PackageNamespace(""),"Number"), 1
                     bitand
                     callproperty QName(PackageNamespace(""),"Boolean"), 1
                     dup
                     convert_b
                     iffalse ofs0043
                     pop
                     findpropstrict QName(PackageNamespace(""),"Boolean")
                     pushbyte 5
                     callproperty QName(PackageNamespace(""),"Boolean"), 1
            ofs0043:
                     iffalse ofs004e
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImplicitCoerce"),ProtectedNamespace("tests:TestImplicitCoerce"),StaticProtectedNs("tests:TestImplicitCoerce"),PrivateNamespace("TestImplicitCoerce.as$0")])
                     pushstring "OK"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImplicitCoerce"),ProtectedNamespace("tests:TestImplicitCoerce"),StaticProtectedNs("tests:TestImplicitCoerce"),PrivateNamespace("TestImplicitCoerce.as$0")]), 1
            ofs004e:
                     pushstring "hello: "
                     getlocal3
                     add
                     coerce_s
                     setlocal 4
                     findpropstrict QName(PackageNamespace(""),"Boolean")
                     getlocal 4
                     callproperty QName(PackageNamespace(""),"Boolean"), 1
                     iffalse ofs0067
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImplicitCoerce"),ProtectedNamespace("tests:TestImplicitCoerce"),StaticProtectedNs("tests:TestImplicitCoerce"),PrivateNamespace("TestImplicitCoerce.as$0")])
                     pushstring "F"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImplicitCoerce"),ProtectedNamespace("tests:TestImplicitCoerce"),StaticProtectedNs("tests:TestImplicitCoerce"),PrivateNamespace("TestImplicitCoerce.as$0")]), 1
            ofs0067:
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
            findpropstrict Multiname("TestImplicitCoerce",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestImplicitCoerce")
            returnvoid
         end ; code
      end ; body
   end ; method
   
