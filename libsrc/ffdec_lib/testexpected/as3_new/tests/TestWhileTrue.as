package tests
{
   public class TestWhileTrue
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
      
      public function TestWhileTrue()
      {
         method
            name "tests:TestWhileTrue/TestWhileTrue"
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
               name "tests:TestWhileTrue/run"
               returns null
               
               body
                  maxstack 3
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     getlex QName(PackageNamespace(""),"Math")
                     getlex QName(PackageNamespace(""),"Math")
                     callproperty QName(PackageNamespace(""),"random"), 0
                     pushbyte 6
                     multiply
                     callproperty QName(PackageNamespace(""),"floor"), 1
                     convert_i
                     setlocal1
                     getlocal1
                     pushbyte 4
                     ifngt ofs0027
                     jump ofs0022
            ofs0021:
                     label
            ofs0022:
                     pushtrue
                     iftrue ofs0021
            ofs0027:
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
            findpropstrict Multiname("TestWhileTrue",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestWhileTrue")
            returnvoid
         end ; code
      end ; body
   end ; method
   
