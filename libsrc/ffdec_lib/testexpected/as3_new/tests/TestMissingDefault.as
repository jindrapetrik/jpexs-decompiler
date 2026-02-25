package tests
{
   public class TestMissingDefault
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
      
      public function TestMissingDefault()
      {
         method
            name "tests:TestMissingDefault/TestMissingDefault"
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
               name "tests:TestMissingDefault/run"
               returns null
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "jj", 0, 13
                     pushbyte 1
                     convert_i
                     setlocal1
                     jump ofs002b
            ofs000f:
                     label
                     pushbyte 1
                     convert_i
                     setlocal1
                     jump ofs0056
            ofs0018:
                     label
                     pushbyte 2
                     convert_i
                     setlocal1
                     jump ofs0056
            ofs0021:
                     label
                     pushbyte 3
                     convert_i
                     setlocal1
                     label
                     jump ofs0056
            ofs002b:
                     getlocal1
                     setlocal2
                     pushbyte 1
                     getlocal2
                     ifstrictne ofs003a
                     pushbyte 0
                     jump ofs0049
            ofs003a:
                     pushbyte 2
                     getlocal2
                     ifstrictne ofs0047
                     pushbyte 1
                     jump ofs0049
            ofs0047:
                     pushbyte -1
            ofs0049:
                     kill 2
                     lookupswitch ofs0021, [ofs000f, ofs0018]
            ofs0056:
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
            findpropstrict Multiname("TestMissingDefault",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestMissingDefault")
            returnvoid
         end ; code
      end ; body
   end ; method
   
