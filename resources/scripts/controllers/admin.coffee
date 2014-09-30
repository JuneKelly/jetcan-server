angular.module('jetcanApp')
  .controller 'AdminCtrl',
    ($scope, Auth, Notifications, Snippet, $modal) ->
      $scope.Auth = Auth
      $scope.Notifications = Notifications

      Auth.mustBeLoggedIn()
      Auth.mustBeAdmin()

      # State

      # Functions
      init = () ->
        console.log "Admin Panel"

      # load page
      init()
