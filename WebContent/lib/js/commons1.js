function warningMe(id) {
//   var o = document.getElementById("shared");
   var o = $(id);
   if (o.checked) {
      alert("Warning: You have chosen to share this Rubric to all users in eUreka!");
   }
}

function chkboxAll(gridId, chkBoxAllId){
	var input = $(chkBoxAllId);
	var inputs = $$('#'+gridId + ' input');
	inputs.each(function (elem) {
		if(elem.type=='checkbox'){
			 if(!elem.disabled){
				 elem.checked = input.checked;
			 }
		}
	});
}
function chkbox(gridId, chkBoxAllId){
	var input = $(chkBoxAllId);
	var inputs = $$('#'+gridId + ' input');
	
	var total = 0; 
	var count = 0;
	inputs.each(function (elem) {
	    if(elem.type=='checkbox'){
	    	if(!elem.disabled && elem!=input){
		    	if(elem.checked)
		    		count++;
		    	total++;
	    	}
	    }
	});
	if(count == total)
		input.checked = true;
	else
	 	input.checked = false;
}
function checkHasSelectChBox(gridId, errMsg){
	var inputs = $$('#'+gridId + ' input');
	var found = false;
	inputs.each(function (elem) {
		if(elem.type=='checkbox'){ 
			if(elem.checked){
	        	found = true;
	        }
		}
    });
    if(!found)
    	alert(errMsg);
    return found;
}
function checkHasSelectOneChBox(gridId, errMsg){
	var inputs = $$('#'+gridId + ' input');
	var count = 0;
	 inputs.each(function (elem) {
	     if(elem.type=='checkbox'){
			 if(elem.checked){
		     	count++;
			 }
	     }
	 });
    if(count!=1){
    	alert(errMsg);
    	return false;
    }
    return true;
}
function checkChBoxAndConfirmDelete(gridId, alertText, confirmText){
	if(checkHasSelectChBox(gridId, alertText))
		return confirm(confirmText);
	else
		return false;
}
function confirmDelete(confirmText){
	return confirm(confirmText);
}

function hideElm(id){
	//$(id).hide();
	Effect.BlindUp(id, {duration: 0.3});
}
function showElm(id){
	Effect.BlindDown(id, {duration: 0.3});
}

function updateElm(id, replacement){
	$(id).update(replacement);
}
function toggleDisplay(selector){
	$$(selector).each(function(e){
		toggleElm(e.id);
	});
}
function toggleElm(id){
	Effect.toggle(id, 'blind', {duration: 0.3});
}
function toggleTxt(id, txt1, txt2){
	if(txt1 == $(id).innerHTML){
		$(id).update(txt2);
	}
	else{
		$(id).update(txt1);
	}
}
function showHideDisplayNText(selector, id, txt1, txt2){
	if(txt1 == $(id).innerHTML){
		$(id).update(txt2);
		$$(selector).each(function(e){
			showElm(e.id);
		});
	}
	else{
		$(id).update(txt1);
		$$(selector).each(function(e){
			hideElm(e.id);
		});
	}
}

function showCloseBtn(elm){
	var e = $(elm).down('.closeBtn');
	if(e)
		e.show();
}
function hideCloseBtn(elm){
	var e = $(elm).down('.closeBtn');
	if(e)
		e.hide();
}
function showEditBtn(elm){
	var e = $(elm).down('.editBtn');
	if(e)
		e.show();
}
function hideEditBtn(elm){
	var e = $(elm).down('.editBtn');
	if(e)
		e.hide();
}

var g_urlarray = [];
function popup(myLink, windowName ) {
	openPopup(myLink, windowName, 1024, 760);
}
function openPopup(myLink, windowName, width, height) {
	var winRef = g_urlarray[windowName];
	if(winRef){
		winRef.close();
	}
	winRef = window.open(myLink, windowName,'height='+height+',width='+width+',toolbar=no,directories=no,status=yes,menubar=no,scrollbars=yes,resizable=yes');

	g_urlarray[windowName] = winRef;
	return false;
}

function odump(object, depth, max){
  depth = depth || 0;
  max = max || 2;

  if (depth > max)
    return false;

  var indent = "";
  for (var i = 0; i < depth; i++)
    indent += "  ";

  var output = "";  
  for (var key in object){
    output += "\n" + indent + key + ": ";
    switch (typeof object[key]){
      case "object": output += odump(object[key], depth + 1, max); break;
      case "function": output += "function"; break;
      default: output += object[key]; break;        
    }
  }
  return output;
}


function activateResize(element) {
    Event.observe(element, 'keyup', function() {
      updateSize(element);
    });
    updateSize(element);
}

function updateSize(element) {
	//if scrollbars appear, make it bigger, unless it's bigger then the user's browser area.
    if(Element.getHeight(element)<$(element).scrollHeight&&Element.getHeight(element)<document.viewport.getHeight()) {
        $(element).style.height = $(element).getHeight()+15+'px';
        if(Element.getHeight(element)<$(element).scrollHeight) {
                window.setTimeout("updateSize('"+element+"')",5);
        }               
    }
}

function greaterThan(var1, var2){
	return var1 > var2;
}
function greaterThanOrEqual(var1, var2){
	return var1 >= var2;
}
function lessThan(var1, var2){
	return var1 < var2;
}
function lessThanOrEqual(var1, var2){
	return var1 <= var2;
}
function isInt(n) {
	return (n+"").match(/^\d+$/);
}
function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

/*
  document.observe("dom:loaded", function(){
  	
  });

 */


/************Useful Prototypes**************/
Array.prototype.findElemt = function(searchStr) {
  var returnArray = false;
  for (i=0; i<this.length; i++) {
    if (typeof(searchStr) == 'function') {
      if (searchStr.test(this[i])) {
        if (!returnArray) { returnArray = []; }
        returnArray.push(i);
      }
    } else {
      if (this[i]===searchStr) {
        if (!returnArray) { returnArray = []; }
        returnArray.push(i);
      }
    }
  }
  return returnArray;
};
Array.prototype.sortNum = function() {
   return this.sort( function (a,b) { return a-b; } );
};


DOMTokenList.prototype.addMany = function(classes) {
    var array = classes.split(' ');
    for (var i = 0, length = array.length; i < length; i++) {
    	if(array[i]!="")
    		this.add(array[i]);
    }
}

DOMTokenList.prototype.removeMany = function(classes) {
    var array = classes.split(' ');
    for (var i = 0, length = array.length; i < length; i++) {
    	if(array[i]!="")
    		this.remove(array[i]);
    }
}





