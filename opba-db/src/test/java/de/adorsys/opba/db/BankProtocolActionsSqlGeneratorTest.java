package de.adorsys.opba.db;

import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import de.adorsys.opba.db.config.TestConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static de.adorsys.opba.db.BankProtocolActionsSqlGeneratorTest.ENABLE_BANK_PROTOCOL_ACTIONS_SQL_GENERATION;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Is not truly a test. This class generates 'banks_random_data.csv' out of 'banks.csv',
 * which contains sql insert statements into 'opb_bank_action' and 'opb_bank_sub_action' tables
 */
@SpringBootTest(classes = TestConfig.class)
@EnabledIfEnvironmentVariable(named = ENABLE_BANK_PROTOCOL_ACTIONS_SQL_GENERATION, matches = "true")
public class BankProtocolActionsSqlGeneratorTest {
    public static final String ENABLE_BANK_PROTOCOL_ACTIONS_SQL_GENERATION = "ENABLE_BANK_PROTOCOL_ACTIONS_SQL_GENERATION";

    private static final String BANKS_SOURCE = "migration/migrations/banks.csv";
    private static final String BANKS_TARGET = "src/main/resources/migration/migrations/bank_action_data.sql";

    @Value("${bank-action-generator.action.start-id}")
    private Integer bankActionId;

    @Value("${bank-action-generator.sub-action.start-id}")
    private Integer bankSubActionId;

    @Test
    @SneakyThrows
    public void convertToDbSql() {
        List<String> banks = readResourceLines(BANKS_SOURCE);
        banks.remove(0);
        prepareDestinationFile(BANKS_TARGET);

        for (String bank : banks) {
            writeXs2aData(bank);
            writeHbciData(bank);
        }
    }

    private void writeXs2aData(String bankRecord) {
        String[] data = bankRecord.split(",");
        int authorizationId;

        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name) values (%d, '%s', 'LIST_ACCOUNTS', 'xs2aListAccounts');",
                bankActionId++, data[0]));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name) values (%d, '%s', 'LIST_TRANSACTIONS', 'xs2aSandboxListTransactions');",
                bankActionId++, data[0]));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name) values (%d, '%s', 'AUTHORIZATION', '');",
                authorizationId = bankActionId++, data[0]));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name) values (%d, '%s', 'SINGLE_PAYMENT', 'xs2aInitiateSinglePayment');",
                bankActionId++, data[0]));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name) values (%d, '%s', 'GET_PAYMENT_INFORMATION', 'xs2aGetPaymentInfoState');",
                bankActionId++, data[0]));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name) values (%d, '%s', 'GET_PAYMENT_STATUS', 'xs2aGetPaymentStatusState');",
                bankActionId++, data[0]));

        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_sub_action (id, action_id, protocol_action, sub_protocol_bean_name) values (%d, '%d, 'GET_AUTHORIZATION_STATE', 'xs2aGetAuthorizationState');",
                bankSubActionId++, authorizationId));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_sub_action (id, action_id, protocol_action, sub_protocol_bean_name) values (%d, '%d, 'UPDATE_AUTHORIZATION', 'xs2aUpdateAuthorization');",
                bankSubActionId++, authorizationId));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_sub_action (id, action_id, protocol_action, sub_protocol_bean_name) values (%d, '%d, 'FROM_ASPSP_REDIRECT', 'xs2aFromAspspRedirect');",
                bankSubActionId++, authorizationId));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_sub_action (id, action_id, protocol_action, sub_protocol_bean_name) values (%d, '%d, 'DENY_AUTHORIZATION', 'xs2aDenyAuthorization');",
                bankSubActionId++, authorizationId));
    }

    private void writeHbciData(String bankRecord) {
        String[] data = bankRecord.split(",");
        int authorizationId;

        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name, consent_supported) values (%d, '%s', 'LIST_ACCOUNTS', 'hbciListAccounts', %s);",
                bankActionId++, data[0], false));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name, consent_supported) values (%d, '%s', 'LIST_TRANSACTIONS', 'hbciListTransactions', %s);",
                bankActionId++, data[0], false));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name, consent_supported) values (%d, '%s', 'AUTHORIZATION', '', %s);",
                authorizationId = bankActionId++, data[0], false));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name, consent_supported) values (%d, '%s', 'SINGLE_PAYMENT', 'hbciInitiateSinglePayment', %s);",
                bankActionId++, data[0], false));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name, consent_supported) values (%d, '%s', 'GET_PAYMENT_INFORMATION', 'hbciGetPaymentStatusState', %s);",
                bankActionId++, data[0], false));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_action (id, bank_uuid, protocol_action, protocol_bean_name, consent_supported) values (%d, '%s', 'GET_PAYMENT_STATUS', 'hbciGetPaymentInfoState', %s);",
                bankActionId++, data[0], false));

        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_sub_action (id, action_id, protocol_action, sub_protocol_bean_name) values (%d, '%d, 'GET_AUTHORIZATION_STATE', 'hbciGetAuthorizationState');",
                bankSubActionId++, authorizationId));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_sub_action (id, action_id, protocol_action, sub_protocol_bean_name) values (%d, '%d, 'UPDATE_AUTHORIZATION', 'hbciUpdateAuthorization');",
                bankSubActionId++, authorizationId));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_sub_action (id, action_id, protocol_action, sub_protocol_bean_name) values (%d, '%d, 'FROM_ASPSP_REDIRECT', 'hbciFromAspspRedirect');",
                bankSubActionId++, authorizationId));
        writeToFile(BANKS_TARGET, String.format(
                "insert into ${table-prefix}bank_sub_action (id, action_id, protocol_action, sub_protocol_bean_name) values (%d, '%d, 'DENY_AUTHORIZATION', 'hbciDenyAuthorization');",
                bankSubActionId++, authorizationId));
    }

    @SneakyThrows
    private List<String> readResourceLines(String path) {
        return Resources.readLines(Resources.getResource(path), UTF_8);
    }

    private void prepareDestinationFile(String path) {
        boolean exists = new File(path).exists();

        if (!exists){
            createFile(path);
            return;
        }

        clearFile(path);
    }

    @SneakyThrows
    private void clearFile(String path) {
        new FileWriter(path, false).close();
    }

    @SneakyThrows
    private void createFile(String path) {
        Files.touch(new File(path));
    }

    @SneakyThrows
    private void writeToFile(String path, String data) {
        Files.asCharSink(new File(path), UTF_8, FileWriteMode.APPEND).write(data);
    }
}