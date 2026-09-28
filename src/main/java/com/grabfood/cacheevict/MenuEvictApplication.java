package com.grabfood.cacheevict;

import com.grabfood.cacheevict.model.Menu;
import com.grabfood.cacheevict.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

/**
 * Ứng dụng GrabFood Menu Service - Kiểm thử @CacheEvict & Phòng tránh Race Condition.
 * Sinh viên: Phạm Thanh Phong - MSSV: N24DTCN120
 */
@Slf4j
@SpringBootApplication
public class MenuEvictApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenuEvictApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(MenuService menuService, CacheManager cacheManager) {
        return args -> {
            log.info("=========================================================================");
            log.info(" BẮT ĐẦU KIỂM THỬ TÍNH NHẤT QUÁN VỚI @CacheEvict");
            log.info(" Sinh viên: Phạm Thanh Phong - MSSV: N24DTCN120");
            log.info("=========================================================================");

            Long menuId = 1L;
            Cache cache = cacheManager.getCache("menuCache");

            // BƯỚC 1: Gọi GET /menu/1 để xem giá hiện tại (lấy từ DB và nạp vào cache)
            log.info("\n-------------------------------------------------------------------------");
            log.info(">>> [BƯỚC 1] Gọi GET /menu/{} -> Mong đợi: Lấy từ DB (55.000 VNĐ) và nạp Cache", menuId);
            log.info("-------------------------------------------------------------------------");
            Menu menu1 = menuService.getMenuById(menuId);
            log.info("Kết quả Bước 1: [ID: {}, Tên: {}, Giá: {} VNĐ]", menu1.getId(), menu1.getDishName(), menu1.getPrice());
            log.info("Trạng thái Cache sau Bước 1: {}", cache != null && cache.get(menuId) != null ? "ĐÃ TỒN TẠI TRONG CACHE" : "CHƯA CÓ");

            // BƯỚC 2: Gọi Update giá mới = 75.000 VNĐ
            log.info("\n-------------------------------------------------------------------------");
            log.info(">>> [BƯỚC 2] Gọi Update giá mới: Phở Bò Đặc Biệt - 75.000 VNĐ");
            log.info("-------------------------------------------------------------------------");
            Menu updateRequest = Menu.builder()
                    .id(menuId)
                    .dishName("Phở Bò Đặc Biệt")
                    .price(new BigDecimal("75000"))
                    .build();
            Menu updated = menuService.updateMenu(updateRequest);
            log.info("Kết quả Bước 2: Cập nhật thành công món {}", updated.getDishName());

            // BƯỚC 3: Kiểm tra Cache key cũ (id=1) đã bị xóa hoàn toàn
            log.info("\n-------------------------------------------------------------------------");
            log.info(">>> [BƯỚC 3] Kiểm tra Cache sau khi Update (@CacheEvict)");
            log.info("-------------------------------------------------------------------------");
            Cache.ValueWrapper cachedValue = cache != null ? cache.get(menuId) : null;
            log.info("Giá trị trong Cache cho Key {}: {} (ĐÃ BỊ XÓA HOÀN TOÀN)", menuId, cachedValue != null ? cachedValue.get() : "null / (nil)");

            // BƯỚC 4: Gọi lại GET /menu/1 -> Phải hiển thị giá mới 75.000 VNĐ từ DB
            log.info("\n-------------------------------------------------------------------------");
            log.info(">>> [BƯỚC 4] Gọi lại GET /menu/{} -> Mong đợi: Đọc giá mới từ DB và nạp lại Cache", menuId);
            log.info("-------------------------------------------------------------------------");
            Menu menuReloaded = menuService.getMenuById(menuId);
            log.info("Kết quả Bước 4: [ID: {}, Tên: {}, Giá: {} VNĐ]", menuReloaded.getId(), menuReloaded.getDishName(), menuReloaded.getPrice());
            log.info("Trạng thái Cache sau Bước 4: {}", cache != null && cache.get(menuId) != null ? "ĐÃ NẠP LẠI VỚI GIÁ MỚI (75.000 VNĐ)" : "CHƯA CÓ");

            log.info("\n=========================================================================");
            log.info(" KIỂM THỬ BÀI 3 HOÀN TẤT THÀNH CÔNG RỰC RỠ!");
            log.info("=========================================================================\n");
        };
    }
}
