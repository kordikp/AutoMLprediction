package configuration;

import java.io.File;
import java.util.List;

public class ConfigurationFile {

    private List<File> files;
    private String module;
    private List<ConfigurationFile> modules;

    public ConfigurationFile(String module, List<ConfigurationFile> modules, List<File> files) {
        this.module = module;
        this.files = files;
        this.modules = modules;
    }

    public String getModule() {
        return module;
    }

    public List<File> getFiles() {
        return files;
    }

    public List<ConfigurationFile> getModules() {
        return modules;
    }

}
