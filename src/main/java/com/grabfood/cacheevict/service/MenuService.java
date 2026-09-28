package com.grabfood.cacheevict.service;

import com.grabfood.cacheevict.model.Menu;
import com.grabfood.cacheevict.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service quản lý Menu với chiến lược Cache-Aside và xóa vô hiệu hóa (@CacheEvict).
 * 
 * =========================================================================================
 * GIẢI THÍCH CHỌN @CacheEvict (XÓA CACHE) THAY VÌ @CachePut (GHI ĐÈ CACHE):
 * =========================================================================================
 * 1. PHÒNG TRÁNH RACE CONDITION (Xung đột đa luồng cập nhật đồng thời):
 *    - Nếu dùng @CachePut: Giả sử Luồng A cập nhật giá = 70k, Luồng B cập nhật giá = 75k.
 *      Do độ trễ mạng, Luồng A ghi DB trước (70k), sau đó Luồng B ghi DB sau (75k).
 *      Nhưng khi ghi đè vào Redis, Luồng B ghi trước (75k), Luồng A ghi sau (70k).
 *      HỆ QUẢ: DB có giá 75k nhưng Redis lại lưu giá cũ 70k vĩnh viễn (Sai lệch dữ liệu trầm trọng!).
 *    - Nếu dùng @CacheEvict: Cả 2 luồng đều XÓA key khỏi Redis. Luồng đọc tiếp theo sẽ tự động
 *      truy vấn CSDL (nơi đã lưu giá 75k mới nhất) và nạp lại vào Cache. Dữ liệu luôn đảm bảo nhất quán tuyệt đối!
 * 
 * 2. TỐI ƯU HIỆU NĂNG CHO HỆ THỐNG ĐỌC NHIỀU - GHI ÍT:
 *    - Việc xóa key diễn ra tức thì trong O(1), không tốn chi phí CPU để Serialize/Deserialize đối tượng DTO phức tạp
 *      khi cập nhật dữ liệu. Chỉ khi có request đọc mới thực hiện tính toán và nạp lại.
 * =========================================================================================
 * 
 * Sinh viên: Phạm Thanh Phong - MSSV: N24DTCN120
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    /**
     * Lấy thông tin thực đơn theo ID.
     * Sử dụng @Cacheable để lưu kết quả vào vùng 'menuCache' với key là id.
     */
    @Cacheable(value = "menuCache", key = "#id")
    public Menu getMenuById(Long id) {
        log.info(">>> [SERVICE] Đang gọi CSDL lấy thông tin Menu ID = {}", id);
        return menuRepository.findById(id).orElse(null);
    }

    /**
     * Cập nhật thông tin thực đơn (món ăn, giá bán).
     * 
     * Thứ tự thực thi chuẩn của Cache-Aside Pattern:
     * 1. Cập nhật Database trước: menuRepository.save(menu).
     * 2. Spring AOP tự động thực thi @CacheEvict sau khi phương thức hoàn tất,
     *    xóa sạch key '#menu.id' khỏi cache 'menuCache'.
     */
    @CacheEvict(value = "menuCache", key = "#menu.id")
    public Menu updateMenu(Menu menu) {
        if (menu == null || menu.getId() == null) {
            throw new IllegalArgumentException("Thông tin Menu và ID không được để trống!");
        }

        log.info(">>> [SERVICE] BƯỚC 1: Cập nhật CSDL trước cho Menu ID = {} với giá mới = {} VNĐ",
                menu.getId(), menu.getPrice());
        
        // Cập nhật CSDL trước
        Menu updatedMenu = menuRepository.save(menu);

        log.info(">>> [SERVICE] BƯỚC 2: Chuẩn bị kích hoạt @CacheEvict xóa key '{}' khỏi 'menuCache'...", menu.getId());
        return updatedMenu;
    }
}
