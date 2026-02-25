package tests_classes
{
   import tests.TestHello;
   
   public class TestScriptInitializer
   {
      
      private static var sv:int;
      
      private static var sa:int = 5;
      
      private static const sc:int;
      
      private static var sb:int;
      
      method
         name ""
         returns null
         
         body
            maxstack 4
            localcount 3
            initscopedepth 3
            maxscopedepth 4
            
            code
               getlocal0
               pushscope
               findproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sa")
               pushbyte 5
               setproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sa")
               findproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sc")
               getlex QName(PackageNamespace(""),"Math")
               getlex QName(PackageNamespace(""),"Math")
               callproperty QName(PackageNamespace(""),"random"), 0
               pushbyte 50
               multiply
               callproperty QName(PackageNamespace(""),"floor"), 1
               getlex QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sa")
               add
               getlex QName(PrivateNamespace("TestScriptInitializer.as$0"),"x")
               add
               initproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sc")
               findproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sb")
               getlex QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sa")
               pushbyte 20
               add
               setproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sb")
               getlex QName(PackageNamespace(""),"Math")
               callproperty QName(PackageNamespace(""),"random"), 0
               pushbyte 10
               multiply
               pushbyte 5
               ifnge ofs0043
               findproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sa")
               getlex QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sa")
               pushbyte 100
               add
               setproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sa")
               jump ofs004d
      ofs0043:
               findproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sa")
               getlex QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sa")
               pushshort 200
               add
               setproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sa")
      ofs004d:
               getlex QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sb")
               pushbyte 100
               ifngt ofs0062
               findproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sb")
               getlex QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sb")
               pushbyte 10
               add
               setproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sb")
               jump ofs006b
      ofs0062:
               findproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sb")
               getlex QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sb")
               pushbyte 20
               add
               setproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sb")
      ofs006b:
               pushbyte 0
               setlocal1
               pushbyte 1
               pushbyte 3
               pushbyte 5
               newarray 3
               coerce_a
               setlocal2
               jump ofs008b
      ofs007c:
               label
               findproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sv")
               getlocal2
               getlocal1
               nextvalue
               setproperty QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sv")
               findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestScriptInitializer"),ProtectedNamespace("tests_classes:TestScriptInitializer"),StaticProtectedNs("tests_classes:TestScriptInitializer"),PrivateNamespace("TestScriptInitializer.as$0")])
               getlex QName(PrivateNamespace("tests_classes:TestScriptInitializer"),"sv")
               callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestScriptInitializer"),ProtectedNamespace("tests_classes:TestScriptInitializer"),StaticProtectedNs("tests_classes:TestScriptInitializer"),PrivateNamespace("TestScriptInitializer.as$0")]), 1
      ofs008b:
               hasnext2 2, 1
               iftrue ofs007c
               kill 2
               kill 1
               returnvoid
            end ; code
         end ; body
      end ; method
      
      public function TestScriptInitializer()
      {
         method
            name "tests_classes:TestScriptInitializer/TestScriptInitializer"
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
      
      public function test() : void
      {
         trait method QName(PackageNamespace(""),"test")
            dispid 0
            method
               name "tests_classes:TestScriptInitializer/test"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 1
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "x", 0, 44
                     debug 1, "th", 1, 45
                     pushbyte 5
                     convert_i
                     setlocal1
                     findpropstrict QName(PackageNamespace("tests"),"TestHello")
                     constructprop QName(PackageNamespace("tests"),"TestHello"), 0
                     coerce QName(PackageNamespace("tests"),"TestHello")
                     setlocal2
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
      }
   }
   
   import tests.TestHello;
   
   var v:int;
   
   var x:int;
   
   var a:int = 5;
   
   const c:int;
   
   var b:int;
   
   method
      name ""
      returns null
      
      body
         maxstack 4
         localcount 3
         initscopedepth 1
         maxscopedepth 3
         
         code
            getlocal0
            pushscope
            findpropstrict Multiname("TestScriptInitializer",[PackageNamespace("tests_classes")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests_classes"),"TestScriptInitializer")
            findproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"x")
            getlex QName(PackageNamespace(""),"Math")
            callproperty QName(PackageNamespace(""),"random"), 0
            pushbyte 100
            multiply
            setproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"x")
            findproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"a")
            pushbyte 5
            setproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"a")
            getlex QName(PackageNamespace(""),"Math")
            callproperty QName(PackageNamespace(""),"random"), 0
            pushbyte 10
            multiply
            pushbyte 5
            ifnge ofs003b
            findproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"a")
            getlex QName(PrivateNamespace("TestScriptInitializer.as$0"),"a")
            pushbyte 100
            add
            setproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"a")
            jump ofs0045
   ofs003b:
            findproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"a")
            getlex QName(PrivateNamespace("TestScriptInitializer.as$0"),"a")
            pushshort 200
            add
            setproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"a")
   ofs0045:
            findproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"c")
            getlex QName(PackageNamespace(""),"Math")
            getlex QName(PackageNamespace(""),"Math")
            callproperty QName(PackageNamespace(""),"random"), 0
            pushbyte 50
            multiply
            callproperty QName(PackageNamespace(""),"floor"), 1
            getlex QName(PrivateNamespace("TestScriptInitializer.as$0"),"a")
            add
            initproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"c")
            findproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"b")
            getlex QName(PrivateNamespace("TestScriptInitializer.as$0"),"a")
            pushbyte 20
            add
            setproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"b")
            getlex QName(PrivateNamespace("TestScriptInitializer.as$0"),"b")
            pushbyte 100
            ifngt ofs0077
            findproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"b")
            getlex QName(PrivateNamespace("TestScriptInitializer.as$0"),"b")
            pushbyte 10
            add
            setproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"b")
            jump ofs0080
   ofs0077:
            findproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"b")
            getlex QName(PrivateNamespace("TestScriptInitializer.as$0"),"b")
            pushbyte 20
            add
            setproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"b")
   ofs0080:
            pushbyte 0
            setlocal1
            pushbyte 1
            pushbyte 3
            pushbyte 5
            newarray 3
            coerce_a
            setlocal2
            jump ofs00a0
   ofs0091:
            label
            findproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"v")
            getlocal2
            getlocal1
            nextvalue
            setproperty QName(PrivateNamespace("TestScriptInitializer.as$0"),"v")
            findpropstrict QName(PackageNamespace(""),"trace")
            getlex QName(PrivateNamespace("TestScriptInitializer.as$0"),"v")
            callpropvoid QName(PackageNamespace(""),"trace"), 1
   ofs00a0:
            hasnext2 2, 1
            iftrue ofs0091
            kill 2
            kill 1
            getlex QName(PackageNamespace("tests"),"TestHello")
            pop
            returnvoid
         end ; code
      end ; body
   end ; method
   
