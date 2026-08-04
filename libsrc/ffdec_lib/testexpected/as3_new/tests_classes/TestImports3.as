package tests_classes
{
   import flash.utils.getTimer;
   
   public class TestImports3
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
      
      public function TestImports3()
      {
         method
            name "tests_classes:TestImports3/TestImports3"
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
      
      public static function getTimer() : Number
      {
         trait method QName(PackageNamespace(""),"getTimer")
            flag FINAL
            dispid 3
            method
               name "tests_classes:TestImports3/getTimer"
               returns QName(PackageNamespace(""),"Number")
               
               body
                  maxstack 1
                  localcount 1
                  initscopedepth 3
                  maxscopedepth 4
                  
                  code
                     getlocal0
                     pushscope
                     pushbyte 0
                     returnvalue
                  end ; code
               end ; body
            end ; method
         }
         
         public function run() : *
         {
            trait method QName(PackageNamespace(""),"run")
               dispid 0
               method
                  name "tests_classes:TestImports3/run"
                  returns null
                  
                  body
                     maxstack 1
                     localcount 1
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        findpropstrict QName(PackageNamespace("flash.utils"),"getTimer")
                        callproperty QName(PackageNamespace("flash.utils"),"getTimer"), 0
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
               findpropstrict Multiname("TestImports3",[PackageNamespace("tests_classes")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests_classes"),"TestImports3")
               returnvoid
            end ; code
         end ; body
      end ; method
      
