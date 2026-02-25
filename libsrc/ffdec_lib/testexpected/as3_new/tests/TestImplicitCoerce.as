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
                  maxstack 3
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
                     getlocal1
                     getlocal3
                     pushbyte 1
                     equals
                     convert_d
                     bitand
                     convert_b
                     dup
                     convert_b
                     iffalse ofs0037
                     pop
                     pushbyte 5
                     convert_b
            ofs0037:
                     iffalse ofs0042
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImplicitCoerce"),ProtectedNamespace("tests:TestImplicitCoerce"),StaticProtectedNs("tests:TestImplicitCoerce"),PrivateNamespace("TestImplicitCoerce.as$0")])
                     pushstring "OK"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImplicitCoerce"),ProtectedNamespace("tests:TestImplicitCoerce"),StaticProtectedNs("tests:TestImplicitCoerce"),PrivateNamespace("TestImplicitCoerce.as$0")]), 1
            ofs0042:
                     pushstring "hello: "
                     getlocal3
                     add
                     coerce_s
                     setlocal 4
                     getlocal 4
                     convert_b
                     iffalse ofs0057
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImplicitCoerce"),ProtectedNamespace("tests:TestImplicitCoerce"),StaticProtectedNs("tests:TestImplicitCoerce"),PrivateNamespace("TestImplicitCoerce.as$0")])
                     pushstring "F"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestImplicitCoerce"),ProtectedNamespace("tests:TestImplicitCoerce"),StaticProtectedNs("tests:TestImplicitCoerce"),PrivateNamespace("TestImplicitCoerce.as$0")]), 1
            ofs0057:
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
   
