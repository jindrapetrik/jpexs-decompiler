package tests
{
   public class TestWhileBreak3
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
      
      public function TestWhileBreak3()
      {
         method
            name "tests:TestWhileBreak3/TestWhileBreak3"
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
               name "tests:TestWhileBreak3/run"
               returns null
               
               body
                  maxstack 3
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "i", 0, 13
                     getlex QName(PackageNamespace(""),"Math")
                     getlex QName(PackageNamespace(""),"Math")
                     callproperty QName(PackageNamespace(""),"random"), 0
                     pushbyte 11
                     multiply
                     callproperty QName(PackageNamespace(""),"floor"), 1
                     convert_i
                     setlocal1
                     jump ofs005a
            ofs001a:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak3"),ProtectedNamespace("tests:TestWhileBreak3"),StaticProtectedNs("tests:TestWhileBreak3"),PrivateNamespace("TestWhileBreak3.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak3"),ProtectedNamespace("tests:TestWhileBreak3"),StaticProtectedNs("tests:TestWhileBreak3"),PrivateNamespace("TestWhileBreak3.as$0")]), 1
                     getlocal1
                     pushbyte 100
                     ifnlt ofs0043
                     getlocal1
                     pushbyte 0
                     ifnlt ofs0034
                     jump ofs005f
            ofs0034:
                     getlocal1
                     pushbyte 4
                     ifnlt ofs003f
                     jump ofs005f
            ofs003f:
                     jump ofs004a
            ofs0043:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak3"),ProtectedNamespace("tests:TestWhileBreak3"),StaticProtectedNs("tests:TestWhileBreak3"),PrivateNamespace("TestWhileBreak3.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak3"),ProtectedNamespace("tests:TestWhileBreak3"),StaticProtectedNs("tests:TestWhileBreak3"),PrivateNamespace("TestWhileBreak3.as$0")]), 1
            ofs004a:
                     getlocal1
                     pushbyte 4
                     ifne ofs005a
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak3"),ProtectedNamespace("tests:TestWhileBreak3"),StaticProtectedNs("tests:TestWhileBreak3"),PrivateNamespace("TestWhileBreak3.as$0")])
                     pushstring "D"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak3"),ProtectedNamespace("tests:TestWhileBreak3"),StaticProtectedNs("tests:TestWhileBreak3"),PrivateNamespace("TestWhileBreak3.as$0")]), 1
                     getlocal1
                     returnvalue
            ofs005a:
                     pushtrue
                     iftrue ofs001a
            ofs005f:
                     getlocal1
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
            findpropstrict Multiname("TestWhileBreak3",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestWhileBreak3")
            returnvoid
         end ; code
      end ; body
   end ; method
   
