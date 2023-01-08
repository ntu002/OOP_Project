package json;

import com.fasterxml.jackson.databind.ObjectMapper;
import history.HistoricalEntity;

import java.io.File;
import java.io.IOException;

public class JSON {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    //Cần chỉnh sửa khi clone project
    public static final String PREFIX_URL = "/Users/ntu00/Desktop/hoc/Năm_3/OOP/OOP_project/src/json";

    public static void writeJSON(String filename, HistoricalEntity entity) {
        try {
            MAPPER.writeValue(new File(PREFIX_URL + filename), entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
