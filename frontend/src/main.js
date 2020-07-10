import Vue from 'vue'
import App from './App.vue'
import {router} from './router'
import store from './store'
import Toasted from 'vue-toasted'

Vue.use(Toasted, {
    iconPack: 'fontawesome', // set your iconPack, defaults to material. material|fontawesome|custom-class
})

Vue.config.productionTip = false

if (typeof (String.prototype.trim) === "undefined") {
    String.prototype.trim = function () {
        return String(this).replace(/^\s+|\s+$/g, '');
    };
}

new Vue({
    router,
    store,
    render: h => h(App),
}).$mount('#app')
