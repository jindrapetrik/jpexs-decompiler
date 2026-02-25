package tests
{
   public class TestWhileDoWhile
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
      
      public function TestWhileDoWhile()
      {
         method
            name "tests:TestWhileDoWhile/TestWhileDoWhile"
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
               name "tests:TestWhileDoWhile/run"
               returns null
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "i", 0, 14
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileDoWhile"),ProtectedNamespace("tests:TestWhileDoWhile"),StaticProtectedNs("tests:TestWhileDoWhile"),PrivateNamespace("TestWhileDoWhile.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileDoWhile"),ProtectedNamespace("tests:TestWhileDoWhile"),StaticProtectedNs("tests:TestWhileDoWhile"),PrivateNamespace("TestWhileDoWhile.as$0")]), 1
                     pushbyte 0
                     convert_i
                     setlocal1
                     jump ofs0033
            ofs0016:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileDoWhile"),ProtectedNamespace("tests:TestWhileDoWhile"),StaticProtectedNs("tests:TestWhileDoWhile"),PrivateNamespace("TestWhileDoWhile.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileDoWhile"),ProtectedNamespace("tests:TestWhileDoWhile"),StaticProtectedNs("tests:TestWhileDoWhile"),PrivateNamespace("TestWhileDoWhile.as$0")]), 1
                     jump ofs0023
            ofs0022:
                     label
            ofs0023:
                     inclocal_i 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileDoWhile"),ProtectedNamespace("tests:TestWhileDoWhile"),StaticProtectedNs("tests:TestWhileDoWhile"),PrivateNamespace("TestWhileDoWhile.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileDoWhile"),ProtectedNamespace("tests:TestWhileDoWhile"),StaticProtectedNs("tests:TestWhileDoWhile"),PrivateNamespace("TestWhileDoWhile.as$0")]), 1
                     getlocal1
                     pushbyte 5
                     iflt ofs0022
            ofs0033:
                     getlocal1
                     pushbyte 10
                     iflt ofs0016
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileDoWhile"),ProtectedNamespace("tests:TestWhileDoWhile"),StaticProtectedNs("tests:TestWhileDoWhile"),PrivateNamespace("TestWhileDoWhile.as$0")])
                     pushstring "E"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileDoWhile"),ProtectedNamespace("tests:TestWhileDoWhile"),StaticProtectedNs("tests:TestWhileDoWhile"),PrivateNamespace("TestWhileDoWhile.as$0")]), 1
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
            findpropstrict Multiname("TestWhileDoWhile",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestWhileDoWhile")
            returnvoid
         end ; code
      end ; body
   end ; method
   
