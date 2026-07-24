package com.example.fileimporter.controller;

import com.example.fileimporter.dto.ImportProgress;
import com.example.fileimporter.model.FileImport;
import com.example.fileimporter.service.ImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Controller
@RequestMapping("/imports")
public class ImportController {
    private final ImportService importService;

    public ImportController(ImportService importService) { this.importService = importService; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("imports", importService.findAll());
        return "imports/list";
    }

    @PostMapping
    public String submit(@RequestParam("file") MultipartFile file, Model model) {
        try {
            FileImport created = importService.submit(file);
            return "redirect:/imports/" + created.getId();
        } catch (IllegalArgumentException exception) {
            model.addAttribute("imports", importService.findAll());
            model.addAttribute("error", exception.getMessage());
            return "imports/list";
        }
    }

    @GetMapping("/{importId}")
    public String detail(@PathVariable UUID importId, Model model) {
        FileImport fileImport = importService.require(importId);
        model.addAttribute("fileImport", fileImport);
        model.addAttribute("errors", importService.errors(importId));
        model.addAttribute("progress", ImportProgress.from(fileImport));
        return "imports/detail";
    }
}
