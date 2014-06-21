angular.module('jetcanApp')
  .controller 'ProfileCtrl',
    ($scope, Auth, Notifications, User, $stateParams) ->
      $scope.Auth = Auth
      $scope.Notifications = Notifications

      Auth.mustBeLoggedIn()

      $scope.editMode = false
      $scope.editToggle = () ->
        $scope.editMode = !$scope.editMode

      $scope.userId = $stateParams.id
      $scope.profile = null

      $scope.isCurrentUser = () ->
        $scope.userId == Auth.currentUser()

      $scope.loadProfile = () ->
        User.get($scope.userId)
          .then (profileData) ->
            $scope.profile = profileData

      $scope.updateProfile = () ->
        User.update($scope.userId, $scope.profile)
        $scope.editMode = false

      if Auth.loggedIn()
        $scope.loadProfile()

