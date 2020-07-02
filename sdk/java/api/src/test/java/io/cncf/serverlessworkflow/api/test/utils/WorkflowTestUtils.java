/*
 * Copyright 2020-Present The Serverless Workflow Specification Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.cncf.serverlessworkflow.api.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.cncf.serverlessworkflow.api.Workflow;
import io.cncf.serverlessworkflow.api.mapper.JsonObjectMapper;
import io.cncf.serverlessworkflow.api.mapper.YamlObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorkflowTestUtils {
    private static JsonObjectMapper jsonObjectMapper = new JsonObjectMapper();
    private static YamlObjectMapper yamlObjectMapper = new YamlObjectMapper();

    private static Logger logger = LoggerFactory.getLogger(WorkflowTestUtils.class);

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

    public static Workflow toWorkflow(String markup) {
        // try it as json markup first, if fails try yaml
        try {
            return jsonObjectMapper.readValue(markup,
                    Workflow.class);
        } catch (Exception e) {
            try {
                return yamlObjectMapper.readValue(markup,
                        Workflow.class);
            } catch (Exception ee) {
                throw new IllegalArgumentException("Could not convert markup to Workflow: " + ee.getMessage());
            }
        }
    }

    public static final Path resourceDirectory = Paths.get("src",
            "test",
            "resources");
    public static final String absolutePath = resourceDirectory.toFile().getAbsolutePath();

    public static Path getResourcePath(String file) {
        return Paths.get(absolutePath + File.separator + file);
    }

    public static InputStream getInputStreamFromPath(Path path) throws Exception {
        return Files.newInputStream(path);
    }

    public static String readWorkflowFile(String location) {
        return readFileAsString(classpathResourceReader(location));
    }

    public static Reader classpathResourceReader(String location) {
        return new InputStreamReader(WorkflowTestUtils.class.getResourceAsStream(location));
    }

    public static String readFileAsString(Reader reader) {
        try {
            StringBuilder fileData = new StringBuilder(1000);
            char[] buf = new char[1024];
            int numRead;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf,
                        0,
                        numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
            return fileData.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
