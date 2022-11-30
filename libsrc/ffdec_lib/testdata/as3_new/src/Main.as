package
{
	import flash.display.Sprite;
	import flash.events.Event;
	import tests.*;
	import tests_classes.mypackage1.SetupMyPackage1;
	import tests_classes.mypackage2.SetupMyPackage2;
	import tests_classes.mypackage3.SetupMyPackage3;
	import tests_classes.*;
	import tests_edit.*;
	
	/**
	 * ...
	 * @author JPEXS
	 */
	public class Main extends Sprite
	{
		TestActivationArguments;
		TestArguments;
        TestCallCall;
		TestCallLocal;
		TestCatchFinally;
		TestChain2;
		TestChainedAssignments;
        TestConvert;
		TestComplexExpressions;
		TestContinueLevels;
		TestComma;
		TestCompoundAssignments;
		TestDecl2;
		TestDeclarations;
		TestDeobfuscation;
		TestDefaultNotLastGrouped;
		TestDotParent;
		TestDoWhile;
		TestDoWhile2;
		TestDoWhile3;
		TestDoWhile4;
		TestExpressions;
		TestFinallyZeroJump;
		TestFor;
		TestForAnd;
		TestForBreak;
		TestForContinue;
		TestForEach;
		TestForEachObjectArray;
		TestForEachObjectAttribute;
		TestForEachReturn;
		TestForEachReturn2;
		TestForGoto;
		TestForIn;
		TestForInIf;
		TestForInReturn;
		TestForInSwitch;
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
		TestIfFinally;
		TestIfInIf;
		TestIfTry;
		TestIgnoreAndOr;
        TestImplicitCoerce;
		TestImportedVar;
		TestInc2;
		TestIncDec;
		TestInlineFunctions;
		TestInnerFunctions;
		TestInnerFunctionScope;
		TestInnerIf;
		TestInnerTry;
		TestLogicalComputing;
		TestManualConvert;
		TestMissingDefault;
		TestMultipleCondition;
		TestNamedAnonFunctions;
		TestNames;
		TestNegate;
		TestNumberCall;
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
		TestSwitchIf;
		TestTernarOperator;
		TestTry;
		TestTryIf;
		TestTryReturn;
		TestTryReturn2;
        TestUndefined;
		TestUsagesTry;
		TestVector;
		TestVector2;		
		TestWhileAnd;
		TestWhileBreak;
        TestWhileBreak2;
		TestWhileContinue;
		TestWhileTry;
		TestWhileTry2;		
		TestXml;
		
		SetupMyPackage1;
		SetupMyPackage2;
		SetupMyPackage3;
		
		TestThisOutsideClass;
		TestImports;
		TestInitializer;
		TestRegexpHilight;
		
		TestPropertyCoerce;
		TestUnaryMinus;
		
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