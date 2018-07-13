FCKConfig.LinkBrowser = false ;
FCKConfig.LinkUpload = false ;
FCKConfig.ImageBrowser = false ;
FCKConfig.ImageUpload = false ;
//FCKConfig.ImageBrowserURL = "/webapp/web/upload.jsp";
//FCKConfig.ImageUploadURL = "/webapp/web/upload.jsp";
FCKConfig.ImageUploadAllowedExtensions = ".(jpg|gif|jpeg|png|bmp)$";
FCKConfig.FlashBrowser = false ;
FCKConfig.FlashUpload = false ;


/*FCKConfig.ToolbarSets["Default"] = [
['Source','DocProps','-','NewPage','Preview','-','Templates'],
['Cut','Copy','Paste','PasteText','PasteWord','-','Print','SpellCheck'],
['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
['Form','Checkbox','Radio','TextField','Textarea','Select','Button','ImageButton','HiddenField'],
'/',
['Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'],
['OrderedList','UnorderedList','-','Outdent','Indent','Blockquote'],
['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
['Link','Unlink','Anchor'],
['Image','Flash','Table','Rule','Smiley','SpecialChar','PageBreak'],
'/',
['Style','FontFormat','FontName','FontSize'],
['TextColor','BGColor'],
['FitWindow','ShowBlocks','-','About'] // No comma for the last row.
] ;
*/
FCKConfig.ToolbarSets["Default"] = [
['Source','DocProps','-','NewPage','Preview','Templates'],
['Cut','Copy','Paste','PasteText','PasteWord','-','Print','SpellCheck'],
['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
['Link','Unlink','Anchor'],
'/',
['Bold','Italic','Underline','StrikeThrough','Subscript','Superscript','TextColor','BGColor'],
['OrderedList','UnorderedList','Outdent','Indent','Blockquote','JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
['Table','Rule','Smiley','SpecialChar'],
'/',
['Style','FontFormat','FontName','FontSize'],
['About']
];

FCKConfig.ToolbarSets["simpleToolbar"] = [
	    	['Source','Bold','Italic','Underline','StrikeThrough','Subscript','Superscript','TextColor','BGColor',
	    	 '-','JustifyLeft','JustifyCenter','JustifyRight','JustifyFull',
	    	 '-','OrderedList','UnorderedList',
	    	 '-','Link','Unlink','Table','Rule','Smiley','SpecialChar',
	    	 '-','About']
		];