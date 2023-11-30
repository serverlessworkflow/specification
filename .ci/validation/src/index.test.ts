import { SWSchemaValidator } from './index'
import fs from 'fs'
import { join } from 'path'

SWSchemaValidator.prepareSchemas()

const examplePath = "../../../examples"

describe(`Verify every example in the repository`, () => {
    fs.readdirSync(join(__dirname, examplePath), { encoding: SWSchemaValidator.defaultEncoding, recursive: false, withFileTypes: true })
        .forEach(file => {
            if (file.isFile() && file.name.endsWith(".json")) {
                test(`Example ${file.name}`, () => {
                    const workflow = JSON.parse(fs.readFileSync(join(__dirname, `${examplePath}/${file.name}`), SWSchemaValidator.defaultEncoding))
                    const results = SWSchemaValidator.validateSchema(workflow)
                    if (results?.errors != null) {
                        console.warn(`Schema validation on ${file.name} failed with: `, JSON.stringify(results.errors, null, 2))
                    }
                    expect(results?.valid).toBeTruthy()
                });
            }
        })
})
