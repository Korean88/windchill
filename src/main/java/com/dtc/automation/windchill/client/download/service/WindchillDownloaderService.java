package com.dtc.automation.windchill.client.download.service;

import com.dtc.automation.windchill.client.download.model.WtDocumentObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class WindchillDownloaderService {

    static final String WT_DOC_WTDOCUMENT = "wt.doc.WTDocument";
    static final String REF_CHECK = "refcheck";
    static final String DOWNLOAD = "download";

    private final DocumentDownloadService documentDownloadService;
    private final ObjectService objectService;

    public WindchillDownloaderService(DocumentDownloadService documentDownloadService, ObjectService objectService) {
        this.documentDownloadService = documentDownloadService;
        this.objectService = objectService;
    }

    public void processDocNumbersFromExcel(String filename, String option) {
        File file = Paths.get(filename).toFile();
        try (FileInputStream excelFile = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(excelFile)) {
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Set<String> uniqueDocNumbers = new HashSet<>();
            int numberOfRows = datatypeSheet.getPhysicalNumberOfRows();
            StringBuilder htmlReport = createHtmlStart();
            for (int i = 1; i < numberOfRows; i++) {
                Row currentRow = datatypeSheet.getRow(i);
                Cell docNumberCell = currentRow.getCell(0);
                Cell versionCell = currentRow.getCell(1);

                String docNumber = docNumberCell.getStringCellValue();
                if (StringUtils.isBlank(docNumber)) {
                    continue;
                } else {
                    docNumber = docNumber.trim();
                }
                String version = versionCell.getStringCellValue();

                htmlReport.append("<tr>")
                        .append("<td>").append(docNumber).append("</td>")
                        .append("<td>").append(version).append("</td>");
                if (uniqueDocNumbers.contains(docNumber)) {
                    htmlReport.append("<td></td>")
                            .append("<td>duplicate</td>")
                            .append("</tr>");
                    continue;
                }
                uniqueDocNumbers.add(docNumber);

                Optional<WtDocumentObject> wtDocumentOptional = objectService.fetchObject(WT_DOC_WTDOCUMENT, docNumber);
                if (wtDocumentOptional.isPresent()) {
                    WtDocumentObject wtDocumentObject = wtDocumentOptional.get();
                    if (DOWNLOAD.equals(option)) {
                        String status = documentDownloadService.downloadFile(wtDocumentObject, docNumber) ? "downloaded" : "failure";
                        htmlReport.append("<td></td>")
                                .append("<td>").append(status).append("</td>")
                                .append("</tr>");
                    } else if (REF_CHECK.equals(option)) {
                        String actualVersion = wtDocumentObject.getObjectAttributes().getVersion();
                        if (version.equals(actualVersion)) {
                            htmlReport.append("<td>").append(actualVersion).append("</td>")
                                    .append("<td>checked</td>")
                                    .append("</tr>");
                        } else {
                            htmlReport.append("<td bgcolor=#FFFF00>").append(actualVersion).append("</td>")
                                    .append("<td>wrong revision</td>")
                                    .append("</tr>");
                        }
                    }
                } else {
                    htmlReport.append("<td></td>")
                            .append("<td>not found</td>")
                            .append("</tr>");
                }
            }
            htmlReport.append("</table>")
                    .append("</body>")
                    .append("</html>");
            SimpleDateFormat df = new SimpleDateFormat("YYYY-MMM-dd-hhmmss");
            String timeSuffix = df.format(new Date());
            File reportFile = new File("report-" + option + "-" + timeSuffix + ".html");
            FileUtils.write(reportFile, htmlReport.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Could not read .xlsx file or create .html file", e);
        }
    }

    private StringBuilder createHtmlStart() {
        StringBuilder sb = new StringBuilder();
        return sb.append("<html>")
                .append("<style>")
                .append("table, th, td {")
                .append("  border: 1px solid black;")
                .append("  border-collapse: collapse;")
                .append("}")
                .append("</style>")
                .append("<body>")
                .append("<table>")
                .append("<tr>")
                .append("<th>Doc Number<th3>")
                .append("<th>Revision</th>")
                .append("<th>Actual Revision</th>")
                .append("<th>Status</th>")
                .append("</tr>");
    }

}
