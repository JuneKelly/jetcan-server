angular.module('jetcanApp')
  .service 'Auth', ($http, Notifications, Util, $state, Storage) ->

    reset = () ->
      Storage.setProfile({})
      Storage.setToken('')
      Notifications.resetAll()
      Util.kickToRoot()

    register = (credentials) ->
      $http(
        method: 'POST'
        url: '/api/user'
        data: credentials
        headers:
          'Accept': 'application/json'
          'auth_token': Storage.getToken()
      )
        .success (payload, status, headers, config) ->
          Notifications.success('Created user ' + credentials.id)

        .error (payload, status, headers, config) ->
          console.log 'ERROR'
          console.log status
          Notifications.error(
            'Error, User registration failed'
          )

    login = (id, password) ->
      reset()
      $http(
        method: 'POST'
        url: '/api/auth'
        data: {id: id, password: password}
        headers: { 'Accept': 'application/json' }
      )
        .success (payload, status, headers, config) ->
          if payload.token == null
            Notifications.error(
              'Error, authentication failed'
            )
          else
            if status == 201
              Storage.setProfile(payload.profile)
              Storage.setToken(payload.token)
              Notifications.success(
                'Logged in as ' + payload.profile.id)
            else
              Notifications.error(
                'Error, authentication failed'
              )

        .error (payload, status, headers, config) ->
          console.log 'ERROR'
          console.log status
          console.log payload
          Notifications.error(
            'Error, authentication failed'
          )

    logout = () ->
      reset()
      Notifications.success('Logging out...')

    mustBeLoggedIn = () ->
      if !loggedIn()
        Notifications.error('You must be logged in to do that')
        Util.kickToRoot()

    mustBeAdmin = () ->
      mustBeLoggedIn()
      if !isAdmin()
        Notifications.error('You must be an admin user to do that')
        Util.kickToRoot()

    isAdmin = () ->
      Storage.getProfile().admin == true

    loggedIn = () ->
      token = Storage.getToken()
      if token == '' or token == null or token == "null" or token == undefined
        false
      else
        true

    currentUser = () ->
      Storage.getUserId()

    return {
      currentUser: currentUser
      login: login
      logout: logout
      register: register
      loggedIn: loggedIn
      mustBeLoggedIn: mustBeLoggedIn
      mustBeAdmin: mustBeAdmin
      isAdmin: isAdmin
    }

