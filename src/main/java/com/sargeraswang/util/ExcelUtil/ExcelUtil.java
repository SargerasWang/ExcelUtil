package com.sargeraswang.util.ExcelUtil;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The <code>ExcelUtil</code> 与 {@link ExcelCell}搭配使用
 *
 * @author sargeras.wang
 * @version 1.0, Created at 2013年9月14日
 */
public class ExcelUtil {

    private static Logger LG = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * 用来验证excel与Vo中的类型是否一致 <br>
     * Map<栏位类型,只能是哪些Cell类型>
     */
    private static Map<Class<?>, Integer[]> validateMap = new HashMap<Class<?>, Integer[]>();

    static {
        validateMap.put(String[].class, new Integer[]{Cell.CELL_TYPE_STRING});
        validateMap.put(Double[].class, new Integer[]{Cell.CELL_TYPE_NUMERIC});
        validateMap.put(String.class, new Integer[]{Cell.CELL_TYPE_STRING});
        validateMap.put(Double.class, new Integer[]{Cell.CELL_TYPE_NUMERIC});
        validateMap.put(Date.class, new Integer[]{Cell.CELL_TYPE_NUMERIC, Cell.CELL_TYPE_STRING});
        validateMap.put(Integer.class, new Integer[]{Cell.CELL_TYPE_NUMERIC});
        validateMap.put(Float.class, new Integer[]{Cell.CELL_TYPE_NUMERIC});
        validateMap.put(Long.class, new Integer[]{Cell.CELL_TYPE_NUMERIC});
        validateMap.put(Boolean.class, new Integer[]{Cell.CELL_TYPE_BOOLEAN});
    }

