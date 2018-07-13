var APPLET_LIMIT_FREE = 2147483648;  //in Byte file limit
var APPLET_WAIT = 60000;  //in milisecond
var APPLET_STATUS = 0;
var HTTP_COUNT = 0;

function IsAppletReady()
{
    if (APPLET_STATUS == -1)
      return false;
    return true;
}


function SetAppletQuota(nFree)
{
    APPLET_LIMIT_FREE = nFree;
    if (APPLET_STATUS != -1)
    {
        var objUC = document.jumpLoaderApplet.getUploaderConfig();
        objUC.setMaxLength(APPLET_LIMIT_FREE);
        objUC.setMaxFileLength(APPLET_LIMIT_FREE);
    }
    return true;
}


function AddAppletFile(sFile)
{
    if (APPLET_STATUS != 0)
      return false;

    var uploader = document.jumpLoaderApplet.getUploader();
    var bAdd = true;
    for (var i = uploader.getFileCount() - 1; i >= 0; i--)
    {
        var objFile = uploader.getFile(i);
        if (sFile == objFile.getPath())
        {
            bAdd = false;
            break;
        }
    }
    if (bAdd)
      uploader.addFile(sFile);
    return false;
}


function RemoveAppletFile()
{
    if (APPLET_STATUS != 0)
      return false;

    var uploader = document.jumpLoaderApplet.getUploader();
    for (var i = uploader.getFileCount() - 1; i >= 0; i--)
    {
        var objFile = uploader.getFile(i);
        if (objFile.getKey() == 'remove')
          uploader.removeFileAt(i);
    }
    return true;
}


function StartAppletUpload()
{
    APPLET_RESUME = 1;
    setTimeout('ResumeAppletUpload()', APPLET_WAIT);
    return true;
}


function ResumeAppletUpload()
{
    if (APPLET_STATUS == 0)
    {
        var uploader = document.jumpLoaderApplet.getUploader();
        if (uploader.canStartUpload())
        {
            APPLET_RESUME = 0;
            uploader.startUpload();
            return true;
        }
    }
    return false;
}


function RefreshFileList()
{
    try
    {
    	return true;
       // window.frames['fraFile'].location.href = '/jLoader/List';
    }
    catch (err)
    {
        // error
    }
    return true;
}


function ResumeFileList()
{
    try
    {
    	return true;
        //window.frames['fraFile'].Action('RESUME_LIST');
    }
    catch (err)
    {
        // error
    }
    return true;
}


function AttachFileList()
{
    try
    {
    	return true;
        //window.frames['fraFile'].Action('ATTACH_LIST');
    }
    catch (err)
    {
        // error
    }
    return true;
}


function appletInitialized(applet)
{
    var objVC = applet.getViewConfig();
    var objView = applet.getMainView();
    objVC.setMainViewFileTreeViewVisible(false);
    objVC.setMainViewFileListViewVisible(false);
    objVC.setUseThumbs(false);
    objView.updateView();
    objVC.setUploadViewFilesSummaryBarVisible(false);
    objView.getUploadView().updateView();

    APPLET_STATUS = 0;
    SetAppletQuota(APPLET_LIMIT_FREE);
    setTimeout('ResumeFileList()', APPLET_WAIT);
    return true;
}


function uploaderFileAdded(uploader, file)
{
    if (file.getLength() < 0)
    {
        APPLET_STATUS = 1;
        return true;
    }

    var sError = '';
    var sName = file.getName() + '';
    if (sError == '')
    {
        var nSize = file.getLength();
        if (nSize > 2147483648)
          sError = 'File must be less than 2.00 GB: ' + "\n" + sName;
        else if (nSize < 0)
          sError = 'File must be at least 0 B: ' + "\n" + sName;
    }
    if (sError != '')
    {
        alert(sError);
        file.setKey('remove');
        setTimeout('RemoveAppletFile()', APPLET_WAIT);
    }
    var ul = document.jumpLoaderApplet.getUploader();
    var attrset = ul.getAttributeSet();
    var name = "ul_id";
    var value = "HHH";
    var attr = attrset.createStringAttribute(name, value);
    attr.setSendToServer(true);
    //alert(attr);
    return true;
}


function uploaderFileStatusChanged(uploader, file)
{
    if (file.isReady())
    {
        uploaderFileAdded(uploader, file);
        if (uploader.getFileCountByStatus(4) == 0)
        {
            APPLET_STATUS = 0;
            if (APPLET_RESUME)
              setTimeout('ResumeAppletUpload()', APPLET_WAIT);
        }
    }
    else if (uploader.isUploading())
      APPLET_STATUS = 1;
    return true;
}


