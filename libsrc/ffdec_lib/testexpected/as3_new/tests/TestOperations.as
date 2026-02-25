package tests
{
   import flash.utils.Dictionary;
   
   public class TestOperations
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
      
      public function TestOperations()
      {
         method
            name "tests:TestOperations/TestOperations"
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
               name "tests:TestOperations/run"
               returns null
               
               body
                  maxstack 4
                  localcount 17
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "cr", 0, 15
                     debug 1, "br", 1, 16
                     debug 1, "r", 2, 17
                     debug 1, "v", 3, 18
                     debug 1, "xlr", 4, 19
                     debug 1, "sr", 5, 20
                     debug 1, "c", 6, 21
                     debug 1, "d", 7, 22
                     debug 1, "n1", 8, 23
                     debug 1, "n2", 9, 24
                     debug 1, "b1", 10, 25
                     debug 1, "b2", 11, 26
                     debug 1, "x", 12, 34
                     debug 1, "o", 13, 38
                     debug 1, "s1", 14, 39
                     debug 1, "s2", 15, 40
                     pushnull
                     coerce QName(PrivateNamespace("TestOperations.as$0"),"MyClass")
                     setlocal1
                     pushfalse
                     convert_b
                     setlocal2
                     getlex Multiname("NaN",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOperations"),ProtectedNamespace("tests:TestOperations"),StaticProtectedNs("tests:TestOperations"),PrivateNamespace("TestOperations.as$0")])
                     convert_d
                     setlocal3
                     pushundefined
                     coerce_a
                     setlocal 4
                     pushnull
                     coerce QName(PackageNamespace(""),"XMLList")
                     setlocal 5
                     pushnull
                     coerce_s
                     setlocal 6
                     findpropstrict QName(PrivateNamespace("TestOperations.as$0"),"MyClass")
                     constructprop QName(PrivateNamespace("TestOperations.as$0"),"MyClass"), 0
                     coerce QName(PrivateNamespace("TestOperations.as$0"),"MyClass")
                     setlocal 7
                     findpropstrict QName(PackageNamespace("flash.utils"),"Dictionary")
                     constructprop QName(PackageNamespace("flash.utils"),"Dictionary"), 0
                     coerce QName(PackageNamespace("flash.utils"),"Dictionary")
                     setlocal 8
                     pushbyte 2
                     convert_d
                     setlocal 9
                     pushbyte 3
                     convert_d
                     setlocal 10
                     pushtrue
                     convert_b
                     setlocal 11
                     pushfalse
                     convert_b
                     setlocal 12
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<a>\r\n            \t\t\t<b>one\r\n            \t\t\t\t<c> \r\n            \t\t\t\t\t<b>two</b> \r\n            \t\t\t\t</c> \r\n            \t\t\t</b>\r\n            \t\t\t<b>three</b>\r\n            \t\t</a>"
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setlocal 13
                     pushstring "a"
                     pushbyte 1
                     pushstring "b"
                     pushbyte 2
                     newobject 2
                     coerce QName(PackageNamespace(""),"Object")
                     setlocal 14
                     pushstring "hello"
                     coerce_s
                     setlocal 15
                     pushstring "there"
                     coerce_s
                     setlocal 16
                     getlocal 9
                     negate
                     convert_d
                     setlocal3
                     getlocal 9
                     bitnot
                     convert_d
                     setlocal3
                     getlocal 11
                     not
                     convert_b
                     setlocal2
                     getlocal 9
                     increment
                     dup
                     convert_d
                     setlocal 9
                     convert_d
                     setlocal3
                     getlocal 9
                     dup
                     increment
                     convert_d
                     setlocal 9
                     convert_d
                     setlocal3
                     getlocal 7
                     getlex QName(PrivateNamespace("TestOperations.as$0"),"MyClass")
                     astypelate
                     coerce QName(PrivateNamespace("TestOperations.as$0"),"MyClass")
                     setlocal1
                     pushstring "hello"
                     getlocal 8
                     in
                     convert_b
                     setlocal2
                     getlocal 11
                     iffalse ofs00ec
                     getlocal 9
                     jump ofs00ee
            ofs00ec:
                     getlocal 10
            ofs00ee:
                     convert_d
                     setlocal3
                     getlocal 9
                     getlocal 10
                     lshift
                     convert_d
                     setlocal3
                     getlocal 9
                     getlocal 10
                     rshift
                     convert_d
                     setlocal3
                     getlocal 9
                     getlocal 10
                     urshift
                     convert_d
                     setlocal3
                     getlocal 9
                     getlocal 10
                     bitand
                     convert_d
                     setlocal3
                     getlocal 9
                     getlocal 10
                     bitor
                     convert_d
                     setlocal3
                     getlocal 9
                     getlocal 10
                     divide
                     convert_d
                     setlocal3
                     getlocal 9
                     getlocal 10
                     modulo
                     convert_d
                     setlocal3
                     getlocal 9
                     getlocal 10
                     equals
                     convert_b
                     setlocal2
                     getlocal 9
                     getlocal 10
                     strictequals
                     convert_b
                     setlocal2
                     getlocal 9
                     getlocal 10
                     equals
                     not
                     convert_b
                     setlocal2
                     getlocal 9
                     getlocal 10
                     strictequals
                     not
                     convert_b
                     setlocal2
                     getlocal 9
                     getlocal 10
                     lessthan
                     convert_b
                     setlocal2
                     getlocal 9
                     getlocal 10
                     lessequals
                     convert_b
                     setlocal2
                     getlocal 9
                     getlocal 10
                     greaterthan
                     convert_b
                     setlocal2
                     getlocal 9
                     getlocal 10
                     greaterequals
                     convert_b
                     setlocal2
                     getlocal 11
                     dup
                     iffalse ofs0165
                     pop
                     getlocal 12
            ofs0165:
                     convert_b
                     setlocal2
                     getlocal 11
                     dup
                     iftrue ofs0171
                     pop
                     getlocal 12
            ofs0171:
                     convert_b
                     setlocal2
                     getlocal 9
                     getlocal 10
                     subtract
                     convert_d
                     setlocal3
                     getlocal 9
                     getlocal 10
                     multiply
                     convert_d
                     setlocal3
                     getlocal 9
                     getlocal 10
                     add
                     convert_d
                     setlocal3
                     getlocal 9
                     getlocal 10
                     bitxor
                     convert_d
                     setlocal3
                     getlocal 7
                     getlex QName(PrivateNamespace("TestOperations.as$0"),"MyClass")
                     instanceof
                     convert_b
                     setlocal2
                     getlocal 7
                     getlex QName(PrivateNamespace("TestOperations.as$0"),"MyClass")
                     istypelate
                     convert_b
                     setlocal2
                     getlocal 13
                     getdescendants Multiname("b",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOperations"),ProtectedNamespace("tests:TestOperations"),StaticProtectedNs("tests:TestOperations"),PrivateNamespace("TestOperations.as$0")])
                     coerce QName(PackageNamespace(""),"XMLList")
                     setlocal 5
                     getlocal 15
                     getlocal 16
                     add
                     coerce_s
                     setlocal 6
                     getlocal3
                     getlocal 9
                     bitand
                     convert_d
                     setlocal3
                     getlocal3
                     getlocal 9
                     bitor
                     convert_d
                     setlocal3
                     getlocal3
                     getlocal 9
                     divide
                     convert_d
                     setlocal3
                     getlocal3
                     getlocal 9
                     subtract
                     convert_d
                     setlocal3
                     getlocal3
                     getlocal 9
                     modulo
                     convert_d
                     setlocal3
                     getlocal3
                     getlocal 9
                     multiply
                     convert_d
                     setlocal3
                     getlocal3
                     getlocal 9
                     add
                     convert_d
                     setlocal3
                     getlocal3
                     getlocal 9
                     lshift
                     convert_d
                     setlocal3
                     getlocal3
                     getlocal 9
                     rshift
                     convert_d
                     setlocal3
                     getlocal3
                     getlocal 9
                     urshift
                     convert_d
                     setlocal3
                     getlocal3
                     getlocal 9
                     bitxor
                     convert_d
                     setlocal3
                     getlocal2
                     dup
                     iffalse ofs01f8
                     pop
                     getlocal 11
            ofs01f8:
                     convert_b
                     setlocal2
                     getlocal2
                     dup
                     iftrue ofs0203
                     pop
                     getlocal 11
            ofs0203:
                     convert_b
                     setlocal2
                     getlocal 6
                     getlocal 15
                     add
                     coerce_s
                     setlocal 6
                     getlocal 14
                     deleteproperty Multiname("a",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOperations"),ProtectedNamespace("tests:TestOperations"),StaticProtectedNs("tests:TestOperations"),PrivateNamespace("TestOperations.as$0")])
                     pop
                     pushstring "test"
                     getlocal0
                     callproperty QName(PackageNamespace(""),"f"), 0
                     add
                     pop
                     pushundefined
                     coerce_a
                     setlocal 4
                     getlocal 7
                     typeof
                     coerce_s
                     setlocal 6
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
         
         public function f() : int
         {
            trait method QName(PackageNamespace(""),"f")
               dispid 0
               method
                  name "tests:TestOperations/f"
                  returns QName(PackageNamespace(""),"int")
                  
                  body
                     maxstack 2
                     localcount 1
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOperations"),ProtectedNamespace("tests:TestOperations"),StaticProtectedNs("tests:TestOperations"),PrivateNamespace("TestOperations.as$0")])
                        pushstring "f"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOperations"),ProtectedNamespace("tests:TestOperations"),StaticProtectedNs("tests:TestOperations"),PrivateNamespace("TestOperations.as$0")]), 1
                        pushbyte 5
                        returnvalue
                     end ; code
                  end ; body
               end ; method
            }
         }
      }
      
      class MyClass
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
         
         public function MyClass()
         {
            method
               name "TestOperations.as$0:MyClass/MyClass"
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
            maxscopedepth 3
            
            code
               getlocal0
               pushscope
               findpropstrict Multiname("TestOperations",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestOperations")
               findpropstrict Multiname("MyClass",[PrivateNamespace("TestOperations.as$0")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 1
               popscope
               initproperty QName(PrivateNamespace("TestOperations.as$0"),"MyClass")
               returnvoid
            end ; code
         end ; body
      end ; method
      
