<script lang="ts">
import {Options, Vue} from 'vue-class-component';
import {logout} from "@/api";

@Options({
  components : {}
})
export default class UserArea extends Vue {
  async handleLogout() {
    let res = await logout();
    if(res.ok) {
      this.$router.push("/login")
    }
  }
}
</script>

<template>
  <div>
    <label>Hello, user!</label>
    <button @click="handleLogout">Logout</button>
  </div>
</template>

<style scoped>
div {
  display: flex;
  flex-direction: row;
  width: 150px;
  justify-content: space-around;
}
</style>