package io.serverlessworkflow.api.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.mapper.JsonObjectMapper;
import io.serverlessworkflow.api.mapper.YamlObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Workflow provides some extra functionality for the Workflow types
 */
public class BaseWorkflow {

    private static JsonObjectMapper jsonObjectMapper = new JsonObjectMapper();
    private static YamlObjectMapper yamlObjectMapper = new YamlObjectMapper();

    private static Logger logger = LoggerFactory.getLogger(BaseWorkflow.class);

    public static Workflow fromSource(String source) {
        // try it as json markup first, if fails try yaml
        try {
            return jsonObjectMapper.readValue(source,
                    Workflow.class);
        } catch (Exception e) {
            try {
                return yamlObjectMapper.readValue(source,
                        Workflow.class);
            } catch (Exception ee) {
                throw new IllegalArgumentException("Could not convert markup to Workflow: " + ee.getMessage());
            }
        }
    }

    public static String toJson(Workflow workflow) {
        try {
            return jsonObjectMapper.writeValueAsString(workflow);
        } catch (JsonProcessingException e) {
            logger.error("Error mapping to json: " + e.getMessage());
            return null;
        }
    }

    public static String toYaml(Workflow workflow) {
        try {
            String jsonString = jsonObjectMapper.writeValueAsString(workflow);
            JsonNode jsonNode = jsonObjectMapper.readTree(jsonString);
            YAMLFactory yamlFactory = new YAMLFactory()
                    .disable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
            return new YAMLMapper(yamlFactory).writeValueAsString(jsonNode);
        } catch (Exception e) {
            logger.error("Error mapping to yaml: " + e.getMessage());
            return null;
        }
    }
}
