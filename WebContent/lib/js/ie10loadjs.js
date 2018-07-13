function loadScript(url)
{
    // Adding the script tag to the head as suggested before
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;

    // Fire the loading
    head.appendChild(script);
}

function IE(v) {
  var r = RegExp('msie' + (!isNaN(v) ? ('\\s' + v) : ''), 'i');
  return r.test(navigator.userAgent);
}

