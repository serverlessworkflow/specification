# Contributing to Serverless Workflow

This page contains information about reporting issues, how to suggest changes as
well as the guidelines we follow for how our documents are formatted.

## Table of Contents

- [Reporting an Issue](#reporting-an-issue)
- [Suggesting a Change](#suggesting-a-change)
- [Spec Formatting Conventions](#spec-formatting-conventions)

## Reporting an Issue

If you have a question consider opening a
[discussion](https://github.com/serverlessworkflow/specification/discussions).

To report an issue, or to suggest an idea for a change that you haven't had time
to write-up yet, open an
[issue](https://github.com/serverlessworkflow/specification/issues). It is best
to check our existing
[issues](https://github.com/serverlessworkflow/specification/issues) first to
see if a similar one has already been opened and discussed.

## Suggesting a Change

Enhancements or bugs in a specification are not always easy to describe at first
glance, requiring some discussions with other contributors before reaching a
conclusion.

Before opening a pull request, we kindly ask you to consider opening a
[discussion](https://github.com/serverlessworkflow/specification/discussions)
or an [issue](https://github.com/serverlessworkflow/specification/issues). The
community will be more than happy to discuss your proposals there.

Having the discussion or issue settled, please submit a
[pull request](https://github.com/serverlessworkflow/specification/pulls) (PR)
with the complete set of changes discussed with the community. See the
[Spec Formatting Conventions](#spec-formatting-conventions) section for the
guidelines we follow for how documents are formatted.

Each PR must be signed per the following section and have a link to the issue or
discussion.

### Fixing Typos, Spelling and Formatting issues

Due to the amount of text a specification can have, typos, spelling and
formatting issues are quite common. In these cases, please submit a
[pull request](https://github.com/serverlessworkflow/specification/pulls)
directly only with the fixes that you see fit.

### Assigning and Owning work

If you want to own and work on an issue, add a comment or “#dibs” it asking
about ownership. A maintainer will then add the Assigned label and modify the
first comment in the issue to include `Assigned to: @person`

## Spec Formatting Conventions

Documents in this repository will adhere to the following rules:

- Lines are wrapped at 80 columns (when possible)
- Specifications will use [RFC2119](https://tools.ietf.org/html/rfc2119)
  keywords to indicate normative requirements

## Checks

### Markdown style

Markdown files should be properly formatted before a pull request is sent out.
In this repository we follow the
[markdownlint rules](https://github.com/DavidAnson/markdownlint#rules--aliases)
with some customizations. See [markdownlint](.markdownlint.yaml) or
[settings](.vscode/settings.json) for details.

We highly encourage to use line breaks in markdown files at `80` characters
wide. There are tools that can do it for you effectively. Please submit proposal
to include your editor settings required to enable this behavior so the out of
the box settings for this repository will be consistent.

If you are using Visual Studio Code,
you can also use the `fixAll` command of the
[vscode markdownlint extension](https://github.com/DavidAnson/vscode-markdownlint).

To otherwise check for style violations, use

```bash
# Ruby and gem are required for mdl
gem install mdl
mdl -c .mdlrc .
```

To fix style violations, follow the
[instruction](https://github.com/DavidAnson/markdownlint#optionsresultversion)
with the Node version of markdownlint.

### Typos

In addition, please make sure to clean up typos before you submit the change.

To check for typos, you may use

```bash
# Golang is needed for the misspell tool.
make install-misspell
make misspell
```

To quickly fix typos, use

```bash
make misspell-correction
```
