package tests
{
   import flash.utils.Dictionary;
   import flash.utils.getTimer;
   import tests_classes.TestConvertParent;
   
   public class TestConvert extends TestConvertParent
   {
      
      public static var TEST:String = "Hello";
      
      method
         name ""
         returns null
         
         body
            maxstack 2
            localcount 1
            initscopedepth 4
            maxscopedepth 5
            
            code
               getlocal0
               pushscope
               findproperty QName(PackageNamespace(""),"TEST")
               pushstring "Hello"
               setproperty QName(PackageNamespace(""),"TEST")
               returnvoid
            end ; code
         end ; body
      end ; method
      
      private var n:int = 1;
      
      private var ns:String = "b";
      
      public var TEST:int = 5;
      
      private var f:Function = null;
      
      public function TestConvert()
      {
         method
            name "tests:TestConvert/TestConvert"
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
      
      public function run() : void
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestConvert/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 6
                  localcount 16
                  initscopedepth 5
                  maxscopedepth 6
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "s", 0, 27
                     debug 1, "i", 1, 28
                     debug 1, "a", 2, 29
                     debug 1, "dict", 3, 30
                     debug 1, "j", 4, 33
                     debug 1, "o", 5, 47
                     debug 1, "v", 6, 49
                     debug 1, "x", 7, 65
                     debug 1, "xlist", 8, 68
                     debug 1, "lc", 9, 74
                     debug 1, "f", 10, 77
                     debug 1, "d", 11, 95
                     debug 1, "a2", 12, 106
                     debug 1, "s2", 13, 107
                     debug 1, "i2", 14, 108
                     pushnull
                     coerce_s
                     setlocal1
                     pushbyte 0
                     convert_i
                     setlocal2
                     pushundefined
                     coerce_a
                     setlocal3
                     findpropstrict QName(PackageNamespace("flash.utils"),"Dictionary")
                     constructprop QName(PackageNamespace("flash.utils"),"Dictionary"), 0
                     coerce QName(PackageNamespace("flash.utils"),"Dictionary")
                     setlocal 4
                     pushstring "a"
                     coerce_s
                     setlocal1
                     getlocal1
                     convert_i
                     convert_i
                     setlocal2
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestConvert"),"n")
                     convert_i
                     setlocal 5
                     getlocal 5
                     coerce_s
                     coerce_s
                     setlocal1
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestConvert"),"ns")
                     coerce_s
                     setlocal1
                     getlocal2
                     pushbyte 4
                     ifne ofs0085
                     pushstring ""
                     jump ofs0086
            ofs0085:
                     getlocal2
            ofs0086:
                     coerce_s
                     coerce_s
                     setlocal1
                     getlocal2
                     pushbyte 4
                     ifne ofs0096
                     pushstring ""
                     jump ofs0098
            ofs0096:
                     getlocal2
                     coerce_s
            ofs0098:
                     coerce_s
                     setlocal1
                     getlex QName(PackageNamespace("tests"),"TestConvert")
                     getproperty QName(PackageNamespace(""),"TEST")
                     coerce_s
                     setlocal1
                     getlocal0
                     getproperty QName(PackageNamespace(""),"TEST")
                     convert_i
                     setlocal2
                     pushstring "4"
                     convert_d
                     pushbyte 5
                     multiply
                     convert_i
                     setlocal2
                     getlocal3
                     pushbyte 6
                     multiply
                     convert_i
                     setlocal2
                     getlocal3
                     convert_i
                     setlocal2
                     pushstring "0"
                     pushstring "A"
                     pushstring "1"
                     pushstring "B"
                     pushstring "2"
                     pushstring "C"
                     newobject 3
                     coerce QName(PackageNamespace(""),"Object")
                     setlocal 6
                     getlocal1
                     pushbyte 10
                     callproperty QName(Namespace("http://adobe.com/AS3/2006/builtin"),"charAt"), 1
                     convert_i
                     convert_i
                     setlocal2
                     getlex QName(PackageNamespace("__AS3__.vec"),"Vector")
                     getlex QName(PackageNamespace(""),"String")
                     applytype 1
                     construct 0
                     coerce TypeName(QName(PackageNamespace("__AS3__.vec"),"Vector")<QName(PackageNamespace(""),"String")>)
                     setlocal 7
                     getlocal 7
                     pushstring "A"
                     callpropvoid Multiname("push",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")]), 1
                     getlocal 7
                     pushstring "B"
                     callpropvoid Multiname("push",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")]), 1
                     getlocal 7
                     pushbyte 0
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     convert_i
                     convert_i
                     setlocal2
                     getlocal 7
                     pushbyte 1
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     coerce_s
                     setlocal1
                     getlocal 7
                     pushstring "x"
                     callproperty Multiname("join",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")]), 1
                     coerce_s
                     setlocal1
                     getlocal 7
                     pushstring "x"
                     callproperty Multiname("join",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")]), 1
                     convert_i
                     convert_i
                     setlocal2
                     getlex QName(ProtectedNamespace("tests_classes:TestConvertParent"),"prot")
                     convert_i
                     setlocal2
                     getlex QName(ProtectedNamespace("tests_classes:TestConvertParent"),"prot")
                     coerce_s
                     coerce_s
                     setlocal1
                     getlex QName(StaticProtectedNs("tests_classes:TestConvertParent"),"sprot")
                     convert_i
                     setlocal2
                     getlex QName(StaticProtectedNs("tests_classes:TestConvertParent"),"sprot")
                     coerce_s
                     coerce_s
                     setlocal1
                     findpropstrict QName(PackageNamespace("flash.utils"),"getTimer")
                     callproperty QName(PackageNamespace("flash.utils"),"getTimer"), 0
                     coerce_s
                     coerce_s
                     setlocal1
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<list>\r\n\t \t\t\t\t\t\t<item id=\"1\">1</item>\r\n\t\t\t\t\t\t\t<item id=\"2\">2</item>\r\n\t\t\t\t\t\t\t<item id=\"3\">3</item>\r\n\t\t\t\t\t\t</list>"
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setlocal 8
                     getlocal 8
                     coerce_s
                     setlocal1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     pushstring "a"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")]), 1
                     getlocal 8
                     getproperty Multiname("item",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     coerce QName(PackageNamespace(""),"XMLList")
                     setlocal 9
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     pushstring "b"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")]), 1
                     getlocal 9
                     getlocal2
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     getproperty MultinameA("id",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     convert_i
                     convert_i
                     setlocal2
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     pushstring "c"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")]), 1
                     getlocal 8
                     getproperty Multiname("item",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     getlocal2
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     getproperty MultinameA("id",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     convert_i
                     convert_i
                     setlocal2
                     getlocal 4
                     getlocal 8
                     getproperty Multiname("item",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     getlocal2
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     getproperty MultinameA("id",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     coerce_s
                     pushstring "Hello"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     findpropstrict QName(PrivateNamespace("TestConvert.as$0"),"LocalClass")
                     constructprop QName(PrivateNamespace("TestConvert.as$0"),"LocalClass"), 0
                     coerce QName(PrivateNamespace("TestConvert.as$0"),"LocalClass")
                     setlocal 10
                     getlocal 10
                     getproperty Multiname("attr",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     convert_i
                     setlocal2
                     getlocal 10
                     getproperty Multiname("attr",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     coerce_s
                     coerce_s
                     setlocal1
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestConvert"),"f")
                     coerce QName(PackageNamespace(""),"Function")
                     setlocal 11
                     getlocal 11
                     convert_b
                     iffalse ofs01a5
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     pushstring "OK"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")]), 1
            ofs01a5:
                     getlocal2
                     convert_b
                     iffalse ofs01b1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     getlocal2
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")]), 1
            ofs01b1:
                     getlocal1
                     convert_b
                     iffalse ofs01bd
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     getlocal1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")]), 1
            ofs01bd:
                     getlocal 6
                     convert_b
                     iffalse ofs01cb
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     pushstring "obj"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")]), 1
            ofs01cb:
                     getlocal 9
                     coerce_s
                     setlocal1
                     pushbyte 0
                     convert_d
                     setlocal 12
                     pushbyte 1
                     convert_d
                     setlocal 12
                     pushdouble 1.5
                     convert_d
                     setlocal 12
                     pushbyte 1
                     convert_i
                     setlocal2
                     pushdouble 1.5
                     convert_i
                     setlocal2
                     getlocal 6
                     getlocal 12
                     pushbyte 5
                     multiply
                     convert_i
                     pushbyte 1
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),StaticProtectedNs("tests_classes:TestConvertParent"),PrivateNamespace("tests:TestConvert"),ProtectedNamespace("tests:TestConvert"),StaticProtectedNs("tests:TestConvert"),PrivateNamespace("TestConvert.as$0")])
                     getlocal0
                     pushdouble 1.5
                     setproperty QName(PrivateNamespace("tests:TestConvert"),"n")
                     pushdouble 1.5
                     findproperty QName(ProtectedNamespace("tests_classes:TestConvertParent"),"prot")
                     swap
                     setsuper QName(ProtectedNamespace("tests_classes:TestConvertParent"),"prot")
                     getlocal1
                     convert_i
                     findproperty QName(ProtectedNamespace("tests_classes:TestConvertParent"),"prot")
                     swap
                     setsuper QName(ProtectedNamespace("tests_classes:TestConvertParent"),"prot")
                     findpropstrict QName(ProtectedNamespace("tests_classes:TestConvertParent"),"prot")
                     getsuper QName(ProtectedNamespace("tests_classes:TestConvertParent"),"prot")
                     convert_i
                     setlocal2
                     findpropstrict QName(ProtectedNamespace("tests_classes:TestConvertParent"),"prot")
                     getsuper QName(ProtectedNamespace("tests_classes:TestConvertParent"),"prot")
                     coerce_s
                     coerce_s
                     setlocal1
                     pushstring "5"
                     coerce_a
                     setlocal 13
                     pushstring "s"
                     coerce_s
                     setlocal 14
                     getlocal 13
                     convert_i
                     setlocal 15
                     getlocal 13
                     convert_d
                     coerce_a
                     setlocal 13
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
      }
   }
   
   class LocalClass
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
      
      public var attr:int = 5;
      
      public function LocalClass()
      {
         method
            name "TestConvert.as$0:LocalClass/LocalClass"
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
            findpropstrict Multiname("TestConvert",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace("tests_classes"),"TestConvertParent")
            pushscope
            getlex QName(PackageNamespace("tests_classes"),"TestConvertParent")
            newclass 0
            popscope
            popscope
            initproperty QName(PackageNamespace("tests"),"TestConvert")
            findpropstrict Multiname("LocalClass",[PrivateNamespace("TestConvert.as$0")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 1
            popscope
            initproperty QName(PrivateNamespace("TestConvert.as$0"),"LocalClass")
            returnvoid
         end ; code
      end ; body
   end ; method
   
