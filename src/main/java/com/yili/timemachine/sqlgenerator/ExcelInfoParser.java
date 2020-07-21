package com.yili.timemachine.sqlgenerator;

import com.yili.timemachine.sqlgenerator.pojo.TableModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.*;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class ExcelInfoParser {

    @Test
    public void sqlGenerator() {
        int sheetIndex = 0 ;
        String xlslPath = "E:\\timemachine\\src\\main\\resources\\static\\database_tables.xlsx";

        Map<String, TableModel> map = parseSheetToTableModel(sheetIndex, xlslPath);
        List<String> sqlList = tableModelToSql(map);

        Path pathxlsl = Paths.get(xlslPath);
        Path parent = pathxlsl.getParent();

        String fileName = sheetIndex + ".sql";
        String sqlPath = parent.toString() + File.separator + fileName;

        try {
            FileWriter fileWriter = new FileWriter(sqlPath);

            for (String sql :sqlList) {
                fileWriter.append(sql);
            }
            fileWriter.close();
        } catch (IOException e) {
            log.error("IOException when write sql file",e);
        }
    }


    public List<String> tableModelToSql(Map<String, TableModel> tableMap) {
        List<String> sqlList = new ArrayList<>();
        for (Map.Entry<String, TableModel> entry : tableMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());

            TableModel tm = entry.getValue();
            String tableName = tm.getTableName();
            List<String> fields = tm.getFieldList();
            List<String> comments = tm.getCommentList();
            StringBuffer z = new StringBuffer();
            for (int i = 0; i < fields.size(); i++) {
                String a = fields.get(i);
                String b = comments.get(i);
                z.append( "`"+a+"` varchar(255) DEFAULT NULL COMMENT '"+b+"', \n");
            }
            String sql = "CREATE TABLE IF NOT EXISTS `"+tableName+"`(\n" +
                    z +
                    ")ENGINE=InnoDB DEFAULT CHARSET=utf8\n";

            System.out.println(sql);
            sqlList.add(sql);
        }
        return sqlList;
    }

    public Map<String, TableModel> parseSheetToTableModel(int sheetIndex, String xlslPath) {
        Map<String, TableModel> allTableMap = new LinkedHashMap<>();

            try {
                Workbook wb = WorkbookFactory.create(new File(xlslPath));
                log.info("start to read one sheet ");
                Sheet sheet = wb.getSheetAt(sheetIndex); // sheet页签索引

                String sheetName = sheet.getSheetName();
                log.info("sheetName is {}",sheetName);

                // 临时table信息容器
                List<String> fieldLst = new ArrayList<>();
                List<String> commentLst = new ArrayList<>();
                for (Row row : sheet) {
                    // 每个首行存放表名称
                    // 第二行开始存字段
                    Cell cellA = row.getCell(0); // A列
                    Cell cellB = row.getCell(1); // B列
                    String cellValueA = "";
                    String cellValueB = "";

                    if(cellA != null) {
                        cellValueA = cellA.getStringCellValue();
                    }

                    if(cellB != null) {
                        cellValueB = cellB.getStringCellValue();
                    }

                    if (!cellValueA.equalsIgnoreCase("start") & !cellValueA.equalsIgnoreCase("end")
                     & !StringUtils.isEmpty(cellValueA)) {
                        fieldLst.add(cellValueB);
                        commentLst.add(cellValueA);
                    } else {
                        if (fieldLst.size()!=0) {
                            // 获取首行表名
                            String tableName = fieldLst.get(0);
                            String tableComment = commentLst.get(0); // 汉字表名在A列， 英文表明在B列

                            // 移除list首行表名信息
                            fieldLst.remove(0);
                            commentLst.remove(0);

                            TableModel tm = new TableModel(tableName, tableComment, fieldLst, commentLst);
                            allTableMap.put(tableName, tm);
                        }
                        fieldLst = new ArrayList<>();
                        commentLst = new ArrayList<>();
                    }
                }
            } catch (FileNotFoundException e) {
                log.error("FileNotFoundException {}" , xlslPath,e);
            } catch (IOException e) {
                log.error("IOException ",e);
            }

        return allTableMap;
    }
}
