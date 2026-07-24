package com.example.fileimporter.controller;

import com.example.fileimporter.dto.ChildForm;
import com.example.fileimporter.model.Child;
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

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/parents/{parentId}/children")
public class ChildController {
    private final ChildService childService;
    private final ParentService parentService;
    private final JsonObjectMapper jsonObjectMapper;
    private final MessageSource messageSource;

    public ChildController(ChildService childService, ParentService parentService, JsonObjectMapper jsonObjectMapper,
                           MessageSource messageSource) {
        this.childService = childService;
        this.parentService = parentService;
        this.jsonObjectMapper = jsonObjectMapper;
        this.messageSource = messageSource;
    }

    @GetMapping("/new")
    public String createForm(@PathVariable UUID parentId, Model model) {
        prepareForm(model, new ChildForm(), parentId, null, false);
        return "children/form";
    }

    @PostMapping
    public String create(@PathVariable UUID parentId, @Valid @ModelAttribute("form") ChildForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        Map<String, Object> properties = parseProperties(form.getDynamicProperties(), bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, parentId, null, false);
            return "children/form";
        }
        childService.create(parentId, form.getDisplayName(), properties);
        redirectAttributes.addFlashAttribute("success", message("flash.child.created"));
        return "redirect:/parents/" + parentId;
    }

    @GetMapping("/{childId}/edit")
    public String editForm(@PathVariable UUID parentId, @PathVariable UUID childId, Model model) {
        Child child = childService.require(parentId, childId);
        ChildForm form = new ChildForm();
        form.setDisplayName(child.getDisplayName());
        form.setDynamicProperties(jsonObjectMapper.pretty(child.getDynamicProperties()));
        form.setVersion(child.getVersion());
        prepareForm(model, form, parentId, childId, true);
        return "children/form";
    }

    @PostMapping("/{childId}")
    public String update(@PathVariable UUID parentId, @PathVariable UUID childId,
                         @Valid @ModelAttribute("form") ChildForm form, BindingResult bindingResult,
                         Model model, RedirectAttributes redirectAttributes) {
        if (form.getVersion() == null) {
            bindingResult.reject("version.required");
        }
        Map<String, Object> properties = parseProperties(form.getDynamicProperties(), bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, parentId, childId, true);
            return "children/form";
        }
        childService.update(parentId, childId, form.getVersion(), form.getDisplayName(), properties);
        redirectAttributes.addFlashAttribute("success", message("flash.child.updated"));
        return "redirect:/parents/" + parentId;
    }

    @PostMapping("/{childId}/delete")
    public String delete(@PathVariable UUID parentId, @PathVariable UUID childId, long version,
                         RedirectAttributes redirectAttributes) {
        childService.delete(parentId, childId, version);
        redirectAttributes.addFlashAttribute("success", message("flash.child.deleted"));
        return "redirect:/parents/" + parentId;
    }

    private Map<String, Object> parseProperties(String value, BindingResult bindingResult) {
        try {
            return jsonObjectMapper.parse(value);
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("dynamicProperties", "properties.invalid");
            return Map.of();
        }
    }

    private void prepareForm(Model model, ChildForm form, UUID parentId, UUID childId, boolean editing) {
        model.addAttribute("form", form);
        model.addAttribute("parent", parentService.require(parentId));
        model.addAttribute("parentId", parentId);
        model.addAttribute("childId", childId);
        model.addAttribute("editing", editing);
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
