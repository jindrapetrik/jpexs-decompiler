package 
{
  import flash.display.Sprite;
  import flash.utils.ByteArray;
  import flash.filesystem.File;
  import flash.filesystem.FileStream;
  import flash.filesystem.FileMode;
  
  import flash.utils.Dictionary;
  import flash.geom.Point;
  import flash.xml.XMLDocument;
  
  import flash.net.registerClassAlias;
    
 /**
  * Generating test data file
  */
  public class AmfTest extends Sprite
  {
  
    private function writeToFile(filename:String,data)
    {
      var urlStr:String = "file:///"+File.applicationDirectory.nativePath+"/generated/"+filename;
      var file:File = new File() ;
      file.url = urlStr; 
      var fileStream:FileStream = new FileStream(); 
      fileStream.open(file, FileMode.WRITE); 
      var bytes:ByteArray = new ByteArray();
      bytes.writeObject(data);
      fileStream.writeBytes(bytes, 0, bytes.length);      
      fileStream.close();
      
    }
  
    public function AmfTest() {    
      
      
      
      var ar = ["a","b","c"];
      
      var asoc_array = [1,2,3];
      asoc_array["key1"] = 5;
      asoc_array["key2"] = 6;
      
      var ba:ByteArray = new ByteArray();
      ba.writeByte(65);
      ba.writeByte(66);
      ba.writeByte(67);
      
      
      var vector_int:Vector.<int> = new <int>[-10,20,-30,40];
      var vector_uint:Vector.<uint> = new <uint>[10,20,30,40];
      var vector_double:Vector.<Number> = new <Number>[-10.1,20.2,-30.3,40.4];
      var vector_string:Vector.<String> = new <String>["x","y","z"];
      var vector_point:Vector.<Point> = new <Point>[new Point(10,20),new Point(30,40),new Point(50,60)];
      
      var dict = new Dictionary();
      dict["dkey1"] = "TestOne";
      dict["dkey2"] = "TestTwo";
      
      var txmldoc:XMLDocument = new XMLDocument("<foo><bar>aa</bar></foo>");
      
      var txml:XML = <foo>
                        <bar>Hello</bar>
                        <bar>Hi</bar>
                     </foo>;
      
      var date = new Date();
      
      var me = {
        "01_int":5,
        "02_negative-int":-5,
        "03_string":"String",
        "04_true":true,
        "05_false":false,
        "06_undefined":undefined,
        "07_null":null,
        "08_double":5.6,
        "09_negative-double":-5.6,
        "10_array":ar,
        "11_asoc_array": asoc_array,
        "12_date":date,
        "13_xml":txml,
        "14_byteArray":ba,
        "15_vectorInt":vector_int,
        "16_vectorUInt":vector_uint,
        "17_vectorDouble":vector_double,
        "18_vectorString":vector_string,
        "19_vectorPoint":vector_point,      
        "20_dictionary":dict, 
        "21_xmldoc":txmldoc,
        "22_ref_string":"String",
        "23_ref_array":ar,
        "24_ref_date":date,
        "25_ref_byteArray":ba,
        "26_ref_vectorInt":vector_int,
        "27_ref_vectorUInt":vector_uint,
        "28_ref_vectorDouble":vector_double,
        "29_ref_vectorString":vector_string,
        "30_ref_dictionary":dict,
        "31_ref_xml":txml,
        "32_ref_xmldoc":txmldoc
      };
      me["33_me"] = me;
      
      writeToFile("all.bin",me);
      
      var custom = new CustomClass();
      custom.setVal8(127);
      custom.setVal32(-2500);
      registerClassAlias("CustomClass", CustomClass);
      
      var cust_obj = {
          "member1":5,
          "member2":custom,
          "member3":27
        };
      
      writeToFile("custom.bin",cust_obj);
      
      writeToFile("noserializer_object_dynamic.bin",{
        "a":5,
        "b":8,
        "c":custom,
        "d":6,
        "e":7,
        "f":26
      });
      writeToFile("noserializer_array_dense.bin",
      ["a","b",custom,"d","e","f"]
      );
      
      var arx = [];
      arx["a"] = 1;
      arx["b"] = 2;
      arx["c"] = custom;
      arx["d"] = 4;
      arx["e"] = 5;
      arx["f"] = 6;
      writeToFile("noserializer_array_associative.bin", arx);
       
      
      writeToFile("noserializer_vector.bin", new <Object>["a","b",custom,"d","e","f"]);  
      
      var nsdict = new Dictionary();
      nsdict["a"] = "One";
      nsdict["b"] = "Two";
      nsdict["c"] = custom;
      nsdict["d"] = "Three";
      nsdict["e"] = "Four";
      nsdict["f"] = "Five";
      
      writeToFile("noserializer_dictionary_value.bin", nsdict);
      
      var nsdict2 = new Dictionary();
      nsdict2["a"] = "One";
      nsdict2["b"] = "Two";
      nsdict2[custom] = "Three";                  
      nsdict2["d"] = "Three";
      nsdict2["e"] = "Four";
      nsdict2["f"] = "Five";
      
      writeToFile("noserializer_dictionary_key.bin", nsdict2);
      
      var objWithCustom:ObjectWithCustom = new ObjectWithCustom("1","2",custom,"4","5","6"); 
      writeToFile("noserializer_object_sealed.bin", objWithCustom);


	writeToFile("simple.bin", {"a":7, "b":29});
    }
  }
}