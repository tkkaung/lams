var selectedElm = null ;
 document.observe("dom:loaded", function(){
	$$('.tooltip').each(function(elm){
		elm.hide();
	});
	
	
	Event.observe(document,"click",function(event){
		var elm = Event.element(event);
		if(selectedElm){
			if(elm!=selectedElm){
				if(!elm.descendantOf(selectedElm.next(".tooltip")) ){
					selectedElm.next(".tooltip").hide();
		  			selectedElm = null;  
				}
			}
		}
		
		var i = $$(".tooltipTrigger").indexOf(elm);
		if(i!=-1){
			var prevSelected;
			if(selectedElm){ 
				prevSelected = selectedElm;
			}
			elm.next(".tooltip").show();
			selectedElm = elm;
			
			if(prevSelected){
				if(prevSelected==elm){
					selectedElm.next(".tooltip").hide();
		  			selectedElm = null;  
				}
			}
		}
	});
	
	
	$$('.oUser').invoke('hide');
	
	
});

function moreUserClicked(elm){
	var elmId = Element.identify(elm);
	$(elmId).up('.moreBtn').adjacent('.oUser').each(function(elmt){
		showElm(elmt.identify());
	});
	$(elmId).up('.moreBtn').hide();
}