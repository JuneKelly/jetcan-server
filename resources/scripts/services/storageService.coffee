angular.module('jetcanApp')
  .service 'Storage', () ->

    store = localStorage

    getProfile = ->
      profile = store['jetcan_user_profile']
      if profile == ''
        return null
      else
        return angular.fromJson(profile)

    getUserId = () ->
      user = getProfile()
      if user
        user.id
      else
        ''

    setProfile = (profile) ->
      store['jetcan_user_profile'] = angular.toJson(profile)

    getToken = () ->
      store['jetcan_token']

    setToken = (token) ->
      store['jetcan_token'] = token

    return {
      getProfile: getProfile
      setProfile: setProfile
      getUserId: getUserId
      getToken: getToken
      setToken: setToken
    }
