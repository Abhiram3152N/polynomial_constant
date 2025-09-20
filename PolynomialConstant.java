import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.math.BigInteger;

public class PolynomialConstant {
    
    public static void main(String[] args) {
        try {
            String filename = "input1.json";
            
            if (args.length > 0) {
                filename = args[0];
            }
            
            // Step 1: Read JSON file
            String jsonContent = readJsonFile(filename);
            
            // Step 2: Decode Y values from different bases
            List<Point> points = decodeYValues(jsonContent);
            
            // Step 3: Find constant using Lagrange interpolation at x=0
            BigInteger constant = findConstant(points);
            
            // Print only the constant
            System.out.println(constant);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Read JSON file
     */
    public static String readJsonFile(String filename) throws IOException {
        if (!Files.exists(Paths.get(filename))) {
            System.err.println("File not found: " + filename);
            System.err.println("Please provide the JSON file.");
            throw new FileNotFoundException("File not found: " + filename);
        }
        return new String(Files.readAllBytes(Paths.get(filename)));
    }
    
    // Point class to handle BigInteger coordinates
    static class Point {
        BigInteger x, y;
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }
    
    /**
     * Step 2: Decode Y values from different bases using BigInteger
     */
    public static List<Point> decodeYValues(String jsonContent) {
        List<Point> points = new ArrayList<>();
        
        try {
            String[] lines = jsonContent.split("\n");
            
            for (String line : lines) {
                line = line.trim();
                
                // Look for numbered keys
                if (line.matches("\\s*\"\\d+\":\\s*\\{")) {
                    // Extract key number
                    int start = line.indexOf('"') + 1;
                    int end = line.indexOf('"', start);
                    BigInteger x = new BigInteger(line.substring(start, end));
                    
                    // Find the base and value in next few lines
                    String base = null;
                    String value = null;
                    
                    // Look ahead for base and value
                    for (int i = 0; i < lines.length; i++) {
                        if (lines[i].equals(line)) {
                            // Found current line, look for base and value in next lines
                            for (int j = i + 1; j < i + 5 && j < lines.length; j++) {
                                String nextLine = lines[j].trim();
                                if (nextLine.contains("\"base\":")) {
                                    int baseStart = nextLine.indexOf('"', nextLine.indexOf("\"base\":") + 7) + 1;
                                    int baseEnd = nextLine.indexOf('"', baseStart);
                                    base = nextLine.substring(baseStart, baseEnd);
                                }
                                if (nextLine.contains("\"value\":")) {
                                    int valueStart = nextLine.indexOf('"', nextLine.indexOf("\"value\":") + 8) + 1;
                                    int valueEnd = nextLine.indexOf('"', valueStart);
                                    value = nextLine.substring(valueStart, valueEnd);
                                }
                            }
                            break;
                        }
                    }
                    
                    if (base != null && value != null) {
                        BigInteger y = convertFromBase(value, Integer.parseInt(base));
                        points.add(new Point(x, y));
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
        
        return points;
    }
    
    /**
     * Convert number from given base to decimal using BigInteger
     */
    private static BigInteger convertFromBase(String value, int base) {
        BigInteger result = BigInteger.ZERO;
        BigInteger baseBig = BigInteger.valueOf(base);
        BigInteger power = BigInteger.ONE;
        
        // Process digits from right to left
        for (int i = value.length() - 1; i >= 0; i--) {
            char digit = value.charAt(i);
            int digitValue;
            
            if (digit >= '0' && digit <= '9') {
                digitValue = digit - '0';
            } else if (digit >= 'A' && digit <= 'Z') {
                digitValue = digit - 'A' + 10;
            } else if (digit >= 'a' && digit <= 'z') {
                digitValue = digit - 'a' + 10;
            } else {
                throw new IllegalArgumentException("Invalid digit: " + digit);
            }
            
            result = result.add(BigInteger.valueOf(digitValue).multiply(power));
            power = power.multiply(baseBig);
        }
        
        return result;
    }
    
    /**
     * Step 3: Find constant using Lagrange interpolation at x=0 with BigInteger
     */
    public static BigInteger findConstant(List<Point> points) {
        BigInteger result = BigInteger.ZERO;
        
        // Use only first k points (minimum required for interpolation)
        // For this case, k=7, so we need any 7 points
        int k = Math.min(7, points.size());
        
        // Lagrange interpolation to find f(0)
        for (int i = 0; i < k; i++) {
            BigInteger xi = points.get(i).x;
            BigInteger yi = points.get(i).y;
            
            // Calculate Lagrange basis polynomial Li(0)
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigInteger xj = points.get(j).x;
                    numerator = numerator.multiply(BigInteger.ZERO.subtract(xj)); // (0 - xj)
                    denominator = denominator.multiply(xi.subtract(xj)); // (xi - xj)
                }
            }
            
            // Add yi * (numerator / denominator) to result
            // Since we're dealing with integers, we need to be careful with division
            result = result.add(yi.multiply(numerator).divide(denominator));
        }
        
        return result;
    }
}
