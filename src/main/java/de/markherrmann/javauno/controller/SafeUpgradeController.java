package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.service.HousekeepingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/upgrade")
public class SafeUpgradeController {

    private final HousekeepingService housekeepingService;

    @Autowired
    public SafeUpgradeController(HousekeepingService housekeepingService) {
        this.housekeepingService = housekeepingService;
    }

    @GetMapping("/safe")
    public String getUpgradeSafeState(){
        return housekeepingService.isUpgradeSafe() ? "safe" : "unsafe";
    }


}
