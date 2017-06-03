package com.sargeraswang.util.ExcelUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class TestExportBean {
    public static void main(String[] args) throws IOException {
        
//        String[] headers = {"a","b","c"};
        //用排序的Map且Map的键赢与ExcelCell注解的index对应
        Map<String,String> map1 = new LinkedHashMap<>();
        map1.put("a","姓名");
        map1.put("b","年龄");
        map1.put("c","性别");
        map1.put("d","出生日期");
        Collection<Object> dataset=new ArrayList<Object>();
        dataset.add(new Model("", "", "",null));
        dataset.add(new Model(null, null, null,null));
        dataset.add(new Model("王五", "34", "男",new Date()));
        File f=new File("c:/test.xls");
        OutputStream out =new FileOutputStream(f);
        
        ExcelUtil.exportExcel(map1, dataset, out);
        out.close();
    }
}
