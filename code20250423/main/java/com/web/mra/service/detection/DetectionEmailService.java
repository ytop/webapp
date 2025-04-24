package com.web.mra.service.detection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.boc.utils.EmailUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.mra.dto.DetectionEmailMetaDTO;
import com.web.mra.service.detection.DetectionMetadataService;
import com.web.sys.entity.AiosPortalUser;
import com.web.sys.service.UserService;
import com.web.mra.dto.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date;
import java.text.SimpleDateFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionEmailService {
    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private UserService userService;
    
    final private String RDALINK = "<a href='https://rdareport-prod/rda/#/login'>Click here to access</a><br>";
    
    public Boolean SendEmailNearMissEventUploadFiles(List<DetectionEmailMetaDTO> emailConfigs, Map<String, String> status, String emailList) {
        log.info(" email for upload file");
        try {            
            
            // email body
            StringBuilder emailContent = new StringBuilder()
                    .append("<u><i>Please DO NOT REPLY. This is an automated notification from the RDA Reporting Platform Near Miss Detection Module.</u></i>   <br>")
                    .append("<br> Dear All,<br>");
            emailContent.append("<br>" +
                    "Friendly Reminder, please upload your daily files onto RDA Reporting Platform Near Miss Detection Module for below tasks <b><u>before 12PM today</u></b>.  <br>" +
                    "<br>");
            emailContent.append("<table border=1>");
            emailContent.append("<tr><th>Near Miss Task Name</th><th>Department</th><th>Status</th></tr>");
            for (Map.Entry<String, String> entry : status.entrySet()) {
                String taskName = entry.getKey();
                String taskStatus = entry.getValue();
                emailContent.append("<tr>");
                emailContent.append("<td>" + taskName + "</td>");
                emailContent.append("<td>OSD</td>");
                emailContent.append("<td>" + taskStatus + "</td>");
                emailContent.append("</tr>");
            }
            emailContent.append("</table>");
            emailContent.append("<br>");
            emailContent.append("Link to the RDA Reporting Platform Near Miss Detection Module: " + RDALINK +
                    "<br>" +
                    "Should you have any technical questions, please contact ERM/RDA administrators.<br>" +
                    "<br>" +
                    "Thank you!<br>" +
                    "ERM-RDA Team");
            // log.info(" email content: " + emailContent.toString());
            emailUtil.sendSimpleHtmlMail(emailContent.toString(), emailList, "", "[Do Not Reply] Auto-Reminder: Please Upload Files to RDA Near Miss Detection Module before 12PM. ");
 
        } catch (Exception e) {
            log.info("Send email failed for file upload reminder. Error message: ", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public Boolean SendEmailNearMissEventsDetectedNonORD(String department, List<DetectionEmailMetaDTO> emailConfigs, String nonUserEmails, int count, Map<String, String> statusCount, Map<String, String> status, Map<String, String> runTime) {
        log.info(" email by dept {}: ", department);
        try {
            Date d = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
            String formattedDate = formatter.format(d);

            String emailList = "";
            List<DetectionEmailMetaDTO> departmentUsers = emailConfigs.stream().filter(config -> config.getUserDepartment().equals(department)).collect(Collectors.toList());
            for (DetectionEmailMetaDTO user : departmentUsers) {
                LambdaQueryWrapper<AiosPortalUser> userQuery = new LambdaQueryWrapper<>(AiosPortalUser.class).eq(AiosPortalUser::getUserName, user.getUserName());
                AiosPortalUser portalUser = userService.getOne(userQuery);
                // Skip if user not found or no email address
                if (portalUser == null || StringUtils.isEmpty(portalUser.getEmail())) {
                    log.warn("User not found or no email address for username: " + user.getUserName());
                    continue;
                }
                emailList = emailList + portalUser.getEmail() + ";"; 
                log.info(" email addr: " + portalUser.getEmail());
            }
                
            if (emailList.length() > 0 && nonUserEmails != null && nonUserEmails.length() > 0) {
                emailList = emailList + nonUserEmails;
            } else if (emailList.length() > 0) {
                emailList = emailList.substring(0, emailList.length() - 1);
            } else if (nonUserEmails != null && nonUserEmails.length() > 0){
                emailList = nonUserEmails;
            } else {
                log.info("No email receiver in : " + department);
                return Boolean.FALSE;
            }
                
            // email body
            StringBuilder emailContent = new StringBuilder()
                        .append("<u><i>Please DO NOT REPLY. This is an automated notification from the RDA Reporting Platform Near Miss Detection Module.</u></i>   <br>")
                        .append("<br> Dear All,<br>");
            emailContent.append("<br>" +
                    String.valueOf(count) + " near miss events relevasnt to your department have been detected on " + formattedDate + ":  <br>" +
                    "<br>");
            
            emailContent.append("<table border=1>");
            emailContent.append("<tr><th>Near Miss Task Name</th><th>Department</th><th>Status</th><th>Count of Detected Events</th><th>Run Time</th></tr>");
            for (Map.Entry<String, String> entry : statusCount.entrySet()) {
                String currentDesc = entry.getKey();
                String currentCount = entry.getValue().equals("-") == true ? "0" : entry.getValue();
                String currentStatus = status.get(currentDesc);
                String currentRunTime  = runTime.get(currentDesc);
                // String strToAppend = currentDesc + " " + department + " " + currentStatus + " " + currentCount + " " + currentRunTime + " <br><br>";
                // emailContent.append(strToAppend);
                emailContent.append("<tr>");
                emailContent.append("<td>" + currentDesc + "</td>");
                emailContent.append("<td>" + department + "</td>");
                emailContent.append("<td>" + currentStatus + "</td>");
                emailContent.append("<td>" + currentCount + "</td>");
                emailContent.append("<td>" + currentRunTime + "</td>");
                // String strToAppend = currentDesc + " " + currentDepartment + " " + currentStatus + " " + currentCount + " " + currentRunTime + " <br><br>";
                // emailContent.append(strToAppend);
                emailContent.append("</tr>");
            }
            emailContent.append("</table>");
            emailContent.append("<br>");
            emailContent.append("Link to the RDA Reporting Platform Near Miss Detection Module: " + RDALINK +
                    "<br>" +
                    "Should you have any technical questions, please contact ERM/RDA administrators.<br>" +
                    "<br>" +
                    "Thank you!<br>" +
                    "ERM-RDA Team");
            // log.info(" email content: " + emailContent.toString());
            emailUtil.sendSimpleHtmlMail(emailContent.toString(), emailList, "", "[Do Not Reply] Auto Notification: " + String.valueOf(count) + " Near Miss Events Detected - " + formattedDate);
            
        } catch (Exception e) {
            log.info("Send email failed for department: " + department, e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public Boolean SendEmailNearMissEventsDetectedORD(int totalCount, Map<String, String> nearMissCount, Map<String, String> nearMissStatus, 
            Map<String, String> nearMissRunTime, Map<String, String> nearMissDept, String filePath,
            String ormEmail, String ccEmail) {
        log.info(" email by dept ORD: ");
        try {
            Date d = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
            String formattedDate = formatter.format(d);

            StringBuilder emailContent = new StringBuilder()
                        .append("<u><i>Please DO NOT REPLY. This is an automated notification from the RDA Reporting Platform Near Miss Detection Module.</u></i>   <br>")
                        .append("<br> Dear ORM-IM,<br> <br>");

            emailContent.append("<br>" +
                    " Please see the attached list of near miss events detected on " + formattedDate + " for uploading to Incident Module (EUC).<br>" +
                    "<br>");
            
            emailContent.append("<table border=1>");
            emailContent.append("<tr><th>Near Miss Task Name</th><th>Department</th><th>Status</th><th>Count of Detected Events</th><th>Run Time</th></tr>");

            for (Map.Entry<String, String> entry : nearMissCount.entrySet()) {
                String currentDesc = entry.getKey();
                String currentCount = entry.getValue().equals("-") == true ? "0" : entry.getValue();
                String currentStatus = nearMissStatus.get(currentDesc);
                String currentRunTime  = nearMissRunTime.get(currentDesc);
                String currentDepartment = nearMissDept.get(currentDesc);
                emailContent.append("<tr>");
                emailContent.append("<td>" + currentDesc + "</td>");
                emailContent.append("<td>" + currentDepartment + "</td>");
                emailContent.append("<td>" + currentStatus + "</td>");
                emailContent.append("<td>" + currentCount + "</td>");
                emailContent.append("<td>" + currentRunTime + "</td>");
                // String strToAppend = currentDesc + " " + currentDepartment + " " + currentStatus + " " + currentCount + " " + currentRunTime + " <br><br>";
                // emailContent.append(strToAppend);
                emailContent.append("</tr>");
            }
            emailContent.append("</table>");
            emailContent.append("<br>");
            emailContent.append("Link to the RDA Reporting Platform Near Miss Detection Module: " + RDALINK +
                    "<br>" +
                    "Should you have any technical questions, please contact ERM/RDA administrators.<br>" +
                    "<br>" +
                    "<br>" +
                    "Thank you!<br>" +
                    "ERM-RDA Team");
            log.info(" email to: " + ormEmail + " cc: " + ccEmail);
            String emailTitle = "[Do Not Reply] Auto Notification: " + String.valueOf(totalCount) + " Near Miss Events Detected - " + formattedDate;
            emailUtil.sendHtmlMailWithAttachments(emailContent.toString(), ormEmail, ccEmail, emailTitle, filePath);
        } catch (Exception e) {
            log.info("Send email failed for ORD", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    
    public Boolean SendEmailNearMissEventsDetectedERM(String fileList, String ermEmail) {
        log.info(" Send email to ERM Admin for existing file in shared folder ");
        try {StringBuilder emailContent = new StringBuilder()
                        .append("<u><i>Please DO NOT REPLY. This is an automated notification from the RDA Reporting Platform Near Miss Detection Module.</u></i>   <br>")
                        .append("<br> Dear ERM Administrator,<br> <br>")
                        .append("<br>Please see the attached file list existing in shared folder, " + fileList + "<br>Thank you!<br>ERM-RDA Team");
            String emailTitle = "[Do Not Reply] Auto Notification: file existing in shared folder";
            emailUtil.sendSimpleHtmlMail(emailContent.toString(), ermEmail, "", emailTitle);
        } catch (Exception e) {
            log.info("Send email failed for ERM", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
