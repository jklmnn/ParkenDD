package de.jkliemann.parkendd;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkliemann on 10.06.15.
 */
public class SendFeedback extends AsyncTask<EditText, Void, Integer> {

    private Context context;
    private EditText message;

    protected Integer doInBackground(EditText... message){
        this.message = message[0];
        this.context = this.message.getContext();
        String urlString = PreferenceManager.getDefaultSharedPreferences(this.context).getString("fetch_url", this.context.getString(R.string.default_fetch_url));
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(urlString);
        try{
            List<NameValuePair> nvp = new ArrayList<NameValuePair>(1);
            nvp.add(new BasicNameValuePair("msg", this.message.getText().toString()));
            post.setEntity(new UrlEncodedFormEntity(nvp));
            client.execute(post);
        }catch (IOException e){
            e.printStackTrace();
            return new Integer(1);
        }
        return new Integer(0);
    }

    protected void onPostExecute(Integer state){
        switch (state){
            default:
                Error.showLongErrorToast(this.context, this.context.getString(R.string.sent));
                this.message.setText("");
                break;
            case 1:
                Error.showLongErrorToast(this.context, this.context.getString(R.string.connection_error));
                break;
        }
    }
}
