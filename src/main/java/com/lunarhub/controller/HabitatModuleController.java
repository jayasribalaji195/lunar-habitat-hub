package com.lunarhub.controller;

import com.lunarhub.dto.CreateModuleRequest;
import com.lunarhub.model.HabitatModule;
import com.lunarhub.service.LifeSupportOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lunar")
@CrossOrigin(origins = "*")
public class HabitatModuleController {

    private final LifeSupportOrderService orderService;

    public HabitatModuleController(LifeSupportOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/modules")
    public HabitatModule createModule(@RequestBody CreateModuleRequest request) {
        return orderService.createModule(request);
    }

    @GetMapping("/modules")
    public List<HabitatModule> listModules() {
        return orderService.listModules();
    }
}
