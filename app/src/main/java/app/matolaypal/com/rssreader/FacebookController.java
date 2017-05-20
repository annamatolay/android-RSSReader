package app.matolaypal.com.rssreader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

import java.util.Arrays;
import java.util.List;

public class FacebookController {
    private Context context;
    private CallbackManager callbackManager;
    private static CallbackManager cbm;

    private final String TAG = this.getClass().getSimpleName() + " <#> ";


    FacebookController(Context context) {
        this.context = context;
    }

    void share(final RssFeedModel feed) {
        FacebookSdk.sdkInitialize(context);

        callbackManager = CallbackManager.Factory.create();

        List<String> permissionNeeds = Arrays.asList("publish_actions");
        LoginManager manager = LoginManager.getInstance();
        manager.logInWithPublishPermissions((Activity) context, permissionNeeds);

        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                Log.d(TAG, "onSuccess");
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentTitle(feed.title)
                        .setContentDescription(
                                feed.description)
                        .setContentUrl(Uri.parse(feed.link)).build();
                ShareApi.share(content, null);
            }

            @Override
            public void onCancel()
            {
                Log.d(TAG, "onCancel");
                Toast.makeText(context, "Sharing cancelled!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception)
            {
                Log.d(TAG, "onError");
                Toast.makeText(context, "Sharing error!", Toast.LENGTH_SHORT).show();
            }
        });
        cbm = callbackManager;
    }

    private void shareContent(){
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentTitle("title")
                .setContentDescription(
                        "Description")
                .setContentUrl(Uri.parse("your url")).build();
//        ShareLinkContent content = new ShareLinkContent.Builder()
//                .setContentUrl(Uri.parse("https://developers.facebook.com"))
//                .build();
        ShareApi.share(content, null);
    }

    static CallbackManager getCallbackManager() {
        return cbm;
    }

}
