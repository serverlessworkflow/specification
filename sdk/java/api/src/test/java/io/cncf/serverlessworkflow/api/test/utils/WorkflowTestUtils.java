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
