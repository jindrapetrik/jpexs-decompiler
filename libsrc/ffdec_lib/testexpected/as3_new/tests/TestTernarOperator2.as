package tests
{
   public class TestTernarOperator2
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
      
      public function TestTernarOperator2()
      {
         method
            name "tests:TestTernarOperator2/TestTernarOperator2"
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
               name "tests:TestTernarOperator2/run"
               returns null
               
               body
                  maxstack 3
                  localcount 5
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "b", 0, 13
                     debug 1, "i", 1, 14
                     debug 1, "j", 2, 15
                     debug 1, "k", 3, 16
                     pushtrue
                     convert_b
                     setlocal1
                     pushbyte 1
                     convert_i
                     setlocal2
                     getlocal1
                     iffalse ofs0027
                     getlocal2
                     jump ofs0030
            ofs0027:
                     findpropstrict QName(PackageNamespace(""),"int")
                     getlocal2
                     pushbyte 1
                     add
                     callproperty QName(PackageNamespace(""),"int"), 1
            ofs0030:
                     convert_i
                     setlocal3
                     findpropstrict QName(PackageNamespace(""),"Boolean")
                     getlocal2
                     callproperty QName(PackageNamespace(""),"Boolean"), 1
                     iffalse ofs0041
                     getlocal3
                     jump ofs004a
            ofs0041:
                     findpropstrict QName(PackageNamespace(""),"int")
                     getlocal3
                     pushbyte 1
                     add
                     callproperty QName(PackageNamespace(""),"int"), 1
            ofs004a:
                     convert_i
                     setlocal 4
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
            findpropstrict Multiname("TestTernarOperator2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestTernarOperator2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
