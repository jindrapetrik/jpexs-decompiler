package classes
{
  import spark.components.Panel;
  import spark.components.Button;
	
  public class TestSkinParts extends Panel
  {
    // declare the skin parts - only close is required
    [SkinPart(required="true")]
    public var closeIcon:Button;
    [SkinPart(required="false")]
    public var minimizeIcon:Button;
    [SkinPart(required="false")]
    public var resizeGripper:Button;
    
    [SkinPart(required="false")]
    public static var neco:Button;
    
    [SkinPart(required="false")]
    public function test(){
    }
   
  }
}
