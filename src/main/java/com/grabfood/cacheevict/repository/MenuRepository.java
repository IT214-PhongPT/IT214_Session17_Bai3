package com.grabfood.cacheevict.repository;

import com.grabfood.cacheevict.model.Menu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository mô phỏng tầng lưu trữ CSDL vật lý (Database) cho bảng Menu.
 * Dữ liệu ban đầu: Món 1 có giá 55.000 VNĐ.
 */
@Slf4j
@Repository
public class MenuRepository {

    private final Map<Long, Menu> database = new ConcurrentHashMap<>();

    public MenuRepository() {
        initData();
    }

    private void initData() {
        database.put(1L, Menu.builder().id(1L).dishName("Phở Bò").price(new BigDecimal("55000")).build());
        database.put(2L, Menu.builder().id(2L).dishName("Bún Chả").price(new BigDecimal("60000")).build());
    }

    public Optional<Menu> findById(Long id) {
        log.info("===> [DATABASE QUERY] Đang truy vấn CSDL cho Menu ID = {}...", id);
        return Optional.ofNullable(database.get(id));
    }

    public Menu save(Menu menu) {
        log.info("===> [DATABASE UPDATE] Đang ghi dữ liệu mới vào CSDL: [ID = {}, Món = {}, Giá = {} VNĐ]",
                menu.getId(), menu.getDishName(), menu.getPrice());
        database.put(menu.getId(), menu);
        return menu;
    }
}
