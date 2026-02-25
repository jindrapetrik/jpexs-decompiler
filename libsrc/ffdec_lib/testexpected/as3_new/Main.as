package
{
   import flash.display.Sprite;
   import flash.events.Event;
   import tests_classes.TestScriptInitializer;
   
   public class Main extends Sprite
   {
      
      method
         name ""
         returns null
         
         body
            maxstack 1
            localcount 1
            initscopedepth 8
            maxscopedepth 9
            
            code
               getlocal0
               pushscope
               returnvoid
            end ; code
         end ; body
      end ; method
      
      public function Main()
      {
         method
            name "Main/Main"
            returns null
            
            body
               maxstack 3
               localcount 1
               initscopedepth 9
               maxscopedepth 10
               
               code
                  getlocal0
                  pushscope
                  getlocal0
                  constructsuper 0
                  findpropstrict QName(PackageNamespace(""),"Boolean")
                  getlex QName(PackageNamespace(""),"stage")
                  callproperty QName(PackageNamespace(""),"Boolean"), 1
                  iffalse ofs0018
                  getlocal0
                  callpropvoid QName(PrivateNamespace("Main"),"init"), 0
                  jump ofs0024
         ofs0018:
                  findpropstrict QName(PackageNamespace(""),"addEventListener")
                  getlex QName(PackageNamespace("flash.events"),"Event")
                  getproperty QName(PackageNamespace(""),"ADDED_TO_STAGE")
                  getlocal0
                  getproperty QName(PrivateNamespace("Main"),"init")
                  callpropvoid QName(PackageNamespace(""),"addEventListener"), 2
         ofs0024:
                  returnvoid
               end ; code
            end ; body
         end ; method
      }
      
      private function init(e:Event = null) : void
      {
         trait method QName(PrivateNamespace("Main"),"init")
            dispid 0
            method
               name "Main/private/init"
               flag HAS_OPTIONAL
               param QName(PackageNamespace("flash.events"),"Event")
               optional Null()
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 3
                  localcount 2
                  initscopedepth 9
                  maxscopedepth 10
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "e", 0, 0
                     findpropstrict QName(PackageNamespace(""),"removeEventListener")
                     getlex QName(PackageNamespace("flash.events"),"Event")
                     getproperty QName(PackageNamespace(""),"ADDED_TO_STAGE")
                     getlocal0
                     getproperty QName(PrivateNamespace("Main"),"init")
                     callpropvoid QName(PackageNamespace(""),"removeEventListener"), 2
                     findpropstrict QName(PackageNamespace("tests_classes"),"TestScriptInitializer")
                     constructprop QName(PackageNamespace("tests_classes"),"TestScriptInitializer"), 0
                     pop
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
         maxscopedepth 8
         
         code
            getlocal0
            pushscope
            getscopeobject 0
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace("flash.events"),"EventDispatcher")
            pushscope
            getlex QName(PackageNamespace("flash.display"),"DisplayObject")
            pushscope
            getlex QName(PackageNamespace("flash.display"),"InteractiveObject")
            pushscope
            getlex QName(PackageNamespace("flash.display"),"DisplayObjectContainer")
            pushscope
            getlex QName(PackageNamespace("flash.display"),"Sprite")
            pushscope
            getlex QName(PackageNamespace("flash.display"),"Sprite")
            newclass 0
            popscope
            popscope
            popscope
            popscope
            popscope
            popscope
            initproperty QName(PackageNamespace(""),"Main")
            returnvoid
         end ; code
      end ; body
   end ; method
   
