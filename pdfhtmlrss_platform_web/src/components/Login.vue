<script lang="ts">
// import Vue from 'vue';
import {Options, Vue} from 'vue-class-component';

@Options({
  components : {

  }
})
export default class Login extends Vue {

}
</script>

<template>
  <div class="login">
    <div>
      <label>Username: </label>
      <input type="text">
    </div>
    <div>
      <label>Password: </label>
      <input type="password">
    </div>
  </div>
</template>

<style scoped>
.login {
  display: flex;
  flex-direction: column;
}
</style>