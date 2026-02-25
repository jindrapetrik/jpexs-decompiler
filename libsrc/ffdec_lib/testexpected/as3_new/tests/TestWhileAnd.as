package tests
{
   public class TestWhileAnd
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
      
      public function TestWhileAnd()
      {
         method
            name "tests:TestWhileAnd/TestWhileAnd"
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
               name "tests:TestWhileAnd/run"
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
                     debug 1, "b", 1, 14
                     pushbyte 5
                     convert_i
                     setlocal1
                     pushbyte 10
                     convert_i
                     setlocal2
                     jump ofs001d
            ofs0018:
                     label
                     inclocal_i 1
                     declocal_i 2
            ofs001d:
                     getlocal1
                     pushbyte 10
                     lessthan
                     dup
                     iffalse ofs002b
                     pop
                     getlocal2
                     pushbyte 1
                     greaterthan
            ofs002b:
                     iftrue ofs0018
                     pushbyte 7
                     convert_i
                     setlocal1
                     pushbyte 9
                     convert_i
                     setlocal2
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
            findpropstrict Multiname("TestWhileAnd",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestWhileAnd")
            returnvoid
         end ; code
      end ; body
   end ; method
   
