package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.dto.AttendanceStatisticsDTO;
import com.example.attendance.dto.ImportResult;
import jakarta.persistence.criteria.Predicate;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    // 日期格式化器列表（兼容多种格式）
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    );

    @Override
    public String createAttendance(Attendance attendance) {
        // 校验学生是否存在
        if (attendance.getStudent() == null || attendance.getStudent().getStudentId() == null) {
            throw new RuntimeException("学生信息不能为空");
        }
        Student student = studentRepository.findById(attendance.getStudent().getStudentId())
                .orElseThrow(() -> new RuntimeException("学生不存在！"));

        attendance.setStudent(student);
        attendance.setStudentName(student.getName());
        attendance.setCreateTime(LocalDateTime.now());

        // 判断迟到（8:00前正常，之后迟到，供打卡时调用）
        if (attendance.getCheckInTime() == null) {
            attendance.setCheckInTime(LocalDateTime.now());
        }
        if (attendance.getStatus() == null) {
            attendance.setStatus("NORMAL");
        }

        attendanceRepository.save(attendance);
        return "考勤记录创建成功";
    }

    @Override
    public List<Attendance> getAttendanceByStudentId(String studentId) {
        return attendanceRepository.findByStudentStudentId(studentId);
    }

    @Override
    public Page<Attendance> getAttendancePage(Pageable pageable) {
        return attendanceRepository.findAll(pageable);
    }

    @Override
    public Page<Attendance> getAttendancePageByStudentId(String studentId, Pageable pageable) {
        return attendanceRepository.findByStudentStudentId(studentId, pageable);
    }

    @Override
    public Page<Attendance> getAttendanceByConditions(
            String studentId, String studentName, String courseId,
            String status, LocalDateTime startTime, LocalDateTime endTime,
            Pageable pageable) {

        Specification<Attendance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (studentId != null && !studentId.isEmpty()) {
                predicates.add(cb.equal(root.get("student").get("studentId"), studentId));
            }
            if (studentName != null && !studentName.isEmpty()) {
                predicates.add(cb.like(root.get("studentName"), "%" + studentName + "%"));
            }
            if (courseId != null && !courseId.isEmpty()) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInTime"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkInTime"), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return attendanceRepository.findAll(spec, pageable);
    }

    @Override
    public ImportResult importFromExcel(MultipartFile file) {
        ImportResult result = new ImportResult();

        // 1. 文件验证
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new RuntimeException("文件格式不正确，仅支持 .xlsx 和 .xls");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过10MB");
        }

        // 2. 解析 Excel
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            if (lastRowNum < 1) {
                throw new RuntimeException("Excel文件为空（至少需要标题行+1条数据）");
            }

            // 3. 逐行读取（跳过标题行）
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                try {
                    String studentId = getCellValueAsString(row.getCell(0));
                    String courseId = getCellValueAsString(row.getCell(1));
                    String checkInTimeStr = getCellValueAsString(row.getCell(2));
                    String status = getCellValueAsString(row.getCell(3));
                    String remark = getCellValueAsString(row.getCell(4));
                    String seatRow = getCellValueAsString(row.getCell(5));
                    String seatCol = getCellValueAsString(row.getCell(6));

                    // 数据验证
                    if (studentId.isEmpty()) {
                        result.incrementFail(String.valueOf(i + 1), "学号为空");
                        continue;
                    }
                    if (courseId.isEmpty()) {
                        result.incrementFail(String.valueOf(i + 1), "课程编号为空");
                        continue;
                    }

                    // 查找学生
                    Optional<Student> studentOpt = studentRepository.findById(studentId);
                    if (studentOpt.isEmpty()) {
                        result.incrementFail(String.valueOf(i + 1), "学号[" + studentId + "]不存在");
                        continue;
                    }

                    // 解析时间
                    LocalDateTime checkInTime = parseDateTime(checkInTimeStr);
                    if (checkInTime == null) {
                        result.incrementFail(String.valueOf(i + 1), "日期格式不正确: " + checkInTimeStr);
                        continue;
                    }

                    // 验证状态值
                    if (!status.isEmpty() && !Arrays.asList("NORMAL", "LATE", "EARLY", "ABSENT").contains(status)) {
                        result.incrementFail(String.valueOf(i + 1), "状态值无效: " + status);
                        continue;
                    }

                    // 构建考勤记录
                    Attendance attendance = new Attendance();
                    attendance.setStudent(studentOpt.get());
                    attendance.setStudentName(studentOpt.get().getName());
                    attendance.setCourseId(courseId);
                    attendance.setCheckInTime(checkInTime);
                    attendance.setStatus(status.isEmpty() ? "NORMAL" : status);
                    attendance.setRemark(remark);
                    attendance.setCreateTime(LocalDateTime.now());

                    if (!seatRow.isEmpty()) {
                        attendance.setSeatRow(Integer.parseInt(seatRow));
                    }
                    if (!seatCol.isEmpty()) {
                        attendance.setSeatCol(Integer.parseInt(seatCol));
                    }

                    attendanceRepository.save(attendance);
                    result.incrementSuccess();

                } catch (NumberFormatException e) {
                    result.incrementFail(String.valueOf(i + 1), "数字格式错误");
                } catch (Exception e) {
                    result.incrementFail(String.valueOf(i + 1), e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("解析Excel失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public AttendanceStatisticsDTO getStudentStatistics(String studentId) {
        long total = attendanceRepository.countByStudentStudentId(studentId);
        long normal = attendanceRepository.countByStudentStudentIdAndStatus(studentId, "NORMAL");
        long late = attendanceRepository.countByStudentStudentIdAndStatus(studentId, "LATE");
        long early = attendanceRepository.countByStudentStudentIdAndStatus(studentId, "EARLY");
        long absent = attendanceRepository.countByStudentStudentIdAndStatus(studentId, "ABSENT");

        return AttendanceStatisticsDTO.builder()
                .totalCount(total)
                .normalCount(normal)
                .lateCount(late)
                .earlyCount(early)
                .absentCount(absent)
                .attendanceRate(total > 0 ? (double) normal / total * 100 : 0)
                .build();
    }

    @Override
    public AttendanceStatisticsDTO getStudentStatisticsByDateRange(String studentId,
                                                                   LocalDateTime startTime,
                                                                   LocalDateTime endTime) {
        long total = attendanceRepository.countByStudentIdAndDateRange(studentId, startTime, endTime);
        long normal = attendanceRepository.countByStudentIdAndStatusAndDateRange(studentId, "NORMAL", startTime, endTime);
        long late = attendanceRepository.countByStudentIdAndStatusAndDateRange(studentId, "LATE", startTime, endTime);
        long early = attendanceRepository.countByStudentIdAndStatusAndDateRange(studentId, "EARLY", startTime, endTime);
        long absent = attendanceRepository.countByStudentIdAndStatusAndDateRange(studentId, "ABSENT", startTime, endTime);

        return AttendanceStatisticsDTO.builder()
                .totalCount(total)
                .normalCount(normal)
                .lateCount(late)
                .earlyCount(early)
                .absentCount(absent)
                .attendanceRate(total > 0 ? (double) normal / total * 100 : 0)
                .build();
    }

    @Override
    public AttendanceStatisticsDTO getCourseStatistics(String courseId,
                                                       LocalDateTime startTime,
                                                       LocalDateTime endTime) {
        List<Object[]> results = attendanceRepository.countByCourseGroupByStatus(courseId, startTime, endTime);

        long total = 0, normal = 0, late = 0, early = 0, absent = 0;
        for (Object[] row : results) {
            String status = (String) row[0];
            long count = (Long) row[1];
            total += count;
            switch (status) {
                case "NORMAL": normal = count; break;
                case "LATE": late = count; break;
                case "EARLY": early = count; break;
                case "ABSENT": absent = count; break;
            }
        }

        return AttendanceStatisticsDTO.builder()
                .totalCount(total)
                .normalCount(normal)
                .lateCount(late)
                .earlyCount(early)
                .absentCount(absent)
                .attendanceRate(total > 0 ? (double) normal / total * 100 : 0)
                .build();
    }

    // ==================== 工具方法 ====================

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num) && !Double.isInfinite(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        dateStr = dateStr.trim();

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        // 尝试只解析日期（默认时间00:00:00）
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !getCellValueAsString(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}