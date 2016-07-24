package studios.codelight.smartloginlibrary;

import android.app.Application;

import com.facebook.FacebookSdk;

public class SmartLogin extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(this);
    }
}
