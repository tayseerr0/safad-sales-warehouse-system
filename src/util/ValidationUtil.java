package util;

import javax.swing.*;
import java.math.BigDecimal;

/*
Usage Example:
if (ValidationUtil.isEmpty(nameField.getText())) {
    MessageUtil.showError("Name is required.");
    return;
}
 */

public class ValidationUtil {

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isAnyEmpty(String... values) {
        for (String value : values) {
            if (isEmpty(value)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPositiveInteger(String value) {
        try {
            int number = Integer.parseInt(value.trim());
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeInteger(String value) {
        try {
            int number = Integer.parseInt(value.trim());
            return number >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveDecimal(String value) {
        try {
            BigDecimal number = new BigDecimal(value.trim());
            return number.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeDecimal(String value) {
        try {
            BigDecimal number = new BigDecimal(value.trim());
            return number.compareTo(BigDecimal.ZERO) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return true; // optional email is allowed
        }

        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static boolean isSelected(JComboBox<?> comboBox) {
        return comboBox.getSelectedIndex() >= 0 && comboBox.getSelectedItem() != null;
    }

    public static void require(boolean condition, String errorMessage) {
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}