angular.module('jetcanApp')
  .controller 'NavbarCtrl', ($scope, Auth) ->
    $scope.Auth = Auth
    $scope.aboutText = 'About'

    $scope.login = (user) ->
      Auth.login(user.id, user.password)
      user.username = ''
      user.password = ''

    $scope.logout = () ->
      Auth.logout()
