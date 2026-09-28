package com.grabfood.cacheevict.controller;

import com.grabfood.cacheevict.model.Menu;
import com.grabfood.cacheevict.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller cho API Quản lý Menu GrabFood.
 */
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMenu(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        Menu menu = menuService.getMenuById(id);
        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("executionTimeMs", duration);
        response.put("data", menu);

        if (menu == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateMenu(@RequestBody Menu menu) {
        Menu updated = menuService.updateMenu(menu);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UPDATED_AND_CACHE_EVICTED");
        response.put("message", "Đã cập nhật database thành công và xóa cache tương ứng!");
        response.put("data", updated);

        return ResponseEntity.ok(response);
    }
}
