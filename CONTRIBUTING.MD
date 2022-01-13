# Contributing Covenant

Thank you for taking the time to contribute!  
This repository is the main repository out of a set of metis related repositories.

## Code style

The code style used for the formatting of the code can be found under `.idea/codeStyles/Project.xml`.   
This code style should also be used for all other metis repositories and can be found only here.

### Setting the code style global for the IDE

The location and name of the code style file are the default for the project for intellij. Setting this as a global code
style is recommended so that it can be used on the other projects as well.

To set the code style to global for the IDE:

1. Go to `Settings->Editor->Code Style->Java`
2. On scheme property choose `Project`(This refers to the file just added).
3. Then from the gear, select `Copy to IDE...`
4. This will give it a default name `metis-framework`, at this point press enter and then Ok to close the window

The `metis-framework` scheme will be now enabled for all other projects as well.

_Note: the `.idea/codeStyles/Project.xml` file is to be modifiable only from an administrator of the project. If you
have any formatting requests, please suggest them on the other members of the repository._

## General Process

Contributing to this repository is to be performed with a branch and pull request. Usually the branch goes in line with
a ticket through our ticketing system and our scrum sprints. Nonetheless, changes not corresponding to a ticket may also
be applied with a branch and pull request.  
Small changes can be applied from the maintainers without notice directly to develop branch if they seem safe.

## Commits and Branches

Commits and branches, when connected to a ticket, have to follow a specific format. That format is mostly used to give
clarity and to assist with searching in the git logs and curating release logs.

### Branches

When starting to work on a particular issue/ticket, create a new local branch from develop:

Create only:  
`git branch <branch_name>`

Or create and checkout:  
`git checkout -b <branch_name>`

The format of the `<branch_name>` should include:

- The type of branch which can be one of `feat`, `bug`, `debt`, `hotfix`, `release`.
- A slash separator.
- The ticket number `MET-XXXX`.
- An underscore after the ticket number.
- A clear description of what is being implemented with underscores in between words.

Template:  
`<branch_type>/<ticket_number>_<clear_description_of_the_issue_being_implemented>`  
Examples of branch names:  
`bug/MET-3922_Fix_view_in_collections_when_only_deleted_records_exist`  
`debt/MET-4004_Remove_commons_lang_dependency`  
`feat/MET-4008_caching_default_xslt`

### Commits

Commit messages at the relative branch, just created, also follow a format:

- The commit message starts with the ticket number `MET-XXXX`. If you are working on a story(the branch has that story's
  ticket number) and in several sub-tasks the sub-tasks ticket numbering can be concatenated with an underscore, just
  after the story number. E.g. `MET-XXXX_MET-YYYY`.  
  That is not a necessity though and the story number can simply be used.
- A clear message of what is being implemented. The message should be short enough so that, ideally it doesn't extend to
  the description part of the commit.

Template:  
`<ticket_number> <clear_description_of_the_issue_being_implemented>`  
Examples of commit messages(_Note: do not add any other punctuation in the message_):  
`MET-3922 Fix view in collections when only deleted records exist`  
`MET-4004 Removed any references to commons-lang library`  
`MET-4065 Changed constructor to use getTemplates`

The commits can have a description as noted above, which comes just below the message section. That description can be
long and very detailed if need be.

Sometimes a situation can arise, where a branch was merged, and it has been deployed but not working. A fast commit(with
a ticket number if applicable) on develop can be applied if the developer feels confident it would fix the problem.
Otherwise, a new ticket should be created and handled accordingly.

### Pull Requests

Before creating a Pull Request make sure:

- The build locally is successful
- There are unit tests, and they cover all of the new code.
- Javadoc is complete
- Code formatting is applied
- Sanity checks on a local deployment are successful

Once everything is ready, submit a new pull request on github.  
Wait and verify that all checks on the bottom of the Pull Request are succeeding.  
If there are conflicts or failed checks identified on the pull request, fix them and commit the changes.   
When everything is okay, move the corresponding ticket to the review column in our board.

### Code Reviews

