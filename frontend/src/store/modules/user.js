import Vue from 'vue'

const state = {
  lock: null,
  authenticated: false,
  auth0state: '',
  profile: null,
  notifications: [],
  intervalId: null,
  intervalId2: null
}

const getters = {
}

const mutations = {
  setLock: (state, payload) => {
    state.lock = payload
  },
  authenticate: (state, payload) => {
    state.authenticated = payload
  },
  setAuth0state: (state, payload) => {
    state.auth0state = payload
  },
  setProfile: (state, payload) => {
    state.profile = payload
  },
  setNotifications: (state, payload) => {
    state.notifications = payload
  }
}

const actions = {

  updateProfile: (context) => {
    /* user profile */
    if (context.state.authenticated) {
      /* set auto-update ever 5 seconds */
      if (context.state.intervalId == null) {
        context.state.intervalId = setInterval(() => {
          /* update everything every 10 s */
          if (context.state.authenticated) {
            context.dispatch('updateNotifications')
            context.dispatch('updateMyInitiatives')
            context.dispatch('refreshInitiative')
            context.dispatch('refreshTransfers')
            context.commit('triggerUpdateAssets')
            context.commit('triggerUpdateModel')
          }
        }, 10000)
      }

      Vue.axios.get('/1/user/myProfile').then((response) => {
        if (response.data.result === 'success') {
          context.commit('setProfile', response.data.data)
          context.dispatch('updateNotifications')
        } else {
          if (response.data.message === 'anonymous user') {
            context.commit('authenticate', false)
            context.commit('setProfile', null)
          }
        }
      })
    } else {
      /* autoupdate for non logged user */
      if (context.state.intervalId2 == null) {
        context.state.intervalId2 = setInterval(() => {
          context.dispatch('refreshInitiative')
          context.dispatch('refreshTransfers')
          context.commit('triggerUpdateAssets')
        }, 10000)
      }
    }
  },

  updateNotifications: (context) => {
    /* get notifications */
    if (context.state.authenticated) {
      if (context.state.profile) {
        Vue.axios.get('/1/user/notifications').then((response) => {
          context.commit('setNotifications', response.data.data)
        }).catch(function (error) {
          console.log(error)
        })
      }
    }
  },

  notificationsRead: (context) => {
    /* notifications read */
    if (context.state.profile) {
      Vue.axios.put('/1/user/notifications/read', {}).then((response) => {
      }).catch(function (error) {
        console.log(error)
      })
    }
  },

  logoutUser: (context) => {
    localStorage.removeItem('access_token')
    localStorage.removeItem('id_token')
    context.commit('authenticate', false)
    context.commit('setProfile', null)
  }
}

export default {
  state,
  getters,
  mutations,
  actions
}
