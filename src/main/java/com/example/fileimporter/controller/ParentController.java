package com.example.fileimporter.controller;

import com.example.fileimporter.dto.ParentForm;
import com.example.fileimporter.dto.ChildDetail;
import com.example.fileimporter.model.Parent;
import com.example.fileimporter.service.ChildService;
import com.example.fileimporter.service.ParentService;
import com.example.fileimporter.util.JsonObjectMapper;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/parents")
public class ParentController {
    private final ParentService parentService;
    private final ChildService childService;
    private final JsonObjectMapper jsonObjectMapper;
    private final MessageSource messageSource;

    public ParentController(ParentService parentService, ChildService childService, JsonObjectMapper jsonObjectMapper,
                            MessageSource messageSource) {
        this.parentService = parentService;
        this.childService = childService;
        this.jsonObjectMapper = jsonObjectMapper;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("parents", parentService.findAll());
        return "parents/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        prepareForm(model, new ParentForm(), null, false);
        return "parents/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ParentForm form, BindingResult bindingResult,
                         Model model, RedirectAttributes redirectAttributes) {
        var properties = parseProperties(form.getDynamicProperties(), bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, null, false);
            return "parents/form";
        }
        Parent parent = parentService.create(form.getDisplayName(), properties);
        redirectAttributes.addFlashAttribute("success", message("flash.parent.created"));
        return "redirect:/parents/" + parent.getId();
    }

    @GetMapping("/{parentId}")
    public String detail(@PathVariable UUID parentId, Model model) {
        Parent parent = parentService.require(parentId);
        model.addAttribute("parent", parent);
        model.addAttribute("parentProperties", jsonObjectMapper.pretty(parent.getDynamicProperties()));
        model.addAttribute("children", childService.findForParent(parentId).stream()
                .map(child -> ChildDetail.from(child, jsonObjectMapper)).toList());
        return "parents/detail";
    }

    @GetMapping("/{parentId}/edit")
    public String editForm(@PathVariable UUID parentId, Model model) {
        Parent parent = parentService.require(parentId);
        ParentForm form = new ParentForm();
        form.setDisplayName(parent.getDisplayName());
        form.setDynamicProperties(jsonObjectMapper.pretty(parent.getDynamicProperties()));
        form.setVersion(parent.getVersion());
        prepareForm(model, form, parentId, true);
        return "parents/form";
    }

    @PostMapping("/{parentId}")
    public String update(@PathVariable UUID parentId, @Valid @ModelAttribute("form") ParentForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (form.getVersion() == null) {
            bindingResult.reject("version.required");
        }
        var properties = parseProperties(form.getDynamicProperties(), bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, parentId, true);
            return "parents/form";
        }
        parentService.update(parentId, form.getVersion(), form.getDisplayName(), properties);
        redirectAttributes.addFlashAttribute("success", message("flash.parent.updated"));
        return "redirect:/parents/" + parentId;
    }

    @PostMapping("/{parentId}/delete")
    public String delete(@PathVariable UUID parentId, long version, RedirectAttributes redirectAttributes) {
        parentService.delete(parentId, version);
        redirectAttributes.addFlashAttribute("success", message("flash.parent.deleted"));
        return "redirect:/parents";
    }

    private java.util.Map<String, Object> parseProperties(String value, BindingResult bindingResult) {
        try {
            return jsonObjectMapper.parse(value);
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("dynamicProperties", "properties.invalid");
            return java.util.Map.of();
        }
    }

    private void prepareForm(Model model, ParentForm form, UUID parentId, boolean editing) {
        model.addAttribute("form", form);
        model.addAttribute("parentId", parentId);
        model.addAttribute("editing", editing);
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
