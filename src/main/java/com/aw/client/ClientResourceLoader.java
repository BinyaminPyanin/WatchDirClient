package com.aw.client;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClientResourceLoader
 * Describes Properties File Loader/Processor For Directory Watch Client
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
public class ClientResourceLoader extends ResourceLoader {

    public static final String KEY_DIR_PATH = "dir.path";
    public static final String KEY_FILTER_REGEX = "key.filter.regex";
    public static final String KEY_SERVER_PROTOCOL = "server.http.protocol";
    public static final String KEY_SERVER_HOST = "server.host";
    public static final String KEY_SERVER_PORT = "server.port";

    public Map<String, String> filter(final String regex) {

        if (this.properties.isEmpty()) {
            System.out.println("Nothing To Filter");
            return Collections.emptyMap();
        }

        if (null == regex || regex.isEmpty()) {
            System.out.println("Nothing To Filter : regular expression invalid");
            return Collections.emptyMap();
        }

        System.out.println("Filtering " + this.properties + " by regular expression " + regex);

        Map<String, String> result = new HashMap<>();

        this.properties.keySet().stream().
                filter(x -> x.toString().matches(regex)).
                collect(Collectors.toList()).
                forEach(
                        y -> result.put(y.toString(), this.properties.get(y).toString())
                );

        result.put(ResourceLoader.KEY_ORIGINAL_FILE_NAME, this.files.get(0).getName());

        System.out.println("Result : " + result);
        System.out.println("Filtered");

        return result;
    }

    public void removePropertyFile() {

        if (this.files.isEmpty()) {
            System.out.println("Nothing To Delete");
            return;
        }

        System.out.println("Deleting " + this.files + "...");

        for (File f : this.files) {
            System.out.println("Deleting " + f + "...");
            f.delete();
            System.out.println("Deleted");
        }

        System.out.println("Deleted All Files");
    }

}
