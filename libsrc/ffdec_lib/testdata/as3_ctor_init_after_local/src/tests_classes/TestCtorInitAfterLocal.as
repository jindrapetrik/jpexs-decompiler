package tests_classes
{
   /**
    * Uninitialized ctor local (ASC: setlocal before field inits) + const XML
    * initializer (initproperty). Promotion must not stop at the setlocal.
    */
   public class TestCtorInitAfterLocal
   {
      private const scriptXml:XML = <script><![CDATA[function(){return 1;}]]></script>;

      public function TestCtorInitAfterLocal()
      {
         super();
         var idx:int;
         idx = 1;
         trace(scriptXml, idx);
      }
   }
}
