package classes {

public class Test {
	private var testPriv:int=5;
	protected var testProt:int=9;
	
	
	public function testHello(){
		trace("hello");
	}
	
	public function testIncDec() {
         var a=5;
         var b=0;
         trace("++var");
         b=++a;
         trace("var++");
         b=a++;
         trace("--var");
         b=--a;
         trace("var--");
         b=a--;
         var c=[1,2,3,4,5];
         trace("++arr");
         b=++c[2];
         trace("arr++");
         b=c[2]++;
         trace("--arr");
         b=--c[2];
         trace("arr--");
         b=c[2]--;
		 
         var d=new TestClass1();
         trace("++property");
         trace(++d.attrib);
         trace("property++");
         trace(d.attrib++);
         trace("--property");
         trace(--d.attrib);
         trace("property--");
         trace(d.attrib--);
         return;
      }
      public function testDoWhile()  {
         var a=8;
         do
            {
               trace("a="+a);
               a++;
            }
         while(a<20);
         return;
      }
      public function testInnerTry(){        
         try
            {
               try
                  {
                     trace("try body 1");
                  }
               catch(e:DefinitionError)
                  {
                     trace("catched DefinitionError");
                  }
               trace("after try 1");
            }
         catch(e:Error)
            {               
               trace("catched Error");
            }
         finally
            {
               trace("finally block");
            }
      }
      public function testWhileContinue() {
         var a=5;
         while(true)
            {
               if(a==9)
                  {
                     if(a==8)
                        {
                           continue;
                        }
                     if(a==9)
                        {
                           break;
                        }
                     trace("hello 1");
                  }
               trace("hello2");
            }
         return;
      }
      public function testPrecedence() {
         var a=0;
         a=(5+6)*7;
         a=5*(2+3);
         a=5+6*7;
         a=5*2+2;
         trace("a="+a);
         return;
      }
      public function testStrings() {
         trace("hello");
         trace("quotes:\"hello!\"");
         trace("backslash: \\ ");
         trace("single quotes: \'hello!\'");
		 trace("new line \r\n hello!");         
      }
      public function tryContinueLevels() {
         var a=5;
         loop123:
         switch(a)
            {
               case 57*a:
                  trace("fiftyseven multiply a");
                  var b=0;
                  while(b<50)
                     {
                        if(b==10)
                           {
                              break;
                           }
                        if(b==15)
                           {
                              break loop123;
                           }
                        b=b+1;
                     }
                  break;
               case 13:
                  trace("thirteen");
               case 14:
                  trace("fourteen");
                  break;
               case 89:
                  trace("eightynine");
                  break;
               default:
                  trace("default clause");                 
            }
         
         loop182:
         for(var c=0;c<8;c=c+1)
            {
               
               loop165:
               for(var d=0;d<25;d++)
                  {
                     
                     for(var e=0;e<50;e++)
                        {
                           if(e==9)
                              {
                                 break loop165;
                              }
                           if(e==20)
                              {
                                 continue loop182;
                              }
                           if(e==8)
                              {
                                 break;
                              }
                           break loop182;
                        }
                  }
               trace("hello");
            }
      }
      public function testSwitchDefault(){
         var a=5;
         switch(a)
            {
               case 57*a:
                  trace("fiftyseven multiply a");
                  break;
               case 13:
                  trace("thirteen");
               case 14:
                  trace("fourteen");
                  break;
               case 89:
                  trace("eightynine");
                  break;
               default:
                  trace("default clause");
            }
      }
      public function testMultipleCondition(){
         var a=5;
         var b=8;
         var c=9;
         if((a<=4||b<=8)&&c==7)
            {
               trace("onTrue");
            }
         else
            {
               trace("onFalse");
            }         
      }
      public function testForBreak(){         
         for(var a=0;a<10;a++)
            {
               if(a==5)
                  {
                     break;
                  }
               trace("hello:"+a);
            }
      }
	  
	  public function testIf(){
		 var a=5;
         if(a==7)
            {
               trace("onTrue");
            }  
	  }
	  
      public function testIfElse(){
         var a=5;
         if(a==7)
            {
               trace("onTrue");
            }
         else
            {
               trace("onFalse");
            }
      }
      public function testFor() {         
         for(var a=0;a<10;a++)
            {
               trace("a="+a);               
            }
      }
      public function testForContinue() {         
         for(var a=0;a<10;a=a+1)
            {
               if(a==9)
                  {
                     if(a==5)
                        {
                           trace("part1");
                           continue;
                        }
                     trace("a="+a);
                     if(a==7)
                        {
                           trace("part2");
                           continue;
                        }
                     trace("part3");
                  }
               else
                  {
                     trace("part4");
                  }
               trace("part5");
            }         
      }
      public function testTry() {
         var i:int;
         i=7;
         try
            {
               trace("try body");
            }
         catch(e:DefinitionError)
            {
               trace("catched DefinitionError");
            }
         catch(e:Error)
            {
               trace("Error message:"+e.message);
               trace("Stacktrace:"+e.getStackTrace());
            }
         finally
            {
               trace("Finally part");
            }
      }
      public function testSwitch(){
         var a=5;
         switch(a)
            {
               case 57*a:
                  trace("fiftyseven multiply a");
                  break;
               case 13:
                  trace("thirteen");
               case 14:
                  trace("fourteen");
                  break;
               case 89:
                  trace("eightynine");
                  break;
            }
      }
      public function testTernarOperator(){
         var a=5;
         var b=4;
         var c=4;
         var d=78;
         var e=(a==b)?((c==d)?1:7):3;
         trace("e="+e);         
      }
	  
	  public function testInnerIf(){
		  var a=5;
		  var b=4;
		  if(a==5){
			  if(b==6){
				  trace("b==6");
			  }else{
				  trace("b!=6");
			  }
		  }else{
			  if(b==7){
				  trace("b==7");
			  }else{
				  trace("b!=7");
			  } 
		  }
		  trace("end");
	  }
   }
}