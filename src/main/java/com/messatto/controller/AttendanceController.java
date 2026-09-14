package com.messatto.controller;

import com.messatto.dto.attendance.AttendanceHistoryResponse;
import com.messatto.dto.attendance.ScanRequest;
import com.messatto.dto.attendance.ScanResponse;
import com.messatto.security.UserPrincipal;
import com.messatto.service.AttendanceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/attendance/scan")
    public ResponseEntity<ScanResponse> scan(
            @Valid @RequestBody ScanRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(attendanceService.scan(principal, request));
    }

    @GetMapping("/student/attendance")
    public ResponseEntity<List<AttendanceHistoryResponse>> history(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(attendanceService.getHistory(principal));
    }
}
