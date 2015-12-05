package barqsoft.footballscores;

import android.app.Application;
import android.content.Context;

/**
 * Created by Stas on 05.12.15.
 */
public class FootballScoresApplication extends Application {
    private  static FootballScoresApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;

    }
    public static Context getContext(){
        return instance.getApplicationContext();
    }
}
