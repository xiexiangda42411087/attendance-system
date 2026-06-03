package com.example.attendance.service;

import com.example.attendance.dto.LeaveApplicationDTO;
import com.example.attendance.entity.LeaveApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaveApplicationService {
    LeaveApplication apply(LeaveApplicationDTO dto, String studentId);
    LeaveApplication approve(Long id, boolean approved, String remark, Integer approverId);
    LeaveApplication findById(Long id);
    Page<LeaveApplication> findByStudentId(String studentId, Pageable pageable);
    Page<LeaveApplication> findByStatus(String status, Pageable pageable);
    Page<LeaveApplication> findByStudentIdAndStatus(String studentId, String status, Pageable pageable);
    long countPending();
}