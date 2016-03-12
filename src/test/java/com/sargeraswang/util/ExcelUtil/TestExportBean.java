package com.sargeraswang.util.ExcelUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import com.sargeraswang.util.ExcelUtil.ExcelUtil;

public class TestExportBean {
    public static void main(String[] args) throws IOException {
        
        String[] headers = {"a","b","c"};
        Collection<Object> dataset=new ArrayList<Object>();
        dataset.add(new Model("a1", "b1", "c1"));
        dataset.add(new Model("a2", "b2", "c2"));
        dataset.add(new Model("a3", "b3", "c3"));
        File f=new File("/Users/SagerasWang/Desktop/test.xls");
        OutputStream out =new FileOutputStream(f);
        
        ExcelUtil.exportExcel(headers, dataset, out);
        out.close();
    }
}
