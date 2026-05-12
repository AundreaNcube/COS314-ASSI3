package util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/*
 FORMAT:
 class, age, menopause, tumor_size, inv_nodes, node_caps, deg_malig, breast, breast_quad, irradiat
 HAD TO store the class label as the last element of each record to make sure indexes 0-8 to map directly to f0-f8 
 */
public class DataLoader
{

    public static final int NUM_FEATURES = 9;
    private static final int TOTAL_COLS = 10; //feat+label



    //////////////////{ load }\\\\\\\\\\\\\\\\\\\\
    // return 2d array 
    //  row[i][0..8] = our feature values f0-f8
    // row[i][9]    = class label (0 or 1)

    public static List<int[]> load(String filePath) throws IOException
    {
        List<int[]> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {
            String l;
            int line_num = 0;
            boolean skipped_headers = false;


            while ((l = br.readLine()) != null)
            {
                line_num++;
                l = l.trim();
                if (l.isEmpty()) continue;

                String[] fields = l.split(",", -1);

                // need to skip header: first field is not an integer
                if (!skipped_headers)
                {
                    try
                    {
                        Integer.parseInt(fields[0].trim());
                    }
                    catch (NumberFormatException e)
                    {
                        skipped_headers = true;
                        continue;
                    }
                    skipped_headers = true;
                }


                // need to validate column count
                if (fields.length != TOTAL_COLS) 
                {
                    throw new IllegalArgumentException( "Row " + line_num + ": expected " + TOTAL_COLS + " fields but found " + fields.length + ".");
                }


                
                // parse all fields; CSV column order is:
                // col 0 = class label, cols 1-9 = features
                // rearrange so that record[0-8] = features, record[9] = label.

                int[] record = new int[TOTAL_COLS];
                int class_label = parseField(fields[0], line_num, 1);

                for (int col = 1; col < TOTAL_COLS; col++)
                {
                    record[col - 1] = parseField(fields[col], line_num, col + 1);
                }
                record[NUM_FEATURES] = class_label;

                records.add(record);
            }

        } 
        catch (IOException e) 
        {
            throw new IOException("Cannot open file: " + filePath + " — " + e.getMessage(), e);
        }


        if (records.isEmpty())
        {
            throw new IllegalArgumentException("File '" + filePath + "' contains no data rows.");
        }

        return records;
    }




    ///////////////////{::::::::}\\\\\\\\\\\\\\\\\\\\\
    //returns features as a double[] for tree eval !!convienience
    //record[0..8] -> double[0..8]
    public static double[] toFeatureVector(int[] record)
    {
        double[] fv = new double[NUM_FEATURES];
        for (int i = 0; i < NUM_FEATURES; i++)
        {
            fv[i] = record[i];
        }
        return fv;
    }


    /////////////////{""""""""}\\\\\\\\\\\\\\\\\\\\
    public static int getLabel(int[] record)
    {
        return record[NUM_FEATURES];
    }



    ///////////////{ helpers }\\\\\\\\\\\\\\\\\\\\\
    private static int parseField(String raw, int rowNum, int colNum)
    {
        try
        {
            return Integer.parseInt(raw.trim());
        } 
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Row " + rowNum + ", column " + colNum +": cannot parse '" + raw.trim() + "' as an integer.");
        }
    }
}
