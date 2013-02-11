import models.User;
import play.Application;
import play.GlobalSettings;
import play.Play;
import utils.DemoData;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application application) {
        // load the demo data in dev mode
        if (Play.isDev() && (User.find.all().size() == 0)) {
            DemoData.loadDemoData();
        }

        super.onStart(application);
    }
}
