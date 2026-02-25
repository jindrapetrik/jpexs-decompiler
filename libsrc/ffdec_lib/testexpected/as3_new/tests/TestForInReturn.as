package tests
{
   import flash.utils.Dictionary;
   
   public class TestForInReturn
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
      
      public function TestForInReturn()
      {
         method
            name "tests:TestForInReturn/TestForInReturn"
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
               name "tests:TestForInReturn/run"
               returns null
               
               body
                  maxstack 2
                  localcount 7
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "dic", 0, 15
                     debug 1, "item", 1, 16
                     debug 1, "_loc3_", 2, 17
                     debug 1, "_loc4_", 3, 18
                     pushnull
                     coerce QName(PackageNamespace("flash.utils"),"Dictionary")
                     setlocal1
                     pushnull
                     coerce_a
                     setlocal2
                     pushbyte 0
                     convert_i
                     setlocal3
                     getlocal1
                     coerce_a
                     setlocal 4
                     pushbyte 0
                     setlocal 5
                     getlocal 4
                     coerce_a
                     setlocal 6
                     jump ofs003c
            ofs0032:
                     label
                     getlocal 6
                     getlocal 5
                     nextname
                     coerce_a
                     setlocal2
                     getlocal2
                     returnvalue
            ofs003c:
                     hasnext2 6, 5
                     iftrue ofs0032
                     kill 6
                     kill 5
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
            findpropstrict Multiname("TestForInReturn",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForInReturn")
            returnvoid
         end ; code
      end ; body
   end ; method
   
