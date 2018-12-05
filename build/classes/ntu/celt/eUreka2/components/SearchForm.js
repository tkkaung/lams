function inputTxtFocus(elem , defaultTxt){
	if(elem.value==defaultTxt)
		elem.value = "";
}
function inputTxtBlur(elem , defaultTxt){
	if(elem.value=="")
		elem.value = defaultTxt;
}