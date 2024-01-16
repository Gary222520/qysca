import { createStore } from 'vuex'

export default createStore({
  state: {
    projects: []
  },
  getters: {
    getProjects(state) {
      return state.projects
    },
    getProjectByName: (state) => (name) => {
      return state.projects.find((item) => item.name === name)
    }
  },
  mutations: {},
  actions: {},
  modules: {}
})
