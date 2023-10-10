package hawk.util;
import java.util.Objects;
import java.util.Scanner;
import java.util.ArrayList;
public class Util {
    public static final Scanner input = new Scanner(System.in);

    /**
     * Finds the greatest value in an ArrayList
     * @param arrayList the ArrayList to search
     * @param returnIndex whether to return the index of the greatest value or the value itself
     * @return the greatest value in an ArrayList
     */
    public static int findGreatestValue(ArrayList<Integer> arrayList, boolean returnIndex){
        if (arrayList.isEmpty()) {
            throw new IllegalArgumentException("the ArrayList is empty!!");
        }

        // if all digits are equal to each other, return the first digit:
        for (int i = 1; i < arrayList.size(); i++) {
            if (!Objects.equals(arrayList.get(i), arrayList.get(0))){
                break;
            }
            if (i == arrayList.size() - 1){
                if (returnIndex){
                    return 0;
                }
                return arrayList.get(0);
            }
        }


        int maxValue = arrayList.get(0); // Initialize with the first element

        for (int i = 1; i < arrayList.size(); i++) {
            int current = arrayList.get(i);
            if (current > maxValue) {
                maxValue = current;
            }
        }
        if (returnIndex){
            return arrayList.indexOf(maxValue);
        }
        return maxValue;
    }

    /**
     * Returns the complexity of a number, based of visual density
     * @param number the number to check
     * @return the complexity of the number
     */
    public static int getComplexity(int number) {
        return switch (number) {
            case 1 -> 10;
            case 2 -> 9;
            case 3 -> 8;
            case 0 -> 7;
            case 7 -> 6;
            case 8 -> 5;
            case 9 -> 4;
            case 4 -> 3;
            case 6 -> 2;
            case 5 -> 1;
            default -> -1;
        };
    }

    /**
     * Turn the digits of an int into an ArrayList (1 int per index)
     * @param number the int to convert
     * @return the ArrayList of digits
     */
    public static ArrayList<Integer> getDigits(int number) {
        int temp = number;
        int digitCount = 0;
        while (temp != 0) {
            temp /= 10;
            digitCount++;
        }

        ArrayList<Integer> digits = new ArrayList<>();

        for (int i = digitCount - 1; i >= 0; i--) {
            digits.add(number % 10);
            number /= 10;
        }

        return digits;
    }

    /**
     * Converts a unicode key to a String
     * @param code the unicode key
     * @return the String representation of the unicode key
     */
    public static String unicodeKeyToString(String code){
        int codePoint = Integer.parseInt(code.substring(2), 16); // Remove the leading "\\"
        return new String(Character.toChars(codePoint));
    }
}
