import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javassist.*;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.rules.RuleFactory;
import com.sun.codemodel.JCodeModel;
public class ApplicationMain {

    public static void main(String[] args) throws IOException {
        try (InputStream in = new FileInputStream("/Users/shobha/Downloads/sparktutorial/src/main/resources/file.csv");) {
            CSV csv = new CSV(true, ',', in );
            List< String > fieldNames = null;
            if (csv.hasNext()) fieldNames = new ArrayList< >(csv.next());
            List <Map< String, String >> list = new ArrayList < > ();
            while (csv.hasNext()) {
                List < String > x = csv.next();
                Map < String, String > obj = new LinkedHashMap< >();
                for (int i = 0; i < fieldNames.size(); i++) {
                    obj.put(fieldNames.get(i), x.get(i));
                }
                list.add(obj);
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File("/Users/shobha/Downloads/sparktutorial/src/main/resources/input.json"), list);
        }

        String packageName="convertedPojo";
        File inputJson= new File("/Users/shobha/Downloads/sparktutorial/src/main/resources/input.json");
        File outputPojoDirectory=new File("/Users/shobha/Downloads/sparktutorial/src/main/java/convertedPojo");
        outputPojoDirectory.mkdirs();
        try {
            new ApplicationMain().convert2JSON(inputJson.toURI().toURL(), outputPojoDirectory, packageName, inputJson.getName().replace(".json", ""));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Encountered issue while converting to pojo: "+e.getMessage());
            e.printStackTrace();
        }

    }


    public void convert2JSON(URL inputJson, File outputPojoDirectory, String packageName, String className) throws IOException{
        JCodeModel codeModel = new JCodeModel();
        URL source = inputJson;
        GenerationConfig config = new DefaultGenerationConfig() {
            @Override
            public boolean isGenerateBuilders() { // set config option by overriding method
                return true;
            }
            public SourceType getSourceType(){
                return SourceType.JSON;
            }
        };
        SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()), new SchemaGenerator());
        mapper.generate(codeModel, className, packageName, source);
        codeModel.build(outputPojoDirectory);
    }
}