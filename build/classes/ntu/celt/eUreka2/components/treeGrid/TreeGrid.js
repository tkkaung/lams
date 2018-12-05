function toggleRows(elm) {
 var rows = document.getElementsByTagName("TR");
 elm.className = "folderclose"; 
 var newDisplay = "none";
 var thisID = elm.parentNode.parentNode.parentNode.id + "-";
 // Are we expanding or contracting? If the first child is hidden, we expand
  for (var i = 0; i < rows.length; i++) {
   var r = rows[i];
   if (matchStart(r.id, thisID, true)) {
    if (r.style.display == "none") {
     if (document.all) newDisplay = "block"; //IE4+ specific code
     else newDisplay = "table-row"; //Netscape and Mozilla
         elm.className = "folderopen"; 
    }
    break;
   }
 }
 
 // When expanding, only expand one level.  Collapse all desendants.
 var matchDirectChildrenOnly = (newDisplay != "none");

 for (var j = 0; j < rows.length; j++) {
   var s = rows[j];
   if (matchStart(s.id, thisID, matchDirectChildrenOnly)) {
     var tier =  s.getElementsByClassName("folderDiv")[0];
     var folder = tier.getElementsByTagName("A")[0];
     if (tier.getAttribute("onclick") != null) {
          folder.className = "folderclose"; 
     }
     s.style.display = newDisplay;
   }
 }
}

function toggleRowsDiv(elm) {
        var folder = elm.getElementsByTagName("A")[0];
        toggleRows(folder);     
}

function matchStart(target, pattern, matchDirectChildrenOnly) {
 var pos = target.indexOf(pattern);
 if (pos != 0) return false;
 if (!matchDirectChildrenOnly) return true;
 if (target.slice(pos + pattern.length, target.length).indexOf("-") >= 0) return false;
 return true;
}

function collapseAllRows() {
 var rows = document.getElementsByTagName("TR");
 for (var j = 0; j < rows.length; j++) {
   var r = rows[j];
   if (r.id.indexOf("-") >= 0) {
     r.style.display = "none";    
   }
 }
}
