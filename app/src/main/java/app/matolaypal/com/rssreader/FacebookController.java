package app.matolaypal.com.rssreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import java.util.Collections;
import java.util.List;

class FacebookController {
    private Context context;
    private static CallbackManager cbm;

    private final String TAG = this.getClass().getSimpleName() + " <#> ";


    /**
     * Responsible for feed sharing on Facebook trough this application.
     * (Facebook authentication needed!)
     * @param context from {@link MainActivity}
     */
    FacebookController(Context context) {
        this.context = context;
    }

    /**
     * Initialize FacebookSdk, get login instance and permission, create new FacebookCallback.
     * Finally save CallbackManager object for {@link MainActivity#onActivityResult(int, int, Intent)}
     * @param feed object from {@link RssFeedModel}
     */
    void share(final RssFeedModel feed) {
        FacebookSdk.sdkInitialize(context);
        CallbackManager callbackManager = CallbackManager.Factory.create();
        List<String> permissionNeeds = Collections.singletonList("publish_actions");
        LoginManager manager = LoginManager.getInstance();
        manager.logInWithPublishPermissions((Activity) context, permissionNeeds);
        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            /**
             * Share content from {@link RssFeedModel}, if everything is awesome.
             * @param loginResult from Facebook
             */
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                Log.d(TAG, "onSuccess");
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentTitle(feed.title)
                        .setContentDescription(feed.description)
                        .setContentUrl(Uri.parse(feed.link))
                        .build();
                ShareApi.share(content, null);
            }

            /**
             * If the sharing is cancelled, the app log it and notify the user.
             */
            @Override
            public void onCancel()
            {
                Log.d(TAG, "Sharing cancelled!");
                Toast.makeText(context, "Sharing cancelled!", Toast.LENGTH_SHORT).show();
            }

            /**
             * If something went wrong, the app log it and notify the user.
             * @param exception from Facebook
             */
            @Override
            public void onError(FacebookException exception)
            {
                Log.d(TAG, exception.getMessage());
                Toast.makeText(context, "Sharing error!", Toast.LENGTH_SHORT).show();
            }
        });
        cbm = callbackManager;
    }

    /**
     * @return {@link CallbackManager} object
     */
    static CallbackManager getCallbackManager() {
        return cbm;
    }

}
