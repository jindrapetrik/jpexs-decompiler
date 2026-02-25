package tests
{
   public class TestTryReturn
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
      
      public function TestTryReturn()
      {
         method
            name "tests:TestTryReturn/TestTryReturn"
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
               name "tests:TestTryReturn/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 5
                  maxscopedepth 10
                  trait slot QName(PackageInternalNs("tests"),"i")
                     slotid 1
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"b")
                     slotid 2
                     type QName(PackageNamespace(""),"Boolean")
                  end ; trait
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "+$activation", 0, 0
                     newactivation
                     dup
                     setlocal1
                     pushscope
                     getscopeobject 1
                     pushbyte 0
                     setslot 1
                     getscopeobject 1
                     pushfalse
                     convert_b
                     setslot 2
            ofs0017:
                     getscopeobject 1
                     pushbyte 0
                     setslot 1
                     getscopeobject 1
                     pushtrue
                     convert_b
                     setslot 2
                     getscopeobject 1
                     getslot 1
                     pushbyte 0
                     ifngt ofs0045
                     jump ofs003d
            ofs0031:
                     label
                     getscopeobject 1
                     getslot 2
                     iffalse ofs003d
                     pushbyte 5
                     returnvalue
            ofs003d:
                     getlocal0
                     callproperty QName(PackageNamespace(""),"testDoWhile2"), 0
                     iftrue ofs0031
            ofs0045:
                     getscopeobject 1
                     getscopeobject 1
                     getslot 1
                     increment_i
                     setslot 1
                     pushbyte 2
                     returnvalue
            ofs0051:
                     jump ofs0065
            ofs0055:
                     getlocal0
                     pushscope
                     getlocal1
                     pushscope
                     newcatch 0
                     dup
                     setlocal2
                     dup
                     pushscope
                     swap
                     setslot 1
                     popscope
                     kill 2
            ofs0065:
                     pushbyte 4
                     returnvalue
                  end ; code
                  try from ofs0017 to ofs0051 target ofs0055 type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
               end ; body
            end ; method
         }
         
         public function testDoWhile2() : Boolean
         {
            trait method QName(PackageNamespace(""),"testDoWhile2")
               dispid 0
               method
                  name "tests:TestTryReturn/testDoWhile2"
                  returns QName(PackageNamespace(""),"Boolean")
                  
                  body
                     maxstack 1
                     localcount 1
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        pushtrue
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
               findpropstrict Multiname("TestTryReturn",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestTryReturn")
               returnvoid
            end ; code
         end ; body
      end ; method
      
