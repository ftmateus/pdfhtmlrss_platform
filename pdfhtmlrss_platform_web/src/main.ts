import { createApp } from 'vue'
import App from './App.vue'

import Login from "@/components/Login.vue";
import { createMemoryHistory, createRouter } from 'vue-router'
import HelloWorld from "@/components/MainView.vue";
import MainView from "@/components/MainView.vue";

import {checkAuthStatus} from "@/api";

const routes = [
    {
        path: '/',
        component: MainView,
        meta : {
            requiresAuth : true
        }
    },
    { path: '/login', component: Login }
]

const router = createRouter({
    history: createMemoryHistory(),
    routes,
})

router.beforeEach(async (to, from, next) => {
    if (to.matched.some(record => record.meta.requiresAuth)) {
        const isLoggedIn : boolean = await checkAuthStatus()
            .then(r => r.json().then(j => j.loggedIn))
        if (!isLoggedIn) {
            next({ path: '/login' }); // redirect to home if not logged in
        } else {
            next();
        }
    } else {
        next(); // always call next()!
    }
})

createApp(App)
    .use(router)
    .mount('#app')
