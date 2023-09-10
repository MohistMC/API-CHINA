package com.mohistmc.china;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * @author Mgazul by MohistMC
 * @date 2023/9/10 23:45:55
 *
 *  http://localhost:2023/api/1.19.3/latest/download
 */

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/{variable}/latest/download")
    public ResponseEntity<Resource> hello(@PathVariable String variable) {

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
