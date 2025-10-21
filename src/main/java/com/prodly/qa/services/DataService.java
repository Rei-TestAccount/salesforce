package com.prodly.qa.services;

import com.prodly.qa.api.SalesforceClient;
import com.prodly.qa.factories.AccountFactory;
import com.prodly.qa.models.Account;
import com.prodly.qa.utils.CsvUtil;
import com.prodly.qa.utils.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class DataService {
    private static final Logger log = LoggerFactory.getLogger(DataService.class);
    private final SalesforceClient sf;
    private final List<String> createdIds = new ArrayList<>();

    public DataService(SalesforceClient sf) { this.sf = sf; }

    /** Create N accounts using Factory/Builder. */
    public List<Account> createAccounts(int count) {
        List<Account> accs = AccountFactory.build(count);
        List<Account> created = new ArrayList<>();
        for (Account a : accs) {
            String id = RetryUtil.retry(() -> sf.createAccount(a.toMap()), 3, 300);
            a.setId(id);
            createdIds.add(id);
            created.add(a);
        }
        log.info("Created {} accounts", created.size());
        return created;
    }

    /** Export accounts (without phone) to CSV file. */
    public File exportNoPhoneCsv(List<Account> accs) {
        try {
            File out = new File("target/accounts_no_phone.csv");
            return CsvUtil.writeAccountsNoPhone(out, accs);
        } catch (Exception e) {
            throw new RuntimeException("CSV export failed", e);
        }
    }

    /** Update phones in Salesforce (best-effort, count successes, log failures). */
    public int updateAccountsPhones(List<Account> accs) {
        int ok = 0;
        for (Account a : accs) {
            try {
                Map<String,Object> body = new HashMap<>();
                body.put("Phone", a.getPhone());
                sf.updateAccount(a.getId(), body);
                ok++;
            } catch (RuntimeException ex) {
                log.error("Update failed for {}: {}", a.getId(), ex.getMessage());
            }
        }
        return ok;
    }

    /** Delete all created Accounts (idempotent). */
    public void cleanup() {
        for (String id : createdIds) {
            try { sf.deleteAccount(id); } catch (RuntimeException ignored) {}
        }
        log.info("Cleanup done for {} accounts", createdIds.size());
    }
}
