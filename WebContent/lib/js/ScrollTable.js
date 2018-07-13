
var ScrollTable = Class.create();
ScrollTable.prototype = {
     initialize: function(tbox,theight) {
          // default height
          this.setHeight = 400; if (theight && !isNaN(theight)) {this.setHeight = theight;}
          // get containing box, set style
          this.tableBox = $(tbox); 
          // clean house
          this.resetScroll();
          //set container styles
          Element.setStyle(this.tableBox,{position: "relative",overflow: "auto", height: (this.setHeight+"px"),width: "100%"});
          // set up table
          this.table = this.tableBox.getElementsByTagName("TABLE")[0];
          this.setWidths();
          Element.setStyle(this.table,{position: "absolute", top: this.tableBox.scrollTop+"px", left: "0px"});
          // create table head and foot (if needed)
          this.tableHead = null; this.createThead();
          this.tableFoot = null; this.createTfoot();
          //reposition head and foot onscroll
          this.tableBox.tableHead = this.tableHead;
          this.tableBox.tableFoot = this.tableFoot;
          this.tableBox.onscroll = function() {
               Element.setStyle(this.tableHead,{top:this.scrollTop+"px"});
               if (this.tableFoot!=null) {
                    var delta = Position.realOffset(this.tableFoot);
                    var newTop = (this.clientHeight-this.tableFoot.offsetHeight)+delta[1];
                    Element.setStyle(this.tableFoot,{top:newTop+"px"});
               }
          }
     },
     // return to original state. called on initialize -- used for house cleaning if needed.
     resetScroll: function() {
    	  
          Element.setStyle(this.tableBox,{position: "static",overflow: "visible", width: "auto",height: "auto"});
          var tbl = this.tableBox.getElementsByTagName("TABLE");   
          if (tbl.length>1) {
        	  for(var i=1; i<tbl.length ; i++ ) {
	           	   var position = window.getComputedStyle(tbl[i]).getPropertyValue("position");
	           	
	           	   if(position=="relative" ) {
	           		   this.tableBox.removeChild(tbl[i]); // remove added header and fooer
	           	   }
	           	   else{
	           		   
	           	   }
              }
               //remove spacers
               if ($("_scTbodySpacer")) {Element.remove("_scTbodySpacer");}
               if ($("_scTheadSpacer")) {Element.remove("_scTheadSpacer");}
               if ($("_scTfootSpacer")) {Element.remove("_scTfootSpacer");}
          }
     },
     resize: function(theight) {
          if (isNaN(theight)) {return;}
          this.setHeight = theight;
          Element.setStyle(this.tableBox,{height:this.setHeight+"px"});
          var delta = Position.realOffset(this.tableBox);
          Element.setStyle(this.tableHead,{top:delta[1]+"px"});
          if (this.tableFoot!=null) {
               var delta = Position.realOffset(this.tableFoot);
               var newTop = (this.tableBox.clientHeight-this.tableFoot.offsetHeight)+delta[1];
               Element.setStyle(this.tableFoot,{top:newTop+"px"});
          }
          return;
     },
     //manually fix width of table and cell widths
     setWidths: function() {
          Element.setStyle(this.table,{width:(this.table.offsetWidth-20)+"px"});
          var td = this.table.tBodies[0].rows[0].cells;
          var tdl = td.length; //number of cells in first row in tbody
          var len = new Array();
          for(var i=0; i<tdl; i++) {
               len.push(td[i].offsetWidth);
          }
          // create spacer "gif" - Party like it's 1997
          var div = document.createElement("DIV")
          div.appendChild(document.createTextNode("\u00A0"));
          Element.setStyle(div,{height: "0px",margin: "0px",padding:"0px", overflow:"hidden"});
          var tr = document.createElement("TR");
          for(var j=0;j<tdl;j++) {
               var td = document.createElement("TD");
               var insertSpacer = div.cloneNode(true);
               Element.setStyle(insertSpacer,{width: len[j]+"px"});
               Element.setStyle(td,{height:"0px",overflow:"hidden",padding:"0px",margin:"0px"});
               td.appendChild(insertSpacer);
               tr.appendChild(td);
          }
          //append spacer row to first tbody
          var tbody = this.table.tBodies[0];
          var tbtr = tr.cloneNode(true); tbtr.id = "_scTbodySpacer";
          tbody.appendChild(tbtr);
          //append spacer row to thead
          var tHead = this.table.tHead;
          var thtr = tr.cloneNode(true); thtr.id = "_scTheadSpacer";
          tHead.appendChild(thtr);
          // append spacer row to tfoot if available
          if (this.table.tFoot) {
               var tFoot = this.table.tFoot;
               var tftr = tr.cloneNode(true); tftr.id = "_scTfootSpacer"
               tFoot.appendChild(tftr);
          }
     },
     // clone THEAD or first row into new table to create header
     createThead: function() {
          this.tableHead = this.table.cloneNode(false);
          var append = (this.table.tHead) ? 
               this.table.tHead.cloneNode(true) : 
               this.table.tBodies[0].rows[0].cloneNode(true);
          if (append.nodeName=="THEAD") {
               this.tableHead.appendChild(append);
          } else {
               var tb = document.createElement("TBODY")
               tb.appendChild(append);
               this.tableHead.appendChild(tb);
          }
          this.tableBox.appendChild(this.tableHead);
          Element.setStyle(this.tableHead,{position:"relative",left:"0px",zIndex:9, width:(this.table.offsetWidth)+"px"});
     },
     // create footer from TFOOT if it exists
     createTfoot: function() {
          if (!this.table.tFoot || this.table.tFoot.rows.length==0) {
               this.tableFoot = null;
               return;
          }
          this.tableFoot = this.table.cloneNode(false);
          var append = this.table.tFoot.cloneNode(true);
          this.tableFoot.appendChild(append);
          this.tableBox.appendChild(this.tableFoot);
          var scrollBottom =(this.tableBox.clientHeight-this.tableFoot.offsetHeight);
          Element.setStyle(this.tableFoot,{position: "absolute",top:scrollBottom+"px", zIndex:9, width:(this.table.offsetWidth)+"px"});
     }
} 
