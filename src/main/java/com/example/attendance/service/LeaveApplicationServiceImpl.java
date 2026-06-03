package com.example.attendance.service;

import com.example.attendance.dto.LeaveApplicationDTO;
import com.example.attendance.entity.LeaveApplication;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.LeaveApplicationRepository;
import com.example.attendance.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public LeaveApplication apply(LeaveApplicationDTO dto, String studentId) {
        log.info("收到请假申请: studentId={}, dto={}", studentId, dto);

        // 验证学生
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.warn("学生不存在: {}", studentId);
                    return new RuntimeException("学生不存在");
                });

        // 验证请假时间不能是过去
        if (dto.getEndTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("不能请假过去的日期");
        }
        if (dto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("开始时间不能是过去");
        }

        // 验证结束时间必须晚于开始时间
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }

        // 验证请假时长（不能超过7天）
        Duration duration = Duration.between(dto.getStartTime(), dto.getEndTime());
        if (duration.toDays() > 7) {
            throw new RuntimeException("请假时间不能超过7天");
        }

        // 创建请假申请
        LeaveApplication application = new LeaveApplication();
        application.setStudent(student);
        application.setCourseId(dto.getCourseId());
        application.setStartTime(dto.getStartTime());
        application.setEndTime(dto.getEndTime());
        application.setReason(dto.getReason());
        application.setStatus("PENDING");
        application.setApplyTime(LocalDateTime.now());

        LeaveApplication saved = leaveApplicationRepository.save(application);
        log.info("请假申请已提交: id={}, studentId={}", saved.getId(), studentId);
        return saved;
    }

    @Override
    public LeaveApplication approve(Long id, boolean approved, String remark, Integer approverId) {
        log.info("审批请假: id={}, approved={}, approverId={}", id, approved, approverId);

        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("请假申请不存在"));

        if (!"PENDING".equals(application.getStatus())) {
            throw new RuntimeException("该申请已被审批，不能重复操作");
        }

        application.setStatus(approved ? "APPROVED" : "REJECTED");
        application.setApprovalTime(LocalDateTime.now());
        application.setApproverId(approverId);
        application.setApproverRemark(remark);

        LeaveApplication saved = leaveApplicationRepository.save(application);
        log.info("请假审批完成: id={}, status={}", id, saved.getStatus());
        return saved;
    }

    @Override
    public LeaveApplication findById(Long id) {
        return leaveApplicationRepository.findById(id).orElse(null);
    }

    @Override
    public Page<LeaveApplication> findByStudentId(String studentId, Pageable pageable) {
        return leaveApplicationRepository.findByStudentStudentId(studentId, pageable);
    }

    @Override
    public Page<LeaveApplication> findByStatus(String status, Pageable pageable) {
        return leaveApplicationRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<LeaveApplication> findByStudentIdAndStatus(String studentId, String status, Pageable pageable) {
        return leaveApplicationRepository.findByStudentStudentIdAndStatus(studentId, status, pageable);
    }

    @Override
    public long countPending() {
        return leaveApplicationRepository.countByStatus("PENDING");
    }
}