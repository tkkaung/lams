<html>
<head>
<title>My File Browser</title>
<script type="text/javascript">

function SelectFile( fileUrl )
{
// window.opener.SetUrl( url, width, height, alt);
window.opener.SetUrl( fileUrl ) ;
window.close() ;
}
</script>
</head>
<body>
<a href="javascript:SelectFile('File1.jpg');">File 1</a><br />
<a href="javascript:SelectFile('File2.jpg');">File 2</a>
</body>
</html>