import com.intellij.openapi.compiler.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.*;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import pluginConfig.Config;
import pluginDatabase.DatabaseConnector;
import pluginEvents.*;
import org.jetbrains.annotations.NotNull;
import pluginData.DataProvider;
import pluginData.ProjectData;
import pluginServer.Server;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class PluginController implements VirtualFileListener, ProjectManagerListener, FileEditorManagerListener, CompilationStatusListener, DataProvider {
    private DatabaseConnector db;
    private Server server;
    private Config currentConfig;
    private Project currentProject;

    public PluginController() {
        initDB();
        initServer();
        start();
    }

    private void initDB() {
        this.db = new DatabaseConnector();
        db.connect();
    }

    private void initServer() {
        server = new Server(8888, this); //TODO: Port as Constant
        server.start();
    }

    private void start() {
        IDEEvent ideEvent = new IDEEvent(IDEEvent.MESSAGE_OPENED);
        db.logEvent(ideEvent);
    }

    public void shutdown() {
        IDEEvent ideEvent = new IDEEvent(IDEEvent.MESSAGE_CLOSED);
        db.logEvent(ideEvent);
        server.shutdown();
        db.close();
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        if (!event.getParent().toString().contains("/.")) { //Remove files that are supposed to be hidden
            CodeEvent codeEvent = new CodeEvent(currentProject.getName(), Long.toString(event.getFile().getLength()), event.getFile().getName());
            db.logEvent(codeEvent);
        }
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {
        //db.logNewFile(project.getName(), event.getParent() + event.getFile().getName(), 0); //TODO: Rework
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent event) {
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent event) {
    }

    @Override
    public void projectOpened(@NotNull Project project) {
        this.currentProject = project;
        initEditorFilesEventListener(project);
        initFileEventListener(project);
        initCompileEventListener(project);
        initConfig(project);

        ProjectEvent projectEvent = new ProjectEvent(project.getName(), ProjectEvent.MESSAGE_OPENED);
        db.logEvent(projectEvent);
    }

    private void initEditorFilesEventListener(Project project) {
        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection messageBusConnection = messageBus.connect();
        messageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
    }

    private void initFileEventListener(Project project) {
        VirtualFileManager.getInstance().addVirtualFileListener(this);
    }

    private void initCompileEventListener(Project project) {
        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection messageBusConnection = messageBus.connect();
        messageBusConnection.subscribe(CompilerTopics.COMPILATION_STATUS, this);
    }

    private void initConfig(Project project) {
        //TODO: Change constructor to use project path instead of readied file
        try {
            File file = new File(project.getBasePath() + "/Config.json");
            currentConfig = new Config(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void projectClosed(@NotNull Project project) {
        System.out.println("Project closing");
        ProjectEvent projectEvent = new ProjectEvent(project.getName(), ProjectEvent.MESSAGE_CLOSED);
        db.logEvent(projectEvent);
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        EditorEvent editorEvent = new EditorEvent(currentProject.getName(), EditorEvent.EDITOR_FILE_OPENED, file.getName());
        db.logEvent(editorEvent);
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        EditorEvent editorEvent = new EditorEvent(currentProject.getName(), EditorEvent.EDITOR_FILE_CLOSED, file.getName());
        db.logEvent(editorEvent);
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        EditorEvent editorEventNewFile = new EditorEvent(currentProject.getName(), EditorEvent.EDITOR_FILE_SELECTED, event.getNewFile().getName());
        db.logEvent(editorEventNewFile);
        if (event.getOldFile() != null && event.getOldFile().exists()) {
            EditorEvent editorEventOldFile = new EditorEvent(currentProject.getName(), EditorEvent.EDITOR_FILE_UNSELECTED, event.getOldFile().getName());
            db.logEvent(editorEventOldFile);
        }
    }
    /*
        Compiling Events
     */

    @Override
    public void compilationFinished(boolean aborted, int errors, int warnings, @NotNull CompileContext compileContext) {
        if (errors == 0) {
            CompileEvent compileEvent = new CompileEvent(currentProject.getName(), CompileEvent.COMPILE_SUCCESS);
            db.logEvent(compileEvent);
        } else {
            CompilerMessage[] messages = compileContext.getMessages(CompilerMessageCategory.ERROR);
            for (CompilerMessage message : messages) {
                CompileEvent compileEvent = new CompileEvent(currentProject.getName(), CompileEvent.COMPILE_FAIL + ": " + message);
                db.logEvent(compileEvent);
            }
        }
    }

    /*
    Data Provider
     */
    @Override
    public ProjectData getCurrentProject() {
        return null;
    }

    @Override
    public ProjectData getProject(String project) {
        return null;
    }

    @Override
    public void getInteractionData() {

    }

    @Override
    public void getInteractionData(Date start, Date end) {

    }

    @Override
    public void getProjectList() {

    }

    @Override
    public void getTotalOverview() {

    }
}
