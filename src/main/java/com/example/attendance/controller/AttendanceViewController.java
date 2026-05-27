package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceStatisticsDTO;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Student;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.StudentService;
import com.example.attendance.dto.ImportResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Controller
@RequestMapping("/attendance")
public class AttendanceViewController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StudentService studentService;

    // ==================== 打卡页面 ====================
    @GetMapping("/checkIn")
    public String checkInPage(Model model) {
        // 获取当前登录用户（简化：从 SecurityContext 或 Session 获取）
        // 这里先用 session 模拟
        model.addAttribute("currentTime", LocalDateTime.now());
        return "attendance-check-in";
    }

    // ==================== 提交打卡 ====================
    @PostMapping("/checkIn")
    public String checkIn(@RequestParam String courseId,
                          @RequestParam(required = false) String remark,
                          RedirectAttributes redirectAttributes) {

        // 获取当前学生（实际应通过 SecurityContext 获取）
        String studentId = getCurrentStudentId();
        if (studentId == null) {
            redirectAttributes.addFlashAttribute("message", "请先登录");
            return "redirect:/login-page";
        }

        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            redirectAttributes.addFlashAttribute("message", "学生信息不存在");
            return "redirect:/login-page";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        // 课程时间限制（假设上课时间为 8:00）
        LocalTime classStartTime = LocalTime.of(8, 0);
        LocalTime earliestTime = classStartTime.minusMinutes(15);  // 7:45
        LocalTime latestTime = classStartTime.plusMinutes(30);     // 8:30

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setStudentName(student.getName());
        attendance.setCourseId(courseId);
        attendance.setCheckInTime(now);
        attendance.setRemark(remark);
        attendance.setCreateTime(now);

        // 判断打卡时间是否在允许范围内
        if (currentTime.isBefore(earliestTime)) {
            redirectAttributes.addFlashAttribute("message", "还未到打卡时间（7:45开始）");
            return "redirect:/attendance/checkIn";
        }
        if (currentTime.isAfter(latestTime)) {
            redirectAttributes.addFlashAttribute("message", "已超过打卡时间（8:30截止），请联系教师");
            return "redirect:/attendance/checkIn";
        }

        // 判断是否迟到
        if (currentTime.isAfter(classStartTime)) {
            attendance.setStatus("LATE");
            redirectAttributes.addFlashAttribute("message", "打卡成功，但已迟到！");
        } else {
            attendance.setStatus("NORMAL");
            redirectAttributes.addFlashAttribute("message", "打卡成功！");
        }

        attendanceService.createAttendance(attendance);
        return "redirect:/attendance/list";
    }

    // ==================== 考勤记录列表（含筛选） ====================
    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String quickFilter,   // today/week/month
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "checkInTime") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            Model model) {

        // 快速筛选：今天/本周/本月
        LocalDateTime now = LocalDateTime.now();
        if ("today".equals(quickFilter)) {
            startTime = now.toLocalDate().atStartOfDay();
            endTime = now.toLocalDate().plusDays(1).atStartOfDay();
        } else if ("week".equals(quickFilter)) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            LocalDate startOfWeek = now.toLocalDate().with(weekFields.dayOfWeek(), 1);
            startTime = startOfWeek.atStartOfDay();
            endTime = now.toLocalDate().plusDays(1).atStartOfDay();
        } else if ("month".equals(quickFilter)) {
            startTime = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
            endTime = now.toLocalDate().plusDays(1).atStartOfDay();
        }

        // 构建排序
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Attendance> records = attendanceService.getAttendanceByConditions(
                studentId, studentName, courseId, status, startTime, endTime, pageable);

        model.addAttribute("records", records.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", records.getTotalPages());
        model.addAttribute("totalElements", records.getTotalElements());
        model.addAttribute("studentId", studentId);
        model.addAttribute("studentName", studentName);
        model.addAttribute("courseId", courseId);
        model.addAttribute("status", status);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);
        model.addAttribute("quickFilter", quickFilter);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("size", size);

        return "attendance-list";
    }

    // 获取当前学生ID（简化实现，实际应从 Spring Security Context 获取）
    private String getCurrentStudentId() {
        // TODO: 替换为 SecurityContextHolder.getContext().getAuthentication().getName()
        return null;
    }

    // ==================== 批量导入页面 ====================
    @GetMapping("/import")
    public String importPage() {
        return "attendance-import";
    }

    // ==================== 处理文件上传导入 ====================
    @PostMapping("/import")
    public String importFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            ImportResult result = attendanceService.importFromExcel(file);

            StringBuilder msg = new StringBuilder();
            msg.append("导入完成！成功: ").append(result.getSuccessCount()).append("条");
            if (result.getFailCount() > 0) {
                msg.append("，失败: ").append(result.getFailCount()).append("条");
            }
            redirectAttributes.addFlashAttribute("message", msg.toString());

            if (result.hasErrors() && result.getErrorDetails().size() <= 10) {
                redirectAttributes.addFlashAttribute("errorDetails", result.getErrorDetails());
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("message", "导入失败: " + e.getMessage());
        }
        return "redirect:/attendance/import";
    }

    // ==================== 统计页面 ====================
    @GetMapping("/statistics")
    public String statistics(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            Model model) {

        if (studentId != null && !studentId.isEmpty()) {
            AttendanceStatisticsDTO stats;
            if (startTime != null && endTime != null) {
                stats = attendanceService.getStudentStatisticsByDateRange(studentId, startTime, endTime);
            } else {
                stats = attendanceService.getStudentStatistics(studentId);
            }
            model.addAttribute("stats", stats);
        }

        if (courseId != null && !courseId.isEmpty() && startTime != null && endTime != null) {
            AttendanceStatisticsDTO courseStats = attendanceService.getCourseStatistics(courseId, startTime, endTime);
            model.addAttribute("courseStats", courseStats);
        }

        model.addAttribute("studentId", studentId);
        model.addAttribute("courseId", courseId);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);

        return "attendance-statistics";
    }
}