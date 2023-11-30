import fs from 'fs'
import Ajv from "ajv"
import addFormats from "ajv-formats"
import { join } from 'path'


export module SWSchemaValidator {
    const ajv = new Ajv({ strict: false, allowUnionTypes: true })
    addFormats(ajv)


    const workflowSchemaId = 'https://serverlessworkflow.io/schemas/0.8/workflow.json'
    const schemaPath = '../../../schema'
    export const defaultEncoding = 'utf-8'

    export function prepareSchemas() {
        fs.readdirSync(join(__dirname, schemaPath), { encoding: defaultEncoding, recursive: false, withFileTypes: true })
            .forEach(file => {
                if (file.isFile()) {
                    ajv.addSchema(syncReadSchema(file.name))
                }
            })
    }

    function syncReadSchema(filename: string) {
        return JSON.parse(fs.readFileSync(join(__dirname, `${schemaPath}/${filename}`), defaultEncoding));
    }

    export function validateSchema(workflow: JSON) {
        const validate = ajv.getSchema(workflowSchemaId)
        if (validate != undefined) {
            const isValid = validate(workflow)
            return {
                valid: isValid,
                errors: validate.errors
            }
        }
        // throw error
    }
}

console.log("To use this application see the test file index.test.ts")
