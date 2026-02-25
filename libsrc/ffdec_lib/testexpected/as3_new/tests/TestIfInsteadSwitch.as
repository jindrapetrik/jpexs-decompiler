package tests
{
   public class TestIfInsteadSwitch
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
      
      public function TestIfInsteadSwitch()
      {
         method
            name "tests:TestIfInsteadSwitch/TestIfInsteadSwitch"
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
               name "tests:TestIfInsteadSwitch/run"
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
                     convert_i
                     setlocal1
                     getlocal1
                     pushbyte 5
                     ifngt ofs0020
                     getlocal1
                     pushbyte 0
                     ifstrictne ofs0020
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfInsteadSwitch"),ProtectedNamespace("tests:TestIfInsteadSwitch"),StaticProtectedNs("tests:TestIfInsteadSwitch"),PrivateNamespace("TestIfInsteadSwitch.as$0")])
                     pushstring "X"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIfInsteadSwitch"),ProtectedNamespace("tests:TestIfInsteadSwitch"),StaticProtectedNs("tests:TestIfInsteadSwitch"),PrivateNamespace("TestIfInsteadSwitch.as$0")]), 1
            ofs0020:
                     getlocal1
                     pushbyte 1
                     ifstrictne ofs002a
                     pushstring "A"
                     returnvalue
            ofs002a:
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
            findpropstrict Multiname("TestIfInsteadSwitch",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestIfInsteadSwitch")
            returnvoid
         end ; code
      end ; body
   end ; method
   
