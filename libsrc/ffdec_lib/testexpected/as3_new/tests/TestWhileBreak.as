package tests
{
   public class TestWhileBreak
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
      
      public function TestWhileBreak()
      {
         method
            name "tests:TestWhileBreak/TestWhileBreak"
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
               name "tests:TestWhileBreak/run"
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
                     pushbyte 0
                     convert_i
                     setlocal1
                     jump ofs0055
            ofs000f:
                     label
                     getlocal1
                     pushbyte 1
                     greaterthan
                     dup
                     iffalse ofs001e
                     pop
                     getlocal1
                     pushbyte 2
                     greaterthan
            ofs001e:
                     dup
                     iffalse ofs0028
                     pop
                     getlocal1
                     pushbyte 3
                     greaterthan
            ofs0028:
                     dup
                     iffalse ofs0032
                     pop
                     getlocal1
                     pushbyte 4
                     greaterthan
            ofs0032:
                     dup
                     iffalse ofs003c
                     pop
                     getlocal1
                     pushbyte 5
                     greaterthan
            ofs003c:
                     iffalse ofs0043
                     pushstring "A"
                     returnvalue
            ofs0043:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak"),ProtectedNamespace("tests:TestWhileBreak"),StaticProtectedNs("tests:TestWhileBreak"),PrivateNamespace("TestWhileBreak.as$0")])
                     pushstring "middle"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak"),ProtectedNamespace("tests:TestWhileBreak"),StaticProtectedNs("tests:TestWhileBreak"),PrivateNamespace("TestWhileBreak.as$0")]), 1
                     getlocal1
                     pushbyte 5
                     ifne ofs0055
                     jump ofs005c
            ofs0055:
                     getlocal1
                     pushbyte 10
                     iflt ofs000f
            ofs005c:
                     pushstring "B"
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
            findpropstrict Multiname("TestWhileBreak",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestWhileBreak")
            returnvoid
         end ; code
      end ; body
   end ; method
   
