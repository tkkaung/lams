package org.lamsfoundation.lams.common.dictionary
{
	import flash.events.EventDispatcher;
	
	import mx.controls.Alert;
	
	public class XMLDictionary
	{
		private var eventDispatcher:EventDispatcher;
		private var dictionaryXML:XML;
		
		public function XMLDictionary(xml:XML)
		{
			eventDispatcher = new EventDispatcher();
			dictionaryXML = new XML(xml);
			
			eventDispatcher.dispatchEvent(new XMLDictionaryEvent("COMPELTE"));
		}
		
		public function getLabel(s:String):String{
			if(!isEmpty()){
				return dictionaryXML.language.entry.(@key==s).name;
			}
			
			return null;
		}
		
		public function getLabelAndConcatenate(s:String, a:Array):String{
			if(!isEmpty()){
				var concat:String = "";
				for(var i:int = 0; i < a.length; i++){
					concat += a[i];
				}
				
				return getLabel(s) + concat;
			}
			
			return null;
		}
		
		public function getLabelAndReplace(s:String, a:Array):String{
			if(!isEmpty()){
				var label:String = getLabel(s);
				
				for(var i:int = 0; i < a.length; i++){
					label = label.replace("{" + String(i) + "}", getLabel(a[i]));
				}
			}
			
			return label;
		}
		
		public function isEmpty():Boolean{
			return dictionaryXML.toString() == "";
		}
	}
}