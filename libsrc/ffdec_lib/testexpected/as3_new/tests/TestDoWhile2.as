package tests
{
   public class TestDoWhile2
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
      
      public function TestDoWhile2()
      {
         method
            name "tests:TestDoWhile2/TestDoWhile2"
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
               name "tests:TestDoWhile2/run"
               returns null
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "k", 0, 13
                     pushbyte 5
                     convert_i
                     setlocal1
                     jump ofs0010
            ofs000f:
                     label
            ofs0010:
                     inclocal_i 1
                     getlocal1
                     pushbyte 7
                     ifne ofs0023
                     pushbyte 5
                     getlocal1
                     multiply
                     convert_i
                     setlocal1
                     jump ofs0029
            ofs0023:
                     pushbyte 5
                     getlocal1
                     subtract
                     convert_i
                     setlocal1
            ofs0029:
                     declocal_i 1
                     getlocal1
                     pushbyte 9
                     iflt ofs000f
                     pushbyte 2
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
            findpropstrict Multiname("TestDoWhile2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestDoWhile2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
