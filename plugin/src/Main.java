import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.ProjectManager;

public class Main implements ProjectComponent {
    PluginController pluginController;

    public Main() {

    }

    @Override
    public void initComponent() {
        pluginController = new PluginController();
        ProjectManager.getInstance().addProjectManagerListener(pluginController);
    }


    @Override
    public void disposeComponent() {
        pluginController.shutdown();
    }
}

