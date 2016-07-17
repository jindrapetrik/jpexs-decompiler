package 
{	
	import flash.utils.IExternalizable;
	import flash.utils.IDataOutput;
	import flash.utils.IDataInput;
	
	public class CustomClass implements IExternalizable
	{		

		private var val8:int;
		private var val32:int;
		public function CustomClass()
		{								
		}

		public function setVal8(v:int){
			this.val8 = v;
		}
		public function setVal32(v:int){
			this.val32 = v;
		}

		public function writeExternal(output:IDataOutput):void { 
			output.writeByte(val8);
			output.writeInt(val32);
			
      		}
  
		public function readExternal(input:IDataInput):void {  
			this.val8 = input.readByte();
			this.val32 = input.readInt();
		}
	}
}