package tests
{
   public class TestDecl2
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
      
      public function TestDecl2()
      {
         method
            name "tests:TestDecl2/TestDecl2"
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
               name "tests:TestDecl2/run"
               returns null
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "k", 0, 13
                     debug 1, "i", 1, 14
                     pushbyte 0
                     convert_i
                     setlocal1
                     pushbyte 5
                     convert_i
                     setlocal2
                     getlocal2
                     pushbyte 7
                     add
                     convert_i
                     setlocal2
                     getlocal2
                     pushbyte 5
                     ifne ofs002c
                     getlocal2
                     pushbyte 8
                     ifnlt ofs002c
                     pushbyte 6
                     convert_i
                     setlocal1
            ofs002c:
                     pushbyte 7
                     convert_i
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
            findpropstrict Multiname("TestDecl2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestDecl2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
