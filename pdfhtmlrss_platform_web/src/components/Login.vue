<script lang="ts">
// import Vue from 'vue';
import {Options, Vue} from 'vue-class-component';
import {login} from "@/api";

@Options({
  components : {

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
    <div v-if="loading">
      Loading...
    </div>
    <div v-if="wrongCredentials" style="color: red">
        Wrong credentials!
    </div>
    <div>
      <button type="submit" style="width: 50px" :disabled="!username || !password">
        Login
      </button>
    </div>
  </form>
</template>

<style scoped>
.login {
  display: flex;
  flex-direction: column;
}
</style>