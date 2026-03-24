package com.example.attendance.controller;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
@RestController
public class StudentController {
    @GetMapping("/student/info")
    public String getStudentInfo(){
        return "姓名：谢祥达，学号：42411087，班级：2024级网络空间安全";
    }
    @PostMapping("/student/attendance")
    public String checkAttendance(@RequestBody String studentId){
        return "学号为 "+studentId+" 的学生打卡成功！";
    }
    @GetMapping("/student/courses")
    public List<String> getCourseList(){
        return Arrays.asList(
                "Java EE开发实践",
                "大众排球"
        );
    }
}
