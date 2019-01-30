package pluginData;

import java.util.Date;

public interface DataProvider {
    ProjectData getCurrentProject(); //Get Configuration data of project, get total time, total project size, other aggregated data
    ProjectData getProject(String project);
    void getInteractionData(); // Get time spent, get coding activity, get other data
    void getInteractionData(Date start, Date end); // Above within timespan
    void getProjectList(); // Get list of all projects
    void getTotalOverview(); // Get Overview of all data over all projects
}