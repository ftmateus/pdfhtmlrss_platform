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

  async handleLogin() {
    if(!this.username || !this.password)
      return;

    try {
      let res = await login(this.username, this.password)
      console.log(res)
      if(res.ok) {
        this.$router.push('/')
      }
    } catch (e) {
      console.log(e)
      this.wrongCredentials = true;
    }
  }
}
</script>

<template>
  <form class="login" @submit.prevent="handleLogin">
    <div>
      <label>Username: </label>
      <input type="text" v-model="username">
    </div>
    <div>
      <label>Password: </label>
      <input type="password" v-model="password">
    </div>
    <div v-if="wrongCredentials" style="color: red">
        Wrong credentials!
    </div>
    <div>
      <button type="submit" style="width: 50px">Login</button>
    </div>
  </form>
</template>

<style scoped>
.login {
  display: flex;
  flex-direction: column;
}
</style>