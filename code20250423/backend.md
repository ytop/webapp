# RDA Backend Technical Specification

## 1. System Architecture

### 1.1 Technology Stack
- Language: Java 11+
- Framework: Spring Boot
- Database: Microsoft SQL Server
- ORM: MyBatis
- Task Scheduling: Quartz
- Build Tool: Maven
- Logging: SLF4J + Logback

### 1.2 Package Structure
```
com.web.mra
├── config/           # Configuration classes
├── controller/       # REST endpoints
├── service/         # Business logic
│   ├── detection/   # Detection related services
│   ├── incident/    # Incident management
│   └── email/       # Email notification
├── mapper/          # MyBatis mappers
├── model/           # Domain models
├── dto/             # Data transfer objects
└── util/            # Utility classes
```

## 2. Core Services

### 2.1 Detection Service

#### 2.1.1 Detection Process Implementation
```java
@Service
@Transactional
public class DetectionService {
    
    @Autowired
    private DetectionMetadataService metadataService;
    
    @Autowired
    private DetectionEmailService emailService;
    
    public String detectNearMiss(Integer detectionId, LocalDate eventDate, boolean forceDetection) {
        try {
            validateDetectionId(detectionId);
            LocalDate effectiveDate = eventDate != null ? eventDate : LocalDate.now();
            
            // Skip if already done today's detection
            if (!forceDetection && metadataService.doneTodayDetection(detectionId)) {
                log.info("SKIP Detection {}, already done today", detectionId);
                return getExistingDetectionSummary(detectionId, effectiveDate);
            }
            
            // Perform detection
            DetectionResult result = performDetection(detectionId, effectiveDate);
            
            // Save and notify
            saveDetectionResult(result);
            notifyStakeholders(result);
            
            return result.toJson();
        } catch (Exception e) {
            log.error("Detection failed for ID: " + detectionId, e);
            throw new DetectionServiceException("Detection process failed", e);
        }
    }
    
    private DetectionResult performDetection(Integer detectionId, LocalDate date) {
        // Implementation details for detection logic
        DetectionMetaDTO metadata = metadataService.getDetectionMetadata(detectionId);
        List<DetectionComponent> components = loadDetectionComponents(metadata);
        
        return components.stream()
            .map(component -> component.detect(date))
            .collect(DetectionResult.collector());
    }
}
```

#### 2.1.2 Scheduled Detection Tasks
```java
@Component
public class DetectionQuartzService {
    
    private static final Map<String, List<Integer>> DEPARTMENT_DETECTIONS = Map.of(
        "ADC", Arrays.asList(101, 102),
        "CLD", Arrays.asList(103, 104),
        "OSD", Arrays.asList(105, 106)
    );
    
    @Scheduled(cron = "${detection.schedule.cron}")
    public void scheduledDetection() {
        LocalDate today = LocalDate.now();
        boolean isLastRun = isLastRunOfDay();
        
        checkDepartmentCompletion(today, isLastRun);
        checkOverallCompletion(today, isLastRun);
    }
    
    private void checkDepartmentCompletion(LocalDate today, boolean isLastRun) {
        for (Map.Entry<String, List<Integer>> entry : DEPARTMENT_DETECTIONS.entrySet()) {
            String department = entry.getKey();
            List<Integer> detectionIds = entry.getValue();
            
            boolean allDeptDone = detectionIds.stream()
                    .allMatch(id -> metadataService.doneTodayDetection(id));
            
            if ((allDeptDone || isLastRun) && !loggedDepartments.contains(department)) {
                detectionService.SendEmailNearMissEvents(department);
                loggedDepartments.add(department);
            }
        }
    }
}
```

### 2.2 Metadata Service

#### 2.2.1 Detection Metadata Management
```java
@Service
public class DetectionMetadataService {
    
    public void saveDetectionSummary(Integer detectionId, DetectionMetaDTO detectionMeta,
                                   LocalDate eventDate, String detectionDetail, String detectionStatus) {
        Map<String, Object> matchCounts = getMatchCounts(detectionId, eventDate);
        String nearMissStatus = determineNearMissStatus(matchCounts, detectionStatus);

        try {
            Map<String, Object> params = createSummaryParams(detectionId, detectionMeta,
                    eventDate, detectionDetail, matchCounts, nearMissStatus, detectionStatus);
            insertDetectionSummary(params);
        } catch (Exception e) {
            log.error("Error saving detection summary", e);
            throw new DetectionServiceException("Failed to save detection summary", e);
        }
    }
    
    private String determineNearMissStatus(Map<String, Object> matchCounts, String detectionStatus) {
        if ("Failed".equals(detectionStatus)) {
            return "-";
        }
        // Implement near miss status determination logic
        return calculateNearMissStatus(matchCounts);
    }
}
```

