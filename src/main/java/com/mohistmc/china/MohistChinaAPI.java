package com.mohistmc.china;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import mjson.Json;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Mgazul by MohistMC
 * @date 2023/9/10 23:44:25
 */

@Order(0)
@Component
public class MohistChinaAPI {

    public static Map<String, String> dataMap = new HashMap<>();
    public static List<String> versionList = List.of("1.16.5", "1.18.2", "1.19.2", "1.19.4", "1.20.1", "1.20.2");

    public static final ScheduledExecutorService LIVE = new ScheduledThreadPoolExecutor(4, new NamedThreadFactory("syncMohistAPI - "));

    public static Map<String, Boolean> canDownload = new HashMap<>();

    @PostConstruct
    public void init() {
        System.out.println("初始化后端");
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyPort", "7890");
        dataMap.put("1.7.10", "Mohist-1.7.10-46-server.jar");
        dataMap.put("1.12.2", "mohist-1.12.2-343-server.jar");
        run0();
        syncMohistAPI();
    }

    public void syncMohistAPI() {
        LIVE.scheduleAtFixedRate(this::run0, 1000, 1000 * 5 * 60 * 3, TimeUnit.MILLISECONDS); // 3分钟同步一次 时间单位毫秒
    }

    @SneakyThrows
    private void run0() {
        for (String version : versionList) {
            Json json = Json.read(new URL("https://mohistmc.com/api/" + version + "/latest"));
            String url = json.at("url").asString();
            File mohist = new File("mohist/" + version, json.at("name").asString());
            File folder = new File("mohist", version);
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (!file.getName().equals(mohist.getName())) {
                            file.delete(); // 删除旧版本jar
                        } else {
                            // 添加一次MD5检测
                            if (!MD5Util.getMd5(file).equals(json.at("md5").asString())) {
                                canDownload.put(version, false);
                                downloadFile(url, mohist);
                                canDownload.put(version, true);
                            }
                            canDownload.put(version, true);
                            dataMap.put(version, file.getName());
                        }

                    }
                }
            } else {
                mohist.getParentFile().mkdirs();
            }
            if (!mohist.exists()) {
                canDownload.put(version, false);
                downloadFile(url, mohist);
                dataMap.put(version, mohist.getName());
                canDownload.put(version, true);
            }
        }
    }

    public static void downloadFile(String URL, File f) throws Exception {
        System.out.println("下载文件中: " + URL);
        URLConnection conn = getConn(URL, true);
        ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
        FileChannel fc = FileChannel.open(f.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

        fc.transferFrom(rbc, 0, Long.MAX_VALUE);
        fc.close();
        rbc.close();
        System.out.println("下载完毕: " + URL);
    }

    public static URLConnection getConn(String URL, boolean useProxy) {
        URLConnection conn = null;
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new java.net.InetSocketAddress("127.0.0.1", 7890));
            URL url = new URL(URL);
            conn = useProxy ? url.openConnection(proxy) : url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
        } catch (IOException e) {
            e.fillInStackTrace();
        }
        return conn;
    }
}
