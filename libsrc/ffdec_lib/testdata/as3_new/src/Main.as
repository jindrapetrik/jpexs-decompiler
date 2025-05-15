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
    import tests_uses.TestOtherClass;
    
    /**
     * ...
     * @author JPEXS
     */
    public class Main extends Sprite
    {
        TestActivationArguments;
        TestAndOrCoercion;
        TestArguments;
        TestBitwiseOperands;
        TestCallCall;
        TestCallLocal;
        TestCatchFinally;
        TestChain2;
        TestChainedAssignments;
        TestCollidingTraitNames;
        TestCollidingTry;        
        TestComplexExpressions;
        TestContinueLevels;
        TestConvert;        
        TestComma;
        TestCompoundAssignments;
        TestDecl2;
        TestDeclarations;
        TestDeclarationInterface;
        TestDeobfuscation;
        TestDefaultNotLastGrouped;
        TestDotParent;
        TestDoWhile;
        TestDoWhile2;
        TestDoWhile3;
        TestDoWhile4;
        TestExecutionOrder;
        TestExpressions;
        TestFinallyZeroJump;
        TestFor;
        TestForAnd;
        TestForBreak;
        TestForContinue;
        TestForEach;
        TestForEachObjectArray;
        TestForEachObjectAttribute;
        TestForEachSwitch;
        TestForEachReturn;
        TestForEachReturn2;
        TestForEachTry;
        TestForGoto;
        TestForIn;
        TestForInIf;
        TestForInReturn;
        TestForInSwitch;
        TestForXml;
        TestGetProtected;
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
        TestImportedConst;
        TestImportedVar;
        TestInc2;
        TestIncDec;
        TestInlineFunctions;
        TestInlineFunctions2;
        TestInnerFunctions;
        TestInnerFunctionScope;
        TestInnerIf;
        TestInnerTry;
        TestLogicalComputing;
        TestManualConvert;
        TestMetadata;
        TestMissingDefault;
        TestMultipleCondition;
        TestNamedAnonFunctions;
        TestNames;
        TestNames2;
        TestNegate;
        TestNumberCall;
        TestOperations;
        TestOptimization;
        TestOptimizationAndOr;
        TestOptimizationWhile;
        TestOptionalParameters;
        TestParamNames;
        TestParamsCount;
        TestPrecedence;
        TestPrecedenceX;
        TestProperty;
        TestRegExp;
        TestRest;
        TestSlots;
        TestSlots2;
        TestStrictEquals;
        TestStringCoerce;
        TestStringConcat;
        TestStrings;        
        TestSwitch;
        TestSwitchContinue;
        TestSwitchComma;
        TestSwitchDefault;
        TestSwitchIf;
        TestTernarOperator;
        TestTernarOperator2;
        TestTry;
        TestTryIf;
        TestTryReturn;
        TestTryReturn2;
        TestUndefined;
        TestUsagesTry;
        TestVarFqn;
        TestVector;
        TestVector2;        
        TestWhileAnd;
        TestWhileBreak;
        TestWhileBreak2;
        TestWhileContinue;
        TestWhileDoWhile;
        TestWhileSwitch;
        TestWhileTry;
        TestWhileTry2;        
        TestXml;
        
        SetupMyPackage1;
        SetupMyPackage2;
        SetupMyPackage3;
        
        TestThisOutsideClass;
        TestImports;
        TestImports2;
        TestInitializer;
        TestRegexpHilight;
        TestScriptInitializer;
        
        TestPropertyCoerce;
        TestUnaryMinus;
        
        TestModifiers;
        
        TestOtherClass;
        
        TestSubClass;
        
        initializedvar;
        initializedconst;
        
        public function Main()
        {
            if (stage) init();
            else addEventListener(Event.ADDED_TO_STAGE, init);
        }
        
        private function init(e:Event = null):void
        {
            removeEventListener(Event.ADDED_TO_STAGE, init);
            // entry point
            
            new TestScriptInitializer();
        }    
    
    }

}