function uploaderStatusChanged(uploader)
{
    var nStatus = uploader.getStatus();
    if (APPLET_STATUS == 1)
    {
        var sMessage = '';
        var nSuccess = uploader.getFileCountByStatus(2);
        var nFailed = uploader.getFileCountByStatus(3);
        var bError = false;

        if (nSuccess != 0)
          sMessage = nSuccess + ' files(s) uploaded successfully.';
        if (nFailed != 0)
        {
            if (sMessage != '')
              sMessage += "\n\n";
            sMessage += nFailed + ' files(s) cannot be uploaded due to errors.' + "\n\n" +
                        'Do you want to view the error messages?';
        }
        if (sMessage != '')
        {
            RefreshFileList();
            //window.location.href = '#TOP';
            if (nFailed == 0){
              //alert(sMessage);
              APPLET_STATUS = 0;
              window.location.reload();
            }
            else if (confirm(sMessage))
              bError = true;
        }
        for (var i = uploader.getFileCount() - 1; i >= 0; i--)
        {
            var objFile = uploader.getFile(i);
            if (objFile.getStatus() == 2)
              uploader.removeFileAt(i);
        }
        if (bError)
        {
            var re = /(^\s+|\s+$)/g;
            for (var i = uploader.getFileCount() - 1; i >= 0; i--)
            {
                var objFile = uploader.getFile(i);
                if (objFile.getStatus() == 3)
                {
                    if (! confirm('Failed to upload: ' +  objFile.getName() + "\n" + objFile.getError().getMessage().replace(re, '')))
                      break;
                }
            }
        }
    }
    APPLET_STATUS = nStatus;
    return true;
}


function SetUploadForm(bEnable)
{
    var myUpload = document.getElementById('uploadUpload');
    if (myUpload != null)
    {
        if (bEnable)
          myUpload.disabled = false;
        else
          myUpload.disabled = true;
    }
    var myClear = document.getElementById('uploadClear');
    if (myClear != null)
    {
        if (bEnable)
          myClear.disabled = false;
        else
          myClear.disabled = true;
    }

    return true;
}


function SetHTTPForm(n, bEnable, bReset, bProgress, sResponse)
{
    var myClear = document.getElementById('httpClear_' + n);
    if (myClear != null)
    {
        if (bEnable)
          myClear.disabled = false;
        else
          myClear.disabled = true;
    }

    var myForm = document.getElementById('frmHTTP_' + n);
    if (myForm != null)
    {
        if (bReset)
          myForm.reset();
    }

    var progress = document.getElementById('httpProgress_' + n);
    if (progress != null)
    {
        if (bProgress)
          progress.style.display = 'inline';
        else
          progress.style.display = 'none';
    }

    var response = document.getElementById('httpResponse_' + n);
    if (response != null && sResponse != null)
      response.innerHTML = sResponse;

    return true;
}


function BrowseHTTPForm(n)
{
    var myFile = document.getElementById('httpFile_' + n);
    if (myFile == null)
      return true;

    var myClear = document.getElementById('httpClear_' + n);
    if (myClear == null)
      return true;

    return (myClear.disabled) ? false : true;
}


function ContinueHTTPUpload()
{
    if (HTTP_COUNT == 0)
    {
        SetUploadForm(false);
        for (var i = 1; i <= 3; i++)
        {
            SetHTTPForm(i, false, false, false, '');
        }
    }

    HTTP_COUNT++;

    if (HTTP_COUNT > 3)
    {
        var sMessage = '';
        var nSuccess = HTTP_FILE_OK;
        var nFailed = HTTP_FILE_ERR;
        if (nSuccess != 0)
          sMessage = nSuccess + ' files(s) uploaded successfully.';
        if (nFailed != 0)
        {
            if (sMessage != '')
              sMessage += "\n\n";
            sMessage += nFailed + ' files(s) cannot be uploaded due to errors.';
        }
        if (sMessage != '')
        {
            RefreshFileList();
            alert(sMessage);
            SetUploadForm(true);
            for (var i = 1; i <= 3; i++)
            {
                SetHTTPForm(i, true, false, false, null);
            }
        }
        HTTP_COUNT = 0;
        HTTP_FILE_OK = 0;
        HTTP_FILE_ERR = 0;
        return true;
    }
    else
    {
        var myFile = document.getElementById('httpFile_' + HTTP_COUNT);
        if (myFile != null && myFile.value != '')
        {
            SetHTTPForm(HTTP_COUNT, false, false, true, '');
            var myForm = document.getElementById('frmHTTP_' + HTTP_COUNT);
            myForm.submit();
        }
        else
          setTimeout('ContinueHTTPUpload()', APPLET_WAIT);
    }
    return true;
}


function ClearHTTPUpload()
{
    for (var i = 1; i <= 3; i++)
    {
        SetHTTPForm(i, true, true, false, '');
    }
    return true;
}


function EndHTTPFile()
{
    if (HTTP_COUNT < 1 || HTTP_COUNT > 3)
      return false;

    var sError = '';
    var myFrame = window.frames['fraHTTP'];
    if (myFrame != null && myFrame.document != null && myFrame.location.href != 'about:blank')
    {
        var sResponse = myFrame.document.body.innerHTML;
        switch (sResponse.substr(0, 7))
        {
           case 'success':
                break;

            case 'Error: ':
                sError = sResponse.substr(7, sResponse.length - 7);
                break;

            default:
                sError = 'Failed to upload file.';
        }
    }
    if (sError == '')
    {
        HTTP_FILE_OK++;
        SetHTTPForm(HTTP_COUNT, false, true, false, '');
    }
    else
    {
        HTTP_FILE_ERR++;
        SetHTTPForm(HTTP_COUNT, false, false, false, sError);
    }
    setTimeout('ContinueHTTPUpload()', APPLET_WAIT);
    return true;
}


window.onbeforeunload = function()
{
    if (APPLET_STATUS == 1 || HTTP_COUNT != 0)
      return('File uploading is in progress.  Navigating away from this page will terminate the operation.');
}