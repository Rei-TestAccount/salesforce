package com.prodly.qa.utils;

import com.prodly.qa.models.Account;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

    /** Write CSV without phone column. */
    public static File writeAccountsNoPhone(File f, List<Account> accs) throws IOException {
        try (OutputStream os = new FileOutputStream(f);
             OutputStreamWriter w = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             CSVPrinter p = new CSVPrinter(w, CSVFormat.DEFAULT.withHeader("Id","Name","Country","NumberOfEmployees"))) {
            for (Account a : accs) {
                p.printRecord(a.getId(), a.getName(), a.getCountry(), a.getNumberOfEmployees());
            }
        }
        return f;
    }

    /** Read CSV with or without Phone column. */
    public static List<Account> readWithPhones(File f) throws IOException {
        List<Account> list = new ArrayList<>();
        try (InputStream is = new FileInputStream(f);
             InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(r)) {
            for (CSVRecord rec : parser) {
                Account a = new Account(rec.get("Name"), rec.get("Country"),
                        Integer.valueOf(rec.get("NumberOfEmployees")));
                a.setId(rec.get("Id"));
                String phone = parser.getHeaderMap().containsKey("Phone") ? rec.get("Phone") : null;
                a.setPhone(phone);
                list.add(a);
            }
        }
        return list;
    }

    /** Produce a new CSV file with Phone column added based on country. */
    public static File addPhoneColumn(File src, File dst) throws IOException {
        try (InputStream is = new FileInputStream(src);
             InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(r);
             OutputStream os = new FileOutputStream(dst);
             OutputStreamWriter w = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             CSVPrinter p = new CSVPrinter(w, CSVFormat.DEFAULT.withHeader("Id","Name","Country","NumberOfEmployees","Phone"))) {
            for (CSVRecord rec : parser) {
                String country = rec.get("Country");
                String phone = PhoneGenerator.forCountry(country);
                p.printRecord(rec.get("Id"), rec.get("Name"), country, rec.get("NumberOfEmployees"), phone);
            }
        }
        return dst;
    }
}
