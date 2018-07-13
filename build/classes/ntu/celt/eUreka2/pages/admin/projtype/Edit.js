
/*e.g: if checkbox1 is ON, then hide div2 , */
function initHidePair(prefix1ChId, prefix2Id, listSize){ 
	for(var i=0; i<listSize; i++){
		var elm1 = $(prefix1ChId+i);
		var elm2 = $(prefix2Id+i);
		if(elm1){
			if(elm2){
				if(elm1.visible() && elm1.type=='checkbox' && elm1.checked){
					elm2.hide();
				}
			}
		}
	}
}

function updateHidePair(chId1, id2){
	var elm1 = $(chId1);
	if(elm1){
		if(elm1.visible() && elm1.type=='checkbox' ){
			if(elm1.checked){
				hideElm(id2);
			}
			else{
				$(id2).show();
			}
		}
	}
}