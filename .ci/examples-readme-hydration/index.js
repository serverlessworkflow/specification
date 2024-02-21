const fs = require('fs');
const path = require('path');
const YAML = require('yaml');

const examplesPath = path.resolve(__dirname, '../../examples/');
const readMeTemplatePath = path.resolve(examplesPath, 'README_TEMPLATE.md');
const readMeOutputPath = path.resolve(examplesPath, 'README.md');
const extractor = /<include file="(.*)" format="(.*)" \/>/g;
const disclaimer = `<!--


   !!! THIS IS A GENERATED FILE !!!
   Please do not update this file but the TEMPLATE instead!

   
-->
`;

(async () => {
  let readMe = await fs.promises.readFile(readMeTemplatePath, 'utf8');
  const includes = readMe.matchAll(extractor);
  for await(let include of includes) {
    const fileName = include[1];
    const format = include[2];
    let fileContent = await fs.promises.readFile(path.resolve(examplesPath, fileName), 'utf8');
    if (format === 'yaml') {
      try {
        const schema = JSON.parse(fileContent);
        fileContent = YAML.stringify(schema);
      }
      catch(ex) {
        console.error('Enable to parse JSON or convert it to YAML, output as it is.', ex);
      }
    }
    fileContent = fileContent.trim() + '\n';
    readMe = readMe.replace(include[0], fileContent);
  };
  await fs.promises.writeFile(readMeOutputPath, disclaimer + readMe, { encoding: 'utf8', flag: 'w' });
})();