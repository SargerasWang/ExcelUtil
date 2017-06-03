/**
 * @author SargerasWang
 */
package com.sargeraswang.util.ExcelUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * The <code>TestExportMap</code>	
 * 
 * @author SargerasWang
 * Created at 2014年9月21日 下午4:38:42
 */
public class TestExportMap {
  public static void main(String[] args) throws IOException {
    List<Map<String,Object>> list = new ArrayList<>();
    Map<String,Object> map =new LinkedHashMap<>();
    map.put("name", "");
    map.put("age", "");
    map.put("birthday","");
    map.put("sex","");
    Map<String,Object> map2 =new LinkedHashMap<String, Object>();
    map2.put("name", null);
    map2.put("age", null);
    map2.put("sex", null);
    map.put("birthday",null);
    Map<String,Object> map3 =new LinkedHashMap<String, Object>();
    map3.put("name", "张三");
    map3.put("age", 12);
    map3.put("sex", "男");
    map3.put("birthday",new Date());
    list.add(map);
    list.add(map2);
    list.add(map3);
    Map<String,String> map1 = new LinkedHashMap<>();
    map1.put("name","姓名");
    map1.put("age","年龄");
    map1.put("birthday","出生日期");
    map1.put("sex","性别");
    File f= new File("c:/test.xls");
    OutputStream out = new FileOutputStream(f);
    ExcelUtil.exportExcel(map1,list, out );
    out.close();
  }
}
