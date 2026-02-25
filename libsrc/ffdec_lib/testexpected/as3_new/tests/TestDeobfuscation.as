package tests
{
   public class TestDeobfuscation
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
      
      public function TestDeobfuscation()
      {
         method
            name "tests:TestDeobfuscation/TestDeobfuscation"
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
               name "tests:TestDeobfuscation/run"
               returns null
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "r", 0, 13
                     debug 1, "t", 1, 14
                     debug 1, "f", 2, 15
                     getlex QName(PackageNamespace(""),"Math")
                     callproperty QName(PackageNamespace(""),"random"), 0
                     convert_i
                     setlocal1
                     pushtrue
                     convert_b
                     setlocal2
                     pushfalse
                     convert_b
                     setlocal3
                     getlocal1
                     pushbyte 5
                     greaterthan
                     dup
                     iffalse ofs0029
                     pop
                     getlocal2
            ofs0029:
                     iffalse ofs0034
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")]), 1
            ofs0034:
                     getlocal1
                     pushbyte 10
                     greaterthan
                     dup
                     iftrue ofs003f
                     pop
                     getlocal3
            ofs003f:
                     iffalse ofs004a
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")]), 1
            ofs004a:
                     getlocal2
                     dup
                     iffalse ofs0055
                     pop
                     getlocal1
                     pushbyte 15
                     greaterthan
            ofs0055:
                     iffalse ofs0060
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")]), 1
            ofs0060:
                     getlocal3
                     dup
                     iftrue ofs006b
                     pop
                     getlocal1
                     pushbyte 20
                     greaterthan
            ofs006b:
                     iffalse ofs0076
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")])
                     pushstring "D"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")]), 1
            ofs0076:
                     getlocal3
                     iffalse ofs0082
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")])
                     pushstring "trash1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")]), 1
            ofs0082:
                     getlocal2
                     not
                     iffalse ofs008f
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")])
                     pushstring "trash2"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeobfuscation"),ProtectedNamespace("tests:TestDeobfuscation"),StaticProtectedNs("tests:TestDeobfuscation"),PrivateNamespace("TestDeobfuscation.as$0")]), 1
            ofs008f:
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
            findpropstrict Multiname("TestDeobfuscation",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestDeobfuscation")
            returnvoid
         end ; code
      end ; body
   end ; method
   
