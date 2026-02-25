package tests
{
   import flash.events.Event;
   
   public class TestOptionalParameters
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
      
      public function TestOptionalParameters()
      {
         method
            name "tests:TestOptionalParameters/TestOptionalParameters"
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
      
      public function run(p1:Event = null, p2:Number = 1, p3:Number = -1, p4:Number = -1.1, p5:Number = -1.1, p6:String = "a") : *
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestOptionalParameters/run"
               flag HAS_OPTIONAL
               param QName(PackageNamespace("flash.events"),"Event")
               param QName(PackageNamespace(""),"Number")
               param QName(PackageNamespace(""),"Number")
               param QName(PackageNamespace(""),"Number")
               param QName(PackageNamespace(""),"Number")
               param QName(PackageNamespace(""),"String")
               optional Null()
               optional Integer(1)
               optional Integer(-1)
               optional Double(-1.1)
               optional Double(-1.1)
               optional Utf8("a")
               returns null
               
               body
                  maxstack 1
                  localcount 7
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "p1", 0, 0
                     debug 1, "p2", 1, 0
                     debug 1, "p3", 2, 0
                     debug 1, "p4", 3, 0
                     debug 1, "p5", 4, 0
                     debug 1, "p6", 5, 0
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
            findpropstrict Multiname("TestOptionalParameters",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestOptionalParameters")
            returnvoid
         end ; code
      end ; body
   end ; method
   
