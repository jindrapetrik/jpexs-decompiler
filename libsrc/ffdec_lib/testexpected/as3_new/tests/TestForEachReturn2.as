package tests
{
   public class TestForEachReturn2
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
      
      public function TestForEachReturn2()
      {
         method
            name "tests:TestForEachReturn2/TestForEachReturn2"
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
               name "tests:TestForEachReturn2/run"
               returns null
               
               body
                  maxstack 2
                  localcount 7
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "item", 0, 13
                     debug 1, "obj", 1, 14
                     debug 1, "x", 2, 15
                     pushundefined
                     coerce_a
                     setlocal1
                     pushnull
                     coerce_a
                     setlocal2
                     pushbyte 5
                     coerce_a
                     setlocal3
                     getlocal3
                     pushnull
                     ifeq ofs00a4
                     newobject 0
                     coerce_a
                     setlocal2
                     pushbyte 0
                     setlocal 4
                     getlocal2
                     coerce_a
                     setlocal 5
                     jump ofs0099
            ofs0031:
                     label
                     getlocal 5
                     getlocal 4
                     nextvalue
                     coerce_a
                     setlocal1
                     jump ofs0045
            ofs003d:
                     label
                     getlocal1
                     returnvalue
            ofs0040:
                     label
                     jump ofs0099
            ofs0045:
                     getlocal1
                     pushstring "key"
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachReturn2"),ProtectedNamespace("tests:TestForEachReturn2"),StaticProtectedNs("tests:TestForEachReturn2"),PrivateNamespace("TestForEachReturn2.as$0")])
                     setlocal 6
                     pushbyte 1
                     getlocal 6
                     ifstrictne ofs005a
                     pushbyte 0
                     jump ofs0086
            ofs005a:
                     pushbyte 2
                     getlocal 6
                     ifstrictne ofs0068
                     pushbyte 1
                     jump ofs0086
            ofs0068:
                     pushbyte 3
                     getlocal 6
                     ifstrictne ofs0076
                     pushbyte 2
                     jump ofs0086
            ofs0076:
                     pushbyte 4
                     getlocal 6
                     ifstrictne ofs0084
                     pushbyte 3
                     jump ofs0086
            ofs0084:
                     pushbyte -1
            ofs0086:
                     kill 6
                     lookupswitch ofs0040, [ofs003d, ofs003d, ofs003d, ofs003d]
            ofs0099:
                     hasnext2 5, 4
                     iftrue ofs0031
                     kill 5
                     kill 4
            ofs00a4:
                     pushnull
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
            findpropstrict Multiname("TestForEachReturn2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForEachReturn2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