### 2.3 Email Service

#### 2.3.1 Notification System
```java
@Service
public class DetectionEmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${mail.template.path}")
    private String templatePath;
    
    public void SendEmailNearMissEventsDetected(String department, Map<String, String> stats) {
        try {
            List<String> recipients = getRecipientsByDepartment(department);
            String subject = String.format("Near Miss Detection Report - %s", department);
            
            Context context = new Context();
            context.setVariable("department", department);
            context.setVariable("stats", stats);
            context.setVariable("date", LocalDate.now());
            
            String content = templateEngine.process(templatePath, context);
            
            sendEmail(recipients, subject, content);
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
            throw new EmailServiceException("Email notification failed", e);
        }
    }
    
    private void sendEmail(List<String> recipients, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        helper.setTo(recipients.toArray(new String[0]));
        helper.setSubject(subject);
        helper.setText(content, true);
        
        mailSender.send(message);
    }
}
```

## 3. Data Access Layer

### 3.1 MyBatis Mappers

#### 3.1.1 Detection Mapper
```java
@Mapper
public interface DetectionMapper {
    
    @Select("SELECT * FROM web.IMP_DETECTION WHERE detection_id = #{id} AND event_date = #{date}")
    DetectionDTO getDetection(@Param("id") Integer id, @Param("date") LocalDate date);
    
    @Insert("INSERT INTO web.IMP_DETECTION (event_date, near_miss_status, detection_status, " +
            "detection_id, detection_dept, detection_desc, detection_meta_data, event_detail, create_time) " +
            "VALUES (#{eventDate}, #{nearMissStatus}, #{detectionStatus}, #{detectionId}, " +
            "#{detectionDept}, #{detectionDesc}, #{metaData}, #{eventDetail}, GETDATE())")
    void insertDetection(DetectionDTO detection);
}
```

### 3.2 Data Models

#### 3.2.1 Detection DTO
```java
@Data
@Builder
public class DetectionDTO {
    private Long autoId;
    private LocalDate eventDate;
    private String nearMissStatus;
    private String detectionStatus;
    private Integer detectionId;
    private String detectionDept;
    private String detectionDesc;
    private String detectionMetaData;
    private String eventDetail;
    private LocalDateTime createTime;
}
```

## 4. Configuration

### 4.1 Database Configuration
```java
@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                .url("${spring.datasource.url}")
                .username("${spring.datasource.username}")
                .password("${spring.datasource.password}")
                .build();
    }
    
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/*.xml"));
        return sessionFactory.getObject();
    }
}
```

### 4.2 Email Configuration
```java
@Configuration
public class EmailConfig {
    
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("${mail.host}");
        mailSender.setPort(587);
        mailSender.setUsername("${mail.username}");
        mailSender.setPassword("${mail.password}");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        return mailSender;
    }
}
```

## 5. Error Handling

### 5.1 Global Exception Handler
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DetectionServiceException.class)
    public ResponseEntity<ErrorResponse> handleDetectionException(DetectionServiceException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Detection Service Error",
            ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred"
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

## 6. Security

### 6.1 Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
```

## 7. Monitoring and Logging

### 7.1 Logging Configuration
```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/rda.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/rda-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

## 8. Testing

### 8.1 Unit Testing
```java
@SpringBootTest
public class DetectionServiceTest {
    
    @MockBean
    private DetectionMetadataService metadataService;
    
    @Autowired
    private DetectionService detectionService;
    
    @Test
    public void testDetectNearMiss() {
        // Given
        Integer detectionId = 101;
        LocalDate eventDate = LocalDate.now();
        
        // When
        String result = detectionService.detectNearMiss(detectionId, eventDate, false);
        
        // Then
        assertNotNull(result);
        verify(metadataService).doneTodayDetection(detectionId);
    }
}
```

## 9. Deployment

### 9.1 Application Properties
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Database Configuration
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=RDA
spring.datasource.username=sa
spring.datasource.password=password

# Email Configuration
mail.host=smtp.company.com
mail.username=rda@company.com
mail.password=secret
mail.template.path=templates/email/detection-report.html

# Detection Schedule
detection.schedule.cron=0 0/30 * * * ?
```

## 10. Performance Optimization

### 10.1 Caching Configuration
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("detectionMetadata"),
            new ConcurrentMapCache("emailTemplates")
        ));
        return cacheManager;
    }
}
```

## 11. Documentation

### 11.1 API Documentation
```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.web.mra.controller"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo());
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("RDA API Documentation")
            .description("API documentation for Risk Detection & Analysis system")
            .version("1.0")
            .build();
    }
}
```