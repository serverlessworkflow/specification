# Serverless Workflow Project Governance

As a CNCF member project, we abide by the [CNCF Code of Conduct](https://github.com/cncf/foundation/blob/master/code-of-conduct.md).

For specific guidance on practical contribution steps for any Serverless Workflow sub-project please
see our [contributing guide](contributing.md).

You can contact the project maintainers at any time by sending an email to the 
[Serverless Workflow Specification Maintainers](mailto:cncf-serverlessws-maintainers@lists.cncf.io)
 mailing list.

## Maintainership

Main responsibilities of maintainers include:

1) Sharing responsibility for the project's success.
2) Making a long-term, recurring time investment to improve the project.
3) Performing necessary tasks, even if they are not the most interesting or fun.

## Reviewers

A reviewer is a core role within the project. They share in reviewing issues and pull requests.
Their pull request approvals are needed to merge significant code changes into the project.

## Emeritus Maintainers

Emeritus maintainers are retired maintainers who have chosen to remain involved in the project as advisors.
Their main responsibilities include:

1) Providing guidance and mentorship to current maintainers and contributors.
2) Offering historical context and insights based on their past experiences.
3) Participating in discussions and reviews on an advisory basis, without the obligations of active maintainers.

## Adding maintainers

Maintainers are first and foremost contributors who have demonstrated their commitment to the long-term success of the project. Contributors wishing to become maintainers are expected to be deeply involved in contributing code, reviewing pull requests, and triaging issues for more than three months.

Just contributing does not make you a maintainer; it is about building trust with the current maintainers and being someone they can depend on to make decisions in the project's best interest.

Periodically, the existing maintainers curate a list of contributors who have shown regular activity over the prior months. From this list, maintainer candidates are selected and proposed on the project mailing list. Only one maintainer per organization is allowed to avoid conflicts of interest.

After a candidate is announced on the project mailing list, the existing maintainers have fourteen business days to discuss, raise objections, and vote on the candidate. Votes can be cast on the mailing list or via pull request comments. Candidates must be approved by at least 66% of the current maintainers. The reviewer role has the same process but only requires 33% approval from current maintainers. Only maintainers of the repository the candidate is proposed for are allowed to vote.

If approved, a maintainer will contact the candidate to invite them to open a pull request adding themselves to the MAINTAINERS file. The voting process can also take place inside a pull request if a maintainer has already discussed the candidacy with the candidate and is willing to sponsor them by opening the pull request. The candidate becomes a maintainer once the pull request is merged.

## Adding Emeritus Maintainers

To transition a maintainer to an emeritus role, the current maintainers can nominate a retiring maintainer who has expressed interest in staying involved as an advisor. The nomination process follows the same voting and approval procedures as adding new maintainers, requiring a 66% approval vote from the current maintainers. Once approved, the emeritus maintainer is added to the EMERITUS file and announced to the community.

## Subprojects

Serverless Workflow subprojects all culminate in officially supported and
maintained releases of the specification.
All subprojects must adhere to
[CNCF Code of Conduct](https://github.com/cncf/foundation/blob/master/code-of-conduct.md)
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
fourteen business days. Issues related to a maintainers performance should be
discussed with them among the other maintainers so that they are not surprised
by a pull request removing them.

## How are decisions made?

Serverless Workflow is an open-source project with an open design philosophy. This means
that the repository is the source of truth for EVERY aspect of the project,
including its philosophy, design, road map, and APIs. *If it's part of the
project, it's in the repository. If it's in the repository, it's part of the project.*

As a result, all decisions can be expressed as changes to the repository. An
implementation change is a change to the source code. An API change is a change
to the API specification, and so on.

Decisions are built on consensus between maintainers. Proposals and ideas can be submitted
for agreement via a GitHub [issue](issues) or [discussion](discussions).
Upon agreement, a pull request should be opened. We encourage not opening pull
requests without a discussion first either in a new [issue](issues) or using
the [discussion](discussions) tool.

All decisions affecting Serverless Workflow, big and small, follow the same 3 steps:

* Step 1: Open a pull request. Anyone can do this.

* Step 2: Discuss the pull request. Anyone can do this.

* Step 3: Merge or refuse the pull request. Who does this depends on the nature
of the pull request and which areas of the project it affects.

## I'm a maintainer. Should I make pull requests too?

Yes. Nobody should ever push to master directly. All changes should be
made through a pull request. Please see the [Contributing](contributing.md)
document for more information about opening pull requests.

## Conflict Resolution

To merge changes into the specification, approval from at least one maintainer, other than the pull request's author, is required.
Maintainers who do not explicitly voice their opinions on the pull request within the two-day approval period are assumed to agree through [lazy consensus](http://communitymgt.wikia.com/wiki/Lazy_consensus).

Discussions and voting can be posponed in case one of the maintainers expressed that
they won't be available for personal reasons, e.g. parental leave, vacations, sick leave, etc.

We generally prefer that technical issues and maintainer membership are amicably
worked out between the persons involved. If a dispute cannot be resolved independently,
get a third-party maintainer (e.g., a mutual contact with some background on the issue
but not involved in the conflict) to intercede. If a dispute cannot be resolved,
the core maintainers have the final say in deciding an issue. The core maintainers
may reach this decision by consensus or by a simple majority vote if necessary.
The maintainers should endeavor to make this decision within a reasonable amount
of time, not to extend it longer than two weeks.
