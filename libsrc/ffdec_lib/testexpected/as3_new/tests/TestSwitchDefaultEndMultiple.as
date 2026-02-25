package tests
{
   public class TestSwitchDefaultEndMultiple
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
      
      public function TestSwitchDefaultEndMultiple()
      {
         method
            name "tests:TestSwitchDefaultEndMultiple/TestSwitchDefaultEndMultiple"
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
               name "tests:TestSwitchDefaultEndMultiple/run"
               returns null
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     pushstring "X"
                     coerce_a
                     setlocal1
                     jump ofs002d
            ofs000f:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefaultEndMultiple"),ProtectedNamespace("tests:TestSwitchDefaultEndMultiple"),StaticProtectedNs("tests:TestSwitchDefaultEndMultiple"),PrivateNamespace("TestSwitchDefaultEndMultiple.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefaultEndMultiple"),ProtectedNamespace("tests:TestSwitchDefaultEndMultiple"),StaticProtectedNs("tests:TestSwitchDefaultEndMultiple"),PrivateNamespace("TestSwitchDefaultEndMultiple.as$0")]), 1
                     jump ofs0078
            ofs001b:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefaultEndMultiple"),ProtectedNamespace("tests:TestSwitchDefaultEndMultiple"),StaticProtectedNs("tests:TestSwitchDefaultEndMultiple"),PrivateNamespace("TestSwitchDefaultEndMultiple.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefaultEndMultiple"),ProtectedNamespace("tests:TestSwitchDefaultEndMultiple"),StaticProtectedNs("tests:TestSwitchDefaultEndMultiple"),PrivateNamespace("TestSwitchDefaultEndMultiple.as$0")]), 1
                     jump ofs0078
            ofs0027:
                     label
            ofs0028:
                     label
                     jump ofs0078
            ofs002d:
                     getlocal1
                     setlocal2
                     pushstring "A"
                     getlocal2
                     ifstrictne ofs003c
                     pushbyte 0
                     jump ofs0065
            ofs003c:
                     pushstring "B"
                     getlocal2
                     ifstrictne ofs0049
                     pushbyte 1
                     jump ofs0065
            ofs0049:
                     pushstring "C"
                     getlocal2
                     ifstrictne ofs0056
                     pushbyte 2
                     jump ofs0065
            ofs0056:
                     pushstring "D"
                     getlocal2
                     ifstrictne ofs0063
                     pushbyte 3
                     jump ofs0065
            ofs0063:
                     pushbyte -1
            ofs0065:
                     kill 2
                     lookupswitch ofs0028, [ofs000f, ofs001b, ofs0027, ofs0027]
            ofs0078:
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
            findpropstrict Multiname("TestSwitchDefaultEndMultiple",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestSwitchDefaultEndMultiple")
            returnvoid
         end ; code
      end ; body
   end ; method
   
