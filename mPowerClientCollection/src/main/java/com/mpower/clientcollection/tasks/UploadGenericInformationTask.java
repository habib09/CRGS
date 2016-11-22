package com.mpower.clientcollection.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.mpower.clientcollection.R;
import com.mpower.clientcollection.listeners.UploadGenericInformationListener;
import com.mpower.clientcollection.utilities.ErrorMessageUtils;
import com.mpower.clientcollection.utilities.LogUtils;
import com.mpower.clientcollection.utilities.NetUtils;
import com.mpower.clientcollection.utilities.NotificationUtils;
import com.mpower.clientcollection.utilities.WebUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.net.SocketTimeoutException;

/**
 * Created by ratna on 4/4/16.
 */
public class UploadGenericInformationTask extends AsyncTask<UploadInformation, Void, String> {

    private Context context;
    private Exception getE = null;
    private int statusCode = 0;
    private String serverResponse = null;
    private String errorMsg = null;
    private ProgressDialog dialog;
    private UploadGenericInformationListener listener;

    public UploadGenericInformationTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.please_wait));
        dialog.show();
    }

    @Override
    protected String doInBackground(UploadInformation... values) {
        if (NetUtils.isConnected(context)) {
            // NotificationUtils.setNetworkIndicator(true);
            serverResponse = sendInformation(values[0]);

            //NotificationUtils.setNetworkIndicator(false);
        } else {
            LogUtils.debugLog(this, "Internet not found");
            errorMsg = "Internet not found";
        }
        return serverResponse;
    }

    private String sendInformation(UploadInformation information) {
        String url = information.url;
        String data = information.data;
        LogUtils.warningLog(this, "Urls = " + url + " \n data = " + data);
        String serverResponse = null;
        try {
            HttpResponse response = NetUtils.stringResponsePost(url, data, NetUtils.getHttpContext(), NetUtils.createHttpClient(WebUtils.CONNECTION_TIMEOUT));
            HttpEntity entity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            if (entity != null && statusCode == 200) {
                serverResponse = EntityUtils.toString(entity, "utf-8");
            }else if(entity != null){
                errorMsg = EntityUtils.toString(entity, "utf-8");
            }
            LogUtils.debugLog(this, "serverResponse = " + serverResponse);
            LogUtils.debugLog(this, "\n***********statusCode = " + statusCode);

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            errorMsg = "Socket Timeout Exception";
        } catch (Exception e) {
            e.printStackTrace();
            getE = e;
        }
        return serverResponse;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result != null) {
            listener.uploadCompleted(result);
        } else {
            if (getE != null || errorMsg == null) {
                errorMsg = ErrorMessageUtils.createServereErrorMsgGet(getE, statusCode);
                LogUtils.debugLog(this, "Error Message is  : " + errorMsg);
            }
            listener.showErrorMsg(errorMsg);
        }
    }

    public void setDownloaderListener(UploadGenericInformationListener listener) {
        this.listener = listener;
    }
}

