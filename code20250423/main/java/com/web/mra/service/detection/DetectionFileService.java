package com.web.mra.service.detection;

import com.web.mra.dto.FileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DetectionFileService {

    private final String sharedFolder;
    private final String archiveFolder;

    // Constructor injection for configuration properties
    public DetectionFileService(@Value("${spring.filepath.shared}") String sharedFolder,
            @Value("${spring.filepath.archive}") String archiveFolder) {
        this.sharedFolder = sharedFolder;
        this.archiveFolder = archiveFolder;
    }

    /**
     * Processes a file based on the provided file name pattern and archive date.
     *
     * @param fileNamePattern The regex pattern for matching file names.
     * @param archiveDate     The date to use for archiving (format: yyyyMMdd).
     * @return The path of the processed file, or an empty string if no file is
     *         found.
     */
    public String processFile(String fileNamePattern, String archiveDate) {
        try {
            // Create Pattern from fileNamePattern
            Pattern pattern = Pattern.compile(fileNamePattern);

            // Get current date folder name
            String dateFolderName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Path dateFolderPath = Paths.get(archiveFolder, dateFolderName);

            // Search in shared folder
            File sharedDir = new File(sharedFolder);
            File[] files = sharedDir.listFiles();
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
                for (File file : files) {
                    if (pattern.matcher(file.getName()).matches()) {
                        // Create date folder if it doesn't exist
                        Files.createDirectories(dateFolderPath);

                        // Move file to archive/date folder
                        Path targetPath = dateFolderPath.resolve(file.getName());
                        Files.move(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        return targetPath.toString();
                    }
                }
            }

            // If not found in shared folder, search in archive and date folder
            if (Files.exists(dateFolderPath)) {
                File[] dateFiles = dateFolderPath.toFile().listFiles();
                if (dateFiles != null) {
                    Arrays.sort(dateFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                    for (File file : dateFiles) {
                        if (pattern.matcher(file.getName()).matches()) {
                            return file.getAbsolutePath();
                        }
                    }
                }
            }

            return "";

        } catch (IOException e) {
            throw new DetectionServiceException("Error processing file: " + e.getMessage(), e);
        }
    }

    public String hasFileInSharedFolder() {
        File sharedDir = new File(sharedFolder);
        File[] files = sharedDir.listFiles();
        if (files != null && files.length > 0) {
            return Arrays.stream(files).map(File::getName).collect(Collectors.joining("; "));
        }
        return null;
    }

    public boolean filesAllReady(String fileNamePatterns) {
        if (fileNamePatterns == null || fileNamePatterns.isEmpty()) {
            return false;
        }
        
        String[] patterns = fileNamePatterns.split("\\|");
        for (String pattern : patterns) {
            String filePath = processFile(pattern, null);
            if (filePath == null || filePath.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public boolean fileInPattern(List<String> fileList, String fileNamePatterns) {
        if ( fileList == null ||  fileList.isEmpty() || fileNamePatterns == null || fileNamePatterns.isEmpty()) {
            return false;
        }
        
        String[] patterns = fileNamePatterns.split("\\|");
        for (String fileName : fileList) {
            for (String patternStr : patterns) {
                Pattern pattern = Pattern.compile(patternStr);
                if (pattern.matcher(fileName).matches()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Searches for a file matching the pattern in the archive directory.
     *
     * @param archiveDir The archive directory to search in.
     * @param pattern    The regex pattern for matching file names.
     * @return The absolute path of the matching file, or null if no file is found.
     */
    private String searchInArchive(File archiveDir, Pattern pattern) {
        File[] dateFolders = archiveDir.listFiles();
        if (dateFolders != null) {
            for (File dateFolder : dateFolders) {
                if (dateFolder.isDirectory()) {
                    File[] files = dateFolder.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (pattern.matcher(file.getName()).matches()) {
                                return file.getAbsolutePath();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public List<String> uploadFiles(MultipartFile[] files) {
        List<String> fileNames = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                // Save file
                String fileName = file.getOriginalFilename();
                Path filePath = Paths.get(sharedFolder, fileName);
                Files.copy(file.getInputStream(), filePath);
                fileNames.add(fileName);
            }
            return (fileNames);
        } catch (IOException e) {
            return fileNames;
        }
    }

    public List<FileDTO> listFiles(String relativePath) {
        try {
            Path dirPath = Paths.get(archiveFolder, relativePath);
            List<FileDTO> items = Files.list(dirPath).map(path -> {
                FileDTO dto = new FileDTO();
                dto.setName(path.getFileName().toString());
                dto.setPath(relativePath.isEmpty() ? path.getFileName().toString()
                        : relativePath + "/" + path.getFileName());
                dto.setType(Files.isDirectory(path) ? "directory" : "file");
                try {
                    dto.setModifiedDate(Files.getLastModifiedTime(path).toMillis());
                    if (!Files.isDirectory(path)) {
                        dto.setSize(Files.size(path));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return dto;
            }).collect(Collectors.toList());

            return items;
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files", e);
        }
    }

    public Resource loadFileAsResource(String relativePath) {
        try {
            Path filePath = Paths.get(archiveFolder, relativePath);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + relativePath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + relativePath, e);
        }
    }
    

    
    public String saveWBtoFile(Workbook wb) {
        try {
            // Get current date folder name
            String dateFolderName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Path dateFolderPath = Paths.get(archiveFolder, dateFolderName);
            Files.createDirectories(dateFolderPath);

            String randomChars = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            String fileName = "NearMissEvents" + dateFolderName + "-" + randomChars + ".xlsx";
            Path filePath = dateFolderPath.resolve(fileName);
            
            try (OutputStream outputStream = Files.newOutputStream(filePath)) {
                wb.write(outputStream);
            }
            return filePath.toString();

        } catch (IOException e) {
            throw new DetectionServiceException("Error processing file: " + e.getMessage(), e);
        }
    }
}
