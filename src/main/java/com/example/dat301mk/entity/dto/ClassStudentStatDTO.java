package com.example.dat301mk.entity.dto;

public class ClassStudentStatDTO {
    private Long classId;
    private String className;
    private String classCode;
    private int soBaiDaThi;
    private int soTaiLieu;

    public ClassStudentStatDTO(Long classId, String className, String classCode, int soBaiDaThi, int soTaiLieu) {
        this.classId = classId;
        this.className = className;
        this.classCode = classCode;
        this.soBaiDaThi = soBaiDaThi;
        this.soTaiLieu = soTaiLieu;
    }

    public Long getClassId() { return classId; }
    public String getClassName() { return className; }
    public String getClassCode() { return classCode; }
    public int getSoBaiDaThi() { return soBaiDaThi; }
    public int getSoTaiLieu() { return soTaiLieu; }
}
