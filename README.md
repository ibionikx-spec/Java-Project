<div align="center">
 <h1>PROJECT TOPIC</h2>
<H2>Hệ thống quản lý quy trình sáng tác và xuất bản Manga</H2>
<h2>Manga Creation Workflow and Publishing Management System</h2>
</div>

## Context
- Trong ngành công nghiệp Manga, quá trình từ lúc sáng tác đến khi xuất bản đòi hỏi sự phối hợp chặt chẽ giữa nhiều bên: tác giả, trợ lý, biên tập viên và hội đồng biên tập. Hệ thống hỗ trợ quản lý toàn bộ quy trình này, từ nộp bản thảo, phân công công việc nội bộ studio, đến dữ liệu bình chọn và ra quyết định xuất bản.

## Problems
- Tác giả và trợ lý phải dùng nhiều ứng dụng khác nhau để trao đổi công việc, dễ nhầm lẫn và khó kiểm soát tiến độ từng trang, từng khung hình.
- Biên tập viên và hội đồng không có công cụ chung để theo dõi xem studio đang làm đến đâu, dẫn đến chậm deadline và thiếu thông tin khi ra quyết định."

## Primary Actors
- Mangaka
- Assistant 
- Tantou Editor
- Editorial Board"

## Functional Requirements
### Mangaka
- Tạo hồ sơ giới thiệu series mới và nộp bản thảo sơ bộ để trình lên hội đồng xét duyệt
- Chọn từng vùng trên trang truyện và giao việc cụ thể cho từng trợ lý (vẽ nền, tô bóng, hiệu ứng…)
- Xem bản tổng hợp sau khi trợ lý hoàn thành, phê duyệt hoặc yêu cầu chỉnh sửa ngay trên trang
- Theo dõi thứ hạng của series mình trên bảng xếp hạng và nhận thông báo khi series có nguy cơ bị huỷ

### Assistant
- Xem danh sách công việc được giao, tải file trang truyện cần xử lý cùng các tài nguyên hỗ trợ
- Hoàn thiện phần việc được giao và gửi lại kết quả cho tác giả kiểm duyệt
- Theo dõi số trang đã được duyệt và thu nhập tương ứng theo từng tháng

### Tantou Editor
- Xem bản thảo và đánh dấu trực tiếp lên trang những chỗ cần chỉnh sửa nội dung, thoại, kịch bản
- Quản lý hồ sơ và số liệu để bảo vệ series trước hội đồng biên tập
- Theo dõi tiến độ hoàn thiện của studio theo thời gian thực để đảm bảo kịp deadline giao bản in

### Editorial Board
- Bỏ phiếu thông qua series mới và quyết định lịch xuất bản (hàng tuần hoặc hàng tháng)
- Ra quyết định huỷ series đang xếp hạng thấp hoặc thay đổi hình thức xuất bản dựa trên kết quả thực tế
- Nhập dữ liệu bình chọn từ độc giả vào hệ thống sau mỗi kỳ phát hành
- Xem bảng xếp hạng các series được tổng hợp sau mỗi lần nhập dữ liệu

### Tùy chọn: Tích hợp AI
- AI tự động tô màu trang truyện
- AI hỗ trợ phân đoạn vùng trên trang truyện"

## Main Entities
- Series
- Chapter
- Page
- Manuscript
- Task
- Submission

<div align="center">
  <h2>HOW TO RUN THIS</h2>
</div>

## System requirements
- Java 21+
- Spring-boot 4/ Spring Framework 7
- Jarkarta EE Namespace
- Docker

## Tree Directory
```text
mangakousei-backend/
├── src/
│   ├── main/
│   │   ├── java/com/mangakousei/mangakousei_backend/
│   │   │   ├── MangakouseiBackendApplication.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── WebSocketConfig.java
│   │   │   │   └── JwtHandshakeInterceptor.java
│   │   │   │
│   │   │   ├── constant/
│   │   │   │   └── RealtimeQueues.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── UserProfileController.java
│   │   │   │   ├── GenreController.java
│   │   │   │   ├── LookupController.java
│   │   │   │   ├── TantouController.java
│   │   │   │   ├── TantouScheduleController.java
│   │   │   │   ├── TantouReportController.java
│   │   │   │   ├── MangakaRiskController.java
│   │   │   │   ├── MangakaRegionTaskController.java
│   │   │   │   ├── AssistantTaskAttachmentController.java
│   │   │   │   └── AdminDashboardController.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── LogContext.java
│   │   │   │   │   └── ReviewSubmissionReq.java
│   │   │   │   └── response/
│   │   │   │       ├── ApiResponse.java
│   │   │   │       ├── ProposalRes.java
│   │   │   │       ├── PublicPageRes.java
│   │   │   │       ├── ValidationErrorRes.java
│   │   │   │       ├── GenreRes.java
│   │   │   │       ├── AdminContactRes.java
│   │   │   │       ├── MangakaDashboardStatsRes.java
│   │   │   │       └── UserFullProfileRes.java
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Chapter.java
│   │   │   │   │   ├── Page.java
│   │   │   │   │   ├── Manuscript.java
│   │   │   │   │   ├── Series.java
│   │   │   │   │   └── User.java
│   │   │   │   ├── status/
│   │   │   │   │   └── ChapterStatus.java
│   │   │   │   └── engagement/
│   │   │   │       └── ReaderVoteBatches.java
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── CustomAppException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   ├── mapper/
│   │   │   │   └── UserMapper.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── TantouScheduleRepository.java
│   │   │   │   ├── AnnotationStatusRepository.java
│   │   │   │   ├── AnnotationTypeRepository.java
│   │   │   │   ├── TaskAttachmentRepository.java
│   │   │   │   ├── IssueCodeRepository.java
│   │   │   │   ├── TaskRepository.java
│   │   │   │   ├── DecisionTypeRepository.java
│   │   │   │   ├── TaskStatusRepository.java
│   │   │   │   ├── PublicationDecisionRepository.java
│   │   │   │   ├── MangakaAssistantAssignmentRepository.java
│   │   │   │   ├── GenreRepository.java
│   │   │   │   ├── TaskSubmissionStatusRepository.java
│   │   │   │   └── TaskTypeRepository.java
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── PageService.java
│   │   │   │   ├── AdminService.java
│   │   │   │   ├── ReaderVoteBatchService.java
│   │   │   │   ├── GenreService.java
│   │   │   │   ├── ChapterService.java
│   │   │   │   ├── RealtimePushService.java
│   │   │   │   ├── TantouService.java
│   │   │   │   ├── SeriesProposalService.java
│   │   │   │   └── TaskSubmissionService.java
│   │   │   │
│   │   │   └── util/
│   │   │       └── SecurityUtils.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       ├── java/com/mangakousei/mangakousei_backend/
│       │   ├── MangakouseiBackendApplicationTests.java
│       │   ├── security/
│       │   │   └── JwtTokenProviderTest.java
│       │   └── service/
│       │       ├── SeriesProposalServiceTest.java
│       │       └── AuthServiceTest.java
│       └── resources/
│           └── application-test.properties // You can get it from the Owner
```
> [!NOTE]
> You can recive the application.properties from the Project Owners
## How to run Project
### First: Clone the project
```bash
git clone https://github.com/Java-Project-Team-2026/Java-Project.git
```
### Second: List the project
```bash
ls
```
- If you see the mvnw file
- Then follow the step
- Else change the folder into the Java-Project
  ```bash
  cd Java-Project
  ```
### Third: Run the docker
```bash
docker-compose -f docker-compose.rabbitmq.yml
```
### Finally: Run the project
```bash
.\mvnw spring-boot:run
```


