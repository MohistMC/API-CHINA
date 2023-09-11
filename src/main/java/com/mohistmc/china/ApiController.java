package com.mohistmc.china;

import lombok.SneakyThrows;
import mjson.Json;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URL;

/**
 * @author Mgazul by MohistMC
 * @date 2023/9/10 23:45:55
 *
 *  http://localhost:2023/api/1.19.3/latest/download
 */

@RestController
@RequestMapping("/api")
public class ApiController {

    @SneakyThrows
    @GetMapping("/{variable}/latest")
    public String latest(@PathVariable String variable) {
        Json mohist = Json.read(new URL("https://mohistmc.com/api/" + variable + "/latest"));
        Json json = Json.object(
                "number", mohist.at("number").asInteger(),
                "md5", mohist.at("md5").asString(),
                "url", "http://s1.devicloud.cn:32023/api/" + variable+ "/latest/download");
        return json.toString();
    }

    @GetMapping("/{variable}/latest/download")
    public ResponseEntity<Resource> download(@PathVariable String variable) {

        try {
            File folder = new File("mohist/" + variable, MohistChinaAPI.dataMap.get(variable));
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();

                if (files != null) {
                    for (File file : files) {
                        folder = file;
                    }
                } else {
                    return ResponseEntity.notFound().build();
                }
            }

            Resource resource = new UrlResource(folder.toPath().toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
