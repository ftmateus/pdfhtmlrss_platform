import { createApp } from 'vue'
import App from './App.vue'

import Login from "@/components/Login.vue";
import { createMemoryHistory, createRouter } from 'vue-router'
import HelloWorld from "@/components/MainView.vue";
import MainView from "@/components/MainView.vue";

import {checkAuthStatus} from "@/api";
import ToastNotification from "@/components/ToastNotification.vue";
import {ToastType} from "@/components/ToastNotificationType";

const routes = [
    {
        path: '/',
        component: MainView,
        meta : {
            requiresAuth : true
        }
    },
    { path: '/login', component: Login },
    {
        path: '/backend-error',
        component: ToastNotification,
        props : {
            type : ToastType.ERROR,
            message : "Backend Error"
        }
    },
]

const router = createRouter({
    history: createMemoryHistory(),
    routes,
})

router.beforeEach(async (to, from, next) => {
    if (!to.matched.some(record => record.meta.requiresAuth)) {
        next();
        return;
    }
    try {
        const isLoggedIn : boolean = await checkAuthStatus()
            .then(j => j.loggedIn)
        if (isLoggedIn) {
            next();
        } else {
            next({ path: '/login' });
        }
    } catch (error) {
        next( {path: '/backend-error'} );
    }
})

createApp(App)
    .use(router)
    .mount('#app')
