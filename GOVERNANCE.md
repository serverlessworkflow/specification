# Serverless Workflow Project Governance

As a CNCF member project, we abide by the [CNCF Code of Conduct](https://github.com/cncf/foundation/blob/master/code-of-conduct.md).

For specific guidance on practical contribution steps for any Serverless Workflow sub-project please
see our [contributing guide](CONTRIBUTING.md).

You can contact the project maintainers at any time by sending an email to the 
[Serverless Workflow Specification Maintainers](mailto:cncf-serverlessws-maintainers@lists.cncf.io)
 mailing list.

## Maintainership

Main responsibilities of maintainers include:

1) They share responsibility in the project's success.
2) They have made a long-term, recurring time investment to improve the project.
3) They spend that time doing whatever needs to be done, not necessarily what
is the most interesting or fun.

## Reviewers

A reviewer is a core role within the project.
They share in reviewing issues and pull requests. Their pull request approvals 
are needed to merge a large code change into the project.

## Adding maintainers

Maintainers are first and foremost contributors that have shown they are
committed to the long term success of a project. Contributors wanting to become
maintainers are expected to be deeply involved in contributing code, pull
request review, and triage of issues in the project for more than three months.

Just contributing does not make you a maintainer, it is about building trust
with the current maintainers of the project and being a person that they can
depend on and trust to make decisions in the best interest of the project.

Periodically, the existing maintainers curate a list of contributors that have
shown regular activity on the project over the prior months. From this list,
maintainer candidates are selected and proposed on the project mailing list.

After a candidate has been announced on the project mailing list, the
existing maintainers are given fourteen business days to discuss the candidate,
raise objections and cast their vote. Votes may take place on the mailing list
or via pull request comment. Candidates must be approved by at least 66% of the
current maintainers by adding their vote on the mailing list. The reviewer role
has the same process but only requires 33% of current maintainers. Only
maintainers of the repository that the candidate is proposed for are allowed to
vote.

If a candidate is approved, a maintainer will contact the candidate to invite
the candidate to open a pull request that adds the contributor to the
MAINTAINERS file. The voting process may take place inside a pull request if a
maintainer has already discussed the candidacy with the candidate and a
maintainer is willing to be a sponsor by opening the pull request. The candidate
becomes a maintainer once the pull request is merged.

## Subprojects

Serverless Workflow subprojects all culminate in officially supported and maintained releases
of the specification. 
All subprojects must adhere to [CNCF Code of Conduct](https://github.com/cncf/foundation/blob/master/code-of-conduct.md)
as well as this governance document.

### Adding core subprojects

New subprojects can request to be added to the Serverless Workflow GitHub
organization by submitting a GitHub issue in the specification repository.

The existing maintainers are given fourteen business days to discuss the new
project, raise objections and cast their vote. Projects must be approved by at
least 66% of the current maintainers.

If a project is approved, a maintainer will add the project to the Serverless Workflow
GitHub organization, and make an announcement on a public Slack channel.

## Stepping down policy

Life priorities, interests, and passions can change. If you're a maintainer but
feel you must remove yourself from the list, inform other maintainers that you
intend to step down, and if possible, help find someone to pick up your work.
At the very least, ensure your work can be continued where you left off.

After you've informed other maintainers, create a pull request to remove
yourself from the MAINTAINERS file.

## Removal of inactive maintainers

Similar to the procedure for adding new maintainers, existing maintainers can
be removed from the list if they do not show significant activity on the
project. Periodically, the maintainers review the list of maintainers and their
activity over the last three months.

If a maintainer has shown insufficient activity over this period, a neutral
person will contact the maintainer to ask if they want to continue being
a maintainer. If the maintainer decides to step down as a maintainer, they
open a pull request to be removed from the MAINTAINERS file.

If the maintainer wants to remain a maintainer, but is unable to perform the
required duties they can be removed with a vote of at least 66% of
the current maintainers. An e-mail is sent to the
mailing list, inviting maintainers of the project to vote. The voting period is
fourteen business days. Issues related to a maintainer's performance should be
discussed with them among the other maintainers so that they are not surprised
by a pull request removing them.

## How are decisions made?

Serverless Workflow is an open-source project with an open design philosophy. This means
that the repository is the source of truth for EVERY aspect of the project,
including its philosophy, design, road map, and APIs. *If it's part of the
project, it's in the repo. If it's in the repo, it's part of the project.*

As a result, all decisions can be expressed as changes to the repository. An
implementation change is a change to the source code. An API change is a change
to the API specification, and so on.

All decisions affecting Serverless Workflow, big and small, follow the same 3 steps:

* Step 1: Open a pull request. Anyone can do this.

* Step 2: Discuss the pull request. Anyone can do this.

* Step 3: Merge or refuse the pull request. Who does this depends on the nature
of the pull request and which areas of the project it affects.

## I'm a maintainer. Should I make pull requests too?

Yes. Nobody should ever push to master directly. All changes should be
made through a pull request.

## Conflict Resolution

If you have a technical dispute that you feel has reached an impasse with a
subset of the community, any contributor may open an issue, specifically
calling for a resolution vote of the current core maintainers to resolve the dispute.
The same voting quorums required (2/3) for adding and removing maintainers
will apply to conflict resolution.