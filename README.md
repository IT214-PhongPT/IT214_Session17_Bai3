# IT214 - Session 17 - Bài Tập 3: Đảm bảo nhất quán dữ liệu với @CacheEvict

- **Học phần:** Kiến trúc Microservices (IT214)
- **Sinh viên:** Phạm Thanh Phong
- **MSSV:** N24DTCN120
- **Mã bài tập:** `IT214_Session17_BT3`

---

## 1. Giới thiệu dự án
Dự án triển khai mô hình **Cache-Aside Pattern** với cơ chế **Xóa Cache Vô hiệu hóa (`@CacheEvict`)** trên hệ thống GrabFood Menu Service.

### Điểm nhấn kỹ thuật:
1. **Phòng tránh Race Condition:** Giải thích chi tiết vì sao sử dụng `@CacheEvict(value = "menuCache", key = "#menu.id")` tối ưu và an toàn hơn `@CachePut` khi có nhiều luồng ghi đồng thời.
2. **Quy trình chuẩn Cache-Aside:** Cập nhật CSDL trước (`menuRepository.save(menu)`), sau đó xóa key cũ trong Redis để lần đọc sau nạp dữ liệu mới nhất từ CSDL.

---

## 2. Hướng dẫn kiểm thử 4 bước

```bash
# Bước 1: Tra cứu món ăn ban đầu (lấy từ DB và lưu Cache)
curl -X GET http://localhost:8080/menu/1
# Kết quả: Giá 55000 VNĐ

# Bước 2: Cập nhật giá mới qua API PUT
curl -X PUT http://localhost:8080/menu \
  -H "Content-Type: application/json" \
  -d '{"id": 1, "dish_name": "Phở Bò Đặc Biệt", "price": 75000}'

# Bước 3: Kiểm tra key trên Redis CLI -> Đã bị xóa (nil)
redis-cli GET "menuCache::1"
# Output: (nil)

# Bước 4: Gọi lại API GET -> Hiển thị giá mới 75000 VNĐ
curl -X GET http://localhost:8080/menu/1
# Kết quả: Giá 75000 VNĐ
```
