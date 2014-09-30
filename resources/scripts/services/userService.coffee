angular.module('jetcanApp')
  .service 'User', ($http, Notifications, Auth, Storage, $q) ->

    get = (id) ->
      deferred = $q.defer()

      $http(
        method: 'GET'
        url: 'api/user/' + id
        headers: {'auth_token': Storage.getToken() }
      )
        .success (payload, status, headers, config) ->
          deferred.resolve(payload)

        .error (payload, status, headers, config) ->
          if status == 401
            Notifications.error('You are not authorized to do that')

      return deferred.promise

    getAll = () ->
      deferred = $q.defer()

      $http(
        method: 'GET'
        url: 'api/user'
        headers: {'auth_token': Storage.getToken() }
      )
        .success (payload, status, headers, config) ->
          deferred.resolve(payload)

        .error (payload, status, headers, config) ->
          if status == 401
            Notifications.error('You are not authorized to do that')

      return deferred.promise

    update = (id, newData) ->
      data =
        id: id
        name: newData.name

      $http(
        method: 'POST'
        url: 'api/user/' + id
        headers: {'auth_token': Storage.getToken() }
        data: data
      )
        .success (payload, status, headers, config) ->
          Notifications.success('Updated Profile of ' + id)

        .error (payload, status, headers, config) ->
          Notifications.error(status + ', something went wrong')

    return {
      get: get
      update: update
      getAll: getAll
    }
