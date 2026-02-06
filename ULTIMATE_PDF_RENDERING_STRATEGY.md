# Chiến lược hiển thị PDF cuối cùng: Hiển thị theo dải dọc & ưu tiên sự mượt mà

## 1. Bối cảnh & Thách thức

Quá trình tối ưu hóa này giải quyết một trong những thách thức khó khăn nhất khi hiển thị PDF trên di động: xử lý các tệp PDF cực lớn (hàng trăm MB) chứa các trang có chiều dài không đồng đều, đặc biệt là các trang truyện tranh dài (webtoon-style), mà không gặp lỗi `OutOfMemoryError` và vẫn đảm bảo trải nghiệm cuộn mượt mà.

Các vấn đề chính cần giải quyết:
- **Lỗi hết bộ nhớ**: Tải một trang PDF dài thành một `Bitmap` duy nhất sẽ làm sập ứng dụng.
- **Trải nghiệm người dùng**: Người dùng mong muốn một trải nghiệm cuộn liền mạch, giống như đọc một trang web, ngay cả khi nội dung được chia thành nhiều trang PDF.
- **Nội dung không đồng đều**: Các trang PDF có thể chứa các vùng trắng lớn ở cuối, gây ra các khoảng trống không mong muốn khi hiển thị.

## 2. Giải pháp: Hiển thị theo dải dọc (Vertical Strip Rendering)

Giải pháp cuối cùng của chúng ta là một hệ thống hiển thị theo dải dọc, được thiết kế để mạnh mẽ, hiệu quả về bộ nhớ và linh hoạt.

### Cách hoạt động:

1.  **Phân tích trước (Pre-analysis)**: Khi một tệp PDF được tải, `PdfReaderViewModel` sẽ lặp qua tất cả các trang và lưu trữ kích thước (chiều rộng, chiều cao) của từng trang vào `PageInfo`. Đây là bước chuẩn bị quan trọng.

2.  **Chia để trị**: Composable `PdfReaderScreen` sử dụng một `LazyColumn` thông minh. Đối với mỗi trang PDF, nó sẽ:
    a.  Tính toán chiều cao của trang sau khi đã được thu nhỏ để vừa với chiều rộng màn hình.
    b.  **Nếu trang ngắn**: Hiển thị nó như một mục duy nhất.
    c.  **Nếu trang dài**: Tự động tính toán số lượng "dải" (strips) dọc cần thiết để hiển thị toàn bộ trang và hiển thị chúng thành nhiều mục liên tiếp trong `LazyColumn`.

3.  **Hiển thị theo yêu cầu**: `ViewModel` cung cấp một hàm `renderPageStrip` có khả năng hiển thị chỉ một phần hình chữ nhật cụ thể của một trang PDF. Bằng cách sử dụng một phép biến đổi `Matrix`, nó chỉ hiển thị phần cần thiết vào một `Bitmap` có kích thước hợp lý, ngăn chặn việc sử dụng bộ nhớ quá mức.

4.  **Tính toán kích thước chính xác**: Logic trong `LazyColumn` tính toán chính xác chiều cao của mỗi dải, đặc biệt đảm bảo dải cuối cùng của một trang dài chỉ có chiều cao bằng phần nội dung còn lại. Điều này loại bỏ các khoảng trống do lỗi logic.

5.  **Chuyển đổi đơn vị chính xác**: Một lỗi nghiêm trọng đã được khắc phục, trong đó chiều cao được tính bằng pixel đã bị diễn giải sai thành DP. Giờ đây, chiều cao của mỗi dải được chuyển đổi chính xác sang DP bằng `LocalDensity.current` trước khi được áp dụng, đảm bảo kích thước hiển thị luôn chính xác.

## 3. Quyết định về trải nghiệm người dùng: Ưu tiên sự mượt mà

Trong quá trình gỡ lỗi, chúng ta đã phát hiện ra rằng một số tệp PDF có các vùng trắng lớn được nhúng sẵn ở cuối trang. Chúng ta đã phải đối mặt với một sự lựa chọn quan trọng:

*   **Lựa chọn A: Cắt bỏ vùng trắng động**: Phân tích bitmap sau khi hiển thị và thay đổi kích thước của nó. **Nhược điểm**: Gây ra hiệu ứng "giật" hoặc "nhấp nháy" khi cuộn, vì kích thước thay đổi sau khi đã được vẽ lần đầu.
*   **Lựa chọn B: Hiển thị trung thực**: Hiển thị nội dung PDF một cách chính xác như nó vốn có, bao gồm cả vùng trắng. **Nhược điểm**: Có thể có các khoảng trống nếu tệp gốc có.

**Quyết định cuối cùng của chúng ta là chọn Lựa chọn B.**

Chúng ta ưu tiên một trải nghiệm cuộn hoàn toàn mượt mà và không có giật lag. Việc hiển thị trung thực nội dung của tệp PDF, ngay cả khi nó chứa vùng trắng, được coi là một sự đánh đổi chấp nhận được để đạt được sự ổn định và hiệu suất cao nhất.

## 4. Kết luận

Kiến trúc hiện tại là sự kết hợp của việc quản lý bộ nhớ thông minh, tính toán bố cục linh hoạt và một quyết định có ý thức về trải nghiệm người dùng. Nó có khả năng xử lý hiệu quả các tệp PDF có kích thước và độ dài bất kỳ, mang lại trải nghiệm đọc truyện tranh liền mạch, mượt mà và ổn định.
