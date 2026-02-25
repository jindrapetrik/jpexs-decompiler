package tests
{
   public class TestForAnd
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
      
      public function TestForAnd()
      {
         method
            name "tests:TestForAnd/TestForAnd"
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
               name "tests:TestForAnd/run"
               returns null
               
               body
                  maxstack 2
                  localcount 7
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "x", 0, 13
                     debug 1, "len", 1, 14
                     debug 1, "a", 2, 15
                     debug 1, "b", 3, 16
                     debug 1, "c", 4, 17
                     debug 1, "i", 5, 18
                     pushfalse
                     convert_b
                     setlocal1
                     pushbyte 5
                     convert_i
                     setlocal2
                     pushbyte 4
                     convert_i
                     setlocal3
                     pushbyte 7
                     convert_i
                     setlocal 4
                     pushbyte 9
                     convert_i
                     setlocal 5
                     pushbyte 0
                     convert_u
                     setlocal 6
                     jump ofs0090
            ofs003e:
                     label
                     pushbyte 1
                     convert_i
                     setlocal 5
                     getlocal 5
                     pushbyte 2
                     ifne ofs006d
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForAnd"),ProtectedNamespace("tests:TestForAnd"),StaticProtectedNs("tests:TestForAnd"),PrivateNamespace("TestForAnd.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForAnd"),ProtectedNamespace("tests:TestForAnd"),StaticProtectedNs("tests:TestForAnd"),PrivateNamespace("TestForAnd.as$0")]), 1
                     getlocal 5
                     pushbyte 7
                     ifne ofs0066
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForAnd"),ProtectedNamespace("tests:TestForAnd"),StaticProtectedNs("tests:TestForAnd"),PrivateNamespace("TestForAnd.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForAnd"),ProtectedNamespace("tests:TestForAnd"),StaticProtectedNs("tests:TestForAnd"),PrivateNamespace("TestForAnd.as$0")]), 1
                     jump ofs0074
            ofs0066:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForAnd"),ProtectedNamespace("tests:TestForAnd"),StaticProtectedNs("tests:TestForAnd"),PrivateNamespace("TestForAnd.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForAnd"),ProtectedNamespace("tests:TestForAnd"),StaticProtectedNs("tests:TestForAnd"),PrivateNamespace("TestForAnd.as$0")]), 1
            ofs006d:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForAnd"),ProtectedNamespace("tests:TestForAnd"),StaticProtectedNs("tests:TestForAnd"),PrivateNamespace("TestForAnd.as$0")])
                     pushstring "D"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForAnd"),ProtectedNamespace("tests:TestForAnd"),StaticProtectedNs("tests:TestForAnd"),PrivateNamespace("TestForAnd.as$0")]), 1
            ofs0074:
                     getlocal3
                     pushbyte 4
                     greaterthan
                     dup
                     iffalse ofs0083
                     pop
                     getlocal 4
                     pushbyte 2
                     lessthan
            ofs0083:
                     dup
                     iftrue ofs008e
                     pop
                     getlocal 5
                     pushbyte 10
                     greaterthan
            ofs008e:
                     convert_b
                     setlocal1
            ofs0090:
                     getlocal 6
                     getlocal2
                     iflt ofs003e
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
            findpropstrict Multiname("TestForAnd",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForAnd")
            returnvoid
         end ; code
      end ; body
   end ; method
   
