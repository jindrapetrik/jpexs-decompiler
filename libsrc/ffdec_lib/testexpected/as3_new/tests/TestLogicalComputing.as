package tests
{
   public class TestLogicalComputing
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
      
      public function TestLogicalComputing()
      {
         method
            name "tests:TestLogicalComputing/TestLogicalComputing"
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
               name "tests:TestLogicalComputing/run"
               returns null
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "b", 0, 13
                     debug 1, "i", 1, 14
                     debug 1, "j", 2, 15
                     pushfalse
                     convert_b
                     setlocal1
                     pushbyte 5
                     coerce_a
                     setlocal2
                     pushbyte 7
                     coerce_a
                     setlocal3
                     getlocal2
                     getlocal3
                     ifngt ofs0029
                     pushbyte 9
                     coerce_a
                     setlocal3
                     pushtrue
                     convert_b
                     setlocal1
            ofs0029:
                     getlocal2
                     pushbyte 0
                     equals
                     dup
                     iftrue ofs0037
                     pop
                     getlocal2
                     pushbyte 1
                     equals
            ofs0037:
                     dup
                     iffalse ofs0041
                     pop
                     getlocal3
                     pushbyte 0
                     equals
            ofs0041:
                     convert_b
                     setlocal1
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
            findpropstrict Multiname("TestLogicalComputing",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestLogicalComputing")
            returnvoid
         end ; code
      end ; body
   end ; method
   
