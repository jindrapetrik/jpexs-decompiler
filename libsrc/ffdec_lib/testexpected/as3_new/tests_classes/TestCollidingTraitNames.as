package tests_classes
{
   public class TestCollidingTraitNames extends CollidingAttributeParent
   {
      
      method
         name ""
         returns null
         
         body
            maxstack 1
            localcount 1
            initscopedepth 4
            maxscopedepth 5
            
            code
               getlocal0
               pushscope
               returnvoid
            end ; code
         end ; body
      end ; method
      
      public var CollidingAttribute:tests_classes.CollidingAttribute;
      
      public function TestCollidingTraitNames()
      {
         method
            name "tests_classes:TestCollidingTraitNames/TestCollidingTraitNames"
            returns null
            
            body
               maxstack 1
               localcount 1
               initscopedepth 5
               maxscopedepth 6
               
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
      
      public function test() : void
      {
         trait method QName(PackageNamespace(""),"test")
            dispid 0
            method
               name "tests_classes:TestCollidingTraitNames/test"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 1
                  localcount 2
                  initscopedepth 5
                  maxscopedepth 6
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "t", 0, 15
                     pushnull
                     coerce QName(PackageNamespace("tests_classes"),"CollidingAttribute2")
                     setlocal1
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
         
         public function CollidingMethod() : void
         {
            trait method QName(PackageNamespace(""),"CollidingMethod")
               dispid 0
               method
                  name "tests_classes:TestCollidingTraitNames/CollidingMethod"
                  returns QName(PackageNamespace(""),"void")
                  
                  body
                     maxstack 1
                     localcount 2
                     initscopedepth 5
                     maxscopedepth 6
                     
                     code
                        getlocal0
                        pushscope
                        debug 1, "t", 0, 20
                        pushnull
                        coerce QName(PackageNamespace("tests_classes"),"CollidingMethod")
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
            maxscopedepth 4
            
            code
               getlocal0
               pushscope
               findpropstrict Multiname("TestCollidingTraitNames",[PackageNamespace("tests_classes")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace("tests_classes"),"CollidingAttributeParent")
               pushscope
               getlex QName(PackageNamespace("tests_classes"),"CollidingAttributeParent")
               newclass 0
               popscope
               popscope
               initproperty QName(PackageNamespace("tests_classes"),"TestCollidingTraitNames")
               returnvoid
            end ; code
         end ; body
      end ; method
      
