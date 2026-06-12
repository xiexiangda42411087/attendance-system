package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceStatisticsDTO;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.StudentService;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.Course;
import com.example.attendance.service.CourseService;
import com.example.attendance.service.UserService;
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
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/attendance")
public class AttendanceViewController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @GetMapping("/checkIn")
    public String checkInPage(Model model) {
        // 查询所有课程
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        model.addAttribute("currentTime", LocalDateTime.now());
        return "attendance-check-in";
    }

    // ==================== 提交打卡 ====================
    @PostMapping("/checkIn")
    public String checkIn(@RequestParam String courseId,
                          @RequestParam(required = false) String remark,
                          RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            redirectAttributes.addFlashAttribute("message", "请先登录");
            return "redirect:/login-page";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null || user.getStudentId() == null) {
            redirectAttributes.addFlashAttribute("message", "未绑定学号");
            return "redirect:/attendance/checkIn";
        }
        String studentId = user.getStudentId();

        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            redirectAttributes.addFlashAttribute("message", "学生信息不存在");
            return "redirect:/attendance/checkIn";
        }

        // 从课程表获取上下课时间
        Course course = courseService.getCourseById(courseId);
        if (course == null || course.getStartTime() == null || course.getEndTime() == null) {
            redirectAttributes.addFlashAttribute("message", "课程信息不完整");
            return "redirect:/attendance/checkIn";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        LocalTime classStartTime = course.getStartTime();
        LocalTime classEndTime = course.getEndTime();

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setStudentName(student.getName());
        attendance.setCourseId(courseId);
        attendance.setCheckInTime(now);
        attendance.setRemark(remark);
        attendance.setCreateTime(now);

        if (currentTime.isBefore(classStartTime)) {
            redirectAttributes.addFlashAttribute("message", "还未到上课时间（" + classStartTime + "开始）");
            return "redirect:/attendance/checkIn";
        }
        if (currentTime.isAfter(classEndTime)) {
            redirectAttributes.addFlashAttribute("message", "已下课（" + classEndTime + "），不能打卡");
            return "redirect:/attendance/checkIn";
        }

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

        // 从 session 获取当前用户角色和学号（通过 SecurityContext）
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        String currentRole = auth.getAuthorities().iterator().next().getAuthority();

        // USER 角色只能查自己的记录，强制覆盖 studentId
        if ("ROLE_USER".equals(currentRole)) {
            User user = userService.findByUsername(currentUsername);
            if (user != null && user.getStudentId() != null) {
                studentId = user.getStudentId();
            }
        }

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
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);

        return "attendance-list";
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

    @GetMapping("/statistics")
    public String statistics(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String courseId,
            Model model) {

        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);

        // 学号 + 课程 = 该学生该课程的统计
        if (studentId != null && !studentId.isEmpty() && courseId != null && !courseId.isEmpty()) {
            AttendanceStatisticsDTO stats = attendanceService.getStudentStatisticsByCourse(studentId, courseId);
            model.addAttribute("stats", stats);
        }
        // 仅学号 = 该学生全部统计
        else if (studentId != null && !studentId.isEmpty()) {
            AttendanceStatisticsDTO stats = attendanceService.getStudentStatistics(studentId);
            model.addAttribute("stats", stats);
        }

        model.addAttribute("studentId", studentId);
        model.addAttribute("courseId", courseId);

        return "attendance-statistics";
    }
}