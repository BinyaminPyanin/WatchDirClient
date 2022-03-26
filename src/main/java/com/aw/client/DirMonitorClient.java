package com.aw.client;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * DirMonitorClient
 * Describes Directory Watch Client
 * <p>
 * When a new Java properties file appears in the monitored directory,
 * it processes by calling {@link ClientResourceLoader} properties file handler,
 * which can :
 * <p>
 * 1) Read the file into a Map
 * 2) Apply a regular expression pattern filter for the keys
 * (i.e., remove key/value mappings where keys do not match a configurable regular expression pattern).
 * 3) Relay the filtered mappings to a server program
 * 4) Delete the file
 * <p>
 * The client program’s main method accepts an argument specifying a config file path.
 * The client config file should contain values defining:
 * a)the directory path that will be monitored
 * b)the key filtering pattern that will be applied
 * c)the address of the corresponding server program
 * d)any other value(s) you think should be configurable
 *
 * @author Binyamin (Dima) Pyanin
 * @version POC
 * @since March 23, 2022
 */
public class DirMonitorClient {

    private final ClientResourceLoader resourceLoader;
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;
    private final String serverHost;
    private final int serverPort;
    private final String serverUrl;
    private final boolean trace;

    public DirMonitorClient(String configFilePath, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.recursive = recursive;

        this.resourceLoader = new ClientResourceLoader();

        this.resourceLoader.readAllProperties(configFilePath);

        String httpProtocol = (String) this.resourceLoader.getProperties().get(ClientResourceLoader.KEY_SERVER_PROTOCOL);
        System.out.println("Http protocol = " + httpProtocol);

        this.serverHost = (String) this.resourceLoader.getProperties().get(ClientResourceLoader.KEY_SERVER_HOST);
        System.out.println("Server host = " + this.serverHost);

        this.serverPort = Integer.parseInt((String) this.resourceLoader.getProperties().get(ClientResourceLoader.KEY_SERVER_PORT));
        System.out.println("Server port = " + this.serverPort);

        this.serverUrl = httpProtocol + this.serverHost + ":" + this.serverPort;
        System.out.println("Server url = " + this.serverUrl);

        Path dir = Paths.get((String) this.resourceLoader.getProperties().get(ClientResourceLoader.KEY_DIR_PATH));

        System.out.println(dir + " Directory Monitoring Client Service Started ...");

        if (recursive) {
            System.out.println("Scanning " + dir + "...");
            registerAll(dir);
            System.out.println("Done.");
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.println("register: " + dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.println("update: " + prev + "=>" + dir);
                }
            }
        }
        keys.put(key, dir);
    }

    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void sendMessage(final String message) {
        System.out.println("Attempting to deliver message " + message + "...");

        try (
                Socket serverSocket = new Socket(this.serverHost, this.serverPort);
                DataOutputStream dout = new DataOutputStream(serverSocket.getOutputStream())
        ) {
            dout.writeUTF(message);
            dout.flush();

            System.out.println("Client sent " + message + "...");
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + this.serverUrl);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + this.serverUrl);
            System.exit(1);
        }

    }

    private void processEvents() {
        for (; ; ) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                System.out.println(event.kind().name() + " : " + child);

                if (kind == ENTRY_CREATE) {
                    //1) Read the file into a Map
                    ClientResourceLoader rl = new ClientResourceLoader();
                    rl.readAllProperties(dir.toString());

                    if (!rl.getProperties().isEmpty()) {
                        //2) Apply a regular expression pattern filter for the keys
                        // (i.e., remove key/value mappings where keys do not match a configurable regular expression pattern).
                        Map<String, String> filteredMap =
                                rl.filter(this.resourceLoader.getProperties().get(ClientResourceLoader.KEY_FILTER_REGEX).toString());

                        //3) Relay the filtered mappings to a server program
                        sendMessage(filteredMap.toString());

                        //4) Delete the file
                        rl.removePropertyFile();
                    } else {
                        System.err.println("Client has no data to process");
                    }
                }

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    static void usage() {
        System.err.println("Usage: java DirMonitorClient [-r] config_file");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        // parse arguments
        if (args.length == 0 || args.length > 2) {
            usage();
        }

        boolean recursive = false;

        int dirArg = 0;

        if (args[0].equals("-r")) {
            if (args.length < 2)
                usage();
            recursive = true;
            dirArg++;
        }

        // register directory and process its events
        new DirMonitorClient(args[dirArg], recursive).processEvents();
    }

}
