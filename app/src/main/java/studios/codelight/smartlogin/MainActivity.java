package studios.codelight.smartlogin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

import studios.codelight.smartloginlibrary.SmartCustomLoginListener;
import studios.codelight.smartloginlibrary.SmartCustomLogoutListener;
import studios.codelight.smartloginlibrary.SmartLoginBuilder;
import studios.codelight.smartloginlibrary.SmartLoginConfig;
import studios.codelight.smartloginlibrary.manager.UserSessionManager;
import studios.codelight.smartloginlibrary.users.SmartFacebookUser;
import studios.codelight.smartloginlibrary.users.SmartGoogleUser;
import studios.codelight.smartloginlibrary.users.SmartUser;

public class MainActivity extends AppCompatActivity implements SmartCustomLogoutListener, SmartCustomLoginListener {
    TextView loginResult;
    SmartUser currentUser;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginResult = (TextView) findViewById(R.id.login_result);

        // Get the current user details
        currentUser = UserSessionManager.getCurrentUser(this);
        String display = "no user";
        if(currentUser != null) {
            if (currentUser instanceof SmartFacebookUser) {
                SmartFacebookUser facebookUser = (SmartFacebookUser) currentUser;
                display = facebookUser.getProfileName() + " (FacebookUser)is logged in";
            } else if (currentUser instanceof SmartGoogleUser) {
                display = ((SmartGoogleUser) currentUser).getDisplayName() + " (GoogleUser) is logged in";
            } else {
                display = currentUser.getUsername() + " (Custom User) is logged in";
            }
        }
        loginResult.setText(display);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if (currentUser != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.user_exists);
            builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    UserSessionManager.logout(MainActivity.this, currentUser, MainActivity.this, mGoogleApiClient);
                    currentUser = UserSessionManager.getCurrentUser(MainActivity.this);
                }
            });
            builder.setCancelable(true);

            builder.create().show();
        } else {

            SmartLoginBuilder loginBuilder = new SmartLoginBuilder();

            // Set facebook permissions
            ArrayList<String> permissions = new ArrayList<>();
            permissions.add("public_profile");
            permissions.add("email");
            permissions.add("user_birthday");
            permissions.add("user_friends");


            Intent intent = loginBuilder.with(getApplicationContext())
                    .setAppLogo(R.mipmap.ic_launcher)
                    .withFacebookAppId(getString(R.string.facebook_app_id)).withFacebookPermissions(permissions)
                    //TODO Custom Login (true)
                    .isCustomLoginEnabled(false, SmartLoginConfig.LoginType.withEmail)
                    .setSmartCustomLoginHelper(MainActivity.this)
                    .build();

            startActivityForResult(intent, SmartLoginConfig.LOGIN_REQUEST);
            //startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String fail = "Login Failed";
        if(resultCode == SmartLoginConfig.FACEBOOK_LOGIN_REQUEST){
            SmartFacebookUser user;
            try {
                user = data.getParcelableExtra(SmartLoginConfig.USER);
                String userDetails = user.getProfileName() + " " + user.getEmail() + " " + user.getBirthday();
                loginResult.setText(userDetails);
            }catch (Exception e){
                loginResult.setText(fail);
            }
        }
        else if(resultCode == SmartLoginConfig.GOOGLE_LOGIN_REQUEST){
            SmartGoogleUser user = data.getParcelableExtra(SmartLoginConfig.USER);
            String userDetails = user.getEmail() + " " + user.getDisplayName();
            loginResult.setText(userDetails);
        }
        else if(resultCode == SmartLoginConfig.CUSTOM_LOGIN_REQUEST){
            SmartUser user = data.getParcelableExtra(SmartLoginConfig.USER);
            String userDetails = user.getUsername() + " (Custom User)";
            loginResult.setText(userDetails);
        }
        else if(resultCode == RESULT_CANCELED){
            loginResult.setText(fail);
        }

    }

    @Override
    public boolean customUserSignout(SmartUser smartUser) {
        //Implement your logic
        UserSessionManager.logout(this, smartUser, this, mGoogleApiClient);
        return true;
    }


    @Override
    public boolean customSignin(SmartUser user) {
        //This "user" will have only username and password set.
        Toast.makeText(MainActivity.this, user.getUsername() + " " + user.getEmail(), Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean customSignup(SmartUser newUser) {
        //Implement your our custom sign up logic and return true if success
        return true;
    }
}
