package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentViewController {

    @Autowired
    private StudentService studentService;

    // ==================== 列表页（分页 + 搜索 + 排序） ====================
    @GetMapping("/list")
    public String list(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "studentId") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Model model) {

        Page<Student> studentPage = studentService.findAllWithSearch(keyword, sortField, sortDir, page, size);

        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("totalElements", studentPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", "asc".equals(sortDir) ? "desc" : "asc");
        model.addAttribute("pageSize", size);

        return "student-list";
    }

    // ==================== 新增页面 ====================
    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("isEdit", false);
        model.addAttribute("title", "添加学生");
        return "student-form";
    }

    // ==================== 编辑页面 ====================
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable String id, Model model) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return "redirect:/student/list";
        }
        model.addAttribute("student", student);
        model.addAttribute("isEdit", true);
        model.addAttribute("title", "编辑学生");
        return "student-form";
    }

    // ==================== 保存（新增/编辑） ====================
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("student") Student student,
                       BindingResult result,
                       @RequestParam("isEdit") boolean isEdit,
                       RedirectAttributes redirectAttributes,
                       Model model) {

        // 表单验证
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            result.rejectValue("studentId", "error.student", "学号不能为空");
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            result.rejectValue("name", "error.student", "姓名不能为空");
        }
        if (student.getPhone() != null && !student.getPhone().matches("^1[3-9]\\d{9}$")) {
            result.rejectValue("phone", "error.student", "手机号格式不正确");
        }

        if (result.hasErrors()) {
            model.addAttribute("isEdit", isEdit);
            model.addAttribute("title", isEdit ? "编辑学生" : "添加学生");
            return "student-form";
        }

        try {
            if (isEdit) {
                studentService.updateStudent(student);
                redirectAttributes.addFlashAttribute("message", "学生信息更新成功！");
            } else {
                studentService.createStudent(student);
                redirectAttributes.addFlashAttribute("message", "学生添加成功！");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/student/list";
    }

    // ==================== 删除 ====================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
        String msg = studentService.deleteStudent(id);
        redirectAttributes.addFlashAttribute("message", msg);
        return "redirect:/student/list";
    }

    // ==================== 批量删除 ====================
    @PostMapping("/batch-delete")
    public String batchDelete(@RequestParam("ids") List<String> ids,
                              RedirectAttributes redirectAttributes) {
        String msg = studentService.batchDelete(ids);
        redirectAttributes.addFlashAttribute("message", msg);
        return "redirect:/student/list";
    }
}