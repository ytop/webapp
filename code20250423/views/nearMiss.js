const state = {
    tableName: '',
    detection_id: '',
    activeNames: ["1"],
    refresh: 0
}

const mutations = {
    changeTableName: (state, dept) => {
        state.tableName = dept
    },

    changeDetectionId: (state, id) => {
        state.detection_id = id
    },

    changeActiveNames: (state, an) => {
        state.activeNames = an
    },

    changeRefresh: (state) => {
        state.refresh += 1
    } 
}

const actions = {

}

export default {
    state,
    mutations,
    actions
}