    /**
     * 获取cell类型的文字描述
     *
     * @param cellType <pre>
     *                 Cell.CELL_TYPE_BLANK
     *                 Cell.CELL_TYPE_BOOLEAN
     *                 Cell.CELL_TYPE_ERROR
     *                 Cell.CELL_TYPE_FORMULA
     *                 Cell.CELL_TYPE_NUMERIC
     *                 Cell.CELL_TYPE_STRING
     *                 </pre>
     * @return
     */
    private static String getCellTypeByInt(int cellType) {
        switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                return "Null type";
            case Cell.CELL_TYPE_BOOLEAN:
                return "Boolean type";
            case Cell.CELL_TYPE_ERROR:
                return "Error type";
            case Cell.CELL_TYPE_FORMULA:
                return "Formula type";
            case Cell.CELL_TYPE_NUMERIC:
                return "Numeric type";
            case Cell.CELL_TYPE_STRING:
                return "String type";
            default:
                return "Unknown type";
        }
    }

    /**
     * 获取单元格值
     *
     * @param cell
     * @return
     */
    private static Object getCellValue(Cell cell) {
        if (cell == null
                || (cell.getCellType() == Cell.CELL_TYPE_STRING && StringUtils.isBlank(cell
                .getStringCellValue()))) {
            return null;
        }
        int cellType = cell.getCellType();
        switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_ERROR:
                return cell.getErrorCellValue();
            case Cell.CELL_TYPE_FORMULA:
                return cell.getNumericCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default:
                return null;
        }
    }

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于单个sheet
     *
     * @param <T>
     * @param headers 表格属性列名数组
     * @param dataset 需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     */
    public static <T> void exportExcel(Map<String,String> headers, Collection<T> dataset, OutputStream out) {
        exportExcel(headers, dataset, out, null);
    }

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于单个sheet
     *
     * @param <T>
     * @param headers 表格属性列名数组
     * @param dataset 需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern 如果有时间数据，设定输出格式。默认为"yyy-MM-dd"
     */
    public static <T> void exportExcel(Map<String,String> headers, Collection<T> dataset, OutputStream out,
                                       String pattern) {
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet();

        write2Sheet(sheet, headers, dataset, pattern);
        try {
            workbook.write(out);
        } catch (IOException e) {
            LG.error(e.toString(), e);
        }
    }

    public static void exportExcel(String[][] datalist, OutputStream out) {
        try {
            // 声明一个工作薄
            HSSFWorkbook workbook = new HSSFWorkbook();
            // 生成一个表格
            HSSFSheet sheet = workbook.createSheet();

            for (int i = 0; i < datalist.length; i++) {
                String[] r = datalist[i];
                HSSFRow row = sheet.createRow(i);
                for (int j = 0; j < r.length; j++) {
                    HSSFCell cell = row.createCell(j);
                    //cell max length 32767
                    if (r[j].length() > 32767) {
                        r[j] = "--此字段过长(超过32767),已被截断--" + r[j];
                        r[j] = r[j].substring(0, 32766);
                    }
                    cell.setCellValue(r[j]);
                }
            }
            //自动列宽
            if (datalist.length > 0) {
                int colcount = datalist[0].length;
                for (int i = 0; i < colcount; i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            workbook.write(out);
        } catch (IOException e) {
            LG.error(e.toString(), e);
        }
    }

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于多个sheet
     *
     * @param <T>
     * @param sheets {@link ExcelSheet}的集合
     * @param out    与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     */
    public static <T> void exportExcel(List<ExcelSheet<T>> sheets, OutputStream out) {
        exportExcel(sheets, out, null);
    }

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于多个sheet
     *
     * @param <T>
     * @param sheets  {@link ExcelSheet}的集合
     * @param out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern 如果有时间数据，设定输出格式。默认为"yyy-MM-dd"
     */
    public static <T> void exportExcel(List<ExcelSheet<T>> sheets, OutputStream out, String pattern) {
        if (CollectionUtils.isEmpty(sheets)) {
            return;
        }
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        for (ExcelSheet<T> sheet : sheets) {
            // 生成一个表格
            HSSFSheet hssfSheet = workbook.createSheet(sheet.getSheetName());
            write2Sheet(hssfSheet, sheet.getHeaders(), sheet.getDataset(), pattern);
        }
        try {
            workbook.write(out);
        } catch (IOException e) {
            LG.error(e.toString(), e);
        }
    }

    /**
     * 每个sheet的写入
     *
     * @param sheet   页签
     * @param headers 表头
     * @param dataset 数据集合
     * @param pattern 日期格式
     */
    private static <T> void write2Sheet(HSSFSheet sheet, Map<String,String> headers, Collection<T> dataset,
                                        String pattern) {
        //时间格式默认"yyyy-MM-dd"
        if (StringUtils.isEmpty(pattern)){
            pattern = "yyyy-MM-dd";
        }
        // 产生表格标题行
        HSSFRow row = sheet.createRow(0);
        //todo:标题行转中文
        Set<String> keys = headers.keySet();
        Iterator<String> it1 = keys.iterator();
        String key = "";    //存放临时键变量
        int c= 0;   //标题列数
        while (it1.hasNext()){
            key = it1.next();
            if (headers.containsKey(key)) {
                HSSFCell cell = row.createCell(c);
                HSSFRichTextString text = new HSSFRichTextString(headers.get(key));
                cell.setCellValue(text);
                c++;
            }
        }
//        for (int i = 0; i < headers.length; i++) {
//            HSSFCell cell = row.createCell(i);
//            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
//            cell.setCellValue(text);
//        }

        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        int index = 0;
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            T t = (T) it.next();
            try {
                if (t instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) t;
                    int cellNum = 0;
                    //todo:遍历列名
                    Iterator<String> it2 = keys.iterator();
                    while (it2.hasNext()){
                        key = it2.next();
                        if (!headers.containsKey(key)) {
                            LG.error("Map 中 不存在 key [" + key + "]");
                            continue;
                        }
                        Object value = map.get(key);
                        HSSFCell cell = row.createCell(cellNum);
//                        cell.setCellValue(String.valueOf(value));
                        String textValue = null;
                        if (value instanceof Integer) {
                            int intValue = (Integer) value;
                            cell.setCellValue(intValue);
                        } else if (value instanceof Float) {
                            float fValue = (Float) value;
                            cell.setCellValue(fValue);
                        } else if (value instanceof Double) {
                            double dValue = (Double) value;
                            cell.setCellValue(dValue);
                        } else if (value instanceof Long) {
                            long longValue = (Long) value;
                            cell.setCellValue(longValue);
                        } else if (value instanceof Boolean) {
                            boolean bValue = (Boolean) value;
                            cell.setCellValue(bValue);
                        } else if (value instanceof Date) {
                            Date date = (Date) value;
                            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                            textValue = sdf.format(date);
                        } else if (value instanceof String[]) {
                            String[] strArr = (String[]) value;
                            for (int j = 0; j < strArr.length; j++) {
                                String str = strArr[j];
                                cell.setCellValue(str);
                                if (j != strArr.length - 1) {
                                    cellNum++;
                                    cell = row.createCell(cellNum);
                                }
                            }
                        } else if (value instanceof Double[]) {
                            Double[] douArr = (Double[]) value;
                            for (int j = 0; j < douArr.length; j++) {
                                Double val = douArr[j];
                                // 值不为空则set Value
                                if (val != null) {
                                    cell.setCellValue(val);
                                }

                                if (j != douArr.length - 1) {
                                    cellNum++;
                                    cell = row.createCell(cellNum);
                                }
                            }
                        } else {
                            // 其它数据类型都当作字符串简单处理
                            String empty = StringUtils.EMPTY;
                            textValue = value == null ? empty : value.toString();
                        }
                        if (textValue != null) {
                            HSSFRichTextString richString = new HSSFRichTextString(textValue);
                            cell.setCellValue(richString);
                        }


                        cellNum++;
                    }
//                    for (String k : headers) {
//                        if (map.containsKey(k) == false) {
//                            LG.error("Map 中 不存在 key [" + k + "]");
//                            continue;
//                        }
//                        Object value = map.get(k);
//                        HSSFCell cell = row.createCell(cellNum);
//                        cell.setCellValue(String.valueOf(value));
//                        cellNum++;
//                    }
                } else {
                    List<FieldForSortting> fields = sortFieldByAnno(t.getClass());
                    int cellNum = 0;
                    for (int i = 0; i < fields.size(); i++) {
                        HSSFCell cell = row.createCell(cellNum);
                        Field field = fields.get(i).getField();
                        field.setAccessible(true);
                        Object value = field.get(t);
                        String textValue = null;
                        if (value instanceof Integer) {
                            int intValue = (Integer) value;
                            cell.setCellValue(intValue);
                        } else if (value instanceof Float) {
                            float fValue = (Float) value;
                            cell.setCellValue(fValue);
                        } else if (value instanceof Double) {
                            double dValue = (Double) value;
                            cell.setCellValue(dValue);
                        } else if (value instanceof Long) {
                            long longValue = (Long) value;
                            cell.setCellValue(longValue);
                        } else if (value instanceof Boolean) {
                            boolean bValue = (Boolean) value;
                            cell.setCellValue(bValue);
                        } else if (value instanceof Date) {
                            Date date = (Date) value;
                            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                            textValue = sdf.format(date);
                        } else if (value instanceof String[]) {
                            String[] strArr = (String[]) value;
                            for (int j = 0; j < strArr.length; j++) {
                                String str = strArr[j];
                                cell.setCellValue(str);
                                if (j != strArr.length - 1) {
                                    cellNum++;
                                    cell = row.createCell(cellNum);
                                }
                            }
                        } else if (value instanceof Double[]) {
                            Double[] douArr = (Double[]) value;
                            for (int j = 0; j < douArr.length; j++) {
                                Double val = douArr[j];
                                // 值不为空则set Value
                                if (val != null) {
                                    cell.setCellValue(val);
                                }

                                if (j != douArr.length - 1) {
                                    cellNum++;
                                    cell = row.createCell(cellNum);
                                }
                            }
                        } else {
                            // 其它数据类型都当作字符串简单处理
                            String empty = StringUtils.EMPTY;
                            ExcelCell anno = field.getAnnotation(ExcelCell.class);
                            if (anno != null) {
                                empty = anno.defaultValue();
                            }
                            textValue = value == null ? empty : value.toString();
                        }
                        if (textValue != null) {
                            HSSFRichTextString richString = new HSSFRichTextString(textValue);
                            cell.setCellValue(richString);
                        }

                        cellNum++;
                    }
                }
            } catch (Exception e) {
                LG.error(e.toString(), e);
            }
        }
        // 设定自动宽度
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 把Excel的数据封装成voList
     *
     * @param clazz       vo的Class
     * @param inputStream excel输入流
     * @param pattern     如果有时间数据，设定输入格式。默认为"yyy-MM-dd"
     * @param logs        错误log集合
     * @param arrayCount  如果vo中有数组类型,那就按照index顺序,把数组应该有几个值写上.
     * @return voList
     * @throws RuntimeException
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> importExcel(Class<T> clazz, InputStream inputStream,
                                                String pattern, ExcelLogs logs, Integer... arrayCount) {
        HSSFWorkbook workBook = null;
        try {
            workBook = new HSSFWorkbook(inputStream);
        } catch (IOException e) {
            LG.error(e.toString(), e);
        }
        List<T> list = new ArrayList<T>();
        HSSFSheet sheet = workBook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        try {
            List<ExcelLog> logList = new ArrayList<ExcelLog>();
            // Map<title,index>
            Map<String, Integer> titleMap = new HashMap<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() == 0) {
                    if (clazz == Map.class) {
                        // 解析map用的key,就是excel标题行
                        Iterator<Cell> cellIterator = row.cellIterator();
                        Integer index = 0;
                        while (cellIterator.hasNext()) {
                            String value = cellIterator.next().getStringCellValue();
                            titleMap.put(value, index);
                            index++;
                        }
                    }
                    continue;
                }
                // 整行都空，就跳过
                boolean allRowIsNull = true;
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Object cellValue = getCellValue(cellIterator.next());
                    if (cellValue != null) {
                        allRowIsNull = false;
                        break;
                    }
                }
                if (allRowIsNull) {
                    LG.warn("Excel row " + row.getRowNum() + " all row value is null!");
                    continue;
                }
                T t = null;
                StringBuilder log = new StringBuilder();
                if (clazz == Map.class) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    for (String k : titleMap.keySet()) {
                        Integer index = titleMap.get(k);
                        String value = row.getCell(index).getStringCellValue();
                        map.put(k, value);
                    }
                    list.add((T) map);

                } else {
                    t = clazz.newInstance();
                    int arrayIndex = 0;// 标识当前第几个数组了
                    int cellIndex = 0;// 标识当前读到这一行的第几个cell了
                    List<FieldForSortting> fields = sortFieldByAnno(clazz);
                    for (FieldForSortting ffs : fields) {
                        Field field = ffs.getField();
                        field.setAccessible(true);
                        if (field.getType().isArray()) {
                            Integer count = arrayCount[arrayIndex];
                            Object[] value = null;
                            if (field.getType().equals(String[].class)) {
                                value = new String[count];
                            } else {
                                // 目前只支持String[]和Double[]
                                value = new Double[count];
                            }
                            for (int i = 0; i < count; i++) {
                                Cell cell = row.getCell(cellIndex);
                                String errMsg = validateCell(cell, field, cellIndex);
                                if (StringUtils.isBlank(errMsg)) {
                                    value[i] = getCellValue(cell);
                                } else {
                                    log.append(errMsg);
                                    log.append(";");
                                    logs.setHasError(true);
                                }
                                cellIndex++;
                            }
                            field.set(t, value);
                            arrayIndex++;
                        } else {
                            Cell cell = row.getCell(cellIndex);
                            String errMsg = validateCell(cell, field, cellIndex);
                            if (StringUtils.isBlank(errMsg)) {
                                Object value = null;
                                // 处理特殊情况,Excel中的String,转换成Bean的Date
                                if (field.getType().equals(Date.class)
                                        && cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                    Object strDate = getCellValue(cell);
                                    try {
                                        value = new SimpleDateFormat(pattern).parse(strDate.toString());
                                    } catch (ParseException e) {

                                        errMsg =
                                                MessageFormat.format("the cell [{0}] can not be converted to a date ",
                                                        CellReference.convertNumToColString(cell.getColumnIndex()));
                                    }
                                } else {
                                    value = getCellValue(cell);
                                    // 处理特殊情况,excel的value为String,且bean中为其他,且defaultValue不为空,那就=defaultValue
                                    ExcelCell annoCell = field.getAnnotation(ExcelCell.class);
                                    if (value instanceof String && !field.getType().equals(String.class)
                                            && StringUtils.isNotBlank(annoCell.defaultValue())) {
                                        value = annoCell.defaultValue();
                                    }
                                }
                                field.set(t, value);
                            }
                            if (StringUtils.isNotBlank(errMsg)) {
                                log.append(errMsg);
                                log.append(";");
                                logs.setHasError(true);
                            }
                            cellIndex++;
                        }
                    }
                    list.add(t);
                    logList.add(new ExcelLog(t, log.toString(), row.getRowNum() + 1));
                }
            }
            logs.setLogList(logList);
        } catch (InstantiationException e) {
            throw new RuntimeException(MessageFormat.format("can not instance class:{0}",
                    clazz.getSimpleName()), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(MessageFormat.format("can not instance class:{0}",
                    clazz.getSimpleName()), e);
        }
        return list;
    }

    /**
     * 驗證Cell類型是否正確
     *
     * @param cell    cell單元格
     * @param field   欄位
     * @param cellNum 第幾個欄位,用於errMsg
     * @return
     */
    private static String validateCell(Cell cell, Field field, int cellNum) {
        String columnName = CellReference.convertNumToColString(cellNum);
        String result = null;
        Integer[] integers = validateMap.get(field.getType());
        if (integers == null) {
            result = MessageFormat.format("Unsupported type [{0}]", field.getType().getSimpleName());
            return result;
        }
        ExcelCell annoCell = field.getAnnotation(ExcelCell.class);
        if (cell == null
                || (cell.getCellType() == Cell.CELL_TYPE_STRING && StringUtils.isBlank(cell
                .getStringCellValue()))) {
            if (annoCell != null && annoCell.valid().allowNull() == false) {
                result = MessageFormat.format("the cell [{0}] can not null", columnName);
            }
            ;
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK && annoCell.valid().allowNull()) {
            return result;
        } else {
            List<Integer> cellTypes = Arrays.asList(integers);

            // 如果類型不在指定範圍內,並且沒有默認值
            if (!(cellTypes.contains(cell.getCellType()))
                    || StringUtils.isNotBlank(annoCell.defaultValue())
                    && cell.getCellType() == Cell.CELL_TYPE_STRING) {
                StringBuilder strType = new StringBuilder();
                for (int i = 0; i < cellTypes.size(); i++) {
                    Integer intType = cellTypes.get(i);
                    strType.append(getCellTypeByInt(intType));
                    if (i != cellTypes.size() - 1) {
                        strType.append(",");
                    }
                }
                result =
                        MessageFormat.format("the cell [{0}] type must [{1}]", columnName, strType.toString());
            } else {
                // 类型符合验证,但值不在要求范围内的
                // String in
                if (annoCell.valid().in().length != 0 && cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    String[] in = annoCell.valid().in();
                    String cellValue = cell.getStringCellValue();
                    boolean isIn = false;
                    for (String str : in) {
                        if (str.equals(cellValue)) {
                            isIn = true;
                        }
                    }
                    if (!isIn) {
                        result = MessageFormat.format("the cell [{0}] value must in {1}", columnName, in);
                    }
                }
                // 数字型
                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    double cellValue = cell.getNumericCellValue();
                    // 小于
                    if (!Double.isNaN(annoCell.valid().lt())) {
                        if (!(cellValue < annoCell.valid().lt())) {
                            result =
                                    MessageFormat.format("the cell [{0}] value must less than [{1}]", columnName,
                                            annoCell.valid().lt());
                        }
                    }
                    // 大于
                    if (!Double.isNaN(annoCell.valid().gt())) {
                        if (!(cellValue > annoCell.valid().gt())) {
                            result =
                                    MessageFormat.format("the cell [{0}] value must greater than [{1}]", columnName,
                                            annoCell.valid().gt());
                        }
                    }
                    // 小于等于
                    if (!Double.isNaN(annoCell.valid().le())) {
                        if (!(cellValue <= annoCell.valid().le())) {
                            result =
                                    MessageFormat.format("the cell [{0}] value must less than or equal [{1}]",
                                            columnName, annoCell.valid().le());
                        }
                    }
                    // 大于等于
                    if (!Double.isNaN(annoCell.valid().ge())) {
                        if (!(cellValue >= annoCell.valid().ge())) {
                            result =
                                    MessageFormat.format("the cell [{0}] value must greater than or equal [{1}]",
                                            columnName, annoCell.valid().ge());
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 根据annotation的seq排序后的栏位
     *
     * @param clazz
     * @return
     */
    private static List<FieldForSortting> sortFieldByAnno(Class<?> clazz) {
        Field[] fieldsArr = clazz.getDeclaredFields();
        List<FieldForSortting> fields = new ArrayList<FieldForSortting>();
        List<FieldForSortting> annoNullFields = new ArrayList<FieldForSortting>();
        for (Field field : fieldsArr) {
            ExcelCell ec = field.getAnnotation(ExcelCell.class);
            if (ec == null) {
                // 没有ExcelCell Annotation 视为不汇入
                continue;
            }
            int id = ec.index();
            fields.add(new FieldForSortting(field, id));
        }
        fields.addAll(annoNullFields);
        sortByProperties(fields, true, false, "index");
        return fields;
    }

    @SuppressWarnings("unchecked")
    private static void sortByProperties(List<? extends Object> list, boolean isNullHigh,
                                         boolean isReversed, String... props) {
        if (CollectionUtils.isNotEmpty(list)) {
            Comparator<?> typeComp = ComparableComparator.getInstance();
            if (isNullHigh == true) {
                typeComp = ComparatorUtils.nullHighComparator(typeComp);
            } else {
                typeComp = ComparatorUtils.nullLowComparator(typeComp);
            }
            if (isReversed) {
                typeComp = ComparatorUtils.reversedComparator(typeComp);
            }

            List<Object> sortCols = new ArrayList<Object>();

            if (props != null) {
                for (String prop : props) {
                    sortCols.add(new BeanComparator(prop, typeComp));
                }
            }
            if (sortCols.size() > 0) {
                Comparator<Object> sortChain = new ComparatorChain(sortCols);
                Collections.sort(list, sortChain);
            }
        }
    }

}
