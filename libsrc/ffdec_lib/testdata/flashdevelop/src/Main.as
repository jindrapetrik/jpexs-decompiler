package
{
	import flash.display.Sprite;
	import flash.events.Event;
	import tests.*;
	import tests_classes.mypackage1.SetupMyPackage1;
	import tests_classes.mypackage2.SetupMyPackage2;
	import tests_classes.mypackage3.SetupMyPackage3;
	
	/**
	 * ...
	 * @author JPEXS
	 */
	public class Main extends Sprite
	{
		TestArguments;
		TestCatchFinally;
		TestChain2;
		TestChainedAssignments;
		TestComplexExpressions;
		TestContinueLevels;
		TestDecl2;
		TestDeclarations;
		TestDefaultNotLastGrouped;
		TestDoWhile;
		TestDoWhile2;
		TestExpressions;
		TestFinallyZeroJump;
		TestFor;
		TestForAnd;
		TestForBreak;
		TestForContinue;
		TestForEach;
		TestForEachObjectArray;
		TestForEachObjectAttribute;
		TestForGoto;
		TestForIn;
		TestForXml;
		TestGotos;
		TestGotos2;
		TestGotos3;
		TestGotos4;
		TestGotos5;
		TestGotos6;
		TestGotos7;
		TestHello;
		TestIf;
		TestIfElse;
		TestIfInIf;
		TestInc2;
		TestIncDec;
		TestInlineFunctions;
		TestInnerFunctions;
		TestInnerIf;
		TestInnerTry;
		TestLogicalComputing;
		TestManualConvert;
		TestMissingDefault;
		TestMultipleCondition;
		TestNamedAnonFunctions;
		TestNames;
		TestOptionalParameters;
		TestParamNames;
		TestParamsCount;
		TestPrecedence;
		TestPrecedenceX;
		TestProperty;
		TestRegExp;
		TestRest;
		TestStrictEquals;
		TestStringConcat;
		TestStrings;		
		TestSwitch;
		TestSwitchComma;
		TestSwitchDefault;
		TestTernarOperator;
		TestTry;
		TestTryReturn;
		TestTryReturn2;
		TestVector;
		TestVector2;		
		TestWhileAnd;
		TestWhileContinue;
		TestWhileTry;
		TestWhileTry2;		
		TestXml;
		
		SetupMyPackage1;
		SetupMyPackage2;
		SetupMyPackage3;
		
		public function Main()
		{
			if (stage) init();
			else addEventListener(Event.ADDED_TO_STAGE, init);
		}
		
		private function init(e:Event = null):void
		{
			removeEventListener(Event.ADDED_TO_STAGE, init);
			// entry point
		}
	
	}

}