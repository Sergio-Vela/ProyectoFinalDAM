package io.ionic.starter;

import android.app.Application;
import android.util.Log;

import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.AmplifyException;

public class MainApplication extends Application {

    private static final String TAG = "MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());

            Log.i(TAG, "Amplify Android inicializado correctamente");

        } catch (AmplifyException error) {
            Log.e(TAG, "Error al inicializar Amplify Android", error);
        }
    }
}
