package com.grabfood.cacheevict;

import com.grabfood.cacheevict.config.CacheConfig;
import com.grabfood.cacheevict.model.Menu;
import com.grabfood.cacheevict.repository.MenuRepository;
import com.grabfood.cacheevict.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử tính năng @CacheEvict và luồng Cache-Aside trong MenuService.
 */
@SpringBootTest(classes = {MenuEvictApplication.class, CacheConfig.class})
class MenuServiceEvictTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private CacheManager cacheManager;

    @SpyBean
    private MenuRepository menuRepository;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache("menuCache");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("Kiểm tra toàn diện quy trình: Đọc -> Cache -> Update (Evict) -> Đọc lại")
    void testCacheEvictWorkflow() {
        Long menuId = 1L;
        Cache cache = cacheManager.getCache("menuCache");
        assertNotNull(cache);

        // 1. Lần đầu đọc Menu 1: Cache Miss -> Truy vấn DB và nạp Cache (giá 55k)
        Menu initial = menuService.getMenuById(menuId);
        assertNotNull(initial);
        assertEquals(new BigDecimal("55000"), initial.getPrice());
        assertNotNull(cache.get(menuId), "Cache phải chứa Menu 1 sau lần đọc đầu");

        // 2. Lần thứ 2 đọc lại Menu 1: Cache Hit -> Bỏ qua DB
        Menu cachedResult = menuService.getMenuById(menuId);
        assertEquals(new BigDecimal("55000"), cachedResult.getPrice());
        verify(menuRepository, times(1)).findById(menuId);

        // 3. Thực hiện Update: Đổi giá thành 75k -> Kích hoạt @CacheEvict
        Menu updateRequest = Menu.builder()
                .id(menuId)
                .dishName("Phở Bò Đặc Biệt")
                .price(new BigDecimal("75000"))
                .build();
        Menu updated = menuService.updateMenu(updateRequest);
        assertEquals(new BigDecimal("75000"), updated.getPrice());
        verify(menuRepository, times(1)).save(any(Menu.class));

        // Kiểm tra ngay sau update: Key '1' phải bị xóa khỏi cache (trả về null)
        assertNull(cache.get(menuId), "Key 1 phải bị @CacheEvict xóa sạch khỏi cache sau khi update!");

        // 4. Đọc lại Menu 1: Cache Miss -> Truy vấn lại DB để lấy giá mới 75k
        Menu newResult = menuService.getMenuById(menuId);
        assertEquals(new BigDecimal("75000"), newResult.getPrice());
        assertEquals("Phở Bò Đặc Biệt", newResult.getDishName());
        verify(menuRepository, times(2)).findById(menuId);

        // Kiểm tra sau khi đọc lại: Cache đã được nạp lại với giá mới 75k
        assertNotNull(cache.get(menuId));
        Menu reCached = (Menu) cache.get(menuId).get();
        assertEquals(new BigDecimal("75000"), reCached.getPrice());
    }
}
