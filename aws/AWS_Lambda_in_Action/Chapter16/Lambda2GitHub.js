var GitHubApi = require('github');
var github = new GitHubApi({
    version: '3.0.0'
});

exports.handler = (event, context, callback) => {

    if (!('user' in event) || !('repo' in event) ||
        !('issue' in event) || !('comment' in evet)) {
        callback('Error: the event must contain: user, repo, issue and comment')
    } else {

        var githubUser = event.user;
        var githubRepo = event.repo;
        var githubIssue = event.issue;
        var comment = event.comment;

        github.authenticate({
            type: 'oauth',
            token: '<GITHUB_TOKEN>'
        });

        github.issues.createComment({
            user: githubUser,
            repo: githubRepo,
            number: githubIssue,
            body: comment
        }, callback(null, 'Comment posted'));

    }
};
