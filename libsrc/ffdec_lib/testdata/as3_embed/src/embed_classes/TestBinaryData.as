package embed_classes
{ 
    import flash.utils.ByteArray;
    
    
    [Embed(source="../../assets/data.bin", mimeType="application/octet-stream")] 
    public class TestBinaryData extends ByteArray
    {
    } 
}
