<script lang="ts">
// import Vue from 'vue';
import {Options, Vue} from 'vue-class-component';
import {login} from "@/api";
import ToastNotification from "@/components/ToastNotification.vue";
import {ToastType} from "@/components/ToastNotificationType";

@Options({
  computed: {
    ToastType() {
      return ToastType
    }
  },
  components : {
    ToastNotification

  }
})
export default class Login extends Vue {
  username : string = "";
  password : string = "";
  wrongCredentials : boolean = false;
  loading : boolean = false;

  async handleLogin() {
    if(!this.username || !this.password)
      return;

    try {
      this.loading = true
      this.wrongCredentials = false
      let res = await login(this.username, this.password)
      if(res.ok)
        this.$router.push('/')
    } catch (e) {
      console.log(e)
      this.wrongCredentials = true;
    } finally {
      this.loading = false
    }
  }
}
</script>

<template>
  <form class="login" @submit.prevent="handleLogin">
    <div>
      <label for="username">Username: </label>
      <input type="text" v-model="username" id="username">
    </div>
    <div>
      <label for="pwd">Password: </label>
      <input type="password" v-model="password" id="pwd">
    </div>
    <ToastNotification v-if="loading" :type="ToastType.INFO">
      Loading...
    </ToastNotification>
    <ToastNotification
        v-if="wrongCredentials"
        :dismiss-click="() => wrongCredentials = false"
        :type="ToastType.ERROR"
    >
      Wrong credentials!
    </ToastNotification>
    <div>
      <button type="submit" :disabled="!username || !password">
        <i class="pi pi-sign-in" style="font-size: 0.8rem"></i>
        Login
      </button>
    </div>
  </form>
</template>

<style scoped>
.login {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  width: 50%;
}

input {
  height: 1.8rem;
}

button {
  height: 2.2rem;
}
</style>