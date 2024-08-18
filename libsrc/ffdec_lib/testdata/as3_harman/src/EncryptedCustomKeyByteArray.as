package {
    import flash.utils.ByteArray;

    [Embed(source="/../bin/inside.swf", mimeType="application/octet-stream", encrypted="secret_key")]
    public class EncryptedCustomKeyByteArray extends ByteArray {
    }
}
