package com.example.attendance.controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Arrays;
class Result<T>{
    private Integer code;
    private String message;
    private T data;
    public static <T> Result<T> success(T data){
        Result<T> result=new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    public static <T> Result<T> fail(String message){
        Result<T> result=new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    public Integer getCode(){
        return code;
    }
    public void setCode(Integer code){
        this.code=code;
    }
    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message=message;
    }
    public T getData(){
        return data;
    }
    public void setData(T data){
        this.data=data;
    }
}
class Student {
    private String studentId;
    private String name;
    private String className;
    public Student(String studentId, String name, String className) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
    }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}
class AttendanceRecord {
    private String studentId;
    private String status;
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
@RestController
@RequestMapping("/student")
public class StudentController2 {
    @GetMapping("/info/{studentId}")
    public Result<Student> getStudentInfo(@PathVariable String studentId){
        Student student=new Student(studentId,"谢祥达","2024级网络空间安全");
        return Result.success(student);
    }
    @GetMapping("/list")
    public Result<List<Student>> getStudentList(@RequestParam String className,@RequestParam(defaultValue = "1") Integer page){
        List<Student> studentList=Arrays.asList(new Student("42411087","谢祥达",className),new Student("42411088","张三",className));
        return Result.success(studentList);
    }
}
@RestController
@RequestMapping("/attendance")
class AttendanceController{
    @PostMapping("/update")
    public Result<String> updateAttendance(@RequestBody AttendanceRecord attendanceRecord){
        return Result.success("学号为"+attendanceRecord.getStudentId()+"的考勤记录更新成功");
    }
}
