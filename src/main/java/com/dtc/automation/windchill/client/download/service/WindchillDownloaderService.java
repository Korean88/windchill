package com.dtc.automation.windchill.client.download.service;

import com.dtc.automation.windchill.client.download.model.WtDocumentObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class WindchillDownloaderService {

    static final String WT_DOC_WTDOCUMENT = "wt.doc.WTDocument";

    private final DocumentDownloadService documentDownloadService;
    private final ObjectService objectService;

    public WindchillDownloaderService(DocumentDownloadService documentDownloadService, ObjectService objectService) {
        this.documentDownloadService = documentDownloadService;
        this.objectService = objectService;
    }

    public void processDocNumbersFromExcel(String filename) {
        File file = Paths.get(filename).toFile();
        try (FileInputStream excelFile = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
             FileOutputStream outputStream = new FileOutputStream(filename)) {
            Sheet datatypeSheet = workbook.getSheetAt(0);
            CellStyle strikeThroughStyle = createStrikeThroughStyle(workbook);
            Set<String> uniqueDocNumbers = new HashSet<>();
            int numberOfRows = datatypeSheet.getPhysicalNumberOfRows();
            for (int i = 1; i < numberOfRows; i++) {
                Row currentRow = datatypeSheet.getRow(i);
                Cell docNumberCell = currentRow.getCell(0);
                Cell versionCell = currentRow.getCell(1);
                Cell actualVersionCell = currentRow.createCell(2);
                Cell statusCell = currentRow.createCell(3);

                String docNumber = docNumberCell.getStringCellValue();
                if (StringUtils.isBlank(docNumber)) {
                    continue;
                } else {
                    docNumber = docNumber.trim();
                }
                String version = versionCell.getStringCellValue();
                if (uniqueDocNumbers.contains(docNumber)) {
                    statusCell.setCellValue("duplicate");
                    continue;
                }
                uniqueDocNumbers.add(docNumber);

                Optional<WtDocumentObject> wtDocumentOptional = objectService.fetchObject(WT_DOC_WTDOCUMENT, docNumber);
                if (wtDocumentOptional.isPresent()) {
                    WtDocumentObject wtDocumentObject = wtDocumentOptional.get();
                    if (documentDownloadService.downloadFile(wtDocumentObject, docNumber)) {
                        statusCell.setCellValue("success");
                        String actualVersion = wtDocumentObject.getObjectAttributes().getVersion();
                        if (!version.equals(actualVersion)) {
                            versionCell.setCellStyle(strikeThroughStyle);
                        }
                        actualVersionCell.setCellValue(actualVersion);
                    } else {
                        statusCell.setCellValue("failed");
                    }
                }
            }
            workbook.write(outputStream);
        } catch (IOException e) {
            log.error("Could not read/write xlsx file " + filename, e);
        }
    }

    private CellStyle createStrikeThroughStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setStrikeout(true);
        style.setFont(font);
        return style;
    }

}
