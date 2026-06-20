package com.syariahpulse.common;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * Manual trigger for the nightly batch — for verifying the data pipeline
 * (import/indicator/score/rank) outside the scheduled 18:00 WIB run, e.g.
 * before relying on it for the first time. Not authenticated; do not expose
 * this beyond local/internal use without adding access control.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final NightlyBatchScheduler nightlyBatchScheduler;

    @PostMapping("/batch/run")
    public Map<String, String> runBatchNow() {
        nightlyBatchScheduler.runBatch(LocalDate.now());
        return Map.of("status", "completed");
    }
}
