package com.web.mra.controller;

import com.boc.core.web.controller.BaseController;
import com.boc.core.web.domain.AjaxResult;
import com.web.mra.dto.PageResponse;
import com.web.mra.dto.TableMetadataDTO;
import com.web.mra.service.DatabaseMetadataService;
import com.web.mra.service.DynamicTableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = {"DYNAMIC TABLE MANAGEMENT"})
@RestController
@RequestMapping("/mra/admin")
@RequiredArgsConstructor
@Slf4j
public class DynamicTableController extends BaseController {

    private final DynamicTableService dynamicTableService;
    private final DatabaseMetadataService metadataService;

    @ApiOperation(value = "Get All Table Names")
    @GetMapping("/tables")
    public AjaxResult getAllTables() {
        try {
            List<String> tables = metadataService.getAllTables();
            // log.info("Returning {} tables", tables.size());
            return AjaxResult.success(tables);
        } catch (Exception e) {
            log.error("Error fetching tables", e);
            return AjaxResult.error("Failed to fetch tables: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Get Table Metadata")
    @GetMapping("/metadata/{tableAlias}")
    public AjaxResult getTableMetadata(@PathVariable String tableAlias) {
        if (!dynamicTableService.isReadableTable(tableAlias)) {
            return AjaxResult.error("Invalid or non-readable table : " + tableAlias);
        }
        
        try {
            List<TableMetadataDTO> metadata = metadataService.getTableMetadata(tableAlias);
            return AjaxResult.success(metadata);
        } catch (Exception e) {
            log.error("Error fetching metadata for table: " + tableAlias, e);
            return AjaxResult.error("Failed to fetch table metadata: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Get Table Data with Pagination")
    @GetMapping("/table/{tableAlias}")
    public AjaxResult getTableData(
            @PathVariable String tableAlias,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (!dynamicTableService.isReadableTable(tableAlias)) {
            return AjaxResult.error("Invalid or non-readable table : " + tableAlias);
        }
        try {
            PageResponse<Map<String, Object>> pageResponse =
                    dynamicTableService.getTableData(tableAlias, page, size);
            return AjaxResult.success(pageResponse);
        } catch (Exception e) {
            log.error("Error fetching data for table: " + tableAlias, e);
            return AjaxResult.error("Failed to fetch table data: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Update Existing Record")
    @PutMapping("/table/{tableAlias}/{id}")
    public AjaxResult updateRecord(
            @PathVariable String tableAlias,
            @PathVariable Long id,
            @RequestBody Map<String, Object> record
    ) {
        if (!dynamicTableService.isWriteableTable(tableAlias)) {
            return AjaxResult.error("Invalid or non-writeable table : " + tableAlias);
        }
        try {
            Map<String, Object> updatedRecord = dynamicTableService.updateRecord(tableAlias, id, record);
            return AjaxResult.success("Record updated successfully", updatedRecord);
        } catch (Exception e) {
            log.error("Error updating record in table: " + tableAlias, e);
            return AjaxResult.error("Failed to update record: " + e.getMessage());
        }
    }
}
