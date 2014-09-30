angular.module('jetcanApp')
  .controller 'AdminCtrl',
    ($scope, Auth, Notifications, Snippet, $modal) ->
      $scope.Auth = Auth
      $scope.Notifications = Notifications

      Auth.mustBeLoggedIn()

      # State

      # Functions
      init = () ->
        console.log "admin"

      # load page
      init()
