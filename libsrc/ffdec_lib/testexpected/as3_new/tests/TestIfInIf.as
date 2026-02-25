package tests
{
   public class TestIfInIf
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
      
      public function TestIfInIf()
      {
         method
            name "tests:TestIfInIf/TestIfInIf"
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
      
      public function run() : int
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestIfInIf/run"
               returns QName(PackageNamespace(""),"int")
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "k", 0, 13
                     pushbyte 5
                     convert_i
                     setlocal1
                     getlocal1
                     pushbyte 5
                     greaterthan
                     dup
                     iffalse ofs0019
                     pop
                     getlocal1
                     pushbyte 20
                     lessthan
            ofs0019:
                     iffalse ofs0032
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfInIf"),ProtectedNamespace("tests:TestIfInIf"),StaticProtectedNs("tests:TestIfInIf"),PrivateNamespace("TestIfInIf.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfInIf"),ProtectedNamespace("tests:TestIfInIf"),StaticProtectedNs("tests:TestIfInIf"),PrivateNamespace("TestIfInIf.as$0")]), 1
                     getlocal1
                     pushbyte 4
                     ifnlt ofs002e
                     pushbyte 1
                     returnvalue
            ofs002e:
                     jump ofs0055
            ofs0032:
                     getlocal1
                     pushbyte 4
                     greaterthan
                     dup
                     iffalse ofs0040
                     pop
                     getlocal1
                     pushbyte 10
                     lessthan
            ofs0040:
                     iffalse ofs0055
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfInIf"),ProtectedNamespace("tests:TestIfInIf"),StaticProtectedNs("tests:TestIfInIf"),PrivateNamespace("TestIfInIf.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfInIf"),ProtectedNamespace("tests:TestIfInIf"),StaticProtectedNs("tests:TestIfInIf"),PrivateNamespace("TestIfInIf.as$0")]), 1
                     getlocal1
                     pushbyte 7
                     ifnlt ofs0055
                     pushbyte 2
                     returnvalue
            ofs0055:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfInIf"),ProtectedNamespace("tests:TestIfInIf"),StaticProtectedNs("tests:TestIfInIf"),PrivateNamespace("TestIfInIf.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfInIf"),ProtectedNamespace("tests:TestIfInIf"),StaticProtectedNs("tests:TestIfInIf"),PrivateNamespace("TestIfInIf.as$0")]), 1
                     pushbyte 7
                     returnvalue
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
            findpropstrict Multiname("TestIfInIf",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestIfInIf")
            returnvoid
         end ; code
      end ; body
   end ; method
   
