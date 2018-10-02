// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import beforeEach from './router/beforeEach'
import { store } from './store/store'

import VueI18n from 'vue-i18n'
import { translations } from '@/lang'

import axios from 'axios'
import VueAxios from 'vue-axios'
import VueAnalytics from 'vue-analytics'

import ErrorPanel from '@/components/global/ErrorPanel.vue'
import VueMarkdown from 'vue-markdown'
import MarkdownEditor from '@/components/global/MarkdownEditor.vue'
import Indicator from '@/components/notifications/Indicator.vue'
import DropDownMenu from '@/components/global/DropDownMenu.vue'
import UserAvatar from '@/components/user/UserAvatar.vue'

import ModelSectionModal from '@/components/model/modals/ModelSectionModal.vue'
import ModelCardModal from '@/components/model/modals/ModelCardModal.vue'
import ModelSectionTag from '@/components/model/MoodelSectionTag.vue'

import Popper from '@/components/external/VuePopper.vue'
import HelpPopper from '@/components/HelpPopper.vue'

Vue.config.productionTip = false

Vue.use(VueAxios, axios)
Vue.use(VueAnalytics, {
  id: 'UA-92543820-1',
  router
})

Vue.use(VueI18n)

const getLang = function () {
  if (navigator.languages !== undefined) {
    return navigator.languages[0].substr(0, 2)
  } else {
    return navigator.language.substr(0, 2)
  }
}

export const i18n = new VueI18n({
  locale: getLang(),
  fallbackLocale: 'en',
  messages: translations
})

axios.interceptors.request.use(
  function (config) {
    config.headers.common.Authorization = 'Bearer ' + localStorage.getItem('id_token')
    return config
  },
  function (error) {
    return Promise.reject(error)
  }
)

Vue.component('app-error-panel', ErrorPanel)
Vue.component('vue-markdown', VueMarkdown)
Vue.component('app-markdown-editor', MarkdownEditor)
Vue.component('app-indicator', Indicator)
Vue.component('app-drop-down-menu', DropDownMenu)
Vue.component('app-user-avatar', UserAvatar)

/* registered globally to solve the circular reference */
Vue.component('app-model-section-modal', ModelSectionModal)
Vue.component('app-model-card-modal', ModelCardModal)
Vue.component('app-model-section-tag', ModelSectionTag)
Vue.component('popper', Popper)
Vue.component('app-help-popper', HelpPopper)

/* custom directive to detect click outside from
https://stackoverflow.com/questions/36170425/detect-click-outside-element?answertab=votes#tab-top */
Vue.directive('click-outside', {
  bind: function (el, binding, vnode) {
    Vue.event = function (event) {
      if (!(el === event.target || el.contains(event.target))) {
        vnode.context[binding.expression](event)
      }
    }
    document.body.addEventListener('click', Vue.event)
  },
  unbind: function (el) {
    document.body.removeEventListener('click', Vue.event)
  }
})

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  i18n,
  template: '<App/>',
  components: { App }
})

router.beforeEach(beforeEach)
