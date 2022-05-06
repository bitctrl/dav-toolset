module.exports = {
    branchPrefix: 'renovate/',
    dryRun: false,
    username: 'renovate-release',
    gitAuthor: 'Renovate Bot <bot@renovateapp.com>',
    onboarding: true,
    platform: 'github',
    includeForks: true,
    repositories: [
      'bitctrl/dav-toolset',
    ],
    packageRules: [
      {
        description: 'lockFileMaintenance',
        matchUpdateTypes: [
          'pin',
          'digest',
          'patch',
          'minor',
          'major',
          'lockFileMaintenance',
        ],
        dependencyDashboardApproval: false,
        stabilityDays: 0,
      },
    ],
  };