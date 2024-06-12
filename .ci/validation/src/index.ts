/*
 * Copyright 2023-Present The Serverless Workflow Specification Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import fs from "node:fs";
import Ajv from "ajv";
import addFormats from "ajv-formats";
import { join } from "node:path";
import yaml = require("js-yaml");

export module SWSchemaValidator {
  const ajv = new Ajv({ strict: false, allowUnionTypes: true });
  addFormats(ajv);

  const workflowSchemaId =
    "https://serverlessworkflow.io/schemas/1.0.0-alpha1/workflow.json";
  const schemaPath = "../../../schema";
  export const defaultEncoding = "utf-8";

  export function prepareSchemas() {
    fs.readdirSync(join(__dirname, schemaPath), {
      encoding: defaultEncoding,
      recursive: false,
      withFileTypes: true,
    }).forEach((file) => {
      if (file.isFile()) {
        ajv.addSchema(syncReadSchema(file.name));
      }
    });
  }

  function syncReadSchema(filename: string) {
    return toJSON(join(__dirname, `${schemaPath}/${filename}`));
  }

  export function toJSON(filename: string) {
    const yamlObj = yaml.load(fs.readFileSync(filename, defaultEncoding), {
      json: true,
    });
    return JSON.parse(JSON.stringify(yamlObj, null, 2));
  }

  export function validateSchema(workflow: JSON) {
    const validate = ajv.getSchema(workflowSchemaId);
    if (validate != undefined) {
      const isValid = validate(workflow);
      return {
        valid: isValid,
        errors: validate.errors,
      };
    }
    throw new Error(`Failed to validate schema on workflow`);
  }
}

console.log("To use this application see the test file index.test.ts");