When a ticket is ready for code review, the implementors should move the ticket to the review column and un-assign it
from themselves.  
Reviewers can then choose the ticket and assign it to themselves. When reviewing code, the reviewer should be objective
and has to try to verify that all code conforms to best practises and common practises applicable to the team.  
The reviewer should acknowledge there are different implementation styles from each developer but nonetheless be strict
when readability, security, performance, tests, documentation or other relative issues are compromised.

The review should be drafted from github with the `Start a review` button on the first comment.  
When writing comments on github, those comments should be descriptive and clearly indicating what the reviewer is meant
to imply. Once the reviewer is ready, the review can be submitted.  
If there are comments for potential changes the reviewer should move the corresponding ticket back to `In progress` and
re-assign the ticket to the implementor.  
Resolving a comment should only be allowed from the commenter(usually the reviewer).

Finally, when the reviewer deems everything is fine on the pull request, the branch can be merged. This can be performed
on the github pull request page or manually. To do this manually, checkout the destination branch(usually `develop`) and
execute the merge command with the `--no-ff` parameter:

`git merge --no-ff <branch_to_be_merge>`

The reviewer should also check that the local branch is building before and after merging.  
If the build succeeds the destination branch can be pushed to the remote repository and the pull request will be
resolved.

The reviewer can now move the ticket ahead in the board and re-assign it to the implementor.

### Code conventions

Below follows a non-exhaustive list of conventions used in our codebase:

1. Module directories follow the structure:
    1. module-common -> Common code for the module
    2. module-service -> Contains the core business logic of the application and persistence code
    3. module-rest -> Rest web application
    4. module-client -> A Java REST client that implements the REST API of the module  
       Not all modules contain all the sub-directories since some of those might not be applicable.
2. Similarly to modules there is a general convention of classes hierarchy that we follow.
    1. A Controller that is the front facing component.
    2. A Service that is used from the Controller.
    3. A Dao that is used from the Service.
3. A class is not to be used for both a database and a REST API representation.  
   Model classes do not contain a suffix `Model` and view classed contain a suffix `View`.  
   A Service class can be the mediator that converts the model to the view for the Controller.
4. Method names and fields should be descriptive and not abbreviated. Examples:
    - A parameter that represents a `datasetId` should be named as such and not abbreviated to `di`
    - A local variable that represents an `inputStream` should be named as such and not abbreviated to `is`
    - A method name example `checkMetisUserOrganizationRole`. It's preferable to be long than not descriptive.

   Those conventions are meant for improving readability and avoiding confusing.  
   There are exception cases, for example when using streams an abbreviation of a parameter might be used to improve
   readability.
5. Code should be kept minimally complex but not overly minimalistic.
6. When creating Strings that contain parameters, use formatting, for example:  
   `String.format("Value '%s' cannot be null.", fieldName)`  
   `LOGGER.info("Starting postprocessing of plugin {} in dataset {}.", pluginType, datasetId);`  
   Avoid using addition of Strings
   e.g. `"Starting postprocessing of plugin" + pluginType + "in dataset" + datasetId + "."`.
7. Use `final` keyword whenever applicable, except for method parameters. Method parameters should be `final` when the
   method is a more than normal complex method, where it's not very clear to the reader if the parameter will remain
   unchanged. If it is a complex method though, we should first think "can we make this code simpler?". For simpler
   methods refrain from using it, to avoid code pollution and increase readability.
8. Code that might seem un-used in the working repository, could be currently used from another repository. If the code
   is to be removed or modified it should first be researched for external use. If in doubt, make the new code backwards
   compatible.
9. Packages, similarly to class and method names should be descriptive and ideally unique. For example for two different
   service submodules:  
   `eu.europeana.metis.transformation service`  
   `eu.europeana.metis.enrichment.service`  
   We can clearly tell where each package belongs to and what it contains.
10. Most exceptions should be custom and descriptive. When throwing multiple exceptions, try to bundle them rather than
    throwing multiple exceptions. For example following a hierarchy.

### Versioning

Snapshot versions follow the format `<version_number>-SNAPSHOT`, for example `6-SNAPSHOT`.  
Release versions follow the format `<version_number>`, for example `6` without sub-version numbers.  
Hotfix versions follow the format `<version_number>.<hotfix_version>`, for example `6.1`.

### Scope

The contributing guidelines and license are defined in this project and apply to all other metis related projects.