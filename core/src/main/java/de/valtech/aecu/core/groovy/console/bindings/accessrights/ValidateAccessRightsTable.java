/*
 * Copyright 2019 Valtech GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.valtech.aecu.core.groovy.console.bindings.accessrights;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.valtech.aecu.api.groovy.console.bindings.accessrights.AccessRightValidator;
import hu.vissy.texttable.TableFormatter;
import hu.vissy.texttable.column.ColumnDefinition;

/**
 * Table that contains the results of an access right check.
 * 
 * @author Roland Gruber
 */
public class ValidateAccessRightsTable {

    private TableFormatter<TableRow> formatter;
    private List<TableRow> rows = new ArrayList<>();

    /**
     * Constructor
     */
    public ValidateAccessRightsTable() {
        formatter = new TableFormatter.Builder<TableRow>().withHeading("Access right test results")
                .withColumn(new ColumnDefinition.StatelessBuilder<TableRow, String>().withTitle("User/Group")
                        .withDataExtractor(TableRow::getAuthorizable).build())
                .withColumn(new ColumnDefinition.StatelessBuilder<TableRow, String>().withTitle("Path")
                        .withDataExtractor(TableRow::getPath).build())
                .withColumn(new ColumnDefinition.StatelessBuilder<TableRow, String>().withTitle("Rights")
                        .withDataExtractor(TableRow::getCheckResultsText).build())
                .build();
    }

    /**
     * Adds the result of an validator.
     * 
     * @param validator validator
     */
    public void add(AccessRightValidator validator) {
        boolean success = validator.validate();
        String authorizable = validator.getAuthorizableId();
        String path = validator.getResource().getPath();
        if (rows.isEmpty()) {
            rows.add(new TableRow(authorizable, path, null));
        }
        TableRow lastRow = rows.get(rows.size() - 1);
        if (!(authorizable.equals(lastRow.authorizable) && path.equals(lastRow.path))) {
            if (!(authorizable.equals(lastRow.authorizable))) {
                rows.add(null);
            }
            lastRow = new TableRow(authorizable, path, rows.get(rows.size() - 1));
            rows.add(lastRow);
        }
        lastRow.addResult(validator.getLabel(), success);
    }

    /**
     * Returns the text representation of the table.
     * 
     * @return text
     */
    public String getText() {
        return formatter.apply(rows);
    }

    /**
     * Table row for result table.
     * 
     * @author Roland Gruber
     */
    private static class TableRow {

        private String authorizable;
        private String path;
        private List<String> checkLabelsOk = new ArrayList<>();
        private List<String> checkLabelsFail = new ArrayList<>();
        private TableRow lastRow;

        /**
         * Constructor
         * 
         * @param authorizable user/group name
         * @param path         path
         */
        public TableRow(String authorizable, String path, TableRow lastRow) {
            this.authorizable = authorizable;
            this.path = path;
            this.lastRow = lastRow;
        }

        /**
         * Adds a result entry.
         * 
         * @param label   label of check
         * @param success success or fail
         */
        public void addResult(String label, boolean success) {
            if (success) {
                checkLabelsOk.add(label);
            } else {
                checkLabelsFail.add(label);
            }
        }

        public String getCheckResultsText() {
            Collections.sort(checkLabelsOk);
            Collections.sort(checkLabelsFail);
            StringBuilder output = new StringBuilder();
            if (!checkLabelsFail.isEmpty()) {
                String labels = String.join(", ", checkLabelsFail);
                output.append("FAIL: " + labels);
            }
            if (!checkLabelsOk.isEmpty() && !checkLabelsFail.isEmpty()) {
                output.append("\n");
            }
            if (!checkLabelsOk.isEmpty()) {
                String labels = String.join(", ", checkLabelsOk);
                output.append("OK: " + labels);
            }
            return output.toString();
        }

        /**
         * Returns the authorizable name.
         * 
         * @return user/group name
         */
        public String getAuthorizable() {
            if (lastRow == null) {
                return authorizable;
            }
            if (lastRow.authorizable.equals(authorizable)) {
                return StringUtils.EMPTY;
            }
            return authorizable;
        }

        /**
         * Returns the path.
         * 
         * @return path
         */
        public String getPath() {
            return path;
        }

    }

}
