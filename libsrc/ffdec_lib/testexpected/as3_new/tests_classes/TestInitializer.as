package tests_classes
{
   public class TestInitializer
   {
      
      public static var s_alpha:RegExp;
      
      public static var s_numbers:RegExp;
      
      public static var s_regs:Array;
      
      method
         name ""
         returns null
         
         body
            maxstack 3
            localcount 1
            initscopedepth 3
            maxscopedepth 4
            
            code
               getlocal0
               pushscope
               findproperty QName(PackageNamespace(""),"s_alpha")
               getlex QName(PackageNamespace(""),"RegExp")
               pushstring "[a-z]+"
               construct 1
               setproperty QName(PackageNamespace(""),"s_alpha")
               findproperty QName(PackageNamespace(""),"s_numbers")
               getlex QName(PackageNamespace(""),"RegExp")
               pushstring "[0-9]+"
               construct 1
               setproperty QName(PackageNamespace(""),"s_numbers")
               findproperty QName(PackageNamespace(""),"s_regs")
               getlex QName(PackageNamespace(""),"s_alpha")
               getlex QName(PackageNamespace(""),"s_numbers")
               newarray 2
               setproperty QName(PackageNamespace(""),"s_regs")
               returnvoid
            end ; code
         end ; body
      end ; method
      
      public var i_email:RegExp;
      
      public var i_link:RegExp;
      
      public var i_regs:Array;
      
      public var i_a:int = 1;
      
      public var i_b:int;
      
      public const i_c:int;
      
      public var i_d:int;
      
      public function TestInitializer(p:int)
      {
         method
            name "tests_classes:TestInitializer/TestInitializer"
            param QName(PackageNamespace(""),"int")
            returns null
            
            body
               maxstack 3
               localcount 2
               initscopedepth 4
               maxscopedepth 5
               
               code
                  getlocal0
                  pushscope
                  getlocal0
                  getlex QName(PackageNamespace(""),"RegExp")
                  pushstring ".*@.*\\..*"
                  construct 1
                  setproperty QName(PackageNamespace(""),"i_email")
                  getlocal0
                  getlex QName(PackageNamespace(""),"RegExp")
                  pushstring "<a href=\".*\">"
                  construct 1
                  setproperty QName(PackageNamespace(""),"i_link")
                  getlocal0
                  getlocal0
                  getproperty QName(PackageNamespace(""),"i_email")
                  getlocal0
                  getproperty QName(PackageNamespace(""),"i_link")
                  newarray 2
                  setproperty QName(PackageNamespace(""),"i_regs")
                  getlocal0
                  getlocal0
                  getproperty QName(PackageNamespace(""),"i_a")
                  pushbyte 1
                  add
                  setproperty QName(PackageNamespace(""),"i_b")
                  getlocal0
                  getlocal0
                  getproperty QName(PackageNamespace(""),"i_a")
                  getlocal0
                  getproperty QName(PackageNamespace(""),"i_b")
                  add
                  pushbyte 1
                  add
                  initproperty QName(PackageNamespace(""),"i_c")
                  debug 1, "p", 0, 0
                  getlocal0
                  constructsuper 0
                  findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestInitializer"),ProtectedNamespace("tests_classes:TestInitializer"),StaticProtectedNs("tests_classes:TestInitializer"),PrivateNamespace("TestInitializer.as$0")])
                  getlex QName(PackageNamespace(""),"s_regs")
                  pushbyte 1
                  getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestInitializer"),ProtectedNamespace("tests_classes:TestInitializer"),StaticProtectedNs("tests_classes:TestInitializer"),PrivateNamespace("TestInitializer.as$0")])
                  callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestInitializer"),ProtectedNamespace("tests_classes:TestInitializer"),StaticProtectedNs("tests_classes:TestInitializer"),PrivateNamespace("TestInitializer.as$0")]), 1
                  getlocal0
                  pushbyte 7
                  setproperty QName(PackageNamespace(""),"i_a")
                  getlocal0
                  getlocal1
                  setproperty QName(PackageNamespace(""),"i_d")
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
         findpropstrict Multiname("TestInitializer",[PackageNamespace("tests_classes")])
         getlex QName(PackageNamespace(""),"Object")
         pushscope
         getlex QName(PackageNamespace(""),"Object")
         newclass 0
         popscope
         initproperty QName(PackageNamespace("tests_classes"),"TestInitializer")
         returnvoid
      end ; code
   end ; body
end ; method

