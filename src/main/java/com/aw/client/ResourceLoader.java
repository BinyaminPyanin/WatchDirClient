package com.aw.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * ResourceLoader
 * Describes Base Properties File Loader/Processor Class For Directory Watcher
 * <p>
 * Provides set of methods to:
 * <p>
 * 1) Read the properties file into a Map
 * 2) Apply a regular expression pattern filter for the keys
 * (i.e., remove key/value mappings where keys do not match a configurable regular expression pattern).
 * 3) Delete the file
 *
 * @author Binyamin (Dima) Pyanin
 * @version POC
 * @since March 23, 2022
 */
public class ResourceLoader {
    public static final String KEY_ORIGINAL_FILE_NAME = "key.original.file.name";

    protected Map<Object, Object> properties = new HashMap<>();

    protected List<File> files = new ArrayList<>();

    public Map<Object, Object> getProperties() {
        return this.properties;
    }

    public void readAllProperties(final String basePath) {
        System.out.println("Started Reading All *.properties files in " + basePath + "...");

        if (basePath != null) {
            File directory = new File(basePath);
            File[] fList = directory.listFiles();

            if (null != fList && fList.length > 0) {
                for (File propertyFile : fList) {
                    if (propertyFile.getName().endsWith(".properties")) {
                        System.out.println("Loading " + propertyFile.getPath());

                        initialize(propertyFile);

                        this.files.add(propertyFile);

                        System.out.println("Loaded " + propertyFile.getPath());
                    } else {
                        System.out.println("Skipped " + propertyFile.getPath());
                    }
                }
            }
        }

        System.out.println("Completed Reading All *.properties files in " + basePath);
    }

    public void initialize(File propertyFile) {
        Properties systemProperties = new Properties();
        try (InputStream fis = new FileInputStream(propertyFile)) {
            systemProperties.load(fis);

            this.properties.putAll(systemProperties);

            System.out.println(this.properties.toString());
        } catch (IOException e) {
            System.err.println("Unable to load properties file : " + e.getLocalizedMessage());
        }
    }

}
