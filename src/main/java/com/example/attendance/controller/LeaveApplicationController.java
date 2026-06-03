package com.example.attendance.controller;

import com.example.attendance.dto.LeaveApplicationDTO;
import com.example.attendance.entity.LeaveApplication;
import com.example.attendance.service.LeaveApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/leave")
public class LeaveApplicationController {

    @Autowired
    private LeaveApplicationService leaveApplicationService;

    // 提交请假申请
    @PostMapping("/apply")
    public ResponseEntity<?> applyLeave(@RequestBody LeaveApplicationDTO dto) {
        log.info("收到请假申请请求: {}", dto);
        try {
            // 从 SecurityContext 获取当前学生ID（简化：从请求头或参数获取）
            String studentId = dto.getStudentId();
            LeaveApplication application = leaveApplicationService.apply(dto, studentId);
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "msg", "请假申请已提交",
                    "data", application
            ));
        } catch (RuntimeException e) {
            log.warn("请假申请失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", e.getMessage()));
        }
    }

    // 审批请假
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveLeave(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String remark,
            @RequestParam Integer approverId) {
        log.info("审批请假: id={}, approved={}", id, approved);
        try {
            LeaveApplication application = leaveApplicationService.approve(id, approved, remark, approverId);
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "msg", approved ? "已批准" : "已拒绝",
                    "data", application
            ));
        } catch (RuntimeException e) {
            log.warn("审批失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", e.getMessage()));
        }
    }

    // 查询请假详情
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        LeaveApplication application = leaveApplicationService.findById(id);
        if (application == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(application);
    }

    // 按学生查询（分页）
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Page<LeaveApplication>> getByStudent(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("applyTime").descending());
        return ResponseEntity.ok(leaveApplicationService.findByStudentId(studentId, pageable));
    }

    // 按状态查询（分页）
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<LeaveApplication>> getByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("applyTime").descending());
        return ResponseEntity.ok(leaveApplicationService.findByStatus(status, pageable));
    }

    // 待审批数量
    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Object>> getPendingCount() {
        long count = leaveApplicationService.countPending();
        return ResponseEntity.ok(Map.of("code", 200, "count", count));
    }
}