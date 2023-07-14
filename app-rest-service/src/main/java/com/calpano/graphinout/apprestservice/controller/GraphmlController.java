package com.calpano.graphinout.apprestservice.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/api")
public class GraphmlController {

    @PostMapping("/validate")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> validate(@RequestParam("file") MultipartFile file,
                                                          @RequestParam("type") String fileType,
                                                          HttpServletResponse response,
                                                          RedirectAttributes redirectAttributes) {

        //TODO
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return ResponseEntity.ok(generateResult(response));

    }

    @PostMapping("/read")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> read(@RequestParam("file") MultipartFile file,
                                                      @RequestParam("type") String fileType,
                                                      HttpServletResponse response,
                                                      RedirectAttributes redirectAttributes) {

        //TODO
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return ResponseEntity.ok(generateResult(response));
    }

    /**
     * Create compressed(zip) file by StreamingResponseBody whose Content is result.graphml and log.txt
     *
     * @param response  : May be required to generate output
     * @return : StreamingResponseBody
     */
    private StreamingResponseBody generateResult(HttpServletResponse response) {
        return null;
    }
}
