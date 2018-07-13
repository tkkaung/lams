function projClicked(projId, projChkBox){
	var compChkBoxs = $$('#div_'+projId+' .comps input[type="checkbox"]');
	
	if(projChkBox.checked){
		compChkBoxs.each(function (elem) {
	       elem.checked = true;
	    });
		
		var comp = $$('#div_'+projId+' .comps').first();
		Effect.SlideDown(comp, {duration: 0.5});
	}
	else{
		compChkBoxs.each(function (elem) {
	       elem.checked = false;
	    });
		var comp = $$('#div_'+projId+' .comps').first();
		Effect.SlideUp(comp, {duration: 0.5});
	}
	
}

function compClicked(projId){
	var projChkBox = $$('#div_'+projId+' .proj input[type="checkbox"]').first();
	var compChkBoxs = $$('#div_'+projId+' .comps input[type="checkbox"]');
	
	var count = 0;
	for (var i=0; i<compChkBoxs.length; i++) {
	  	var elem = compChkBoxs[i];
	  	if(elem.checked){
	    	count++;
	    	projChkBox.checked = true;
	    	break;
	    }
	}
	if(count == 0){
		projChkBox.checked = false;
		var comp = $$('#div_'+projId+' .comps').first();
		Effect.SlideUp(comp, {duration: 0.5});
	}
}

